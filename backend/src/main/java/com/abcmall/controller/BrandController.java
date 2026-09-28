package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.entity.ProductBrand;
import com.abcmall.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 品牌 */
@RestController
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping("/list")
    public Result<List<ProductBrand>> list() {
        return Result.success(brandService.listAll());
    }

    @GetMapping("/{id}")
    public Result<ProductBrand> detail(@PathVariable Long id) {
        return Result.success(brandService.getById(id));
    }
}
