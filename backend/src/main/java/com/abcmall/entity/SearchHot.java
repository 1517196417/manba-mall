package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;

/** 热门搜索词 */
@Data
@TableName("search_hot")
public class SearchHot implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private Integer sort;
    @TableField("is_hot")
    private Integer isHot;
}
