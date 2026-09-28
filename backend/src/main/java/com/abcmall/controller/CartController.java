package com.abcmall.controller;

import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.dto.CartAddDTO;
import com.abcmall.dto.CartUpdateDTO;
import com.abcmall.entity.CartItem;
import com.abcmall.service.CartService;
import com.abcmall.service.OrderService;
import com.abcmall.vo.CartItemVO;
import com.abcmall.vo.CartSettleVO;
import com.abcmall.vo.OrderPreviewVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 购物车 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    /** 购物车列表 */
    @GetMapping("/list")
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.listVO(UserContext.requireUserId()));
    }

    /** 购物车角标数量 */
    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.success(cartService.countByUser(UserContext.requireUserId()));
    }

    /** 加入购物车 */
    @PostMapping("/add")
    public Result<CartItem> add(@Valid @RequestBody CartAddDTO dto) {
        CartItem item = cartService.add(UserContext.requireUserId(), dto);
        return Result.success("已加入购物车", item);
    }

    /** 修改数量 / 勾选 */
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartUpdateDTO dto) {
        cartService.updateItem(UserContext.requireUserId(), dto.getId(), dto.getQuantity(), dto.getSelected());
        return Result.success("已更新", null);
    }

    /** 切换规格 */
    @PutMapping("/{id}/sku")
    public Result<Void> changeSku(@PathVariable Long id, @RequestParam Long skuId) {
        cartService.changeSku(UserContext.requireUserId(), id, skuId);
        return Result.success("规格已切换", null);
    }

    /** 全选 / 取消全选 */
    @PutMapping("/select-all")
    public Result<Void> selectAll(@RequestParam(defaultValue = "1") Integer selected) {
        cartService.selectAll(UserContext.requireUserId(), selected);
        return Result.success("已更新", null);
    }

    /** 删除条目 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        cartService.removeItem(UserContext.requireUserId(), id);
        return Result.success("已删除", null);
    }

    /** 清空购物车 */
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clear(UserContext.requireUserId());
        return Result.success("购物车已清空", null);
    }

    /** 清理失效商品（真实实现，前端入口另有「功能说明页」展示） */
    @DeleteMapping("/clean-invalid")
    public Result<Integer> cleanInvalid() {
        int count = cartService.cleanInvalid(UserContext.requireUserId());
        return Result.success("已清理 " + count + " 件失效商品", count);
    }

    /** 优惠试算：返回已勾选商品的金额、运费、优惠与应付 */
    @GetMapping("/settle")
    public Result<CartSettleVO> settle() {
        Long userId = UserContext.requireUserId();
        List<CartItemVO> selected = cartService.listVO(userId).stream()
                .filter(i -> Integer.valueOf(1).equals(i.getSelected()) && Boolean.TRUE.equals(i.getValid()))
                .toList();

        CartSettleVO vo = new CartSettleVO();
        vo.setItems(selected);
        BigDecimal total = BigDecimal.ZERO;
        int qty = 0;
        for (CartItemVO item : selected) {
            total = total.add(item.getTotalPrice());
            qty += item.getQuantity() == null ? 0 : item.getQuantity();
        }
        vo.setTotalAmount(total);
        vo.setTotalQuantity(qty);

        BigDecimal threshold = new BigDecimal("99");
        vo.setFreeFreightThreshold(threshold);
        vo.setFreight(total.compareTo(threshold) >= 0 || total.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO : new BigDecimal("12"));
        vo.setFreeFreightGap(total.compareTo(threshold) >= 0 ? BigDecimal.ZERO : threshold.subtract(total));
        vo.setDiscountAmount(BigDecimal.ZERO);
        vo.setPayAmount(total.add(vo.getFreight()));
        return Result.success(vo);
    }

    /** 去结算：返回勾选商品的结算预览 */
    @GetMapping("/checkout")
    public Result<OrderPreviewVO> checkout(@RequestParam(required = false) Long couponId) {
        Long userId = UserContext.requireUserId();
        List<Long> ids = cartService.selectedItems(userId).stream().map(CartItem::getId).toList();
        return Result.success(orderService.preview(userId, ids, couponId, 1));
    }

    /** 购物车摘要（角标 + 勾选金额），供导航栏使用 */
    @GetMapping("/summary")
    public Result<Map<String, Object>> summary() {
        Long userId = UserContext.requireUserId();
        List<CartItemVO> list = cartService.listVO(userId);
        BigDecimal selectedAmount = BigDecimal.ZERO;
        int selectedCount = 0;
        int invalidCount = 0;
        for (CartItemVO vo : list) {
            if (!Boolean.TRUE.equals(vo.getValid())) {
                invalidCount++;
            } else if (Integer.valueOf(1).equals(vo.getSelected())) {
                selectedCount += vo.getQuantity() == null ? 0 : vo.getQuantity();
                selectedAmount = selectedAmount.add(vo.getTotalPrice());
            }
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("total", list.size());
        map.put("selectedCount", selectedCount);
        map.put("selectedAmount", selectedAmount);
        map.put("invalidCount", invalidCount);
        return Result.success(map);
    }
}
