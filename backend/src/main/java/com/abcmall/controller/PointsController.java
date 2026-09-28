package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.entity.PointsExchange;
import com.abcmall.entity.PointsGoods;
import com.abcmall.service.PointsService;
import com.abcmall.vo.SignStatusVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 积分：签到 / 积分商城 / 积分记录 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    /** 签到状态 */
    @GetMapping("/sign/status")
    public Result<SignStatusVO> signStatus() {
        return Result.success(pointsService.signStatus(UserContext.requireUserId()));
    }

    /** 每日签到 */
    @PostMapping("/sign")
    public Result<SignStatusVO> sign() {
        return Result.success("签到成功", pointsService.sign(UserContext.requireUserId()));
    }

    /** 积分记录 */
    @GetMapping("/records")
    public Result<PageResult<com.abcmall.entity.PointsRecord>> records(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(pointsService.records(UserContext.requireUserId(), type, page, size));
    }

    /** 积分商城商品 */
    @GetMapping("/goods")
    public Result<PageResult<PointsGoods>> goods(@RequestParam(defaultValue = "1") Integer page,
                                                @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(pointsService.goodsPage(page, size));
    }

    /** 积分兑换 */
    @PostMapping("/exchange/{goodsId}")
    public Result<PointsExchange> exchange(@PathVariable Long goodsId) {
        return Result.success("兑换成功", pointsService.exchange(UserContext.requireUserId(), goodsId));
    }

    /** 我的兑换记录 */
    @GetMapping("/exchanges")
    public Result<List<PointsExchange>> exchanges() {
        return Result.success(pointsService.myExchanges(UserContext.requireUserId()));
    }

    /** 积分概览（含会员等级） */
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview() {
        return Result.success(pointsService.overview(UserContext.requireUserId()));
    }
}
