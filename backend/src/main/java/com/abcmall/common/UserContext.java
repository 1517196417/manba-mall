package com.abcmall.common;

import com.abcmall.entity.SysUser;
import com.abcmall.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 基于 Session 的登录用户上下文工具
 *
 * 说明：本项目按需求使用 Session 做身份认证，
 *      登录成功后把 userId 写入 HttpSession，后续请求由拦截器校验。
 */
public final class UserContext {

    private UserContext() {
    }

    /** 获取当前请求的 Session */
    public static HttpSession session() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest().getSession(true);
    }

    public static HttpServletRequest request() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs == null ? null : attrs.getRequest();
    }

    /** 写入登录态 */
    public static void login(Long userId) {
        HttpSession session = session();
        if (session != null) {
            session.setAttribute(Constants.SESSION_USER_KEY, userId);
        }
    }

    /** 退出登录 */
    public static void logout() {
        HttpSession session = session();
        if (session != null) {
            session.invalidate();
        }
    }

    /** 当前登录用户ID，未登录返回 null */
    public static Long userId() {
        HttpServletRequest request = request();
        if (request == null) {
            return null;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(Constants.SESSION_USER_KEY);
        if (value == null) {
            return null;
        }
        if (value instanceof Long l) {
            return l;
        }
        return Long.valueOf(value.toString());
    }

    /** 当前登录用户ID，未登录直接抛 401 */
    public static Long requireUserId() {
        Long userId = userId();
        if (userId == null) {
            throw new BizException(Result.CODE_UNAUTHORIZED, "您还未登录，请先登录");
        }
        return userId;
    }

    /** 当前登录用户实体，未登录返回 null */
    public static SysUser currentUser() {
        Long userId = userId();
        if (userId == null) {
            return null;
        }
        UserService userService = SpringContextHolder.getBean(UserService.class);
        if (userService == null) {
            return null;
        }
        return userService.getById(userId);
    }
}
