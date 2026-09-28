package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 积分兑换记录 */
@Data
@TableName("points_exchange")
public class PointsExchange implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long pointsGoodsId;
    private String goodsName;
    private String image;
    private Integer points;
    /** 0待发货 1已发货 2已完成 */
    private Integer status;
    private String expressNo;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
