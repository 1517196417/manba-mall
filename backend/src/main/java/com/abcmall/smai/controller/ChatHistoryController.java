package com.abcmall.smai.controller;

import com.abcmall.smai.entity.ChatHistory;
import com.abcmall.smai.service.ChatHistoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 历史聊天记录控制器
 */
@RestController
@RequestMapping("/api/chat/history")
public class ChatHistoryController {

    @Autowired
    ChatHistoryService chatHistoryService;

    /**
     * 查询历史聊天记录
     */
    @GetMapping("/list")
    public List<ChatHistory> list(String sessionId, Long limit){
        return chatHistoryService.list(sessionId, limit);
    }

    @DeleteMapping("/clear")
    public void clear(String sessionId) {
        chatHistoryService.remove(new LambdaQueryWrapper<ChatHistory>().eq(ChatHistory::getSessionId, sessionId));
    }
}
