package com.abcmall.vo;

import com.abcmall.entity.UserAddress;
import com.abcmall.entity.UserCoupon;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 结算页预览数据 */
@Data
public class OrderPreviewVO implements Serializable {

    private UserAddress address;
    private List<UserAddress> addressList = new ArrayList<>();
    private List<CartItemVO> items = new ArrayList<>();

    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal freight = BigDecimal.ZERO;
    private BigDecimal discountAmount = BigDecimal.ZERO;
    private BigDecimal payAmount = BigDecimal.ZERO;
    private BigDecimal freeFreightThreshold = BigDecimal.ZERO;

    /** 当前订单可用的优惠券 */
    private List<UserCoupon> coupons = new ArrayList<>();
    /** 已选优惠券 */
    private UserCoupon selectedCoupon;

    private Integer userPoints = 0;
    private BigDecimal userBalance = BigDecimal.ZERO;

    /** 默认配送方式 */
    private Integer deliveryType = 1;
}
