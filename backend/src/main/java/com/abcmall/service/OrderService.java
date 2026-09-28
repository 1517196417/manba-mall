package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.Constants;
import com.abcmall.common.PageResult;
import com.abcmall.config.AbcMallProperties;
import com.abcmall.dto.OrderSubmitDTO;
import com.abcmall.entity.*;
import com.abcmall.mapper.OrderItemMapper;
import com.abcmall.mapper.ProductMapper;
import com.abcmall.mapper.ProductSkuMapper;
import com.abcmall.mapper.ShopOrderMapper;
import com.abcmall.vo.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 订单服务：结算预览 / 提交 / 查询 / 取消 / 收货 */
@Service
@RequiredArgsConstructor
public class OrderService extends ServiceImpl<ShopOrderMapper, ShopOrder> {

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Autowired
    private ShopOrderMapper shopOrderMapper;

    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper productSkuMapper;
    private final CartService cartService;
    private final AddressService addressService;
    private final CouponService couponService;
    private final UserService userService;
    private final MessageService messageService;
    private final LogisticsService logisticsService;
    private final AbcMallProperties props;

    /* ==================== 结算预览 ==================== */

    public OrderPreviewVO preview(Long userId, List<Long> cartItemIds, Long userCouponId, Integer deliveryType) {
        List<CartItem> cartItems = loadCartItems(userId, cartItemIds);
        BizException.check(cartItems.isEmpty(), "请先选择要结算的商品");

        List<CartItemVO> itemVOs = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            CartItemVO vo = cartService.toVO(ci);
            itemVOs.add(vo);
            totalAmount = totalAmount.add(vo.getTotalPrice());
        }

        OrderPreviewVO vo = new OrderPreviewVO();
        vo.setItems(itemVOs);
        vo.setTotalAmount(totalAmount);
        vo.setFreight(calcFreight(totalAmount, deliveryType));
        vo.setFreeFreightThreshold(props.getFreeFreightThreshold());
        vo.setAddressList(addressService.listByUser(userId));
        vo.setAddress(addressService.getDefault(userId));
        vo.setUserPoints(currentPoints(userId));
        SysUser user = userService.getById(userId);
        vo.setUserBalance(user == null || user.getBalance() == null ? BigDecimal.ZERO : user.getBalance());
        vo.setDeliveryType(deliveryType == null ? Constants.DELIVERY_EXPRESS : deliveryType);

        BigDecimal discount = BigDecimal.ZERO;
        if (userCouponId != null) {
            UserCoupon uc = couponService.requireUsable(userId, userCouponId, totalAmount);
            discount = couponService.calcDeduct(uc, totalAmount);
            couponService.fillCouponInfo(List.of(uc));
            vo.setSelectedCoupon(uc);
        }
        vo.setDiscountAmount(discount);

        BigDecimal pay = totalAmount.add(vo.getFreight()).subtract(discount);
        vo.setPayAmount(pay.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : pay);

        List<UserCoupon> coupons = couponService.usable(userId, totalAmount, userCouponId);
        vo.setCoupons(coupons);
        return vo;
    }

    private int currentPoints(Long userId) {
        SysUser user = userService.getById(userId);
        return user == null || user.getPoints() == null ? 0 : user.getPoints();
    }

    /** 运费：满门槛包邮，否则收默认运费；自提免运费 */
    private BigDecimal calcFreight(BigDecimal totalAmount, Integer deliveryType) {
        if (deliveryType != null && deliveryType == Constants.DELIVERY_SELF_PICK) {
            return BigDecimal.ZERO;
        }
        BigDecimal threshold = props.getFreeFreightThreshold() == null
                ? new BigDecimal("99") : props.getFreeFreightThreshold();
        if (totalAmount.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return props.getDefaultFreight() == null ? new BigDecimal("12") : props.getDefaultFreight();
    }

    private List<CartItem> loadCartItems(Long userId, List<Long> cartItemIds) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return new ArrayList<>();
        }
        return cartService.list(Wrappers.<CartItem>lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .in(CartItem::getId, cartItemIds));
    }

    /* ==================== 提交订单 ==================== */

    @Transactional(rollbackFor = Exception.class)
    public OrderVO submit(Long userId, OrderSubmitDTO dto) {
        if (dto.getDeliveryType() != null && dto.getDeliveryType() == Constants.DELIVERY_SELF_PICK) {
            throw new BizException("此为门店自提功能，敬请期待");
        }
        if (dto.getDeliveryType() != null && dto.getDeliveryType() == Constants.DELIVERY_CITY) {
            throw new BizException("此为同城配送功能，敬请期待");
        }

        UserAddress address = addressService.getById(dto.getAddressId());
        BizException.notNull(address, "收货地址不存在");
        BizException.check(!address.getUserId().equals(userId), "请选择本人收货地址");

        List<CartItem> cartItems = loadCartItems(userId, dto.getCartItemIds());
        BizException.check(cartItems.isEmpty(), "请选择要购买的商品");

        // 校验商品与库存，计算金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cartItems) {
            CartItemVO vo = cartService.toVO(ci);
            BizException.check(!Boolean.TRUE.equals(vo.getValid()),
                    "商品「" + vo.getProductName() + "」" + (vo.getInvalidReason() == null ? "已失效" : vo.getInvalidReason()));
            totalAmount = totalAmount.add(vo.getTotalPrice());

            OrderItem oi = new OrderItem();
            oi.setProductId(vo.getProductId());
            oi.setSkuId(vo.getSkuId());
            oi.setProductName(vo.getProductName());
            oi.setSkuDesc(vo.getSkuDesc());
            oi.setImage(vo.getImage());
            oi.setPrice(vo.getPrice());
            oi.setQuantity(vo.getQuantity());
            oi.setTotalPrice(vo.getTotalPrice());
            oi.setReviewStatus(0);
            oi.setRefundStatus(0);
            orderItems.add(oi);
        }

        // 优惠券
        BigDecimal discount = BigDecimal.ZERO;
        UserCoupon userCoupon = null;
        if (dto.getUserCouponId() != null) {
            userCoupon = couponService.requireUsable(userId, dto.getUserCouponId(), totalAmount);
            discount = couponService.calcDeduct(userCoupon, totalAmount);
        }

        // 积分抵扣（需求标注为功能说明页，此处按 100 积分 = 1 元做真实计算，字段保留）
        BigDecimal pointsDeduct = BigDecimal.ZERO;
        int pointsUsed = dto.getPointsUsed() == null ? 0 : Math.max(dto.getPointsUsed(), 0);
        if (pointsUsed > 0) {
            pointsDeduct = BigDecimal.valueOf(pointsUsed)
                    .divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
        }

        BigDecimal freight = calcFreight(totalAmount, dto.getDeliveryType());
        BigDecimal payAmount = totalAmount.add(freight).subtract(discount).subtract(pointsDeduct);
        if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
            payAmount = BigDecimal.ZERO;
        }

        // 创建订单
        ShopOrder order = new ShopOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setFreight(freight);
        order.setDiscountAmount(discount.add(pointsDeduct));
        order.setPayAmount(payAmount);
        order.setUserCouponId(dto.getUserCouponId());
        order.setPointsUsed(pointsUsed);
        order.setPointsDeduct(pointsDeduct);
        order.setBalanceUsed(BigDecimal.ZERO);
        order.setPayStatus(Constants.PAY_STATUS_UNPAID);
        order.setOrderStatus(Constants.ORDER_UNPAID);
        order.setDeliveryType(dto.getDeliveryType() == null ? Constants.DELIVERY_EXPRESS : dto.getDeliveryType());
        order.setAddressId(address.getId());
        order.setReceiver(address.getReceiver());
        order.setReceiverPhone(address.getPhone());
        order.setReceiverAddr(fullAddress(address));
        order.setRemark(dto.getRemark());
        order.setInvoiceTitle(dto.getInvoiceTitle());
        order.setInvoiceTaxNo(dto.getInvoiceTaxNo());
        order.setCreateTime(LocalDateTime.now());
        save(order);

        for (OrderItem oi : orderItems) {
            oi.setOrderId(order.getId());
            oi.setOrderNo(order.getOrderNo());
            orderItemMapper.insert(oi);

            // 扣减库存
            if (oi.getSkuId() != null) {
                productSkuMapper.update(null, Wrappers.<ProductSku>lambdaUpdate()
                        .eq(ProductSku::getId, oi.getSkuId())
                        .setSql("stock = GREATEST(stock - " + oi.getQuantity() + ", 0)")
                        .setSql("sales = sales + " + oi.getQuantity()));
            }
            productMapper.update(null, Wrappers.<Product>lambdaUpdate()
                    .eq(Product::getId, oi.getProductId())
                    .setSql("stock = GREATEST(stock - " + oi.getQuantity() + ", 0)")
                    .setSql("sales = sales + " + oi.getQuantity()));
        }

        // 标记优惠券已使用 & 扣积分
        if (userCoupon != null) {
            couponService.markUsed(userCoupon, order.getId());
        }
        if (pointsUsed > 0) {
            userService.addPoints(userId, -pointsUsed, "order", "订单 " + order.getOrderNo() + " 积分抵扣");
        }

        // 清空已下单的购物车条目
        List<Long> ids = new ArrayList<>();
        for (CartItem ci : cartItems) {
            ids.add(ci.getId());
        }
        cartService.removeByIdsForUser(userId, ids);

        // 站内通知
        messageService.send(userId, "订单提交成功",
                "您的订单 " + order.getOrderNo() + " 已提交，请在 " + props.getOrderAutoCancelMinutes()
                        + " 分钟内完成支付。",
                "order", "/order/" + order.getId());

        return detail(userId, order.getId());
    }

    public static String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FMT)
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private String fullAddress(UserAddress a) {
        StringBuilder sb = new StringBuilder();
        if (a.getProvince() != null) sb.append(a.getProvince()).append(' ');
        if (a.getCity() != null) sb.append(a.getCity()).append(' ');
        if (a.getDistrict() != null) sb.append(a.getDistrict()).append(' ');
        if (a.getDetail() != null) sb.append(a.getDetail());
        return sb.toString().trim();
    }

    /* ==================== 查询 ==================== */

    public PageResult<OrderVO> pageOrders(Long userId, List<Integer> status, String keyword,
                                          Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 5 : Math.min(pageSize, 50);

        // 分页查询订单信息
        Page<ShopOrder> page = new Page<>(current, size);
        if(keyword!=null && !keyword.isEmpty()) keyword = "%"+keyword+"%";
        shopOrderMapper.pageOrders(page, userId, status, keyword);

        // 查询订单的明细，以及物流信息，组装成 OrderVO （重点是 wrap 方法）
        List<OrderVO> list = new ArrayList<>();
        for (ShopOrder order : page.getRecords()) {
            list.add(wrap(order));
        }
        return new PageResult<>(list, page.getTotal(), current, size);
    }

    public OrderVO detail(Long userId, Long orderId) {
        ShopOrder order = getById(orderId);
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权查看他人订单");
        return wrap(order);
    }

    public OrderVO detailByNo(Long userId, String orderNo) {
        ShopOrder order = getOne(Wrappers.<ShopOrder>lambdaQuery().eq(ShopOrder::getOrderNo, orderNo), false);
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权查看他人订单");
        return wrap(order);
    }

    private OrderVO wrap(ShopOrder order) {
        OrderVO vo = new OrderVO();
        vo.setOrder(order);
        List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId())
                .orderByAsc(OrderItem::getId));
        vo.setItems(items);
        int qty = 0;
        for (OrderItem it : items) {
            qty += it.getQuantity() == null ? 0 : it.getQuantity();
        }
        vo.setTotalQuantity(qty);
        vo.setLogistics(logisticsService.getByOrderId(order.getId()));
        return vo;
    }

    public List<OrderItem> itemsOf(Long orderId) {
        return orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getId));
    }

    /* ==================== 状态变更 ==================== */

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long orderId, String reason) {
        ShopOrder order = getById(orderId);
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权操作他人订单");
        BizException.check(order.getOrderStatus() != Constants.ORDER_UNPAID, "当前订单状态不可取消");

        order.setOrderStatus(Constants.ORDER_CANCELED);
        order.setCancelReason(StringUtils.hasText(reason) ? reason : "用户主动取消");
        order.setCancelTime(LocalDateTime.now());
        updateById(order);

        // 回滚库存
        for (OrderItem oi : itemsOf(orderId)) {
            if (oi.getSkuId() != null) {
                productSkuMapper.update(null, Wrappers.<ProductSku>lambdaUpdate()
                        .eq(ProductSku::getId, oi.getSkuId())
                        .setSql("stock = stock + " + oi.getQuantity())
                        .setSql("sales = GREATEST(sales - " + oi.getQuantity() + ", 0)"));
            }
            productMapper.update(null, Wrappers.<Product>lambdaUpdate()
                    .eq(Product::getId, oi.getProductId())
                    .setSql("stock = stock + " + oi.getQuantity())
                    .setSql("sales = GREATEST(sales - " + oi.getQuantity() + ", 0)"));
        }

        // 归还优惠券与积分
        couponService.releaseByOrder(userId, orderId);
        if (order.getPointsUsed() != null && order.getPointsUsed() > 0) {
            userService.addPoints(userId, order.getPointsUsed(), "order",
                    "订单 " + order.getOrderNo() + " 取消，退回积分");
        }

        messageService.send(userId, "订单已取消",
                "您的订单 " + order.getOrderNo() + " 已取消。", "order", "/order/" + orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void receive(Long userId, Long orderId) {
        ShopOrder order = getById(orderId);
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权操作他人订单");
        BizException.check(order.getOrderStatus() != Constants.ORDER_UNRECEIVED, "当前订单状态不可确认收货");

        order.setOrderStatus(Constants.ORDER_UNCOMMENTED);
        order.setReceiveTime(LocalDateTime.now());
        updateById(order);

        // 消费返积分
        int points = order.getPayAmount() == null ? 0
                : order.getPayAmount().multiply(BigDecimal.valueOf(props.getOrderPointsRate())).intValue();
        if (points > 0) {
            userService.addPoints(userId, points, "order", "订单 " + order.getOrderNo() + " 消费返积分");
        }

        messageService.send(userId, "确认收货成功",
                "您的订单 " + order.getOrderNo() + " 已确认收货，快去评价赚积分吧。",
                "order", "/order/" + orderId);
    }

    /** 支付成功后置为待发货（由 PayService 调用） */
    @Transactional(rollbackFor = Exception.class)
    public void markPaid(Long orderId, Integer payType, BigDecimal balanceUsed) {
        ShopOrder order = getById(orderId);
        BizException.notNull(order, "订单不存在");
        BizException.check(order.getPayStatus() != null && order.getPayStatus() == Constants.PAY_STATUS_PAID,
                "订单已支付，请勿重复支付");

        order.setPayStatus(Constants.PAY_STATUS_PAID);
        order.setOrderStatus(Constants.ORDER_UNSHIPPED);
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        if (balanceUsed != null) {
            order.setBalanceUsed(balanceUsed);
        }
        updateById(order);
    }

    public void deleteOrder(Long userId, Long orderId) {
        ShopOrder order = getById(orderId);
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权操作他人订单");
        BizException.check(order.getOrderStatus() != Constants.ORDER_CANCELED
                        && order.getOrderStatus() != Constants.ORDER_FINISHED,
                "仅已完成或已取消的订单可以删除");
        removeById(orderId);
        orderItemMapper.delete(Wrappers.<OrderItem>lambdaQuery().eq(OrderItem::getOrderId, orderId));
    }

    /** 自动取消超时未支付订单 */
    @Transactional(rollbackFor = Exception.class)
    public int autoCancelExpired() {
        LocalDateTime deadline = LocalDateTime.now().minusMinutes(props.getOrderAutoCancelMinutes());
        List<ShopOrder> list = list(Wrappers.<ShopOrder>lambdaQuery()
                .eq(ShopOrder::getOrderStatus, Constants.ORDER_UNPAID)
                .lt(ShopOrder::getCreateTime, deadline));
        for (ShopOrder order : list) {
            order.setOrderStatus(Constants.ORDER_CANCELED);
            order.setCancelReason("超时未支付，系统自动取消");
            order.setCancelTime(LocalDateTime.now());
            updateById(order);
            couponService.releaseByOrder(order.getUserId(), order.getId());
        }
        return list.size();
    }

    /* ==================== 统计 ==================== */

    public OrderStatisticsVO statistics(Long userId) {
        OrderStatisticsVO vo = new OrderStatisticsVO();
        vo.setUnpaid(countStatus(userId, Constants.ORDER_UNPAID));
        vo.setUnshipped(countStatus(userId, Constants.ORDER_UNSHIPPED));
        vo.setUnreceived(countStatus(userId, Constants.ORDER_UNRECEIVED));
        vo.setUncommented(countStatus(userId, Constants.ORDER_UNCOMMENTED));
        vo.setAfterSale(countStatus(userId, Constants.ORDER_AFTER_SALE));
        vo.setTotal(count(Wrappers.<ShopOrder>lambdaQuery().eq(ShopOrder::getUserId, userId)));

        List<ShopOrder> paid = list(Wrappers.<ShopOrder>lambdaQuery()
                .eq(ShopOrder::getUserId, userId)
                .eq(ShopOrder::getPayStatus, Constants.PAY_STATUS_PAID));
        BigDecimal spent = BigDecimal.ZERO;
        for (ShopOrder o : paid) {
            spent = spent.add(o.getPayAmount() == null ? BigDecimal.ZERO : o.getPayAmount());
        }
        vo.setTotalSpent(spent);
        return vo;
    }

    private long countStatus(Long userId, int status) {
        return count(Wrappers.<ShopOrder>lambdaQuery()
                .eq(ShopOrder::getUserId, userId)
                .eq(ShopOrder::getOrderStatus, status));
    }

    /** 用户购买过的商品ID（用于推荐） */
    public List<Long> purchasedProductIds(Long userId) {
        List<ShopOrder> orders = list(Wrappers.<ShopOrder>lambdaQuery()
                .eq(ShopOrder::getUserId, userId)
                .orderByDesc(ShopOrder::getId)
                .last("limit 20"));
        List<Long> ids = new ArrayList<>();
        for (ShopOrder o : orders) {
            for (OrderItem it : itemsOf(o.getId())) {
                ids.add(it.getProductId());
            }
        }
        return ids;
    }
}
