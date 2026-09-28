package com.abcmall.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/** 商品评价统计 */
@Data
public class CommentStatisticsVO implements Serializable {
    private double avgRating;
    private long total;
    private long goodCount;
    private long midCount;
    private long badCount;
    private Map<Integer, Long> starCount = new LinkedHashMap<>();
}
