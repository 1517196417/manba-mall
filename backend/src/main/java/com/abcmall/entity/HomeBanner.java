package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 轮播图 / 广告位 */
@Data
@TableName("home_banner")
public class HomeBanner implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String image;
    private String link;
    /** carousel 轮播 / ad_home 首页广告 / ad_side 侧栏 */
    private String position;
    private Integer sort;
    private Integer status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
