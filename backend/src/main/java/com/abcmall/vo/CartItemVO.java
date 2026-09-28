package com.abcmall.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 购物车条目（已关联商品与 SKU 信息） */
@Data
public class CartItemVO implements Serializable {

    private Long id;
    private Long userId;
    private Long productId;
    private Long skuId;
    private Integer quantity;
    private Integer selected;

    private String productName;
    private String subtitle;
    private String image;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer stock;
    private String skuDesc;
    private String specJson;
    /** 规格可选项，便于购物车切换规格 */
    private List<SpecVO> specs = new ArrayList<>();

    /** true 有效，false 失效（下架 / 无库存） */
    private Boolean valid = true;
    private String invalidReason;

    /** 小计 */
    public BigDecimal getTotalPrice() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
