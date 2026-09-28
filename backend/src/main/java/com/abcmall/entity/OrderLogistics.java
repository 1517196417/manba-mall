package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 物流单 */
@Data
@TableName("order_logistics")
public class OrderLogistics implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private String company;
    private String companyCode;
    private String trackingNo;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddr;
    /** 1已揽收 2运输中 3派送中 4已签收 */
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    @JsonIgnore
    private LocalDateTime createTime;
}
