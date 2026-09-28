package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 签到记录 */
@Data
@TableName("sign_record")
public class SignRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate signDate;
    /** 连续签到天数 */
    private Integer continuousDays;
    private Integer points;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
