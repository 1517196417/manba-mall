package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.entity.Product;
import com.abcmall.entity.ProductSku;
import com.abcmall.service.CommentService;
import com.abcmall.service.FavoriteService;
import com.abcmall.service.ProductService;
import com.abcmall.vo.AttrGroupVO;
import com.abcmall.vo.CommentStatisticsVO;
import com.abcmall.vo.ProductDetailVO;
import com.abcmall.vo.SpecVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** 商品：列表 / 筛选 / 排序 / 详情 */
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FavoriteService favoriteService;
    private final CommentService commentService;

    /**
     * 商品列表 / 搜索
     *
     * @param sort default综合 sales销量 price_asc价格升 price_desc价格降 new新品
     */
    @GetMapping("/page")
    public Result<PageResult<Product>> page(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer isNew,
            @RequestParam(required = false) Integer isHot,
            @RequestParam(required = false) Integer isRecommend,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(productService.page(categoryId, brandId, keyword, minPrice, maxPrice,
                sort, isNew, isHot, isRecommend, page, size));
    }

    /** 商品详情：主图/视频/图文详情/参数/规格/SKU/推荐 */
    @GetMapping("/{id}")
    public Result<ProductDetailVO> detail(@PathVariable Long id) {
        ProductDetailVO vo = productService.detail(id, UserContext.userId());
        vo.setFavorite(favoriteService.isFavorite(UserContext.userId(), id));
        // 记录浏览足迹（登录用户）
        if (UserContext.userId() != null) {
            favoriteService.addFootprint(UserContext.userId(), id);
        }
        return Result.success(vo);
    }

    /** SKU 列表 */
    @GetMapping("/{id}/skus")
    public Result<List<ProductSku>> skus(@PathVariable Long id) {
        return Result.success(productService.listSkus(id));
    }

    /** 规格项与规格值 */
    @GetMapping("/{id}/specs")
    public Result<List<SpecVO>> specs(@PathVariable Long id) {
        return Result.success(productService.buildSpecs(id));
    }

    /** 商品参数 */
    @GetMapping("/{id}/attrs")
    public Result<List<AttrGroupVO>> attrs(@PathVariable Long id) {
        return Result.success(productService.buildAttrGroups(id));
    }

    /** 同类推荐 */
    @GetMapping("/{id}/related")
    public Result<List<Product>> related(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(product == null ? List.of() : productService.related(product));
    }

    /** 评价统计 */
    @GetMapping("/{id}/comment-statistics")
    public Result<CommentStatisticsVO> commentStatistics(@PathVariable Long id) {
        return Result.success(commentService.statistics(id));
    }

    /* ---------- 首页榜单 ---------- */

    @GetMapping("/recommend")
    public Result<List<Product>> recommend(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.listByFlag("recommend", limit));
    }

    @GetMapping("/hot")
    public Result<List<Product>> hot(@RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(productService.listByFlag("hot", limit));
    }

    @GetMapping("/new")
    public Result<List<Product>> news(@RequestParam(defaultValue = "8") Integer limit) {
        return Result.success(productService.listByFlag("new", limit));
    }
}
