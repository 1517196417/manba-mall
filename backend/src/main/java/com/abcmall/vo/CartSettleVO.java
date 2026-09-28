package com.abcmall.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 购物车优惠试算结果 */
@Data
public class CartSettleVO implements Serializable {

    private List<CartItemVO> items = new ArrayList<>();
    /** 商品总额 */
    private BigDecimal totalAmount = BigDecimal.ZERO;
    /** 运费 */
    private BigDecimal freight = BigDecimal.ZERO;
    /** 优惠金额 */
    private BigDecimal discountAmount = BigDecimal.ZERO;
    /** 应付金额 */
    private BigDecimal payAmount = BigDecimal.ZERO;
    /** 包邮门槛 */
    private BigDecimal freeFreightThreshold = BigDecimal.ZERO;
    /** 还差多少包邮 */
    private BigDecimal freeFreightGap = BigDecimal.ZERO;
    /** 选中商品总件数 */
    private Integer totalQuantity = 0;
}
