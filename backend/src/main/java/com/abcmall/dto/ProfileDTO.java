package com.abcmall.dto;

import lombok.Data;

import java.time.LocalDate;

/** 个人资料修改参数 */
@Data
public class ProfileDTO {
    private String nickname;
    private String avatar;
    private Integer gender;
    private LocalDate birthday;
    private String email;
    private String phone;
}
