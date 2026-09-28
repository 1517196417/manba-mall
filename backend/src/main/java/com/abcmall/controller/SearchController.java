package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.entity.Product;
import com.abcmall.service.HomeService;
import com.abcmall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 搜索：联想 / 热门词 / 结果列表
 *
 * 说明：搜索历史保存在前端本地（localStorage），后端只提供联想与热门词。
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;
    private final HomeService homeService;

    /** 搜索联想 */
    @GetMapping("/suggest")
    public Result<List<String>> suggest(@RequestParam String keyword) {
        return Result.success(productService.suggest(keyword));
    }

    /** 热门搜索 */
    @GetMapping("/hot")
    public Result<List<String>> hot() {
        return Result.success(homeService.hotKeywords());
    }

    /** 搜索结果（与商品列表共用筛选与排序能力） */
    @GetMapping
    public Result<PageResult<Product>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(productService.page(categoryId, brandId, keyword, minPrice, maxPrice,
                sort, null, null, null, page, size));
    }
}
