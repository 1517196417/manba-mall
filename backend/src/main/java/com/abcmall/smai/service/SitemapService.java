package com.abcmall.smai.service;

import com.abcmall.smai.entity.Sitemap;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author Administrator
* @description 针对表【sitemap(站点地图/功能导航表)】的数据库操作Service
* @createDate 2026-09-23 10:15:10
*/
public interface SitemapService extends IService<Sitemap> {

    /**
     * 查询网站功能
     */
    List<Sitemap> query(String keyword);

}
