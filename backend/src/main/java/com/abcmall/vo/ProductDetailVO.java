package com.abcmall.vo;

import com.abcmall.entity.Product;
import com.abcmall.entity.ProductBrand;
import com.abcmall.entity.ProductCategory;
import com.abcmall.entity.ProductSku;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 商品详情聚合 */
@Data
public class ProductDetailVO implements Serializable {

    private Product product;
    private ProductBrand brand;
    private ProductCategory category;
    /** 图片集（已解析为列表） */
    private List<String> imageList = new ArrayList<>();
    private List<SpecVO> specs = new ArrayList<>();
    private List<ProductSku> skus = new ArrayList<>();
    private List<AttrGroupVO> attrGroups = new ArrayList<>();
    /** 猜你喜欢 / 同类推荐 */
    private List<Product> related = new ArrayList<>();
    /** 当前用户是否已收藏 */
    private Boolean favorite = false;
}
