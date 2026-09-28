package com.abcmall.smai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 站点地图/功能导航表
 * @TableName sitemap
 */
@TableName(value ="sitemap")
@Data
public class Sitemap {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 功能名称
     */
    private String name;

    /**
     * 功能描述
     */
    private String description;

    /**
     * 页面链接
     */
    private String url;

    /**
     * 关键词，逗号分隔
     */
    private String keywords;

    /**
     * 功能分类
     */
    private String category;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 0禁用 1启用
     */
    private Integer status;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

}