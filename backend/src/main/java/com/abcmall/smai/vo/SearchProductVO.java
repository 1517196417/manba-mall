package com.abcmall.smai.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 搜索商品VO，包含商品基本信息和URL
 */
@Data
@AllArgsConstructor  // 全参构造方法
@NoArgsConstructor   // 无参构造方法
public class SearchProductVO {
    /** 商品ID */
    private Long id;
    /** 商品名称 */
    private String name;
    /** 商品副标题 */
    private String subtitle;
    /** 商品价格 */
    private Double price;
    /** 商品主图 */
    private String mainImage;
    /** 商品详情页URL */
    private String url;
}