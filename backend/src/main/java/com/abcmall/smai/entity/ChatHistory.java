package com.abcmall.smai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * AI客服聊天历史表
 * @TableName chat_history
 */
@TableName(value ="chat_history")
@Data
public class ChatHistory {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 识别的意图
     */
    private String intent;

    /**
     * 附加信息JSON(商品/订单/链接等结构化数据)
     */
    private String metadata;

    /**
     * 
     */
    @JsonIgnore
    private LocalDateTime createTime;

}