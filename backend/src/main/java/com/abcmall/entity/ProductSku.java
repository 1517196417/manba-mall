package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 商品 SKU */
@Data
@TableName("product_sku")
public class ProductSku implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private String skuCode;
    /** 规格组合 JSON，如 [{"specName":"颜色","value":"雅川青"}] */
    private String specJson;
    /** 规格描述，如「雅川青 12GB+512GB」 */
    private String specDesc;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer stock;
    private Integer sales;
    private String image;
    private Integer status;
}
