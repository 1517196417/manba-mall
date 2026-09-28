package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/** 帮助中心 / 常见问题（需求标注为「功能说明页」，此处保留数据与只读接口） */
@Data
@TableName("help_article")
public class HelpArticle implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String category;
    private String question;
    private String answer;
    private Integer sort;
    private Integer status;
}
