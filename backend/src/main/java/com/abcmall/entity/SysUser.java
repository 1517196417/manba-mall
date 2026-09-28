package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 用户 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 密码（MD5），只写不读 */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String nickname;
    private String avatar;
    private String phone;
    private String email;

    /** 0未知 1男 2女 */
    private Integer gender;
    private LocalDate birthday;

    private String realName;
    private String idCard;

    @TableField("is_verified")
    private Integer isVerified;

    /** 会员等级 1-5 */
    private Integer level;
    /** 成长值 */
    private Integer growth;
    /** 积分 */
    private Integer points;
    /** 余额 */
    private BigDecimal balance;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
