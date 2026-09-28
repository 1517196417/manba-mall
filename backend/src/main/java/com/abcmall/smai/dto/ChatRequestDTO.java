package com.abcmall.smai.dto;

import lombok.Data;

/**
 * 聊天请求的参数
 */
@Data
public class ChatRequestDTO {

    /** 对话ID */
    String sessionId;

    /** 发送的消息 */
    String message;

}
