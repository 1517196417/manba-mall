package com.abcmall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** 追评参数 */
@Data
public class AppendCommentDTO {

    @NotNull(message = "缺少评价ID")
    private Long commentId;

    @NotBlank(message = "请输入追评内容")
    private String content;
}
