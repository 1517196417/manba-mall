package com.abcmall.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/** 会员中心订单统计 */
@Data
public class OrderStatisticsVO implements Serializable {
    private long unpaid;
    private long unshipped;
    private long unreceived;
    private long uncommented;
    private long afterSale;
    private long total;
    private BigDecimal totalSpent = BigDecimal.ZERO;
}
