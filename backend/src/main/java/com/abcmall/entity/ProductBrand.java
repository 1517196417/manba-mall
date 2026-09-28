package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/** 品牌 */
@Data
@TableName("product_brand")
public class ProductBrand implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String logo;
    private String description;
    /** 首字母 */
    private String initial;
    private Integer sort;
    private Integer status;
}
