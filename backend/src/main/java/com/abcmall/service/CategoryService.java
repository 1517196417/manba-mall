package com.abcmall.service;

import com.abcmall.entity.ProductCategory;
import com.abcmall.mapper.ProductCategoryMapper;
import com.abcmall.vo.CategoryVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 商品分类服务 */
@Service
public class CategoryService extends ServiceImpl<ProductCategoryMapper, ProductCategory> {

    /** 两级分类树 */
    public List<CategoryVO> tree() {
        List<ProductCategory> all = list(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSort)
                .orderByAsc(ProductCategory::getId));

        Map<Long, CategoryVO> topMap = new LinkedHashMap<>();
        List<CategoryVO> children = new ArrayList<>();

        for (ProductCategory c : all) {
            CategoryVO vo = toVO(c);
            if (c.getParentId() == null || c.getParentId() == 0) {
                topMap.put(c.getId(), vo);
            } else {
                children.add(vo);
            }
        }
        for (CategoryVO child : children) {
            CategoryVO parent = topMap.get(child.getParentId());
            if (parent != null) {
                parent.getChildren().add(child);
            }
        }
        return new ArrayList<>(topMap.values());
    }

    /** 顶级分类 */
    public List<CategoryVO> topList() {
        List<ProductCategory> list = list(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getStatus, 1)
                .eq(ProductCategory::getLevel, 1)
                .orderByAsc(ProductCategory::getSort));
        List<CategoryVO> result = new ArrayList<>();
        for (ProductCategory c : list) {
            result.add(toVO(c));
        }
        return result;
    }

    /** 某个分类自身 + 所有子分类的ID集合（用于按一级分类筛选商品） */
    public List<Long> selfAndChildrenIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<ProductCategory> children = list(Wrappers.<ProductCategory>lambdaQuery()
                .eq(ProductCategory::getParentId, categoryId)
                .eq(ProductCategory::getStatus, 1));
        for (ProductCategory c : children) {
            ids.add(c.getId());
        }
        return ids;
    }

    /** 面包屑 */
    public List<ProductCategory> breadcrumb(Long categoryId) {
        List<ProductCategory> path = new ArrayList<>();
        ProductCategory current = getById(categoryId);
        int guard = 0;
        while (current != null && guard++ < 5) {
            path.add(0, current);
            if (current.getParentId() == null || current.getParentId() == 0) {
                break;
            }
            current = getById(current.getParentId());
        }
        return path;
    }

    private CategoryVO toVO(ProductCategory c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setIcon(c.getIcon());
        vo.setBanner(c.getBanner());
        vo.setDescription(c.getDescription());
        vo.setParentId(c.getParentId());
        vo.setLevel(c.getLevel());
        return vo;
    }
}
