package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.service.PayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 支付
 *
 * 需求说明：微信支付 / 支付宝 / 银联 / 余额支付 / 组合支付 / 支付结果回调 / 退款
 * 均为「功能说明页」，因此这里只保留：
 *   - 收银台支付方式列表（methods）
 *   - 模拟支付成功（mock，用于打通完整演示链路）
 *   - 支付结果查询（result）
 */
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PayController {

    private final PayService payService;

    /** 收银台：支付方式 + 应付金额 */
    @GetMapping("/methods/{orderId}")
    public Result<Map<String, Object>> methods(@PathVariable Long orderId) {
        return Result.success(payService.payMethods(orderId, UserContext.requireUserId()));
    }

    /** 模拟支付成功（演示用；真实项目请对接各支付渠道 SDK 与异步回调） */
    @PostMapping("/mock/{orderId}")
    public Result<Map<String, Object>> mockPay(@PathVariable Long orderId,
                                               @RequestParam(defaultValue = "wechat") String channel) {
        return Result.success("支付成功", payService.mockPay(orderId, UserContext.requireUserId(), channel));
    }

    /** 支付结果 */
    @GetMapping("/result/{orderNo}")
    public Result<Map<String, Object>> result(@PathVariable String orderNo) {
        return Result.success(payService.resultByOrderNo(orderNo, UserContext.requireUserId()));
    }

    /** 支付方式字典（供功能说明页展示） */
    @GetMapping("/channels")
    public Result<java.util.List<Map<String, Object>>> channels() {
        java.util.List<Map<String, Object>> list = new java.util.ArrayList<>();
        list.add(channel("wechat", "微信支付", "此为微信支付功能"));
        list.add(channel("alipay", "支付宝", "此为支付宝功能"));
        list.add(channel("unionpay", "银联支付", "此为银联支付功能"));
        list.add(channel("balance", "余额支付", "此为余额支付功能"));
        list.add(channel("combined", "组合支付", "此为组合支付功能"));
        list.add(channel("callback", "支付结果回调", "此为支付结果回调功能"));
        list.add(channel("refund", "退款", "此为退款功能"));
        list.add(channel("partial-refund", "部分退款", "此为部分退款功能"));
        list.add(channel("refund-progress", "退款进度", "此为退款进度功能"));
        return Result.success(list);
    }

    private Map<String, Object> channel(String key, String name, String tip) {
        Map<String, Object> m = new java.util.LinkedHashMap<>();
        m.put("key", key);
        m.put("name", name);
        m.put("tip", tip);
        return m;
    }
}
