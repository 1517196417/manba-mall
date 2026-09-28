package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.dto.PasswordDTO;
import com.abcmall.dto.ProfileDTO;
import com.abcmall.entity.BalanceRecord;
import com.abcmall.entity.SysUser;
import com.abcmall.mapper.BalanceRecordMapper;
import com.abcmall.service.CommentService;
import com.abcmall.service.FavoriteService;
import com.abcmall.service.OrderService;
import com.abcmall.service.PointsService;
import com.abcmall.service.UserService;
import com.abcmall.vo.CartItemVO;
import com.abcmall.vo.OrderStatisticsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 会员中心 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrderService orderService;
    private final PointsService pointsService;
    private final FavoriteService favoriteService;
    private final CommentService commentService;
    private final BalanceRecordMapper balanceRecordMapper;
    private final com.abcmall.service.CouponService couponService;
    private final com.abcmall.service.AfterSaleService afterSaleService;

    /** 个人资料 */
    @GetMapping("/profile")
    public Result<SysUser> profile() {
        SysUser user = userService.getById(UserContext.requireUserId());
        // 脱敏
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<SysUser> updateProfile(@RequestBody ProfileDTO dto) {
        Long userId = UserContext.requireUserId();
        userService.updateProfile(userId, dto);
        return Result.success("资料已更新", userService.getById(userId));
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        userService.changePassword(UserContext.requireUserId(), dto);
        return Result.success("密码修改成功，请重新登录", null);
    }

    /** 会员中心首页统计：订单、券、积分、收藏、足迹、消息 */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Long userId = UserContext.requireUserId();
        SysUser user = userService.getById(userId);
        OrderStatisticsVO orderStat = orderService.statistics(userId);

        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, Object> userInfo = new LinkedHashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("level", user.getLevel());
        userInfo.put("levelName", PointsService.levelName(user.getLevel()));
        userInfo.put("growth", user.getGrowth());
        userInfo.put("nextLevelGrowth", PointsService.nextLevelGrowth(user.getGrowth()));
        userInfo.put("points", user.getPoints());
        userInfo.put("balance", user.getBalance());
        userInfo.put("phone", user.getPhone());
        userInfo.put("email", user.getEmail());
        userInfo.put("isVerified", user.getIsVerified());
        userInfo.put("gender", user.getGender());
        userInfo.put("birthday", user.getBirthday());
        map.put("user", userInfo);
        map.put("orderStatistics", orderStat);
        map.put("couponCount", couponService.myCoupons(userId, 0).size());
        map.put("favoriteCount", favoriteService.favoritePage(userId, 1, 1).getTotal());
        map.put("afterSaleCount", afterSaleService.countByStatus(userId, null));
        map.put("commentCount", commentService.myComments(userId, 1, 1).getTotal());
        return Result.success(map);
    }

    /** 订单统计（我的页面角标） */
    @GetMapping("/order-statistics")
    public Result<OrderStatisticsVO> orderStatistics() {
        return Result.success(orderService.statistics(UserContext.requireUserId()));
    }

    /** 积分记录 */
    @GetMapping("/points/records")
    public Result<PageResult<com.abcmall.entity.PointsRecord>> pointsRecords(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(pointsService.records(UserContext.requireUserId(), type, page, size));
    }

    /** 积分与会员等级概览 */
    @GetMapping("/points/overview")
    public Result<Map<String, Object>> pointsOverview() {
        return Result.success(pointsService.overview(UserContext.requireUserId()));
    }

    /** 余额流水 */
    @GetMapping("/balance/records")
    public Result<PageResult<BalanceRecord>> balanceRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = UserContext.requireUserId();
        int current = page == null || page < 1 ? 1 : page;
        int size2 = size == null || size < 1 ? 10 : size;
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<BalanceRecord> p =
                balanceRecordMapper.selectPage(
                        new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(current, size2),
                        com.baomidou.mybatisplus.core.toolkit.Wrappers.<BalanceRecord>lambdaQuery()
                                .eq(BalanceRecord::getUserId, userId)
                                .orderByDesc(BalanceRecord::getId));
        return Result.success(PageResult.of(p));
    }

    /** 我的收藏 */
    @GetMapping("/favorite/page")
    public Result<PageResult<com.abcmall.entity.Product>> favorites(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(favoriteService.favoritePage(UserContext.requireUserId(), page, size));
    }

    /** 我的足迹（按日期分组） */
    @GetMapping("/footprint/list")
    public Result<List<Map<String, Object>>> footprints(@RequestParam(defaultValue = "60") Integer limit) {
        return Result.success(favoriteService.footprintGrouped(UserContext.requireUserId(), limit));
    }

    /** 我的购物车数量 */
    @GetMapping("/cart-count")
    public Result<Integer> cartCount() {
        List<CartItemVO> list = com.abcmall.common.SpringContextHolder
                .getBean(com.abcmall.service.CartService.class)
                .listVO(UserContext.requireUserId());
        return Result.success(list.size());
    }

    /** 会员等级 */
    @GetMapping("/level")
    public Result<Map<String, Object>> level() {

        SysUser user = userService.getById(UserContext.requireUserId());
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("level", user.getLevel());
        map.put("levelName", PointsService.levelName(user.getLevel()));
        map.put("growth", user.getGrowth());
        map.put("nextLevelGrowth", PointsService.nextLevelGrowth(user.getGrowth()));
        map.put("levels", List.of(
                levelInfo(1, "普通会员", 0),
                levelInfo(2, "银卡会员", 500),
                levelInfo(3, "金卡会员", 2000),
                levelInfo(4, "白金会员", 5000),
                levelInfo(5, "钻石会员", 10000)
        ));
        return Result.success(map);
    }

    private Map<String, Object> levelInfo(int level, String name, int growth) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("level", level);
        m.put("name", name);
        m.put("growth", growth);
        return m;
    }
}
