package com.abcmall.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** 签到状态 */
@Data
public class SignStatusVO implements Serializable {
    /** 今日是否已签到 */
    private boolean signedToday;
    /** 连续签到天数 */
    private int continuousDays;
    /** 今日可得积分 */
    private int todayPoints;
    /** 当前积分 */
    private int totalPoints;
    /** 本月已签到日期 */
    private List<LocalDate> signedDates = new ArrayList<>();
    /** 本月签到天数 */
    private int monthSignedCount;
    /** 连续签到奖励门槛 */
    private int continuousThreshold;
    /** 连续签到奖励积分 */
    private int continuousPoints;
}
