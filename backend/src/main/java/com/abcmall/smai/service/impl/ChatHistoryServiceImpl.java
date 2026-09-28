package com.abcmall.smai.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.abcmall.smai.entity.ChatHistory;
import com.abcmall.smai.service.ChatHistoryService;
import com.abcmall.smai.mapper.ChatHistoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

/**
* @author Administrator
* @description 针对表【chat_history(AI客服聊天历史表)】的数据库操作Service实现
* @createDate 2026-09-22 15:30:41
*/
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory>
    implements ChatHistoryService{

    /**
     * 查询历史聊天记录
     */
    public List<ChatHistory> list(String sessionId, Long limit){
        // 检查 limit
        if(limit==null || limit<=0)
            limit = 10L;
        if(limit>50L)
            limit = 50L;
        // 创建一个查询
        LambdaQueryChainWrapper<ChatHistory> query = this.lambdaQuery();
        // 添加查询条件
        query.eq(ChatHistory::getSessionId, sessionId);
        query.orderByDesc(ChatHistory::getId);
        // 只查最新的 10 条记录
        query.last("limit " + limit);
        // 执行查询
        List<ChatHistory> list = query.list();
        // 将结果反转
        Collections.reverse(list);
        // 返回结果
        return list;
    }

}




