package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 物流轨迹 */
@Data
@TableName("logistics_trace")
public class LogisticsTrace implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private LocalDateTime traceTime;
    /** 轨迹内容 */
    private String context;
    private Integer status;
}
