package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.PageResult;
import com.abcmall.entity.Activity;
import com.abcmall.entity.Announcement;
import com.abcmall.entity.HelpArticle;
import com.abcmall.entity.HomeBanner;
import com.abcmall.entity.SearchHot;
import com.abcmall.entity.SeckillActivity;
import com.abcmall.entity.Topic;
import com.abcmall.mapper.ActivityMapper;
import com.abcmall.mapper.AnnouncementMapper;
import com.abcmall.mapper.HelpArticleMapper;
import com.abcmall.mapper.HomeBannerMapper;
import com.abcmall.mapper.ProductMapper;
import com.abcmall.mapper.SearchHotMapper;
import com.abcmall.mapper.SeckillActivityMapper;
import com.abcmall.mapper.TopicMapper;
import com.abcmall.entity.Product;
import com.abcmall.vo.HomeVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 首页 / 运营位服务 */
@Service
@RequiredArgsConstructor
public class HomeService {

    private final HomeBannerMapper bannerMapper;
    private final AnnouncementMapper announcementMapper;
    private final ActivityMapper activityMapper;
    private final TopicMapper topicMapper;
    private final SearchHotMapper searchHotMapper;
    private final SeckillActivityMapper seckillMapper;
    private final HelpArticleMapper helpArticleMapper;
    private final ProductMapper productMapper;
    private final ProductService productService;
    private final CategoryService categoryService;

    /** 首页聚合数据 */
    public HomeVO index() {
        HomeVO vo = new HomeVO();
        vo.setCarousels(banners("carousel"));
        vo.setAds(banners("ad_home"));
        vo.setSideAds(banners("ad_side"));
        vo.setNotices(notices(6));
        vo.setActivities(activities());
        vo.setTopics(topics(4));
        vo.setCategories(categoryService.tree());
        vo.setRecommends(productService.listByFlag("recommend", 10));
        vo.setHots(productService.listByFlag("hot", 8));
        vo.setNews(productService.listByFlag("new", 8));
        return vo;
    }

    public List<HomeBanner> banners(String position) {
        return bannerMapper.selectList(Wrappers.<HomeBanner>lambdaQuery()
                .eq(HomeBanner::getStatus, 1)
                .eq(StringUtils.hasText(position), HomeBanner::getPosition, position)
                .orderByAsc(HomeBanner::getSort)
                .orderByAsc(HomeBanner::getId));
    }

    public List<Announcement> notices(int limit) {
        return announcementMapper.selectList(Wrappers.<Announcement>lambdaQuery()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getIsTop)
                .orderByAsc(Announcement::getSort)
                .last("limit " + Math.max(1, limit)));
    }

    public PageResult<Announcement> noticePage(Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        Page<Announcement> page = announcementMapper.selectPage(new Page<>(current, size),
                Wrappers.<Announcement>lambdaQuery()
                        .eq(Announcement::getStatus, 1)
                        .orderByDesc(Announcement::getIsTop)
                        .orderByAsc(Announcement::getSort));
        return PageResult.of(page);
    }

    public Announcement noticeDetail(Long id) {
        Announcement a = announcementMapper.selectById(id);
        BizException.notNull(a, "公告不存在");
        return a;
    }

    public List<Activity> activities() {
        return activityMapper.selectList(Wrappers.<Activity>lambdaQuery()
                .eq(Activity::getStatus, 1)
                .orderByAsc(Activity::getSort)
                .orderByAsc(Activity::getId));
    }

    public Activity activityDetail(Long id) {
        Activity a = activityMapper.selectById(id);
        BizException.notNull(a, "活动不存在");
        return a;
    }

    public List<Topic> topics(int limit) {
        return topicMapper.selectList(Wrappers.<Topic>lambdaQuery()
                .eq(Topic::getStatus, 1)
                .orderByAsc(Topic::getSort)
                .last("limit " + Math.max(1, limit)));
    }

    public Map<String, Object> topicDetail(Long id) {
        Topic topic = topicMapper.selectById(id);
        BizException.notNull(topic, "专题不存在");
        topicMapper.update(null, Wrappers.<Topic>lambdaUpdate()
                .eq(Topic::getId, id)
                .setSql("views = views + 1"));

        List<Product> products = new ArrayList<>();
        if (StringUtils.hasText(topic.getProductIds())) {
            for (String sid : topic.getProductIds().split(",")) {
                if (sid.isBlank()) {
                    continue;
                }
                try {
                    Product p = productMapper.selectById(Long.valueOf(sid.trim()));
                    if (p != null) {
                        products.add(p);
                    }
                } catch (NumberFormatException ignored) {
                    // 忽略非法ID
                }
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topic", topic);
        result.put("products", products);
        return result;
    }

    /** 热门搜索词 */
    public List<String> hotKeywords() {
        List<SearchHot> list = searchHotMapper.selectList(Wrappers.<SearchHot>lambdaQuery()
                .orderByDesc(SearchHot::getIsHot)
                .orderByAsc(SearchHot::getSort)
                .last("limit 12"));
        List<String> result = new ArrayList<>();
        for (SearchHot s : list) {
            result.add(s.getKeyword());
        }
        return result;
    }

    /** 秒杀列表（需求标注为功能说明页，此处保留只读数据接口） */
    public List<SeckillActivity> seckillList() {
        List<SeckillActivity> list = seckillMapper.selectList(Wrappers.<SeckillActivity>lambdaQuery()
                .eq(SeckillActivity::getStatus, 1)
                .orderByAsc(SeckillActivity::getStartTime));
        for (SeckillActivity s : list) {
            Product p = productMapper.selectById(s.getProductId());
            if (p != null) {
                s.setProductName(p.getName());
                s.setProductImage(p.getMainImage());
                s.setOriginPrice(p.getPrice());
            }
        }
        return list;
    }

    /** 帮助中心（需求标注为功能说明页，此处保留只读数据接口） */
    public List<HelpArticle> helpArticles(String category) {
        return helpArticleMapper.selectList(Wrappers.<HelpArticle>lambdaQuery()
                .eq(HelpArticle::getStatus, 1)
                .eq(StringUtils.hasText(category), HelpArticle::getCategory, category)
                .orderByAsc(HelpArticle::getSort));
    }

    /** 服务端时间，供前端倒计时校准 */
    public Map<String, Object> serverTime() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("now", LocalDateTime.now());
        map.put("timestamp", System.currentTimeMillis());
        return map;
    }

    /** 广告位按位置分组 */
    public Map<String, List<HomeBanner>> bannerGroup() {
        Map<String, List<HomeBanner>> map = new LinkedHashMap<>();
        map.put("carousel", banners("carousel"));
        map.put("ad_home", banners("ad_home"));
        map.put("ad_side", banners("ad_side"));
        return map;
    }

    /** 清理过期活动（预留） */
    public List<Activity> expiredActivities() {
        return activityMapper.selectList(Wrappers.<Activity>lambdaQuery()
                .lt(Activity::getEndTime, LocalDateTime.now()));
    }
}
