package com.abcmall.vo;

import com.abcmall.entity.Activity;
import com.abcmall.entity.Announcement;
import com.abcmall.entity.HomeBanner;
import com.abcmall.entity.Product;
import com.abcmall.entity.Topic;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 首页聚合数据 */
@Data
public class HomeVO implements Serializable {
    /** 轮播图 */
    private List<HomeBanner> carousels = new ArrayList<>();
    /** 首页广告位 */
    private List<HomeBanner> ads = new ArrayList<>();
    /** 侧栏广告 */
    private List<HomeBanner> sideAds = new ArrayList<>();
    /** 公告 */
    private List<Announcement> notices = new ArrayList<>();
    /** 活动专区 */
    private List<Activity> activities = new ArrayList<>();
    /** 专题 */
    private List<Topic> topics = new ArrayList<>();
    /** 分类入口 */
    private List<CategoryVO> categories = new ArrayList<>();
    /** 推荐商品 */
    private List<Product> recommends = new ArrayList<>();
    /** 热销榜单 */
    private List<Product> hots = new ArrayList<>();
    /** 新品上架 */
    private List<Product> news = new ArrayList<>();
}
