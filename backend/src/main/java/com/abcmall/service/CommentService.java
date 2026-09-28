package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.Constants;
import com.abcmall.common.PageResult;
import com.abcmall.dto.AppendCommentDTO;
import com.abcmall.dto.CommentDTO;
import com.abcmall.entity.OrderComment;
import com.abcmall.entity.OrderItem;
import com.abcmall.entity.Product;
import com.abcmall.entity.ShopOrder;
import com.abcmall.entity.SysUser;
import com.abcmall.mapper.OrderCommentMapper;
import com.abcmall.mapper.OrderItemMapper;
import com.abcmall.mapper.ProductMapper;
import com.abcmall.mapper.ShopOrderMapper;
import com.abcmall.mapper.SysUserMapper;
import com.abcmall.vo.CommentStatisticsVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 商品评价服务：评价 / 追评 / 评分统计 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final OrderCommentMapper commentMapper;
    private final OrderItemMapper orderItemMapper;
    private final ShopOrderMapper shopOrderMapper;
    private final ProductMapper productMapper;
    private final SysUserMapper userMapper;
    private final UserService userService;
    private final MessageService messageService;

    /** 发表评价，同时校验订单归属与评价状态 */
    @Transactional(rollbackFor = Exception.class)
    public OrderComment add(Long userId, CommentDTO dto) {
        OrderItem item = orderItemMapper.selectById(dto.getOrderItemId());
        BizException.notNull(item, "订单明细不存在");
        ShopOrder order = shopOrderMapper.selectById(item.getOrderId());
        BizException.notNull(order, "订单不存在");
        BizException.check(!order.getUserId().equals(userId), "无权评价他人订单");
        BizException.check(item.getReviewStatus() != null && item.getReviewStatus() == Constants.REVIEW_DONE,
                "该商品已评价过了");
        BizException.check(order.getOrderStatus() != null
                        && order.getOrderStatus() < Constants.ORDER_UNCOMMENTED,
                "确认收货后才能评价");

        OrderComment comment = new OrderComment();
        comment.setOrderId(order.getId());
        comment.setOrderItemId(item.getId());
        comment.setProductId(item.getProductId());
        comment.setUserId(userId);
        comment.setSkuDesc(item.getSkuDesc());
        comment.setRating(dto.getRating());
        comment.setContent(dto.getContent());
        comment.setImages(dto.getImages());
        comment.setIsAnonymous(dto.getIsAnonymous() == null ? 0 : dto.getIsAnonymous());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        item.setReviewStatus(Constants.REVIEW_DONE);
        orderItemMapper.updateById(item);

        // 评价返积分
        userService.addPoints(userId, 50, "order", "商品评价奖励");

        // 该订单所有商品都评价完 -> 订单完成
        List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                .eq(OrderItem::getOrderId, order.getId()));
        boolean allDone = true;
        for (OrderItem it : items) {
            if (it.getReviewStatus() == null || it.getReviewStatus() != Constants.REVIEW_DONE) {
                allDone = false;
                break;
            }
        }
        if (allDone) {
            order.setOrderStatus(Constants.ORDER_FINISHED);
            order.setFinishTime(LocalDateTime.now());
            shopOrderMapper.updateById(order);
        }

        messageService.send(userId, "评价成功",
                "感谢您的评价，已获得 50 积分奖励。", "order", "/product/" + item.getProductId());
        return comment;
    }

    /** 商品评价列表（公开） */
    public PageResult<OrderComment> productComments(Long productId, Integer rating,
                                                    Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 5 : Math.min(pageSize, 50);
        Page<OrderComment> page = commentMapper.selectPage(new Page<>(current, size),
                Wrappers.<OrderComment>lambdaQuery()
                        .eq(OrderComment::getProductId, productId)
                        .eq(rating != null, OrderComment::getRating, rating)
                        .orderByDesc(OrderComment::getId));
        fill(page.getRecords(), false);
        return PageResult.of(page);
    }

    /** 我的评价 */
    public PageResult<OrderComment> myComments(Long userId, Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        Page<OrderComment> page = commentMapper.selectPage(new Page<>(current, size),
                Wrappers.<OrderComment>lambdaQuery()
                        .eq(OrderComment::getUserId, userId)
                        .orderByDesc(OrderComment::getId));
        fill(page.getRecords(), true);
        return PageResult.of(page);
    }

    /** 待评价的订单明细 */
    public List<OrderItem> pendingItems(Long userId) {
        List<ShopOrder> orders = shopOrderMapper.selectList(Wrappers.<ShopOrder>lambdaQuery()
                .eq(ShopOrder::getUserId, userId)
                .in(ShopOrder::getOrderStatus, Constants.ORDER_UNCOMMENTED, Constants.ORDER_FINISHED));
        List<OrderItem> result = new ArrayList<>();
        for (ShopOrder order : orders) {
            List<OrderItem> items = orderItemMapper.selectList(Wrappers.<OrderItem>lambdaQuery()
                    .eq(OrderItem::getOrderId, order.getId())
                    .eq(OrderItem::getReviewStatus, Constants.REVIEW_NOT_YET)
                    .orderByAsc(OrderItem::getId));
            result.addAll(items);
        }
        return result;
    }

    /** 追评 */
    @Transactional(rollbackFor = Exception.class)
    public void append(Long userId, AppendCommentDTO dto) {
        OrderComment comment = commentMapper.selectById(dto.getCommentId());
        BizException.notNull(comment, "评价不存在");
        BizException.check(!comment.getUserId().equals(userId), "无权追评他人评价");
        BizException.check(StringUtils.hasText(comment.getAppendContent()), "该评价已追评过");

        comment.setAppendContent(dto.getContent());
        comment.setAppendTime(LocalDateTime.now());
        commentMapper.updateById(comment);
    }

    public void remove(Long userId, Long id) {
        OrderComment comment = commentMapper.selectById(id);
        BizException.notNull(comment, "评价不存在");
        BizException.check(!comment.getUserId().equals(userId), "无权删除他人评价");
        commentMapper.deleteById(id);
    }

    /** 评分统计 */
    public CommentStatisticsVO statistics(Long productId) {
        List<OrderComment> list = commentMapper.selectList(Wrappers.<OrderComment>lambdaQuery()
                .eq(OrderComment::getProductId, productId));
        CommentStatisticsVO vo = new CommentStatisticsVO();
        for (int i = 5; i >= 1; i--) {
            vo.getStarCount().put(i, 0L);
        }
        long total = list.size();
        long sum = 0;
        for (OrderComment c : list) {
            int rating = c.getRating() == null ? 5 : c.getRating();
            sum += rating;
            vo.getStarCount().merge(rating, 1L, Long::sum);
            if (rating >= 4) {
                vo.setGoodCount(vo.getGoodCount() + 1);
            } else if (rating == 3) {
                vo.setMidCount(vo.getMidCount() + 1);
            } else {
                vo.setBadCount(vo.getBadCount() + 1);
            }
        }
        vo.setTotal(total);
        vo.setAvgRating(total == 0 ? 5.0
                : BigDecimal.valueOf(sum).divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP).doubleValue());
        return vo;
    }

    /** 填充用户与商品信息 */
    private void fill(List<OrderComment> list, boolean withProduct) {
        for (OrderComment c : list) {
            SysUser user = userMapper.selectById(c.getUserId());
            if (user != null) {
                c.setNickname(Integer.valueOf(1).equals(c.getIsAnonymous()) ? maskNickname(user.getNickname())
                        : user.getNickname());
                c.setAvatar(Integer.valueOf(1).equals(c.getIsAnonymous()) ? null : user.getAvatar());
            }
            if (withProduct) {
                Product p = productMapper.selectById(c.getProductId());
                if (p != null) {
                    c.setProductName(p.getName());
                    c.setProductImage(p.getMainImage());
                }
            }
        }
    }

    private String maskNickname(String nickname) {
        if (!StringUtils.hasText(nickname)) {
            return "匿名用户";
        }
        if (nickname.length() <= 1) {
            return nickname + "**";
        }
        return nickname.charAt(0) + "**" + nickname.charAt(nickname.length() - 1);
    }
}
