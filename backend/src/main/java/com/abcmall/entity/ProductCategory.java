package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/** 商品分类 */
@Data
@TableName("product_category")
public class ProductCategory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private String icon;
    private String banner;
    private String description;
    private Integer sort;
    private Integer status;
}
