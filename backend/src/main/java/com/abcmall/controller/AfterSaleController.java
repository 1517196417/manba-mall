package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.dto.AfterSaleDTO;
import com.abcmall.entity.AfterSale;
import com.abcmall.service.AfterSaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 售后：退货 / 退款 / 换货 / 售后进度 */
@RestController
@RequestMapping("/api/aftersale")
@RequiredArgsConstructor
public class AfterSaleController {

    private final AfterSaleService afterSaleService;

    /** 申请售后 */
    @PostMapping("/apply")
    public Result<AfterSale> apply(@Valid @RequestBody AfterSaleDTO dto) {
        return Result.success("售后申请已提交", afterSaleService.apply(UserContext.requireUserId(), dto));
    }

    /** 我的售后单 */
    @GetMapping("/page")
    public Result<PageResult<AfterSale>> page(@RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(afterSaleService.page(UserContext.requireUserId(), status, page, size));
    }

    /** 售后详情（含进度） */
    @GetMapping("/{id}")
    public Result<AfterSale> detail(@PathVariable Long id) {
        return Result.success(afterSaleService.detail(UserContext.requireUserId(), id));
    }

    /** 某订单的售后单 */
    @GetMapping("/order/{orderId}")
    public Result<List<AfterSale>> byOrder(@PathVariable Long orderId) {
        return Result.success(afterSaleService.listByOrder(UserContext.requireUserId(), orderId));
    }

    /** 撤销售后申请 */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        afterSaleService.cancel(UserContext.requireUserId(), id);
        return Result.success("已撤销售后申请", null);
    }

    /** 售后原因选项 */
    @GetMapping("/reasons")
    public Result<List<String>> reasons() {
        return Result.success(afterSaleService.reasonOptions());
    }
}
