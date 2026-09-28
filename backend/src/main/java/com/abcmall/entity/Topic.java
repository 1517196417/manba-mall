package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 专题页面 */
@Data
@TableName("topic")
public class Topic implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String subtitle;
    private String cover;
    /** 专题图文内容 HTML */
    private String content;
    /** 关联商品ID，逗号分隔 */
    private String productIds;
    private Integer views;
    private Integer sort;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
