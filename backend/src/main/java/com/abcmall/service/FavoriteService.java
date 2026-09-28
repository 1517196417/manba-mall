package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.PageResult;
import com.abcmall.entity.Favorite;
import com.abcmall.entity.Footprint;
import com.abcmall.entity.Product;
import com.abcmall.mapper.FavoriteMapper;
import com.abcmall.mapper.FootprintMapper;
import com.abcmall.mapper.ProductMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 收藏 / 浏览足迹服务 */
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final FootprintMapper footprintMapper;
    private final ProductMapper productMapper;

    /* ------------------------- 收藏 ------------------------- */

    public boolean isFavorite(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return false;
        }
        Long c = favoriteMapper.selectCount(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        return c != null && c > 0;
    }

    /** 收藏 / 取消收藏，返回收藏后的状态 */
    @Transactional(rollbackFor = Exception.class)
    public boolean toggle(Long userId, Long productId) {
        BizException.notNull(productMapper.selectById(productId), "商品不存在");
        List<Favorite> list = favoriteMapper.selectList(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (!list.isEmpty()) {
            for (Favorite f : list) {
                favoriteMapper.deleteById(f.getId());
            }
            return false;
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        favorite.setCreateTime(LocalDateTime.now());
        favoriteMapper.insert(favorite);
        return true;
    }

    public void removeFavorite(Long userId, Long productId) {
        favoriteMapper.delete(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
    }

    /** 我的收藏（分页，返回商品信息） */
    public PageResult<Product> favoritePage(Long userId, Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 12 : Math.min(pageSize, 50);

        Page<Favorite> page = favoriteMapper.selectPage(new Page<>(current, size),
                Wrappers.<Favorite>lambdaQuery()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getId));

        List<Product> products = new ArrayList<>();
        for (Favorite f : page.getRecords()) {
            Product p = productMapper.selectById(f.getProductId());
            if (p != null) {
                products.add(p);
            }
        }
        return new PageResult<>(products, page.getTotal(), current, size);
    }

    /* ------------------------- 足迹 ------------------------- */

    /** 记录浏览足迹（同一商品只保留最近一次） */
    @Transactional(rollbackFor = Exception.class)
    public void addFootprint(Long userId, Long productId) {
        if (userId == null || productId == null) {
            return;
        }
        footprintMapper.delete(Wrappers.<Footprint>lambdaQuery()
                .eq(Footprint::getUserId, userId)
                .eq(Footprint::getProductId, productId));

        Footprint fp = new Footprint();
        fp.setUserId(userId);
        fp.setProductId(productId);
        fp.setCreateTime(LocalDateTime.now());
        footprintMapper.insert(fp);

        // 只保留最近 100 条
        List<Footprint> all = footprintMapper.selectList(Wrappers.<Footprint>lambdaQuery()
                .eq(Footprint::getUserId, userId)
                .orderByDesc(Footprint::getId));
        for (int i = 100; i < all.size(); i++) {
            footprintMapper.deleteById(all.get(i).getId());
        }
    }

    /** 我的足迹：按日期分组 */
    public List<Map<String, Object>> footprintGrouped(Long userId, Integer limit) {
        int max = limit == null || limit < 1 ? 60 : Math.min(limit, 200);
        List<Footprint> list = footprintMapper.selectList(Wrappers.<Footprint>lambdaQuery()
                .eq(Footprint::getUserId, userId)
                .orderByDesc(Footprint::getId)
                .last("limit " + max));

        Map<String, List<Map<String, Object>>> group = new LinkedHashMap<>();
        for (Footprint fp : list) {
            Product p = productMapper.selectById(fp.getProductId());
            if (p == null) {
                continue;
            }
            String date = fp.getCreateTime() == null ? "更早"
                    : fp.getCreateTime().toLocalDate().toString();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("footprintId", fp.getId());
            item.put("product", p);
            item.put("time", fp.getCreateTime());
            group.computeIfAbsent(date, k -> new ArrayList<>()).add(item);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        group.forEach((date, items) -> {
            Map<String, Object> node = new HashMap<>();
            node.put("date", date);
            node.put("items", items);
            result.add(node);
        });
        return result;
    }

    public void clearFootprint(Long userId) {
        footprintMapper.delete(Wrappers.<Footprint>lambdaQuery().eq(Footprint::getUserId, userId));
    }

    public void removeFootprint(Long userId, Long footprintId) {
        Footprint fp = footprintMapper.selectById(footprintId);
        if (fp != null && fp.getUserId().equals(userId)) {
            footprintMapper.deleteById(footprintId);
        }
    }
}
