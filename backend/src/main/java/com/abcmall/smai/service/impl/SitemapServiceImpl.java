package com.abcmall.smai.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.abcmall.smai.entity.Sitemap;
import com.abcmall.smai.service.SitemapService;
import com.abcmall.smai.mapper.SitemapMapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
* @author Administrator
* @description 针对表【sitemap(站点地图/功能导航表)】的数据库操作Service实现
* @createDate 2026-09-23 10:15:10
*/
@Service
public class SitemapServiceImpl extends ServiceImpl<SitemapMapper, Sitemap>
    implements SitemapService{

    /**
     * 查询网站功能
     */
    public List<Sitemap> query(String keyword) {
        // 查询关键字为空，则返回空列表
        if(keyword==null || keyword.isBlank())
            return Collections.emptyList();

        // 查询查询
        LambdaQueryChainWrapper<Sitemap> query = this.lambdaQuery();
        // 状态为1，即启用
        query.eq(Sitemap::getStatus, 1);
        // 关键词包含查询关键字
        query.and(x -> x.like(Sitemap::getName, keyword)
                .or().like(Sitemap::getKeywords, keyword)
                .or().like(Sitemap::getDescription, keyword));
        // 排序
        query.orderByAsc(Sitemap::getSortOrder);
        // 查询结果
        return query.list();
    }

}




