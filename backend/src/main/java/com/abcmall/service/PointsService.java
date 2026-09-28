package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.PageResult;
import com.abcmall.config.AbcMallProperties;
import com.abcmall.entity.PointsExchange;
import com.abcmall.entity.PointsGoods;
import com.abcmall.entity.PointsRecord;
import com.abcmall.entity.SignRecord;
import com.abcmall.entity.SysUser;
import com.abcmall.mapper.PointsExchangeMapper;
import com.abcmall.mapper.PointsGoodsMapper;
import com.abcmall.mapper.PointsRecordMapper;
import com.abcmall.mapper.SignRecordMapper;
import com.abcmall.mapper.SysUserMapper;
import com.abcmall.vo.SignStatusVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/** 积分服务：积分记录 / 签到 / 积分商城 */
@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointsRecordMapper pointsRecordMapper;
    private final SignRecordMapper signRecordMapper;
    private final PointsGoodsMapper pointsGoodsMapper;
    private final PointsExchangeMapper pointsExchangeMapper;
    private final SysUserMapper userMapper;
    private final UserService userService;
    private final MessageService messageService;
    private final AbcMallProperties props;

    /* ------------------------- 积分记录 ------------------------- */

    public PageResult<PointsRecord> records(Long userId, String type, Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        Page<PointsRecord> page = pointsRecordMapper.selectPage(new Page<>(current, size),
                Wrappers.<PointsRecord>lambdaQuery()
                        .eq(PointsRecord::getUserId, userId)
                        .eq(type != null && !type.isBlank(), PointsRecord::getType, type)
                        .orderByDesc(PointsRecord::getId));
        return PageResult.of(page);
    }

    /* ------------------------- 签到 ------------------------- */

    public SignStatusVO signStatus(Long userId) {
        SysUser user = userMapper.selectById(userId);
        SignStatusVO vo = new SignStatusVO();
        vo.setTotalPoints(user == null || user.getPoints() == null ? 0 : user.getPoints());
        vo.setContinuousThreshold(props.getSignContinuousDays());
        vo.setContinuousPoints(props.getSignContinuousPoints());

        SignRecord today = signRecordMapper.selectOne(Wrappers.<SignRecord>lambdaQuery()
                .eq(SignRecord::getUserId, userId)
                .eq(SignRecord::getSignDate, LocalDate.now())
                .last("limit 1"));
        vo.setSignedToday(today != null);
        vo.setContinuousDays(today != null ? today.getContinuousDays() : calcContinuousDays(userId));
        vo.setTodayPoints(props.getSignPoints());

        YearMonth month = YearMonth.now();
        List<SignRecord> list = signRecordMapper.selectList(Wrappers.<SignRecord>lambdaQuery()
                .eq(SignRecord::getUserId, userId)
                .ge(SignRecord::getSignDate, month.atDay(1))
                .le(SignRecord::getSignDate, month.atEndOfMonth()));
        List<LocalDate> dates = new ArrayList<>();
        for (SignRecord r : list) {
            dates.add(r.getSignDate());
        }
        vo.setSignedDates(dates);
        vo.setMonthSignedCount(dates.size());
        return vo;
    }

    private int calcContinuousDays(Long userId) {
        SignRecord last = signRecordMapper.selectOne(Wrappers.<SignRecord>lambdaQuery()
                .eq(SignRecord::getUserId, userId)
                .orderByDesc(SignRecord::getSignDate)
                .last("limit 1"));
        if (last == null) {
            return 0;
        }
        if (last.getSignDate().equals(LocalDate.now().minusDays(1))) {
            return last.getContinuousDays();
        }
        return 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public SignStatusVO sign(Long userId) {
        LocalDate today = LocalDate.now();
        SignRecord exist = signRecordMapper.selectOne(Wrappers.<SignRecord>lambdaQuery()
                .eq(SignRecord::getUserId, userId)
                .eq(SignRecord::getSignDate, today)
                .last("limit 1"));
        BizException.check(exist != null, "今天已经签到过了，明天再来吧");

        int continuous = calcContinuousDays(userId) + 1;
        int points = props.getSignPoints();
        String desc = "连续签到 " + continuous + " 天";

        if (continuous % props.getSignContinuousDays() == 0) {
            points += props.getSignContinuousPoints();
            desc += "，额外奖励 " + props.getSignContinuousPoints() + " 积分";
        }

        SignRecord record = new SignRecord();
        record.setUserId(userId);
        record.setSignDate(today);
        record.setContinuousDays(continuous);
        record.setPoints(points);
        record.setCreateTime(LocalDateTime.now());
        signRecordMapper.insert(record);

        userService.addPoints(userId, points, "sign", desc);
        messageService.send(userId, "签到成功",
                "本次签到获得 " + points + " 积分，已连续签到 " + continuous + " 天。", "system", "/sign");

        return signStatus(userId);
    }

    /* ------------------------- 积分商城 ------------------------- */

    public PageResult<PointsGoods> goodsPage(Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 12 : Math.min(pageSize, 50);
        Page<PointsGoods> page = pointsGoodsMapper.selectPage(new Page<>(current, size),
                Wrappers.<PointsGoods>lambdaQuery()
                        .eq(PointsGoods::getStatus, 1)
                        .orderByAsc(PointsGoods::getSort)
                        .orderByAsc(PointsGoods::getId));
        return PageResult.of(page);
    }

    @Transactional(rollbackFor = Exception.class)
    public PointsExchange exchange(Long userId, Long goodsId) {
        PointsGoods goods = pointsGoodsMapper.selectById(goodsId);
        BizException.notNull(goods, "积分商品不存在");
        BizException.check(goods.getStatus() != null && goods.getStatus() == 0, "该商品已下架");
        BizException.check(goods.getStock() == null || goods.getStock() <= 0, "该商品已兑完");

        SysUser user = userMapper.selectById(userId);
        BizException.notNull(user, "用户不存在");
        int points = user.getPoints() == null ? 0 : user.getPoints();
        BizException.check(points < goods.getPoints(), "积分不足，还差 " + (goods.getPoints() - points) + " 积分");

        // 扣积分
        userService.addPoints(userId, -goods.getPoints(), "exchange", "兑换 " + goods.getName());

        // 扣库存
        goods.setStock(goods.getStock() - 1);
        goods.setExchanged((goods.getExchanged() == null ? 0 : goods.getExchanged()) + 1);
        pointsGoodsMapper.updateById(goods);

        PointsExchange exchange = new PointsExchange();
        exchange.setUserId(userId);
        exchange.setPointsGoodsId(goodsId);
        exchange.setGoodsName(goods.getName());
        exchange.setImage(goods.getImage());
        exchange.setPoints(goods.getPoints());
        exchange.setStatus(0);
        exchange.setCreateTime(LocalDateTime.now());
        pointsExchangeMapper.insert(exchange);

        messageService.send(userId, "积分兑换成功",
                "您已成功兑换「" + goods.getName() + "」，我们将尽快为您发货。", "system", "/user/points");
        return exchange;
    }

    public List<PointsExchange> myExchanges(Long userId) {
        return pointsExchangeMapper.selectList(Wrappers.<PointsExchange>lambdaQuery()
                .eq(PointsExchange::getUserId, userId)
                .orderByDesc(PointsExchange::getId));
    }

    /** 积分概览 */
    public java.util.Map<String, Object> overview(Long userId) {
        SysUser user = userMapper.selectById(userId);
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("points", user == null || user.getPoints() == null ? 0 : user.getPoints());
        map.put("balance", user == null || user.getBalance() == null ? 0 : user.getBalance());
        map.put("level", user == null ? 1 : user.getLevel());
        map.put("growth", user == null ? 0 : user.getGrowth());
        map.put("levelName", levelName(user == null ? 1 : user.getLevel()));
        map.put("nextLevelGrowth", nextLevelGrowth(user == null ? 0 : user.getGrowth()));
        SignStatusVO signStatus = signStatus(userId);
        map.put("signedToday", signStatus.isSignedToday());
        map.put("continuousDays", signStatus.getContinuousDays());
        map.put("exchangeCount", pointsExchangeMapper.selectCount(
                Wrappers.<PointsExchange>lambdaQuery().eq(PointsExchange::getUserId, userId)));
        return map;
    }

    public static String levelName(Integer level) {
        if (level == null) return "普通会员";
        return switch (level) {
            case 2 -> "银卡会员";
            case 3 -> "金卡会员";
            case 4 -> "白金会员";
            case 5 -> "钻石会员";
            default -> "普通会员";
        };
    }

    public static int nextLevelGrowth(Integer growth) {
        int g = growth == null ? 0 : growth;
        if (g < 500) return 500;
        if (g < 2000) return 2000;
        if (g < 5000) return 5000;
        if (g < 10000) return 10000;
        return 10000;
    }
}
