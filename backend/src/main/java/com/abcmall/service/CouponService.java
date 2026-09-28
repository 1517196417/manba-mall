package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.Constants;
import com.abcmall.entity.Coupon;
import com.abcmall.entity.UserCoupon;
import com.abcmall.mapper.CouponMapper;
import com.abcmall.mapper.UserCouponMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 优惠券服务 */
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    /** 折扣券最高抵扣金额 */
    private static final BigDecimal MAX_DISCOUNT_DEDUCT = new BigDecimal("300");

    /* ------------------------- 领券中心 ------------------------- */

    public List<Coupon> available(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> list = couponMapper.selectList(Wrappers.<Coupon>lambdaQuery()
                .eq(Coupon::getStatus, 1)
                .and(w -> w.isNull(Coupon::getEndTime).or().gt(Coupon::getEndTime, now))
                .orderByAsc(Coupon::getMinAmount)
                .orderByAsc(Coupon::getId));

        Map<Long, Integer> receivedMap = countReceived(userId);
        for (Coupon c : list) {
            int got = receivedMap.getOrDefault(c.getId(), 0);
            c.setReceivedCount(got);
            int total = c.getTotal() == null ? 0 : c.getTotal();
            int received = c.getReceived() == null ? 0 : c.getReceived();
            int perLimit = c.getPerLimit() == null ? 1 : c.getPerLimit();
            c.setSoldOut(total > 0 && received >= total || got >= perLimit);
        }
        return list;
    }

    private Map<Long, Integer> countReceived(Long userId) {
        Map<Long, Integer> map = new HashMap<>();
        if (userId == null) {
            return map;
        }
        List<UserCoupon> mine = userCouponMapper.selectList(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getUserId, userId));
        for (UserCoupon uc : mine) {
            map.merge(uc.getCouponId(), 1, Integer::sum);
        }
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    public UserCoupon receive(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        BizException.notNull(coupon, "优惠券不存在");
        BizException.check(coupon.getStatus() != null && coupon.getStatus() == 0, "优惠券已停止发放");
        if (coupon.getEndTime() != null && coupon.getEndTime().isBefore(LocalDateTime.now())) {
            throw new BizException("优惠券已过期");
        }
        int total = coupon.getTotal() == null ? 0 : coupon.getTotal();
        int received = coupon.getReceived() == null ? 0 : coupon.getReceived();
        BizException.check(total > 0 && received >= total, "优惠券已被领完");

        Long got = userCouponMapper.selectCount(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        int perLimit = coupon.getPerLimit() == null ? 1 : coupon.getPerLimit();
        BizException.check(got != null && got >= perLimit, "该优惠券每人限领 " + perLimit + " 张");

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(Constants.USER_COUPON_UNUSED);
        uc.setReceiveTime(LocalDateTime.now());
        uc.setExpireTime(coupon.getEndTime());
        userCouponMapper.insert(uc);

        coupon.setReceived(received + 1);
        couponMapper.updateById(coupon);

        return uc;
    }

    /* ------------------------- 我的优惠券 ------------------------- */

    /**
     * 我的优惠券
     *
     * @param status null 全部 / 0 未使用 / 1 已使用 / 2 已过期
     */
    public List<UserCoupon> myCoupons(Long userId, Integer status) {
        expireOutdated(userId);

        List<UserCoupon> list = userCouponMapper.selectList(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(status != null, UserCoupon::getStatus, status)
                .orderByAsc(UserCoupon::getStatus)
                .orderByDesc(UserCoupon::getId));
        fillCouponInfo(list);
        return list;
    }

    /** 把已过期的未使用券置为过期 */
    @Transactional(rollbackFor = Exception.class)
    public void expireOutdated(Long userId) {
        userCouponMapper.update(null, Wrappers.<UserCoupon>lambdaUpdate()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, Constants.USER_COUPON_UNUSED)
                .isNotNull(UserCoupon::getExpireTime)
                .lt(UserCoupon::getExpireTime, LocalDateTime.now())
                .set(UserCoupon::getStatus, Constants.USER_COUPON_EXPIRED));
    }

    /** 填充券面信息 */
    public void fillCouponInfo(List<UserCoupon> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = new ArrayList<>();
        for (UserCoupon uc : list) {
            ids.add(uc.getCouponId());
        }
        List<Coupon> coupons = couponMapper.selectBatchIds(ids);
        Map<Long, Coupon> map = new HashMap<>();
        for (Coupon c : coupons) {
            map.put(c.getId(), c);
        }
        for (UserCoupon uc : list) {
            Coupon c = map.get(uc.getCouponId());
            if (c == null) {
                continue;
            }
            uc.setName(c.getName());
            uc.setType(c.getType());
            uc.setAmount(c.getAmount());
            uc.setDiscount(c.getDiscount());
            uc.setMinAmount(c.getMinAmount());
            uc.setDescription(c.getDescription());
            uc.setCategoryId(c.getCategoryId());
        }
    }

    /* ------------------------- 结算可用券 ------------------------- */

    /** 某订单金额下可用的优惠券 */
    public List<UserCoupon> usable(Long userId, BigDecimal orderAmount, Long couponId) {
        expireOutdated(userId);
        List<UserCoupon> list = userCouponMapper.selectList(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getStatus, Constants.USER_COUPON_UNUSED)
                .orderByDesc(UserCoupon::getId));
        fillCouponInfo(list);

        List<UserCoupon> result = new ArrayList<>();
        for (UserCoupon uc : list) {
            if (couponId != null && couponId.equals(uc.getId())) {
                uc.setUsable(true);
                uc.setDeductAmount(calcDeduct(uc, orderAmount));
                result.add(uc);
                continue;
            }
            BigDecimal min = uc.getMinAmount() == null ? BigDecimal.ZERO : uc.getMinAmount();
            if (orderAmount.compareTo(min) >= 0) {
                uc.setUsable(true);
                uc.setDeductAmount(calcDeduct(uc, orderAmount));
            } else {
                uc.setUsable(false);
                uc.setUnusableReason("满 " + min.stripTrailingZeros().toPlainString() + " 元可用");
                uc.setDeductAmount(BigDecimal.ZERO);
            }
            result.add(uc);
        }
        return result;
    }

    /** 计算抵扣金额 */
    public static BigDecimal calcDeduct(Coupon coupon, BigDecimal orderAmount) {
        if (coupon == null || orderAmount == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal deduct;
        if (coupon.getType() != null && coupon.getType() == Constants.COUPON_TYPE_DISCOUNT) {
            BigDecimal discount = coupon.getDiscount() == null ? new BigDecimal("10") : coupon.getDiscount();
            deduct = orderAmount.multiply(new BigDecimal("10").subtract(discount))
                    .divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
            if (deduct.compareTo(MAX_DISCOUNT_DEDUCT) > 0) {
                deduct = MAX_DISCOUNT_DEDUCT;
            }
        } else {
            deduct = coupon.getAmount() == null ? BigDecimal.ZERO : coupon.getAmount();
        }
        // 抵扣不超过订单金额
        if (deduct.compareTo(orderAmount) > 0) {
            deduct = orderAmount;
        }
        return deduct.setScale(2, RoundingMode.HALF_UP);
    }

    /** 计算某张用户券在指定金额下的抵扣 */
    public BigDecimal calcDeduct(UserCoupon userCoupon, BigDecimal orderAmount) {
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        return calcDeduct(coupon, orderAmount);
    }

    /** 校验并取回用户券实体 */
    public UserCoupon requireUsable(Long userId, Long userCouponId, BigDecimal orderAmount) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        BizException.notNull(uc, "优惠券不存在");
        BizException.check(!uc.getUserId().equals(userId), "无权使用他人优惠券");
        BizException.check(uc.getStatus() != Constants.USER_COUPON_UNUSED, "优惠券已使用或已过期");
        if (uc.getExpireTime() != null && uc.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BizException("优惠券已过期");
        }
        Coupon coupon = couponMapper.selectById(uc.getCouponId());
        BizException.notNull(coupon, "优惠券不存在");
        BigDecimal min = coupon.getMinAmount() == null ? BigDecimal.ZERO : coupon.getMinAmount();
        BizException.check(orderAmount.compareTo(min) < 0,
                "订单金额未满 " + min.stripTrailingZeros().toPlainString() + " 元，无法使用该券");
        return uc;
    }

    /** 标记券已使用 */
    public void markUsed(UserCoupon uc, Long orderId) {
        uc.setStatus(Constants.USER_COUPON_USED);
        uc.setOrderId(orderId);
        uc.setUseTime(LocalDateTime.now());
        userCouponMapper.updateById(uc);
    }

    /** 归还券（取消订单时） */
    public void releaseByOrder(Long userId, Long orderId) {
        List<UserCoupon> list = userCouponMapper.selectList(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getOrderId, orderId));
        for (UserCoupon uc : list) {
            uc.setStatus(Constants.USER_COUPON_UNUSED);
            uc.setOrderId(null);
            uc.setUseTime(null);
            userCouponMapper.updateById(uc);
        }
    }
}
