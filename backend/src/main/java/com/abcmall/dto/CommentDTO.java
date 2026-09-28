package com.abcmall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 发表评价参数 */
@Data
public class CommentDTO {

    @NotNull(message = "缺少订单明细ID")
    private Long orderItemId;

    @NotNull(message = "请给出评分")
    @Min(value = 1, message = "评分最低 1 分")
    @Max(value = 5, message = "评分最高 5 分")
    private Integer rating;

    private String content;

    /** 晒图，逗号分隔；需求标注为功能说明页，此处字段保留 */
    private String images;

    private Integer isAnonymous = 0;
}
