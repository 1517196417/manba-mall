package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.dto.OrderSubmitDTO;
import com.abcmall.entity.OrderItem;
import com.abcmall.service.OrderService;
import com.abcmall.vo.OrderPreviewVO;
import com.abcmall.vo.OrderStatisticsVO;
import com.abcmall.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 订单：结算 / 提交 / 查询 / 取消 / 收货 */
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /** 结算页预览（含地址、商品、运费、可用券、积分余额） */
    @PostMapping("/preview")
    public Result<OrderPreviewVO> preview(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.requireUserId();
        List<Long> ids = toLongList(body.get("cartItemIds"));
        Long couponId = body.get("userCouponId") == null ? null
                : Long.valueOf(body.get("userCouponId").toString());
        Integer deliveryType = body.get("deliveryType") == null ? 1
                : Integer.valueOf(body.get("deliveryType").toString());
        return Result.success(orderService.preview(userId, ids, couponId, deliveryType));
    }

    /** 提交订单 */
    @PostMapping("/submit")
    public Result<OrderVO> submit(@Valid @RequestBody OrderSubmitDTO dto) {
        return Result.success("下单成功", orderService.submit(UserContext.requireUserId(), dto));
    }

    /** 订单列表 */
    @GetMapping("/page")
    public Result<PageResult<OrderVO>> page(@RequestParam(required = false) Integer status,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(orderService.pageOrders(UserContext.requireUserId()
            , status == null  ? null : List.of(status)
            , keyword, page, size));
    }

    /** 订单详情 */
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.detail(UserContext.requireUserId(), id));
    }

    /** 按订单号查询 */
    @GetMapping("/no/{orderNo}")
    public Result<OrderVO> detailByNo(@PathVariable String orderNo) {
        return Result.success(orderService.detailByNo(UserContext.requireUserId(), orderNo));
    }

    /** 订单商品明细 */
    @GetMapping("/{id}/items")
    public Result<List<OrderItem>> items(@PathVariable Long id) {
        orderService.detail(UserContext.requireUserId(), id);
        return Result.success(orderService.itemsOf(id));
    }

    /** 取消订单 */
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestParam(required = false) String reason) {
        orderService.cancel(UserContext.requireUserId(), id, reason);
        return Result.success("订单已取消", null);
    }

    /** 确认收货 */
    @PutMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id) {
        orderService.receive(UserContext.requireUserId(), id);
        return Result.success("确认收货成功", null);
    }

    /** 删除订单 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        orderService.deleteOrder(UserContext.requireUserId(), id);
        return Result.success("订单已删除", null);
    }

    /** 再次购买：把订单内商品重新加入购物车 */
    @PostMapping("/{id}/rebuy")
    public Result<Map<String, Object>> rebuy(@PathVariable Long id) {
        Long userId = UserContext.requireUserId();
        OrderVO vo = orderService.detail(userId, id);
        int count = 0;
        for (OrderItem item : vo.getItems()) {
            try {
                com.abcmall.dto.CartAddDTO cartAdd = new com.abcmall.dto.CartAddDTO();
                cartAdd.setProductId(item.getProductId());
                cartAdd.setSkuId(item.getSkuId());
                cartAdd.setQuantity(item.getQuantity());
                com.abcmall.service.CartService cartService =
                        com.abcmall.common.SpringContextHolder.getBean(com.abcmall.service.CartService.class);
                if (cartService != null) {
                    cartService.add(userId, cartAdd);
                    count++;
                }
            } catch (Exception ignored) {
                // 商品已下架时跳过
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("addedCount", count);
        return Result.success("已加入购物车 " + count + " 件商品", map);
    }

    /** 订单状态统计 */
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics() {
        return Result.success(orderService.statistics(UserContext.requireUserId()));
    }

    /** 订单状态字典 */
    @GetMapping("/status-dict")
    public Result<List<Map<String, Object>>> statusDict() {
        List<Map<String, Object>> list = new java.util.ArrayList<>();
        list.add(dict(0, "待付款"));
        list.add(dict(1, "待发货"));
        list.add(dict(2, "待收货"));
        list.add(dict(3, "待评价"));
        list.add(dict(4, "已完成"));
        list.add(dict(5, "已取消"));
        list.add(dict(6, "售后中"));
        return Result.success(list);
    }

    private Map<String, Object> dict(int code, String name) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", code);
        m.put("name", name);
        return m;
    }

    private List<Long> toLongList(Object value) {
        List<Long> ids = new java.util.ArrayList<>();
        if (value instanceof List<?> list) {
            for (Object o : list) {
                if (o == null) {
                    continue;
                }
                try {
                    ids.add(Long.valueOf(o.toString()));
                } catch (NumberFormatException ignored) {
                    // 跳过非法值
                }
            }
        }
        return ids;
    }
}
