package com.abcmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 余额流水 */
@Data
@TableName("balance_record")
public class BalanceRecord implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    /** 正为收入，负为支出 */
    private BigDecimal amount;
    /** 1充值 2消费 3退款 */
    private Integer type;
    private String description;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
