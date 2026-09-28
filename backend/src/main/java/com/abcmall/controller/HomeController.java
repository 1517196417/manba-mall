package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.entity.Activity;
import com.abcmall.entity.Announcement;
import com.abcmall.entity.HelpArticle;
import com.abcmall.entity.HomeBanner;
import com.abcmall.entity.SeckillActivity;
import com.abcmall.entity.Topic;
import com.abcmall.service.HomeService;
import com.abcmall.vo.HomeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 首页与运营位 */
@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /** 首页聚合数据：轮播、广告、公告、活动、专题、分类、推荐、热销、新品 */
    @GetMapping("/index")
    public Result<HomeVO> index() {
        return Result.success(homeService.index());
    }

    /** 轮播图 / 广告位 */
    @GetMapping("/banners")
    public Result<List<HomeBanner>> banners(@RequestParam(defaultValue = "carousel") String position) {
        return Result.success(homeService.banners(position));
    }

    @GetMapping("/banners/group")
    public Result<Map<String, List<HomeBanner>>> bannerGroup() {
        return Result.success(homeService.bannerGroup());
    }

    /** 公告 */
    @GetMapping("/notice/page")
    public Result<PageResult<Announcement>> noticePage(@RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(homeService.noticePage(page, size));
    }

    @GetMapping("/notice/{id}")
    public Result<Announcement> noticeDetail(@PathVariable Long id) {
        return Result.success(homeService.noticeDetail(id));
    }

    /** 活动专区 */
    @GetMapping("/activity/list")
    public Result<List<Activity>> activities() {
        return Result.success(homeService.activities());
    }

    @GetMapping("/activity/{id}")
    public Result<Activity> activityDetail(@PathVariable Long id) {
        return Result.success(homeService.activityDetail(id));
    }

    /** 专题 */
    @GetMapping("/topic/list")
    public Result<List<Topic>> topics(@RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(homeService.topics(limit));
    }

    @GetMapping("/topic/{id}")
    public Result<Map<String, Object>> topicDetail(@PathVariable Long id) {
        return Result.success(homeService.topicDetail(id));
    }

    /** 热门搜索词 */
    @GetMapping("/hot-keywords")
    public Result<List<String>> hotKeywords() {
        return Result.success(homeService.hotKeywords());
    }

    /** 秒杀（功能说明页数据源） */
    @GetMapping("/seckill/list")
    public Result<List<SeckillActivity>> seckillList() {
        return Result.success(homeService.seckillList());
    }

    /** 帮助中心（功能说明页数据源） */
    @GetMapping("/help/list")
    public Result<List<HelpArticle>> helpArticles(@RequestParam(required = false) String category) {
        return Result.success(homeService.helpArticles(category));
    }

    /** 服务端时间（前端倒计时校准） */
    @GetMapping("/server-time")
    public Result<Map<String, Object>> serverTime() {
        return Result.success(homeService.serverTime());
    }
}
