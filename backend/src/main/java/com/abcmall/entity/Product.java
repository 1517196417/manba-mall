package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品 SPU */
@Data
@TableName("product")
public class Product implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String subtitle;
    private Long categoryId;
    private Long brandId;
    /** 主图 */
    private String mainImage;
    /** 图片集 JSON 数组字符串 */
    private String images;
    /** 主视频 */
    private String video;
    /** 图文详情 HTML */
    private String detailHtml;

    private BigDecimal price;
    private BigDecimal marketPrice;
    private BigDecimal costPrice;
    private Integer stock;
    private Integer sales;
    private Integer views;
    private String unit;
    private BigDecimal weight;
    /** 促销标签，逗号分隔 */
    private String tags;

    @TableField("is_recommend")
    private Integer isRecommend;
    @TableField("is_hot")
    private Integer isHot;
    @TableField("is_new")
    private Integer isNew;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
