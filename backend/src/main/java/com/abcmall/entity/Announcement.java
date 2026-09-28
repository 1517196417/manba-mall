package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 公告 */
@Data
@TableName("announcement")
public class Announcement implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    /** notice 公告 / activity 活动 */
    private String type;
    @TableField("is_top")
    private Integer isTop;
    private Integer sort;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
