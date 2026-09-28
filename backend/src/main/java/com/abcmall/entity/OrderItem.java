package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单明细 */
@Data
@TableName("order_item")
public class OrderItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long productId;
    private Long skuId;
    private String productName;
    private String skuDesc;
    private String image;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    /** 0未评价 1已评价 */
    private Integer reviewStatus;
    /** 0无售后 1售后中 2已完成 */
    private Integer refundStatus;
}
