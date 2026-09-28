package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.entity.OrderLogistics;
import com.abcmall.entity.LogisticsTrace;
import com.abcmall.entity.ShopOrder;
import com.abcmall.mapper.LogisticsTraceMapper;
import com.abcmall.mapper.OrderLogisticsMapper;
import com.abcmall.mapper.ShopOrderMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 物流服务：物流单 + 轨迹 */
@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final OrderLogisticsMapper logisticsMapper;
    private final LogisticsTraceMapper traceMapper;
    private final ShopOrderMapper shopOrderMapper;

    public OrderLogistics getByOrderId(Long orderId) {
        return logisticsMapper.selectOne(Wrappers.<OrderLogistics>lambdaQuery()
                .eq(OrderLogistics::getOrderId, orderId)
                .orderByDesc(OrderLogistics::getId)
                .last("limit 1"));
    }

    /** 轨迹按时间倒序（最新在前） */
    public List<LogisticsTrace> traces(Long orderId) {
        return traceMapper.selectList(Wrappers.<LogisticsTrace>lambdaQuery()
                .eq(LogisticsTrace::getOrderId, orderId)
                .orderByDesc(LogisticsTrace::getTraceTime)
                .orderByDesc(LogisticsTrace::getId));
    }

    /** 物流详情聚合：物流单 + 轨迹 + 订单号 */
    public Map<String, Object> detail(Long userId, Long orderId) {
        ShopOrder order = shopOrderMapper.selectById(orderId);
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权查看他人订单");

        OrderLogistics logistics = getByOrderId(orderId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("orderId", orderId);
        result.put("orderNo", order.getOrderNo());
        result.put("orderStatus", order.getOrderStatus());
        result.put("receiver", order.getReceiver());
        result.put("receiverPhone", order.getReceiverPhone());
        result.put("receiverAddr", order.getReceiverAddr());
        result.put("logistics", logistics);
        result.put("traces", traces(orderId));
        result.put("statusText", statusText(logistics == null ? null : logistics.getStatus()));
        return result;
    }

    public static String statusText(Integer status) {
        if (status == null) {
            return "暂无物流信息";
        }
        return switch (status) {
            case 1 -> "已揽收";
            case 2 -> "运输中";
            case 3 -> "派送中";
            case 4 -> "已签收";
            default -> "运输中";
        };
    }

    /** 支持的快递公司 */
    public List<Map<String, String>> companyList() {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(company("SF", "顺丰速运"));
        list.add(company("YTO", "圆通速递"));
        list.add(company("ZTO", "中通快递"));
        list.add(company("STO", "申通快递"));
        list.add(company("YD", "韵达速递"));
        list.add(company("JD", "京东物流"));
        list.add(company("EMS", "中国邮政EMS"));
        list.add(company("JTSD", "极兔速递"));
        return list;
    }

    private Map<String, String> company(String code, String name) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("code", code);
        m.put("name", name);
        return m;
    }

    /** 模拟推进物流（下单支付后由定时任务或手动触发） */
    public void appendTrace(Long orderId, String context, int status) {
        LogisticsTrace trace = new LogisticsTrace();
        trace.setOrderId(orderId);
        trace.setTraceTime(java.time.LocalDateTime.now());
        trace.setContext(context);
        trace.setStatus(status);
        traceMapper.insert(trace);
    }
}
