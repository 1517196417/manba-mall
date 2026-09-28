package com.abcmall.smai.service;

import com.abcmall.smai.entity.ChatHistory;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Collections;
import java.util.List;

/**
* @author Administrator
* @description 针对表【chat_history(AI客服聊天历史表)】的数据库操作Service
* @createDate 2026-09-22 15:30:41
*/
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 查询历史聊天记录
     */
    List<ChatHistory> list(String sessionId, Long limit);

}

