package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.entity.SeckillActivity;
import com.abcmall.service.HomeService;
import com.abcmall.vo.SignStatusVO;
import com.abcmall.common.UserContext;
import com.abcmall.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 营销互动
 *
 * 需求说明：满减 / 折扣 / 满赠 / 秒杀 / 拼团 / 砍价 / 预售 / 套餐 / 赠品 /
 * 抽奖 / 邀请有礼 / 分销 / 直播 / 短视频 / 种草社区 均标注为「功能说明页」，
 * 这里只提供前端功能说明页所需的标题与描述字典。
 */
@RestController
@RequestMapping("/api/marketing")
@RequiredArgsConstructor
public class MarketingController {

    private final HomeService homeService;
    private final PointsService pointsService;

    /** 营销功能字典（前端「功能说明页」数据源） */
    @GetMapping("/features")
    public Result<List<Map<String, Object>>> features() {
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(feature("full-reduce", "满减", "满 299 减 50、满 599 减 100 等阶梯满减规则"));
        list.add(feature("discount", "折扣", "指定商品或分类的限时折扣"));
        list.add(feature("full-gift", "满赠", "满额赠送指定赠品"));
        list.add(feature("seckill", "秒杀", "每日整点限时秒杀，低至 5 折"));
        list.add(feature("groupon", "拼团", "邀请好友组团享团购价"));
        list.add(feature("bargain", "砍价", "邀请好友助力砍价，最低 0 元拿"));
        list.add(feature("presale", "预售", "新品预售，定金膨胀抵尾款"));
        list.add(feature("combo", "套餐", "多件组合购买，套餐价更优惠"));
        list.add(feature("gift", "赠品", "购买指定商品赠送配套赠品"));
        list.add(feature("lottery", "抽奖", "积分抽奖，赢取优惠券与实物好礼"));
        list.add(feature("invite", "邀请有礼", "邀请好友注册，双方各得优惠券"));
        list.add(feature("distribution", "分销", "分享商品赚取佣金"));
        list.add(feature("live", "直播", "直播间边看边买"));
        list.add(feature("video", "短视频", "短视频种草，一键加购"));
        list.add(feature("community", "种草社区", "用户晒单与达人种草内容"));
        return Result.success(list);
    }

    /** 秒杀场次（只读） */
    @GetMapping("/seckill/list")
    public Result<List<SeckillActivity>> seckillList() {
        return Result.success(homeService.seckillList());
    }

    /** 签到（与 /api/points/sign 等价） */
    @PostMapping("/sign")
    public Result<SignStatusVO> sign() {
        return Result.success("签到成功", pointsService.sign(UserContext.requireUserId()));
    }

    @GetMapping("/sign/status")
    public Result<SignStatusVO> signStatus() {
        return Result.success(pointsService.signStatus(UserContext.requireUserId()));
    }

    private Map<String, Object> feature(String key, String name, String description) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", key);
        m.put("name", name);
        m.put("description", description);
        m.put("tip", "此为" + name + "功能");
        return m;
    }
}
