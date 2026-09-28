package com.abcmall.smai.controller;

import com.abcmall.smai.entity.Sitemap;
import com.abcmall.smai.service.SitemapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SitemapController {

    @Autowired
    SitemapService sitemapService;

    /**
     * 查询网站功能
     */
    @GetMapping("/api/sitemap/query")
    public List<Sitemap> query(String keyword){
        return sitemapService.query(keyword);
    }

}
