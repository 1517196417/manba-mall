package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/** 售后进度 */
@Data
@TableName("after_sale_progress")
public class AfterSaleProgress implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long afterSaleId;
    private String title;
    private String content;
    /** 操作人 */
    private String operator;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
