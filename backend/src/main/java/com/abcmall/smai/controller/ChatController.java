package com.abcmall.smai.controller;


import com.abcmall.common.UserContext;
import com.abcmall.entity.SysUser;
import com.abcmall.smai.config.ChatConfig;
import com.abcmall.smai.dto.ChatRequestDTO;
import com.abcmall.smai.entity.ChatHistory;
import com.abcmall.smai.service.ChatHistoryService;
import com.abcmall.smai.tool.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class ChatController {

    // JSON解释工具
    private final ObjectMapper JSON = new ObjectMapper();

    @Autowired
    ChatConfig chatConfig;

    @Autowired
    ChatHistoryService chatHistoryService;

    // 聊天历史保留条数-高水位
    @Value("${abcmall.smai.chat-history-high-watermark:20}")
    Integer chatHistoryHighWatermark;
    // 聊天历史保留条数-低水位
    @Value("${abcmall.smai.chat-history-low-watermark:10}")
    Integer chatHistoryLowWatermark;

//    public ChatController(ChatModel chatModel, DateTimeTool dateTimeTool, SearchSitemapTool searchSitemapTool, SerachOrderTool serachOrderTool, SearchProductTool searchProductTool){
//        chatClient = ChatClient.builder(chatModel)
//            // 注册工具
//            .defaultTools(dateTimeTool, searchSitemapTool, serachOrderTool, searchProductTool)
//            .build();
//    }

    @Resource // LLM 大模型客户端 改成注入
    private ChatClient chatClient;

    /**
     * SSE 对话
     * 前端通过 axios-eventsource 接收，逐步展示思考过程
     * 推送三类事件（均通过 data-only SSE，type 在 JSON 中区分）：
     * 1. {"type":"step","toolName":"...","description":"...","timestamp":...}
     * 2. {"type":"result","content":"...","type":"...","items":[...],"sessionId":"..."}
     * 3. {"type":"done"}
     */
    @PostMapping(value = "/api/chat/send-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> sendStream(@RequestBody ChatRequestDTO params) {
        // 创建一个 Sinks.Many 实例，用于推送事件
        Sinks.Many<ServerSentEvent<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 获取当前用户ID和昵称
        SysUser user = UserContext.currentUser();
        Long userId = user == null ? null : user.getId();
        String nickname = user == null ? null : user.getNickname();

        // Agent 在后台线程执行，通过 StepTracker 回调推送步骤
        Mono.fromRunnable(() -> {
            try {
                StepTracker.start(step -> { // 开始记录步骤，并设置回调
                    try {
                        String json = JSON.writeValueAsString(Map.of(
                                "type", "step",
                                "toolName", step.toolName(),
                                "description", step.description(),
                                "timestamp", step.timestamp()
                        ));
                        // 推送步骤事件
                        sink.tryEmitNext(ServerSentEvent.<String>builder().data(json).build());
                    } catch (Exception ignored) {}
                });

                // 执行对话（复用 Day01 的非流式 send 方法）
                ChatHistory chatHistory = send(params, userId, nickname);

                // 推送最终结果
                String resultJson = JSON.writeValueAsString(chatHistory);
                sink.tryEmitNext(ServerSentEvent.<String>builder().data(resultJson).build());

            } catch (Exception e) {
                try {
                    ChatHistory err = new ChatHistory();
                    err.setRole("error");
                    err.setContent("抱歉，AI 服务暂时不可用：" + e.getMessage());
                    String errJson = JSON.writeValueAsString(err);
                    sink.tryEmitNext(ServerSentEvent.<String>builder().data(errJson).build());
                } catch (Exception ignored) {}
            } finally {
                try { // 推送结束标志
                    sink.tryEmitNext(ServerSentEvent.<String>builder()
                            .data("{\"type\":\"done\"}").build());
                } catch (Exception ignored) {}
                sink.tryEmitComplete(); // 完成推送
                StepTracker.clear();    // 清理
            }
        }).subscribeOn(Schedulers.boundedElastic()).subscribe();

        // 对外暴露成一个 Flux，交给 Spring MVC / WebFlux 作为 SSE 响应体
        return sink.asFlux();
    }

    /**
     * 调用大模型进行对话
     */
    private ChatHistory send(@RequestBody ChatRequestDTO dto, Long userId, String nickname){
        // 对话id不能为空，若为空则生成新的对话id
        String sessionId = dto.getSessionId();
        if(sessionId==null || sessionId.isBlank())
            sessionId = UUID.randomUUID().toString().replace("-", "");

        // 保存用户发送过来的消息
        String message = dto.getMessage();
        ChatHistory userHistory = new ChatHistory();
        userHistory.setSessionId(sessionId);
        userHistory.setUserId(userId); // 当前登陆用户ID
        userHistory.setRole("user"); // 表示此为用户的消息
        userHistory.setContent(message);
        userHistory.setCreateTime(LocalDateTime.now());
        // 保存用户发送过来的消息
        chatHistoryService.save(userHistory);

        // 加入用户ID和昵称
        String systemPrompt = chatConfig.SYSTEM_PROMPT.formatted(userId, nickname);

        // 查询历史聊天记录
        List<ChatHistory> historyList = chatHistoryService.list(sessionId, 4L);
        List<Message> messages = new ArrayList<>();
        for(ChatHistory history : historyList){
            String role = history.getRole();
            if("user".equalsIgnoreCase(role)){
                // 用户消息
                messages.add(new UserMessage(history.getContent()));
            }else{
                // 客服消息
                Map<String, Object> metadata = null;
                try {
                    metadata = JSON.readValue(history.getMetadata(), Map.class);
                } catch (JsonProcessingException e) {
                    metadata = Map.of();
                }
                AssistantMessage msg = new AssistantMessage(history.getContent(), metadata);
                messages.add(msg);
            }
        }

        // 调用大模型
        String result = chatClient.prompt()
                .system(systemPrompt)              // 系统提示词
                .messages(messages)                // 历史聊天记录
                .user(message)                     // 用户消息
                .call().content();                 // 调用大模型并获取内容
        // 组成历史记录
        ChatHistory history = new ChatHistory();
        history.setSessionId(sessionId);
        history.setUserId(userId); // 当前登陆用户ID，未登录返回 null
        history.setRole("assistant"); // 表示此为AI的回复
        history.setCreateTime(LocalDateTime.now());

        //进行JSON解释
        try {
            // 进行JSON解释
            Map<String, Object> map = JSON.readValue(result, Map.class);

            // 从AI回复的结果中，提取 content
            String content = map.get("content").toString();
            history.setContent(content);

            // 从AI回复的结果中，去除 content，然后保存为 metadata
            map.remove("content");
            history.setMetadata(JSON.writeValueAsString(map));

            // 保存AI回复的消息
            chatHistoryService.save(history);

            // 如果超出了高水位，删除最早的记录
            reduceHistoryWatermark(sessionId);

        } catch (JsonProcessingException e) {
            history.setRole("error");   // 解析失败
            history.setContent(e.getMessage());
        }

        // 返回结果
        return history;
    }

    // 降低历史记录水位
    private void reduceHistoryWatermark(String sessionId) {
        // 查询总水位
        Long historyCount = chatHistoryService.lambdaQuery().eq(ChatHistory::getSessionId, sessionId).count();
        // 超出高位，需要减负至低水位
        if (historyCount > chatHistoryHighWatermark) {
            // 查询低水位标杆记录
            ChatHistory boundary = chatHistoryService.lambdaQuery()
                    .eq(ChatHistory::getSessionId, sessionId)
                    .orderByDesc(ChatHistory::getId).last("limit " + chatHistoryLowWatermark + ", 1").one();
            // 删除历史记录至低水位
            if (boundary != null) {
                // 删除 id <= 边界 id 的记录（包括边界本身），保留最新的 10 条
                chatHistoryService.lambdaUpdate()
                        .eq(ChatHistory::getSessionId, sessionId)
                        .le(ChatHistory::getId, boundary.getId())
                        .remove();
            }
            System.out.println("【降低历史记录水位至低水位 " + sessionId + "】");
        }
    }



}
