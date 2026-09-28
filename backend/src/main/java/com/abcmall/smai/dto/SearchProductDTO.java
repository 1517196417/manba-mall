package com.abcmall.smai.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 搜索商品参数
 */
@Data
public class SearchProductDTO {
    /** 搜索关键词 */
    String keyword;
    /** 扩展关键词 */
    List<String> expandedKeywords;
    /** 分类ID列表 */
    List<Long> categoryId;
    /** 品牌名称 */
    String brandName;
    /** 最小价格 */
    BigDecimal minPrice;
    /** 最大价格 */
    BigDecimal maxPrice;
    /** 规格值列表 */
    List<String> specValues;
    /** 排序字段 */
    String sort;
    /** 是否新商品 */
    Integer isNew;
    /** 是否热门 */
    Integer isHot;
    /** 是否推荐 */
    Integer isRecommend;
    /* 召回数量 */
    //Integer limit;
}