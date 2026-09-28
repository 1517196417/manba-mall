package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.entity.ProductCategory;
import com.abcmall.service.CategoryService;
import com.abcmall.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 商品分类 */
@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /** 两级分类树 */
    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryService.tree());
    }

    /** 顶级分类 */
    @GetMapping("/list")
    public Result<List<CategoryVO>> list() {
        return Result.success(categoryService.topList());
    }

    /** 分类详情 */
    @GetMapping("/{id}")
    public Result<ProductCategory> detail(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    /** 面包屑导航 */
    @GetMapping("/{id}/breadcrumb")
    public Result<List<ProductCategory>> breadcrumb(@PathVariable Long id) {
        return Result.success(categoryService.breadcrumb(id));
    }
}
