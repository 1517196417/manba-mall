package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单 */
@Data
@TableName("shop_order")
public class ShopOrder implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;

    private BigDecimal totalAmount;
    private BigDecimal freight;
    private BigDecimal discountAmount;
    private BigDecimal payAmount;

    private Long userCouponId;
    private BigDecimal pointsDeduct;
    private Integer pointsUsed;
    private BigDecimal balanceUsed;

    /** 1微信 2支付宝 3银联 4余额 */
    private Integer payType;
    /** 0未支付 1已支付 2已退款 */
    private Integer payStatus;
    /** 0待付款 1待发货 2待收货 3待评价 4已完成 5已取消 6售后中 */
    private Integer orderStatus;
    /** 1快递 2自提 3同城配送 */
    private Integer deliveryType;

    private Long addressId;
    private String receiver;
    private String receiverPhone;
    private String receiverAddr;
    private String remark;
    private String invoiceTitle;
    private String invoiceTaxNo;
    private String cancelReason;


    @TableField(fill = FieldFill.INSERT)
    @JsonIgnore
    private LocalDateTime createTime;
    @JsonIgnore
    private LocalDateTime payTime;
    @JsonIgnore
    private LocalDateTime deliveryTime;
    @JsonIgnore
    private LocalDateTime receiveTime;
    @JsonIgnore
    private LocalDateTime finishTime;
    @JsonIgnore
    private LocalDateTime cancelTime;
}
