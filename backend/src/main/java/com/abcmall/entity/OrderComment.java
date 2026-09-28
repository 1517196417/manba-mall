package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 商品评价（含晒图 / 追评） */
@Data
@TableName("order_comment")
public class OrderComment implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long productId;
    private Long userId;
    private String skuDesc;
    /** 评分 1-5 */
    private Integer rating;
    private String content;
    /** 晒图 JSON 数组 */
    private String images;
    @TableField("is_anonymous")
    private Integer isAnonymous;
    /** 商家回复 */
    private String reply;
    /** 追评内容 */
    private String appendContent;
    private LocalDateTime appendTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /* ---------- 非数据库字段：展示用 ---------- */
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
    @TableField(exist = false)
    private String productName;
    @TableField(exist = false)
    private String productImage;
}
