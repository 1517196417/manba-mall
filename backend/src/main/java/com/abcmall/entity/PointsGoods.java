package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 积分商品 */
@Data
@TableName("points_goods")
public class PointsGoods implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String image;
    /** 所需积分 */
    private Integer points;
    private BigDecimal marketPrice;
    private Integer stock;
    private Integer exchanged;
    private String description;
    private Integer status;
    private Integer sort;
}
