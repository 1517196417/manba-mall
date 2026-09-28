package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.entity.HelpArticle;
import com.abcmall.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 帮助中心（需求标注为功能说明页，此处保留只读数据接口） */
@RestController
@RequestMapping("/api/help")
@RequiredArgsConstructor
public class HelpController {

    private final HomeService homeService;

    @GetMapping("/list")
    public Result<List<HelpArticle>> list(@RequestParam(required = false) String category) {
        return Result.success(homeService.helpArticles(category));
    }
}
