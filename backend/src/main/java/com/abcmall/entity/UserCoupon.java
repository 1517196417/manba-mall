package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 用户优惠券 */
@Data
@TableName("user_coupon")
public class UserCoupon implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long couponId;
    /** 0未使用 1已使用 2已过期 */
    private Integer status;
    private Long orderId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime receiveTime;
    private LocalDateTime useTime;
    private LocalDateTime expireTime;

    /* ---------- 非数据库字段：券信息，便于列表直接展示 ---------- */
    @TableField(exist = false)
    private String name;
    @TableField(exist = false)
    private Integer type;
    @TableField(exist = false)
    private BigDecimal amount;
    @TableField(exist = false)
    private BigDecimal discount;
    @TableField(exist = false)
    private BigDecimal minAmount;
    @TableField(exist = false)
    private String description;
    @TableField(exist = false)
    private Long categoryId;
    /** 是否可用于当前订单（结算页计算用） */
    @TableField(exist = false)
    private Boolean usable;
    @TableField(exist = false)
    private String unusableReason;
    /** 预估可抵扣金额 */
    @TableField(exist = false)
    private BigDecimal deductAmount;
}
