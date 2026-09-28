package com.abcmall.smai.tool;

import com.abcmall.smai.entity.Sitemap;
import com.abcmall.smai.service.SitemapService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchSitemapTool {

    @Autowired
    SitemapService sitemapService;

    /**
     * 查询网站功能
     */
    @Tool(description = "搜索商城功能页面导航。用户想找某个功能页面时" +
             "（如修改密码、查看收藏、签到、领券、收货地址管理等），调用此工具搜索匹配的功能链接。" +
             "返回功能名称、URL路径和描述。")
    public List<Sitemap> searchSitemap(String keyword) {
        System.out.println("【工具调用 searchSitemap】");
        StepTracker.addStep("searchSitemap", "正在搜索功能导航【" + keyword + "】...");
        return sitemapService.query(keyword);
    }

}
