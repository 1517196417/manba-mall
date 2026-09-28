package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 积分记录 */
@Data
@TableName("points_record")
public class PointsRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 正为获得，负为消耗 */
    private Integer points;
    /** sign 签到 / order 订单 / exchange 兑换 */
    private String type;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
