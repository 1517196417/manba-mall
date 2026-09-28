package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.entity.Coupon;
import com.abcmall.entity.UserCoupon;
import com.abcmall.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** 优惠券 */
@RestController
@RequestMapping("/api/coupon")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    /** 领券中心 */
    @GetMapping("/available")
    public Result<List<Coupon>> available() {
        return Result.success(couponService.available(UserContext.userId()));
    }

    @GetMapping("/{id}")
    public Result<Coupon> detail(@PathVariable Long id) {
        return Result.success(com.abcmall.common.SpringContextHolder
                .getBean(com.abcmall.mapper.CouponMapper.class).selectById(id));
    }

    /** 领取优惠券 */
    @PostMapping("/receive/{id}")
    public Result<Void> receive(@PathVariable Long id) {
        couponService.receive(UserContext.requireUserId(), id);
        return Result.success("领取成功，可在「我的优惠券」查看", null);
    }

    /** 我的优惠券 */
    @GetMapping("/my")
    public Result<List<UserCoupon>> my(@RequestParam(required = false) Integer status) {
        return Result.success(couponService.myCoupons(UserContext.requireUserId(), status));
    }

    /** 指定金额下可用的优惠券 */
    @GetMapping("/usable")
    public Result<List<UserCoupon>> usable(@RequestParam BigDecimal amount,
                                          @RequestParam(required = false) Long userCouponId) {
        return Result.success(couponService.usable(UserContext.requireUserId(), amount, userCouponId));
    }

    /** 我的优惠券数量统计 */
    @GetMapping("/my/count")
    public Result<java.util.Map<String, Integer>> myCount() {
        Long userId = UserContext.requireUserId();
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        map.put("unused", couponService.myCoupons(userId, 0).size());
        map.put("used", couponService.myCoupons(userId, 1).size());
        map.put("expired", couponService.myCoupons(userId, 2).size());
        return Result.success(map);
    }
}
