package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 秒杀活动（需求标注为「功能说明页」，此处保留数据模型与只读列表接口） */
@Data
@TableName("seckill_activity")
public class SeckillActivity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private Long productId;
    private Long skuId;
    private BigDecimal seckillPrice;
    private Integer stock;
    private Integer sold;
    private Integer limitCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;

    /* ---------- 非数据库字段 ---------- */
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productImage;
    @TableField(exist = false)
    private BigDecimal originPrice;
}
