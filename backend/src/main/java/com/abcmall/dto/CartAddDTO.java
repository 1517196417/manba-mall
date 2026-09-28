package com.abcmall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 加入购物车参数 */
@Data
public class CartAddDTO {

    @NotNull(message = "请选择商品")
    private Long productId;

    private Long skuId;

    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity = 1;
}
