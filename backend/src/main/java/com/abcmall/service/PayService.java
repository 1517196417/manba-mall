package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.Constants;
import com.abcmall.entity.ShopOrder;
import com.abcmall.entity.SysUser;
import com.abcmall.mapper.ShopOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付服务
 *
 * 需求说明：微信支付 / 支付宝 / 银联 / 余额支付 均标注为「功能说明页」，
 * 因此这里不接入真实支付渠道，只提供：
 *   1) 支付方式列表（用于收银台展示）
 *   2) 模拟支付成功接口，用于打通「下单 -> 支付 -> 发货 -> 收货」的完整演示链路
 */
@Service
@RequiredArgsConstructor
public class PayService {

    private final ShopOrderMapper shopOrderMapper;
    private final OrderService orderService;
    private final UserService userService;
    private final MessageService messageService;

    /** 收银台支付方式（channel = placeholder 表示该渠道为功能说明页） */
    public Map<String, Object> payMethods(Long orderId, Long userId) {
        ShopOrder order = requireOrder(orderId, userId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        result.put("payAmount", order.getPayAmount());
        result.put("payStatus", order.getPayStatus());
        result.put("orderStatus", order.getOrderStatus());

        SysUser user = userService.getById(userId);
        result.put("balance", user == null ? BigDecimal.ZERO : user.getBalance());
        result.put("methods", new Object[]{
                method(Constants.PAY_TYPE_WECHAT, "微信支付", "wechat", true, "此为微信支付功能"),
                method(Constants.PAY_TYPE_ALIPAY, "支付宝", "alipay", true, "此为支付宝功能"),
                method(Constants.PAY_TYPE_UNIONPAY, "银联支付", "unionpay", true, "此为银联支付功能"),
                method(Constants.PAY_TYPE_BALANCE, "余额支付", "balance", true, "此为余额支付功能")
        });
        return result;
    }

    private Map<String, Object> method(int type, String name, String icon, boolean placeholder, String tip) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("name", name);
        m.put("icon", icon);
        m.put("placeholder", placeholder);
        m.put("tip", tip);
        return m;
    }

    /**
     * 模拟支付成功
     *
     * @param channel 渠道标识，仅用于展示（wechat / alipay / unionpay / balance）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> mockPay(Long orderId, Long userId, String channel) {
        ShopOrder order = requireOrder(orderId, userId);
        BizException.check(order.getPayStatus() != null && order.getPayStatus() == Constants.PAY_STATUS_PAID,
                "订单已支付，请勿重复支付");
        BizException.check(order.getOrderStatus() != Constants.ORDER_UNPAID, "当前订单状态不可支付");

        int payType = switch (channel == null ? "" : channel) {
            case "wechat" -> Constants.PAY_TYPE_WECHAT;
            case "alipay" -> Constants.PAY_TYPE_ALIPAY;
            case "unionpay" -> Constants.PAY_TYPE_UNIONPAY;
            case "balance" -> Constants.PAY_TYPE_BALANCE;
            default -> Constants.PAY_TYPE_WECHAT;
        };

        BigDecimal balanceUsed = BigDecimal.ZERO;
        if (payType == Constants.PAY_TYPE_BALANCE) {
            SysUser user = userService.getById(userId);
            BigDecimal balance = user == null || user.getBalance() == null ? BigDecimal.ZERO : user.getBalance();
            BizException.check(balance.compareTo(order.getPayAmount()) < 0, "账户余额不足，请更换支付方式");
            balanceUsed = order.getPayAmount();
            userService.addBalance(userId, balanceUsed.negate(), 2, "支付订单 " + order.getOrderNo());
        }

        orderService.markPaid(orderId, payType, balanceUsed);

        messageService.send(userId, "支付成功",
                "您的订单 " + order.getOrderNo() + " 已支付成功，我们将尽快为您发货。",
                "order", "/order/" + orderId);

        return paymentResult(orderService.getById(orderId));
    }

    /** 支付结果 */
    public Map<String, Object> paymentResult(ShopOrder order) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("orderId", order.getId());
        m.put("orderNo", order.getOrderNo());
        m.put("payAmount", order.getPayAmount());
        m.put("payType", order.getPayType());
        m.put("payTypeText", payTypeText(order.getPayType()));
        m.put("payStatus", order.getPayStatus());
        m.put("orderStatus", order.getOrderStatus());
        m.put("payTime", order.getPayTime());
        return m;
    }

    public Map<String, Object> resultByOrderNo(String orderNo, Long userId) {
        ShopOrder order = shopOrderMapper.selectOne(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<ShopOrder>lambdaQuery()
                        .eq(ShopOrder::getOrderNo, orderNo));
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权查看他人订单");
        return paymentResult(order);
    }

    public static String payTypeText(Integer payType) {
        if (payType == null) {
            return "未支付";
        }
        return switch (payType) {
            case Constants.PAY_TYPE_WECHAT -> "微信支付";
            case Constants.PAY_TYPE_ALIPAY -> "支付宝";
            case Constants.PAY_TYPE_UNIONPAY -> "银联支付";
            case Constants.PAY_TYPE_BALANCE -> "余额支付";
            default -> "其它方式";
        };
    }

    private ShopOrder requireOrder(Long orderId, Long userId) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权操作他人订单");
        return order;
    }
}
