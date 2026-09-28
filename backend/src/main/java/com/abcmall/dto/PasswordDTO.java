package com.abcmall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 修改密码参数 */
@Data
public class PasswordDTO {

    @NotBlank(message = "请输入原密码")
    private String oldPassword;

    @NotBlank(message = "请输入新密码")
    private String newPassword;
}
