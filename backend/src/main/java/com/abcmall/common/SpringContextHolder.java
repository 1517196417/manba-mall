package com.abcmall.common;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 静态获取 Spring Bean（供工具类使用）
 */
@Component
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.context = applicationContext;
    }

    public static <T> T getBean(Class<T> clazz) {
        if (context == null) {
            return null;
        }
        try {
            return context.getBean(clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
