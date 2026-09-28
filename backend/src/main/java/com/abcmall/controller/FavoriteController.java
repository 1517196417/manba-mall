package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** 商品收藏 */
@RestController
@RequestMapping("/api/favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 收藏 / 取消收藏，返回最新状态 */
    @PostMapping("/toggle/{productId}")
    public Result<Map<String, Object>> toggle(@PathVariable Long productId) {
        boolean favorite = favoriteService.toggle(UserContext.requireUserId(), productId);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("favorite", favorite);
        return Result.success(favorite ? "收藏成功" : "已取消收藏", map);
    }

    /** 是否已收藏 */
    @GetMapping("/check/{productId}")
    public Result<Boolean> check(@PathVariable Long productId) {
        return Result.success(favoriteService.isFavorite(UserContext.userId(), productId));
    }

    /** 取消收藏 */
    @DeleteMapping("/{productId}")
    public Result<Void> remove(@PathVariable Long productId) {
        favoriteService.removeFavorite(UserContext.requireUserId(), productId);
        return Result.success("已取消收藏", null);
    }
}
