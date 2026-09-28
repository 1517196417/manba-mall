package com.abcmall.smai.tool;

import com.abcmall.entity.Product;
import com.abcmall.entity.ProductBrand;
import com.abcmall.entity.ProductCategory;
import com.abcmall.entity.ProductSpecValue;
import com.abcmall.mapper.ProductBrandMapper;
import com.abcmall.mapper.ProductMapper;
import com.abcmall.mapper.ProductSpecValueMapper;
import com.abcmall.service.CategoryService;
import com.abcmall.smai.dto.SearchProductDTO;
import com.abcmall.smai.tool.StepTracker;
import com.abcmall.smai.vo.SearchProductVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SearchProductTool {

    @Resource
    CategoryService categoryService;
    @Resource
    ProductMapper productMapper;
    @Resource
    ProductBrandMapper brandMapper;
    @Resource
    ProductSpecValueMapper productSpecValueMapper;

    /** 商品召回数量 */
    @Value("${abcmall.smai.product-recall-limit:20}")
    Integer productRecallLimit;

    @Tool(description = "根据关键词搜索商品分类。用户提到品类时（如手机、电脑、耳机），调用此工具找到匹配的分类ID。返回分类列表。")
    public List<ProductCategory> searchCategory(String keyword) {
        if (keyword == null || keyword.isEmpty()) return Collections.emptyList();
        //StepTracker.addStep("searchCategory", "正在搜索【" + keyword + "】分类...");
        String kw = keyword.trim();
        List<ProductCategory> categories = categoryService.lambdaQuery()
                .eq(ProductCategory::getStatus, 1)
                .and(w -> w.like(ProductCategory::getName, kw)
                        .or().like(ProductCategory::getDescription, kw))
                .orderByAsc(ProductCategory::getSort)
                .list();
        StepTracker.addStep("searchCategory", "找到 " + categories.size() + " 个 【" + kw + "】 分类");
        return categories;
    }

    @Tool(description = "通用商品搜索工具。支持按分类IDs、关键词（包括扩展品牌词）、品牌名、价格范围、规格值、排序、标签等多维度查询。所有参数可选，按需传入。用户说6000元笔记本时，传入keyword=笔记本, minPrice=5000, maxPrice=7000, expandedKeywords=[MacBook,联想,戴尔,华硕,小米笔记本,华为MateBook]。返回商品列表(id,name,subtitle,price,mainImage,url)。")
    public List<SearchProductVO> searchProducts(SearchProductDTO dto) {
        // 召回数量由配置文件决定
        int limit = productRecallLimit;

        // --- 查询A：查分类下的商品 ---
        List<Product> categoryResults = new ArrayList<>();
        if (dto.getCategoryId() != null && !dto.getCategoryId().isEmpty()) {
            // 查询所有子分类ID
            List<Long> cids = new ArrayList<>();
            for (Long categoryId : dto.getCategoryId())
                cids.addAll(categoryService.selfAndChildrenIds(categoryId));
            // 去除重复分类ID
            cids = cids.stream().distinct().collect(Collectors.toList());
            // 查询所有子分类下的商品
            LambdaQueryWrapper<Product> qw = Wrappers.<Product>lambdaQuery()
                    .eq(Product::getStatus, 1)
                    .in(Product::getCategoryId, cids);
            // 其它条件筛选
            applyCommonFilters(qw, dto);
            qw.orderByDesc(Product::getSales);
            // 只召回召回量一半的商品
            qw.last("limit " + (limit / 2 + 1));
            categoryResults = productMapper.selectList(qw);
        }

        // 动态分配名额
        int remaining = limit - categoryResults.size();
        // 记录已召回商品，避免重复召回
        Set<Long> seenIds = categoryResults.stream().map(Product::getId).collect(Collectors.toSet());

        // --- 查询B：按关键词 LIKE ---
        List<Product> keywordResults = new ArrayList<>();
        boolean hasKeywords = (StringUtils.hasText(dto.getKeyword()) || (dto.getExpandedKeywords() != null && !dto.getExpandedKeywords().isEmpty()));
        if (remaining > 0 && hasKeywords) {
            List<String> allKeywords = new ArrayList<>();
            if (StringUtils.hasText(dto.getKeyword())) {
                allKeywords.add(dto.getKeyword().trim());
            }
            if (dto.getExpandedKeywords() != null) {
                allKeywords.addAll(dto.getExpandedKeywords());
            }
            LambdaQueryWrapper<Product> qw = Wrappers.<Product>lambdaQuery()
                    .eq(Product::getStatus, 1);
            applyCommonFilters(qw, dto);
            qw.and(w -> {
                for (int i = 0; i < allKeywords.size(); i++) {
                    String kw = allKeywords.get(i).trim();
                    if (kw.isEmpty()) continue;
                    if (i > 0) w.or();
                    w.like(Product::getName, kw)
                            .or().like(Product::getSubtitle, kw)
                            .or().like(Product::getTags, kw)
                            .or().like(Product::getDetailHtml, kw);
                }
            });
            if (!seenIds.isEmpty()) { // 排除已召回商品
                qw.notIn(Product::getId, seenIds);
            }
            qw.orderByDesc(Product::getSales).last("limit " + remaining);
            keywordResults = productMapper.selectList(qw);
        }

        // 合并
        List<Product> merged = new ArrayList<>(categoryResults);
        merged.addAll(keywordResults);
        StepTracker.addStep("searchProducts", "搜索召回 " + merged.size() + " 款商品信息。");

        return merged.stream()
                .map(product -> {
                    SearchProductVO vo = new SearchProductVO();
                    vo.setId(product.getId());
                    vo.setName(product.getName());
                    vo.setSubtitle(product.getSubtitle());
                    vo.setPrice(product.getPrice().doubleValue());
                    vo.setMainImage(product.getMainImage());
                    vo.setUrl("/product/" + product.getId());
                    return vo;
                })
                .collect(Collectors.toList());
    }


    /** 应用公共过滤条件（品牌、价格、规格、标签） */
    private void applyCommonFilters(LambdaQueryWrapper<Product> qw, SearchProductDTO dto) {
        // 品牌
        if (StringUtils.hasText(dto.getBrandName())) {
            List<ProductBrand> brands = brandMapper.selectList(Wrappers.<ProductBrand>lambdaQuery()
                    .like(ProductBrand::getName, dto.getBrandName().trim()));
            if (!brands.isEmpty())
                qw.in(Product::getBrandId, brands.stream().map(ProductBrand::getId).toList());
        }
        // 价格
        if (dto.getMinPrice() != null) {
            qw.ge(Product::getPrice, dto.getMinPrice());
        }
        if (dto.getMaxPrice() != null) {
            qw.le(Product::getPrice, dto.getMaxPrice());
        }
        // 标签
        if (dto.getIsNew() != null && dto.getIsNew() == 1) {
            qw.eq(Product::getIsNew, 1);
        }
        if (dto.getIsHot() != null && dto.getIsHot() == 1) {
            qw.eq(Product::getIsHot, 1);
        }
        if (dto.getIsRecommend() != null && dto.getIsRecommend() == 1) {
            qw.eq(Product::getIsRecommend, 1);
        }
        // 规格值过滤（颜色、容量等）
        if (dto.getSpecValues() != null && !dto.getSpecValues().isEmpty()) {
            List<Long> productIds = findProductIdsBySpecValues(dto.getSpecValues());
            if (!productIds.isEmpty()) {
                qw.in(Product::getId, productIds);
            }
        }
    }

    /** 按规格值查 productId（如"白色""128G"） */
    private List<Long> findProductIdsBySpecValues(List<String> specValues) {
        LambdaQueryWrapper<ProductSpecValue> qw = Wrappers.<ProductSpecValue>lambdaQuery();
        qw.and(w -> {
            for (int i = 0; i < specValues.size(); i++) {
                String sv = specValues.get(i).trim();
                if (sv.isEmpty()) continue;
                if (i > 0) w.or();
                w.like(ProductSpecValue::getValue, sv);
            }
        });
        List<ProductSpecValue> values = productSpecValueMapper.selectList(qw);
        return values.stream()
                .map(ProductSpecValue::getProductId)
                .distinct()
                .collect(Collectors.toList());
    }

}