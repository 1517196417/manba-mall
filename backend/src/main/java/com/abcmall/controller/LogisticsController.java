package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.entity.LogisticsTrace;
import com.abcmall.entity.OrderLogistics;
import com.abcmall.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 物流查询与轨迹展示 */
@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    /** 物流详情：物流单 + 轨迹 + 收货信息 */
    @GetMapping("/{orderId}")
    public Result<Map<String, Object>> detail(@PathVariable Long orderId) {
        return Result.success(logisticsService.detail(UserContext.requireUserId(), orderId));
    }

    /** 物流单 */
    @GetMapping("/{orderId}/info")
    public Result<OrderLogistics> info(@PathVariable Long orderId) {
        UserContext.requireUserId();
        return Result.success(logisticsService.getByOrderId(orderId));
    }

    /** 轨迹列表 */
    @GetMapping("/{orderId}/traces")
    public Result<List<LogisticsTrace>> traces(@PathVariable Long orderId) {
        UserContext.requireUserId();
        return Result.success(logisticsService.traces(orderId));
    }

    /** 快递公司列表 */
    @GetMapping("/company/list")
    public Result<List<Map<String, String>>> companyList() {
        return Result.success(logisticsService.companyList());
    }
}
