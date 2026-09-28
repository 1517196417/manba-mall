package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 消息（站内信 / 系统通知 / 订单通知） */
@Data
@TableName("message")
public class Message implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    /** system 系统 / order 订单 / activity 活动 / private 站内信 */
    private String type;
    private String link;
    @TableField("is_read")
    private Integer isRead;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
