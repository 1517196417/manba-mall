package com.abcmall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** abcmall.* 业务参数 */
@Data
@Component
@ConfigurationProperties(prefix = "abcmall")
public class AbcMallProperties {

    /** 会话中保存用户ID的属性名 */
    private String sessionUserKey = "ABCMALL_USER_ID";

    /** 包邮门槛 */
    private BigDecimal freeFreightThreshold = new BigDecimal("99");

    /** 默认运费 */
    private BigDecimal defaultFreight = new BigDecimal("12");

    /** 订单未支付自动取消时间（分钟） */
    private int orderAutoCancelMinutes = 30;

    /** 消费返积分比例 */
    private int orderPointsRate = 1;

    /** 签到基础积分 */
    private int signPoints = 100;

    /** 连续签到奖励门槛天数 */
    private int signContinuousDays = 7;

    /** 连续签到奖励积分 */
    private int signContinuousPoints = 800;
}
