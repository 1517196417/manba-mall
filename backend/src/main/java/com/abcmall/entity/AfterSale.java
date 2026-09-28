package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 售后单 */
@Data
@TableName("after_sale")
public class AfterSale implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String serviceNo;
    private Long orderId;
    private String orderNo;
    private Long orderItemId;
    private Long userId;
    private String productName;
    private String image;

    /** 1退货 2退款 3换货 4维修 */
    private Integer type;
    private String reason;
    private String description;
    private String images;
    private BigDecimal refundAmount;

    /** 0待审核 1已同意 2已拒绝 3退货中 4退款中 5已完成 6已撤销 */
    private Integer status;
    private String handleNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime handleTime;
    private LocalDateTime finishTime;

    /* ---------- 非数据库字段 ---------- */
    @TableField(exist = false)
    private String typeText;
    @TableField(exist = false)
    private String statusText;
    @TableField(exist = false)
    private List<AfterSaleProgress> progressList;
}
