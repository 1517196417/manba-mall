package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.PageResult;
import com.abcmall.entity.Product;
import com.abcmall.entity.ProductAttr;
import com.abcmall.entity.ProductBrand;
import com.abcmall.entity.ProductSku;
import com.abcmall.entity.ProductSpec;
import com.abcmall.entity.ProductSpecValue;
import com.abcmall.mapper.ProductAttrMapper;
import com.abcmall.mapper.ProductMapper;
import com.abcmall.mapper.ProductSkuMapper;
import com.abcmall.mapper.ProductSpecMapper;
import com.abcmall.mapper.ProductSpecValueMapper;
import com.abcmall.vo.AttrGroupVO;
import com.abcmall.vo.ProductDetailVO;
import com.abcmall.vo.SpecVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 商品服务：列表筛选 / 排序 / 详情 / 推荐 */
@Service
@RequiredArgsConstructor
public class ProductService extends ServiceImpl<ProductMapper, Product> {

    private final ProductSkuMapper skuMapper;
    private final ProductSpecMapper specMapper;
    private final ProductSpecValueMapper specValueMapper;
    private final ProductAttrMapper attrMapper;
    private final CategoryService categoryService;
    private final BrandService brandService;

    private static final ObjectMapper JSON = new ObjectMapper();

    /* ------------------------- 列表 / 搜索 ------------------------- */

    /**
     * 商品分页查询
     *
     * @param sort default综合 / sales销量 / price_asc价格升 / price_desc价格降 / new新品
     */
    public PageResult<Product> page(Long categoryId, Long brandId, String keyword,
                                    BigDecimal minPrice, BigDecimal maxPrice,
                                    String sort, Integer isNew, Integer isHot, Integer isRecommend,
                                    Integer pageNo, Integer pageSize) {

        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 12 : Math.min(pageSize, 100);

        LambdaQueryWrapper<Product> qw = Wrappers.lambdaQuery();
        qw.eq(Product::getStatus, 1);

        if (categoryId != null) {
            qw.in(Product::getCategoryId, categoryService.selfAndChildrenIds(categoryId));
        }
        if (brandId != null) {
            qw.eq(Product::getBrandId, brandId);
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            qw.and(w -> w.like(Product::getName, kw)
                    .or().like(Product::getSubtitle, kw)
                    .or().like(Product::getTags, kw));
        }
        if (minPrice != null) {
            qw.ge(Product::getPrice, minPrice);
        }
        if (maxPrice != null) {
            qw.le(Product::getPrice, maxPrice);
        }
        if (Integer.valueOf(1).equals(isNew)) {
            qw.eq(Product::getIsNew, 1);
        }
        if (Integer.valueOf(1).equals(isHot)) {
            qw.eq(Product::getIsHot, 1);
        }
        if (Integer.valueOf(1).equals(isRecommend)) {
            qw.eq(Product::getIsRecommend, 1);
        }

        switch (sort == null ? "default" : sort) {
            case "sales" -> qw.orderByDesc(Product::getSales);
            case "price_asc" -> qw.orderByAsc(Product::getPrice);
            case "price_desc" -> qw.orderByDesc(Product::getPrice);
            case "new" -> qw.orderByDesc(Product::getCreateTime).orderByDesc(Product::getId);
            default -> qw.orderByDesc(Product::getIsHot).orderByDesc(Product::getSales);
        }

        Page<Product> page = page(new Page<>(current, size), qw);
        return PageResult.of(page);
    }

    /** 首页推荐 / 热销 / 新品 */
    public List<Product> listByFlag(String flag, int limit) {
        LambdaQueryWrapper<Product> qw = Wrappers.<Product>lambdaQuery().eq(Product::getStatus, 1);
        switch (flag) {
            case "recommend" -> qw.eq(Product::getIsRecommend, 1);
            case "hot" -> qw.eq(Product::getIsHot, 1).orderByDesc(Product::getSales);
            case "new" -> qw.eq(Product::getIsNew, 1).orderByDesc(Product::getCreateTime);
            default -> qw.orderByDesc(Product::getSales);
        }
        qw.last("limit " + Math.max(1, Math.min(limit, 50)));
        return list(qw);
    }

    /** 搜索联想：返回商品名匹配的关键词 */
    public List<String> suggest(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }
        List<Product> list = list(Wrappers.<Product>lambdaQuery()
                .eq(Product::getStatus, 1)
                .like(Product::getName, keyword.trim())
                .last("limit 10"));
        List<String> result = new ArrayList<>();
        for (Product p : list) {
            result.add(p.getName());
        }
        return result;
    }

    /* ------------------------- 详情 ------------------------- */

    @Transactional(rollbackFor = Exception.class)
    public ProductDetailVO detail(Long productId, Long currentUserId) {
        Product product = getById(productId);
        BizException.notNull(product, "商品不存在或已下架");

        // 浏览量 +1
        update(Wrappers.<Product>lambdaUpdate()
                .eq(Product::getId, productId)
                .setSql("views = views + 1"));

        ProductDetailVO vo = new ProductDetailVO();
        vo.setProduct(product);
        if (product.getBrandId() != null) {
            vo.setBrand(brandService.getById(product.getBrandId()));
        }
        vo.setCategory(categoryService.getById(product.getCategoryId()));
        vo.setImageList(parseImages(product.getImages(), product.getMainImage()));
        vo.setSkus(listSkus(productId));
        vo.setSpecs(buildSpecs(productId));
        vo.setAttrGroups(buildAttrGroups(productId));
        vo.setRelated(related(product));
        return vo;
    }

    public List<ProductSku> listSkus(Long productId) {
        List<ProductSku> skus = skuMapper.selectList(Wrappers.<ProductSku>lambdaQuery()
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getStatus, 1)
                .orderByAsc(ProductSku::getId));
        return skus;
    }

    /** 组装规格项与规格值 */
    public List<SpecVO> buildSpecs(Long productId) {
        List<ProductSpec> specs = specMapper.selectList(Wrappers.<ProductSpec>lambdaQuery()
                .eq(ProductSpec::getProductId, productId)
                .orderByAsc(ProductSpec::getSort));
        List<SpecVO> result = new ArrayList<>();
        for (ProductSpec spec : specs) {
            SpecVO specVO = new SpecVO();
            specVO.setId(spec.getId());
            specVO.setName(spec.getName());

            List<ProductSpecValue> values = specValueMapper.selectList(
                    Wrappers.<ProductSpecValue>lambdaQuery()
                            .eq(ProductSpecValue::getSpecId, spec.getId())
                            .orderByAsc(ProductSpecValue::getSort));
            for (ProductSpecValue v : values) {
                SpecVO.SpecValueVO valueVO = new SpecVO.SpecValueVO();
                valueVO.setId(v.getId());
                valueVO.setValue(v.getValue());
                specVO.getValues().add(valueVO);
            }
            result.add(specVO);
        }
        return result;
    }

    /** 按分组组装商品参数 */
    public List<AttrGroupVO> buildAttrGroups(Long productId) {
        List<ProductAttr> attrs = attrMapper.selectList(Wrappers.<ProductAttr>lambdaQuery()
                .eq(ProductAttr::getProductId, productId)
                .orderByAsc(ProductAttr::getSort));

        Map<String, AttrGroupVO> groupMap = new LinkedHashMap<>();
        for (ProductAttr attr : attrs) {
            String group = StringUtils.hasText(attr.getGroupName()) ? attr.getGroupName() : "主体";
            AttrGroupVO groupVO = groupMap.computeIfAbsent(group, k -> {
                AttrGroupVO g = new AttrGroupVO();
                g.setGroupName(k);
                return g;
            });
            groupVO.getAttrs().add(attr);
        }
        return new ArrayList<>(groupMap.values());
    }

    /** 同类推荐（同分类其它商品） */
    public List<Product> related(Product product) {
        return list(Wrappers.<Product>lambdaQuery()
                .eq(Product::getStatus, 1)
                .eq(Product::getCategoryId, product.getCategoryId())
                .ne(Product::getId, product.getId())
                .orderByDesc(Product::getSales)
                .last("limit 8"));
    }

    /** 根据 SKU ID 获取（校验归属） */
    public ProductSku requireSku(Long productId, Long skuId) {
        ProductSku sku = skuMapper.selectById(skuId);
        BizException.notNull(sku, "规格不存在");
        BizException.check(!sku.getProductId().equals(productId), "规格与商品不匹配");
        return sku;
    }

    /** 商品默认 SKU（无规格商品） */
    public ProductSku defaultSku(Long productId) {
        return skuMapper.selectOne(Wrappers.<ProductSku>lambdaQuery()
                .eq(ProductSku::getProductId, productId)
                .eq(ProductSku::getStatus, 1)
                .orderByAsc(ProductSku::getId)
                .last("limit 1"));
    }

    /** 解析 images JSON */
    public List<String> parseImages(String imagesJson, String fallback) {
        List<String> result = new ArrayList<>();
        if (StringUtils.hasText(imagesJson)) {
            try {
                result = JSON.readValue(imagesJson, new TypeReference<List<String>>() {
                });
            } catch (Exception ignored) {
                // JSON 解析失败时退化为主图
            }
        }
        if (result.isEmpty() && StringUtils.hasText(fallback)) {
            result.add(fallback);
        }
        return result;
    }

    /** 更新库存与销量 */
    public void changeStock(Long skuId, int delta) {
        skuMapper.update(null, Wrappers.<ProductSku>lambdaUpdate()
                .eq(ProductSku::getId, skuId)
                .setSql("stock = stock + (" + delta + ")")
                .setSql("sales = sales + (" + (-delta) + ")"));
    }
}
