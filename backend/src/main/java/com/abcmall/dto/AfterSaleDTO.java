package com.abcmall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/** 申请售后参数 */
@Data
public class AfterSaleDTO {

    @NotNull(message = "缺少订单明细ID")
    private Long orderItemId;

    /** 1退货 2退款 3换货 4维修 */
    @NotNull(message = "请选择售后类型")
    private Integer type;

    private String reason;
    private String description;
    private String images;
    private BigDecimal refundAmount;
}
