package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/** 商品规格项，如「颜色」「版本」 */
@Data
@TableName("product_spec")
public class ProductSpec implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String name;
    private Integer sort;
}
