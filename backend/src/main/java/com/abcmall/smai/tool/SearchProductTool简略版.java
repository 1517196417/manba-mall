package com.abcmall.smai.tool;

import com.abcmall.entity.Product;
import com.abcmall.entity.ProductCategory;
import com.abcmall.mapper.ProductMapper;
import com.abcmall.service.CategoryService;
import com.abcmall.service.ProductService;
import com.abcmall.smai.dto.SearchProductDTO;
import com.abcmall.smai.vo.SearchProductVO;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchProductTool简略版 {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    ProductMapper productMapper;

    //@Tool(description = "根据关键词搜索商品分类。用户提到品类时（如手机、电脑、耳机），调用此工具找到匹配的分类ID。返回分类列表。")
    public List<ProductCategory> searchCategory(String keyword){
        if(keyword==null || keyword.isEmpty()) return Collections.emptyList();

        List<ProductCategory> list = categoryService.lambdaQuery()
                .and(x -> x.like(ProductCategory::getName, keyword)
                        .or().like(ProductCategory::getDescription, keyword))
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort)
                .list();
        StepTracker.addStep("searchCategory", "找到 " + list.size() + " 个 【" + keyword + "】 分类");
        return list;
    }


    //@Tool(description = "通用商品搜索工具。支持按分类IDs、关键词（包括扩展品牌词）、品牌名、价格范围、规格值、排序、标签等多维度查询。所有参数可选，按需传入。用户说6000元笔记本时，传入keyword=笔记本, minPrice=5000, maxPrice=7000, expandedKeywords=[MacBook,联想,戴尔,华硕,小米笔记本,华为MateBook]。返回商品列表(id,name,subtitle,price,mainImage,url)。")
    public List<SearchProductVO> searchProducts(SearchProductDTO dto){
        if(dto==null) return Collections.emptyList();

        // 新建一个查询条件
        LambdaQueryChainWrapper<Product> w = productService.lambdaQuery();

        // 关键词和扩展关键词查询
        w.and(x-> {
            // 关键词查询
            String keyword = dto.getKeyword();
            if(keyword != null && !keyword.isEmpty()) {
                x.or().like(Product::getName, keyword);
                x.or().like(Product::getSubtitle, keyword);
            }

            // 扩展关键词查询
            List<String> expandedKeywords = dto.getExpandedKeywords();
            for (String expandedKeyword : expandedKeywords) {
                x.or().like(Product::getName, expandedKeyword);
                x.or().like(Product::getSubtitle, expandedKeyword);
            }
        });

        // 价格范围
        BigDecimal minPrice = dto.getMinPrice();
        if(minPrice != null) {
            w.ge(Product::getPrice, minPrice);
        }
        BigDecimal maxPrice = dto.getMaxPrice();
        if(maxPrice != null) {
            w.le(Product::getPrice, maxPrice);
        }

        // 执行查询
        List<Product> products = w.list();

        StepTracker.addStep("searchProducts", "找到 " + products.size() + " 个 【" + dto.getKeyword() + "】【" + dto.getExpandedKeywords() + " 】 商品");

        // 转成 SearchProductVO
        return products.stream()
                .map(product -> new SearchProductVO(
                    product.getId(),
                    product.getName(),
                    product.getSubtitle(),
                    Double.parseDouble(product.getPrice().toString()),
                    product.getMainImage(),
                    String.format("/product/%d", product.getId())))
                .collect(Collectors.toList());
    }

}
