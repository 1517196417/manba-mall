package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 优惠券 */
@Data
@TableName("coupon")
public class Coupon implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    /** 1满减券 2折扣券 3无门槛 */
    private Integer type;
    /** 面额 */
    private BigDecimal amount;
    /** 折扣，如 8.5 表示 8.5 折 */
    private BigDecimal discount;
    /** 使用门槛 */
    private BigDecimal minAmount;
    private Integer total;
    private Integer received;
    /** 每人限领 */
    private Integer perLimit;
    private Long categoryId;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /* ---------- 非数据库字段 ---------- */
    /** 当前用户已领取数量 */
    @TableField(exist = false)
    private Integer receivedCount;
    /** 是否已领完 */
    @TableField(exist = false)
    private Boolean soldOut;
}
