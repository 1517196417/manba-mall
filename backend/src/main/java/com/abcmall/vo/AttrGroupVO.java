package com.abcmall.vo;

import com.abcmall.entity.ProductAttr;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** 商品参数分组 */
@Data
public class AttrGroupVO implements Serializable {
    private String groupName;
    private List<ProductAttr> attrs = new ArrayList<>();
}
