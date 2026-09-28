package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.dto.LoginDTO;
import com.abcmall.dto.PasswordDTO;
import com.abcmall.dto.ProfileDTO;
import com.abcmall.dto.RegisterDTO;
import com.abcmall.entity.BalanceRecord;
import com.abcmall.entity.PointsRecord;
import com.abcmall.entity.SysUser;
import com.abcmall.mapper.BalanceRecordMapper;
import com.abcmall.mapper.PointsRecordMapper;
import com.abcmall.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

/** 用户服务：注册 / 登录 / 资料 / 积分余额 */
@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final PointsRecordMapper pointsRecordMapper;
    private final BalanceRecordMapper balanceRecordMapper;

    /* ------------------------- 密码 ------------------------- */

    /**
     * MD5 摘要。
     * 说明：按课程项目惯例使用 MD5 便于演示与核对，生产环境请替换为 BCrypt。
     */
    public static String md5(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BizException("密码加密失败");
        }
    }

    /* ------------------------- 注册登录 ------------------------- */

    @Transactional(rollbackFor = Exception.class)
    public SysUser register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BizException("两次输入的密码不一致");
        }
        Long count = baseMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BizException("用户名已被占用，请换一个试试");
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(md5(dto.getPassword()));
        user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
                ? "阿帕茨用户" + dto.getUsername()
                : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setAvatar("/api/image/ph?t=新用户&w=120&h=120&c=" + (dto.getUsername().hashCode() & 7));
        user.setGender(0);
        user.setIsVerified(0);
        user.setLevel(1);
        user.setGrowth(0);
        user.setPoints(300);      // 新人礼：300 积分
        user.setBalance(BigDecimal.ZERO);
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        save(user);

        // 新人积分记录
        PointsRecord pr = new PointsRecord();
        pr.setUserId(user.getId());
        pr.setPoints(300);
        pr.setType("other");
        pr.setDescription("新人注册礼");
        pr.setCreateTime(LocalDateTime.now());
        pointsRecordMapper.insert(pr);

        return user;
    }

    public SysUser login(LoginDTO dto) {
        SysUser user = getOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, dto.getUsername()), false);
        if (user == null || !user.getPassword().equals(md5(dto.getPassword()))) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("账号已被禁用，请联系客服");
        }
        return user;
    }

    /* ------------------------- 资料 ------------------------- */

    public void updateProfile(Long userId, ProfileDTO dto) {
        SysUser user = getById(userId);
        BizException.notNull(user, "用户不存在");
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getGender() != null) {
            user.setGender(dto.getGender());
        }
        if (dto.getBirthday() != null) {
            user.setBirthday(dto.getBirthday());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    public void changePassword(Long userId, PasswordDTO dto) {
        SysUser user = getById(userId);
        BizException.notNull(user, "用户不存在");
        if (!user.getPassword().equals(md5(dto.getOldPassword()))) {
            throw new BizException("原密码不正确");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new BizException("新密码长度不能少于 6 位");
        }
        user.setPassword(md5(dto.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    /* ------------------------- 积分 / 余额 ------------------------- */

    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long userId, int points, String type, String description) {
        SysUser user = getById(userId);
        BizException.notNull(user, "用户不存在");
        int now = (user.getPoints() == null ? 0 : user.getPoints()) + points;
        if (now < 0) {
            throw new BizException("积分不足");
        }
        user.setPoints(now);
        // 积分同时累计成长值
        user.setGrowth((user.getGrowth() == null ? 0 : user.getGrowth()) + Math.max(points, 0));
        user.setLevel(calcLevel(user.getGrowth()));
        updateById(user);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setPoints(points);
        record.setType(type);
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        pointsRecordMapper.insert(record);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addBalance(Long userId, BigDecimal amount, Integer type, String description) {
        SysUser user = getById(userId);
        BizException.notNull(user, "用户不存在");
        BigDecimal now = (user.getBalance() == null ? BigDecimal.ZERO : user.getBalance()).add(amount);
        if (now.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("账户余额不足");
        }
        user.setBalance(now);
        updateById(user);

        BalanceRecord record = new BalanceRecord();
        record.setUserId(userId);
        record.setAmount(amount);
        record.setType(type);
        record.setDescription(description);
        record.setCreateTime(LocalDateTime.now());
        balanceRecordMapper.insert(record);
    }

    /** 成长值 -> 会员等级（1-5） */
    public static int calcLevel(Integer growth) {
        int g = growth == null ? 0 : growth;
        if (g >= 10000) return 5;
        if (g >= 5000) return 4;
        if (g >= 2000) return 3;
        if (g >= 500) return 2;
        return 1;
    }
}
