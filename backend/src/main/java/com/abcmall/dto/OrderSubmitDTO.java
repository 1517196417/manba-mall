package com.abcmall.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/** 提交订单参数 */
@Data
public class OrderSubmitDTO {

    @NotNull(message = "请选择收货地址")
    private Long addressId;

    /** 参与结算的购物车条目ID；「立即购买」时可为空 */
    @NotEmpty(message = "请选择要购买的商品")
    private List<Long> cartItemIds;

    /** 1快递 2自提 3同城配送 */
    private Integer deliveryType = 1;

    /** 使用的用户优惠券ID */
    private Long userCouponId;

    /** 使用的积分 */
    private Integer pointsUsed = 0;

    /** 使用的余额 */
    private java.math.BigDecimal balanceUsed;

    /** 订单备注（需求标注为功能说明页，此处字段保留） */
    private String remark;

    /** 发票信息（需求标注为功能说明页，此处字段保留） */
    private String invoiceTitle;
    private String invoiceTaxNo;

    /** 是否来自立即购买 */
    private Boolean buyNow = false;
    private Long buyNowSkuId;
    private Integer buyNowQuantity = 1;
}
