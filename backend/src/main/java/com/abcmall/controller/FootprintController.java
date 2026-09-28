package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 浏览足迹 */
@RestController
@RequestMapping("/api/footprint")
@RequiredArgsConstructor
public class FootprintController {

    private final FavoriteService favoriteService;

    /** 记录足迹 */
    @PostMapping("/{productId}")
    public Result<Void> add(@PathVariable Long productId) {
        favoriteService.addFootprint(UserContext.requireUserId(), productId);
        return Result.success(null);
    }

    /** 足迹列表（按日期分组） */
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(@RequestParam(defaultValue = "60") Integer limit) {
        return Result.success(favoriteService.footprintGrouped(UserContext.requireUserId(), limit));
    }

    /** 清空足迹 */
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        favoriteService.clearFootprint(UserContext.requireUserId());
        return Result.success("足迹已清空", null);
    }

    /** 删除单条足迹 */
    @DeleteMapping("/{footprintId}")
    public Result<Void> remove(@PathVariable Long footprintId) {
        favoriteService.removeFootprint(UserContext.requireUserId(), footprintId);
        return Result.success("已删除", null);
    }
}
