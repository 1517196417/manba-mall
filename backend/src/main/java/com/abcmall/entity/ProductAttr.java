package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/** 商品参数 */
@Data
@TableName("product_attr")
public class ProductAttr implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    /** 参数分组，如「屏幕」「性能」 */
    private String groupName;
    private String name;
    @TableField("`value`")
    private String value;
    private Integer sort;
}
