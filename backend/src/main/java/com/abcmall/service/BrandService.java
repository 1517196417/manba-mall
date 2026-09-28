package com.abcmall.service;

import com.abcmall.entity.ProductBrand;
import com.abcmall.mapper.ProductBrandMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/** 品牌服务 */
@Service
public class BrandService extends ServiceImpl<ProductBrandMapper, ProductBrand> {

    public List<ProductBrand> listAll() {
        return list(Wrappers.<ProductBrand>lambdaQuery()
                .eq(ProductBrand::getStatus, 1)
                .orderByAsc(ProductBrand::getSort));
    }
}
