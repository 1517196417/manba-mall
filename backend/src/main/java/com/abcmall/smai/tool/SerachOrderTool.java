package com.abcmall.smai.tool;

import com.abcmall.common.PageResult;
import com.abcmall.service.OrderService;
import com.abcmall.vo.OrderVO;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class SerachOrderTool {

    @Autowired
    OrderService orderService;

    /** 历史订单召回数量 */
    @Value("${abcmall.smai.order-history-limit:5}")
    Integer orderHistoryLimit;

    @Tool(description = "查询用户正在进行中的订单（待付款、待发货、待收货、待评价、售后中）。传入userId和可选的搜索关键词keyword。返回结果包含订单号、状态、金额、商品明细。用户问'我的订单到哪了''什么时候发货'时调用。")
    public List<OrderVO> queryInProgressOrders(Long userId, String keyword){
        if (userId == null || userId == 0) return Collections.emptyList();
        StepTracker.addStep("queryInProgressOrders", "查询正在进行中的订单...");
        return queryOrders(userId, List.of(0, 1, 2, 3, 6), keyword);
    }

    @Tool(description = "按关键词查询用户历史订单（已完成、已取消）。传入userId和keyword（如订单号、商品名）。用户问'我之前买过什么''以前的订单'时调用。")
    public List<OrderVO> queryHistoricalOrders(Long userId, String keyword){
        if (userId == null || userId == 0) return Collections.emptyList();
        StepTracker.addStep("queryHistoricalOrders", "查询已完成的订单...");
        return queryOrders(userId, List.of(4, 5), keyword);
    }

    // 查询订单
    private List<OrderVO> queryOrders(Long userId, List<Integer> status, String keyword){
        PageResult<OrderVO> orderVOPageResult = orderService.pageOrders(userId, status, keyword, 1, orderHistoryLimit);
        return orderVOPageResult.getList();
    }

}
