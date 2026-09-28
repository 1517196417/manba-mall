package com.abcmall.vo;

import com.abcmall.entity.OrderItem;
import com.abcmall.entity.OrderLogistics;
import com.abcmall.entity.ShopOrder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 订单聚合（订单 + 明细 + 物流） */
@Data
public class OrderVO implements Serializable {

    private ShopOrder order;
    private List<OrderItem> items = new ArrayList<>();
    private OrderLogistics logistics;
    /** 商品总件数 */
    private Integer totalQuantity = 0;
}
