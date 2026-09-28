package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/** 商品规格值，如「雅川青」「512GB」 */
@Data
@TableName("product_spec_value")
public class ProductSpecValue implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Long specId;
    /** 规格值 列名就是 value，需显式指定 */
    @TableField("`value`")
    private String value;
    private Integer sort;
}
