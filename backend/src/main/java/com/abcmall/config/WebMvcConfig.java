package com.abcmall.config;

import com.abcmall.common.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：登录拦截器 + 跨域
 *
 * 说明：按需求，前端开发环境使用 vite.config.js 的 proxy 解决跨域，
 *      这里的 CORS 配置只是为了让「直接访问 8080」的调试方式也能用，
 *      生产部署时前后端同源即可，无需依赖它。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 需要登录才能访问的接口 */
    private static final String[] AUTH_PATTERNS = {
            "/api/cart/**",
            "/api/address/**",
            "/api/order/**",
            "/api/pay/**",
            "/api/user/**",
            "/api/message/**",
            "/api/aftersale/**",
            "/api/favorite/**",
            "/api/footprint/**",
            "/api/points/**",
            "/api/comment/add",
            "/api/comment/my/**",
            "/api/comment/*/append",
            "/api/coupon/receive/**",
            "/api/coupon/my/**",
            "/api/marketing/sign/**"
    };

    /** 无需拦截的公开接口（登录/注册等） */
    private static final String[] EXCLUDE_PATTERNS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/current",
            "/api/auth/logout"
    };

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns(AUTH_PATTERNS)
                .excludePathPatterns(EXCLUDE_PATTERNS);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
