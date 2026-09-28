package com.abcmall.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 分类树节点 */
@Data
public class CategoryVO implements Serializable {
    private Long id;
    private String name;
    private String icon;
    private String banner;
    private String description;
    private Long parentId;
    private Integer level;
    private List<CategoryVO> children = new ArrayList<>();
}
