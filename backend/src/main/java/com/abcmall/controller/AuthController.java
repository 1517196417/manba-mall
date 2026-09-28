package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.dto.LoginDTO;
import com.abcmall.dto.RegisterDTO;
import com.abcmall.entity.SysUser;
import com.abcmall.service.CartService;
import com.abcmall.service.MessageService;
import com.abcmall.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 认证接口（Session 认证）
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final CartService cartService;
    private final MessageService messageService;

    /** 注册 */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        SysUser user = userService.register(dto);
        UserContext.login(user.getId());
        messageService.send(user.getId(), "欢迎加入阿帕茨商城",
                "新人注册礼已到账：300 积分，可在积分商城兑换好礼。", "system", "/points-mall");
        return Result.success("注册成功", sessionInfo(user));
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        SysUser user = userService.login(dto);
        // Session 认证核心：登录成功后把 userId 写入 HttpSession
        UserContext.login(user.getId());
        return Result.success("登录成功", sessionInfo(user));
    }

    /** 退出登录 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return Result.success("已退出登录", null);
    }

    /** 获取当前登录用户（未登录返回 data=null，前端据此判断登录态） */
    @GetMapping("/current")
    public Result<Map<String, Object>> current() {
        SysUser user = UserContext.currentUser();
        if (user == null) {
            return Result.success(null);
        }
        return Result.success(sessionInfo(user));
    }

    /** 会话信息（含购物车数量、未读消息数，方便前端做角标） */
    private Map<String, Object> sessionInfo(SysUser user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("nickname", user.getNickname());
        map.put("avatar", user.getAvatar());
        map.put("phone", maskPhone(user.getPhone()));
        map.put("email", user.getEmail());
        map.put("gender", user.getGender());
        map.put("level", user.getLevel());
        map.put("levelName", com.abcmall.service.PointsService.levelName(user.getLevel()));
        map.put("growth", user.getGrowth());
        map.put("points", user.getPoints());
        map.put("balance", user.getBalance());
        map.put("isVerified", user.getIsVerified());
        map.put("cartCount", cartService.countByUser(user.getId()));
        map.put("unreadCount", messageService.unreadCount(user.getId()));
        return map;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
