package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 活动专区 */
@Data
@TableName("activity")
public class Activity implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String subtitle;
    private String image;
    /** promotion 专场 / seckill 秒杀 / groupon 拼团 / new 新品 / season 季节 */
    private String type;
    private String link;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer sort;
    private Integer status;
}
