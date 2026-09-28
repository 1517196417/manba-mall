package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.Constants;
import com.abcmall.common.PageResult;
import com.abcmall.dto.AfterSaleDTO;
import com.abcmall.entity.AfterSale;
import com.abcmall.entity.AfterSaleProgress;
import com.abcmall.entity.OrderItem;
import com.abcmall.entity.ShopOrder;
import com.abcmall.mapper.AfterSaleMapper;
import com.abcmall.mapper.AfterSaleProgressMapper;
import com.abcmall.mapper.OrderItemMapper;
import com.abcmall.mapper.ShopOrderMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 售后服务：退货 / 退款 / 换货 / 维修 */
@Service
@RequiredArgsConstructor
public class AfterSaleService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final AfterSaleMapper afterSaleMapper;
    private final AfterSaleProgressMapper progressMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShopOrderMapper shopOrderMapper;
    private final MessageService messageService;

    /** 申请售后 */
    @Transactional(rollbackFor = Exception.class)
    public AfterSale apply(Long userId, AfterSaleDTO dto) {
        OrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        BizException.notNull(item, "订单明细不存在");
        ShopOrder order = shopOrderMapper.selectById(item.getOrderId());
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权对该订单申请售后");
        BizException.check(order.getOrderStatus() == Constants.ORDER_CANCELED, "已取消的订单不能申请售后");

        AfterSale exist = afterSaleMapper.selectOne(Wrappers.<AfterSale>lambdaQuery()
                .eq(AfterSale::getOrderItemId, dto.getOrderItemId())
                .notIn(AfterSale::getStatus, Constants.AS_STATUS_REJECTED, Constants.AS_STATUS_CANCELED)
                .last("limit 1"));
        BizException.check(exist != null, "该商品已存在进行中的售后单");

        AfterSale as = new AfterSale();
        as.setServiceNo("AS" + LocalDateTime.now().format(NO_FMT)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000)));
        as.setOrderId(order.getId());
        as.setOrderNo(order.getOrderNo());
        as.setOrderItemId(item.getId());
        as.setUserId(userId);
        as.setProductName(item.getProductName());
        as.setImage(item.getImage());
        as.setType(dto.getType());
        as.setReason(dto.getReason());
        as.setDescription(dto.getDescription());
        as.setImages(dto.getImages());
        as.setRefundAmount(dto.getRefundAmount() == null ? item.getTotalPrice() : dto.getRefundAmount());
        as.setStatus(Constants.AS_STATUS_PENDING);
        as.setCreateTime(LocalDateTime.now());
        afterSaleMapper.insert(as);

        addProgress(as.getId(), "提交申请",
                "用户提交" + typeText(dto.getType()) + "申请，原因：" + (dto.getReason() == null ? "未填写" : dto.getReason()),
                "用户");

        item.setRefundStatus(1);
        orderItemMapper.updateById(item);

        // 订单标记为售后中
        order.setOrderStatus(Constants.ORDER_AFTER_SALE);
        shopOrderMapper.updateById(order);

        messageService.send(userId, "售后申请已提交",
                "您的售后单 " + as.getServiceNo() + " 已提交，平台将在 24 小时内审核。",
                "order", "/user/aftersale");

        return detail(userId, as.getId());
    }

    private void addProgress(Long afterSaleId, String title, String content, String operator) {
        AfterSaleProgress p = new AfterSaleProgress();
        p.setAfterSaleId(afterSaleId);
        p.setTitle(title);
        p.setContent(content);
        p.setOperator(operator);
        p.setCreateTime(LocalDateTime.now());
        progressMapper.insert(p);
    }

    public PageResult<AfterSale> page(Long userId, Integer status, Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 5 : Math.min(pageSize, 50);
        Page<AfterSale> page = afterSaleMapper.selectPage(new Page<>(current, size),
                Wrappers.<AfterSale>lambdaQuery()
                        .eq(AfterSale::getUserId, userId)
                        .eq(status != null, AfterSale::getStatus, status)
                        .orderByDesc(AfterSale::getId));
        for (AfterSale as : page.getRecords()) {
            as.setTypeText(typeText(as.getType()));
            as.setStatusText(statusText(as.getStatus()));
        }
        return PageResult.of(page);
    }

    public AfterSale detail(Long userId, Long id) {
        AfterSale as = afterSaleMapper.selectById(id);
        BizException.notNull(as, "售后单不存在");
        BizException.check(!as.getUserId().equals(userId), "无权查看他人售后单");
        as.setTypeText(typeText(as.getType()));
        as.setStatusText(statusText(as.getStatus()));
        as.setProgressList(progressMapper.selectList(Wrappers.<AfterSaleProgress>lambdaQuery()
                .eq(AfterSaleProgress::getAfterSaleId, id)
                .orderByAsc(AfterSaleProgress::getId)));
        return as;
    }

    /** 按订单查询售后单 */
    public List<AfterSale> listByOrder(Long userId, Long orderId) {
        List<AfterSale> list = afterSaleMapper.selectList(Wrappers.<AfterSale>lambdaQuery()
                .eq(AfterSale::getUserId, userId)
                .eq(AfterSale::getOrderId, orderId)
                .orderByDesc(AfterSale::getId));
        for (AfterSale as : list) {
            as.setTypeText(typeText(as.getType()));
            as.setStatusText(statusText(as.getStatus()));
        }
        return list;
    }

    /** 撤销申请 */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long userId, Long id) {
        AfterSale as = afterSaleMapper.selectById(id);
        BizException.notNull(as, "售后单不存在");
        BizException.check(!as.getUserId().equals(userId), "无权操作他人售后单");
        BizException.check(as.getStatus() != Constants.AS_STATUS_PENDING
                        && as.getStatus() != Constants.AS_STATUS_AGREED,
                "当前状态不可撤销");

        as.setStatus(Constants.AS_STATUS_CANCELED);
        as.setFinishTime(LocalDateTime.now());
        afterSaleMapper.updateById(as);

        addProgress(id, "撤销申请", "用户主动撤销了售后申请", "用户");

        if (as.getOrderItemId() != null) {
            OrderItem item = orderItemMapper.selectById(as.getOrderItemId());
            if (item != null) {
                item.setRefundStatus(0);
                orderItemMapper.updateById(item);
            }
        }
        // 订单恢复为待评价
        ShopOrder order = shopOrderMapper.selectById(as.getOrderId());
        if (order != null && order.getOrderStatus() == Constants.ORDER_AFTER_SALE) {
            order.setOrderStatus(Constants.ORDER_UNCOMMENTED);
            shopOrderMapper.updateById(order);
        }
    }

    /** 统计用户售后单数量 */
    public long countByStatus(Long userId, Integer status) {
        return afterSaleMapper.selectCount(Wrappers.<AfterSale>lambdaQuery()
                .eq(AfterSale::getUserId, userId)
                .eq(status != null, AfterSale::getStatus, status));
    }

    public static String typeText(Integer type) {
        if (type == null) return "售后";
        return switch (type) {
            case Constants.AS_TYPE_RETURN -> "退货";
            case Constants.AS_TYPE_REFUND -> "仅退款";
            case Constants.AS_TYPE_EXCHANGE -> "换货";
            case Constants.AS_TYPE_REPAIR -> "维修";
            default -> "售后";
        };
    }

    public static String statusText(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case Constants.AS_STATUS_PENDING -> "待审核";
            case Constants.AS_STATUS_AGREED -> "已同意";
            case Constants.AS_STATUS_REJECTED -> "已拒绝";
            case Constants.AS_STATUS_RETURNING -> "退货中";
            case Constants.AS_STATUS_REFUNDING -> "退款中";
            case Constants.AS_STATUS_FINISHED -> "已完成";
            case Constants.AS_STATUS_CANCELED -> "已撤销";
            default -> "未知";
        };
    }

    /** 可申请售后的原因 */
    public List<String> reasonOptions() {
        List<String> list = new ArrayList<>();
        list.add("收到商品与描述不符");
        list.add("商品破损或有瑕疵");
        list.add("商品少件或错发");
        list.add("商品质量问题");
        list.add("不喜欢 / 不想要了");
        list.add("尺寸 / 规格不合适");
        list.add("七天无理由退货");
        list.add("其它原因");
        return list;
    }

    /** 退款金额（默认整单金额） */
    public BigDecimal defaultRefundAmount(OrderItem item) {
        return item == null || item.getTotalPrice() == null ? BigDecimal.ZERO : item.getTotalPrice();
    }

    /** 是否需要填写描述 */
    public static boolean requireDescription(Integer type) {
        return type != null && type == Constants.AS_TYPE_REPAIR;
    }

    /** 便于前端展示的售后状态集合 */
    public static String statusDesc(Integer status) {
        return StringUtils.hasText(statusText(status)) ? statusText(status) : "处理中";
    }
}
