package com.abcmall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 购物车条目修改参数 */
@Data
public class CartUpdateDTO {

    @NotNull(message = "缺少购物车条目ID")
    private Long id;

    @Min(value = 1, message = "数量至少为 1")
    private Integer quantity;

    /** 是否勾选 */
    private Integer selected;
}
