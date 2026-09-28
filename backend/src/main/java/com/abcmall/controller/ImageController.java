package com.abcmall.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 演示用占位图接口
 *
 * 商品/头像等图片地址统一使用 /api/image/ph?t=文案&w=宽&h=高&c=配色序号，
 * 由后端实时生成 SVG，保证离线环境下商城界面依然完整美观。
 * 接入真实图片时，只需把数据库里的地址换成 CDN 地址即可，前端无需改动。
 */
@RestController
@RequestMapping("/api/image")
public class ImageController {

    private static final String[] PALETTES = {
            "#5b8def,#8fb8ff",
            "#f2994a,#f7c58b",
            "#27ae60,#7fd8a0",
            "#eb5757,#f7a1a1",
            "#9b51e0,#c9a0f0",
            "#2d9cdb,#8fd3f4",
            "#f2c94c,#f9e08a",
            "#4f4f4f,#9b9b9b",
            "#00b8a9,#7fe0d8",
            "#e5626c,#f5a3a9",
            "#3f4b8f,#8f9ad8",
            "#7a5c3e,#c2a17d"
    };

    @GetMapping(value = "/ph", produces = "image/svg+xml;charset=UTF-8")
    public String placeholder(@RequestParam(required = false, defaultValue = "AbcMall") String t,
                              @RequestParam(defaultValue = "600") int w,
                              @RequestParam(defaultValue = "600") int h,
                              @RequestParam(defaultValue = "0") int c) {
        int width = Math.max(16, Math.min(w, 2000));
        int height = Math.max(16, Math.min(h, 2000));
        String palette = PALETTES[Math.abs(c) % PALETTES.length];
        String[] colors = palette.split(",");

        String text = escape(t);
        // 文案越长字号越小
        int fontSize = Math.max(14, Math.min(48, (int) (Math.min(width, height) / Math.max(3.0, text.length() * 0.62))));
        if (w >= 1000) {
            fontSize = Math.max(fontSize, 40);
        }

        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height
                + "\" viewBox=\"0 0 " + width + " " + height + "\">"
                + "<defs><linearGradient id=\"g\" x1=\"0\" y1=\"0\" x2=\"1\" y2=\"1\">"
                + "<stop offset=\"0%\" stop-color=\"" + colors[0] + "\"/>"
                + "<stop offset=\"100%\" stop-color=\"" + colors[1] + "\"/>"
                + "</linearGradient></defs>"
                + "<rect width=\"100%\" height=\"100%\" fill=\"url(#g)\"/>"
                + "<circle cx=\"" + (width * 0.85) + "\" cy=\"" + (height * 0.18) + "\" r=\""
                + (Math.min(width, height) * 0.18) + "\" fill=\"#ffffff\" opacity=\"0.13\"/>"
                + "<circle cx=\"" + (width * 0.12) + "\" cy=\"" + (height * 0.86) + "\" r=\""
                + (Math.min(width, height) * 0.22) + "\" fill=\"#ffffff\" opacity=\"0.10\"/>"
                + "<text x=\"50%\" y=\"50%\" fill=\"#ffffff\" font-size=\"" + fontSize
                + "\" font-family=\"Microsoft YaHei,PingFang SC,Hiragino Sans GB,sans-serif\""
                + " text-anchor=\"middle\" dominant-baseline=\"central\">" + text + "</text>"
                + "</svg>";
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
