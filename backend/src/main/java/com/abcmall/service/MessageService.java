package com.abcmall.service;

import com.abcmall.common.BizException;
import com.abcmall.common.PageResult;
import com.abcmall.entity.Message;
import com.abcmall.mapper.MessageMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** 消息中心（站内信 / 系统通知 / 订单通知） */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public PageResult<Message> page(Long userId, String type, Integer isRead,
                                    Integer pageNo, Integer pageSize) {
        int current = pageNo == null || pageNo < 1 ? 1 : pageNo;
        int size = pageSize == null || pageSize < 1 ? 10 : Math.min(pageSize, 50);
        Page<Message> page = messageMapper.selectPage(new Page<>(current, size),
                Wrappers.<Message>lambdaQuery()
                        .eq(Message::getUserId, userId)
                        .eq(StringUtils.hasText(type), Message::getType, type)
                        .eq(isRead != null, Message::getIsRead, isRead)
                        .orderByDesc(Message::getId));
        return PageResult.of(page);
    }

    public long unreadCount(Long userId) {
        return messageMapper.selectCount(Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    /** 各类型未读数，用于角标 */
    public Map<String, Long> unreadSummary(Long userId) {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("total", unreadCount(userId));
        for (String type : new String[]{"system", "order", "activity", "private"}) {
            map.put(type, messageMapper.selectCount(Wrappers.<Message>lambdaQuery()
                    .eq(Message::getUserId, userId)
                    .eq(Message::getType, type)
                    .eq(Message::getIsRead, 0)));
        }
        return map;
    }

    public void read(Long userId, Long id) {
        Message message = messageMapper.selectById(id);
        BizException.notNull(message, "消息不存在");
        BizException.check(!message.getUserId().equals(userId), "无权操作他人消息");
        message.setIsRead(1);
        messageMapper.updateById(message);
    }

    public void readAll(Long userId, String type) {
        messageMapper.update(null, Wrappers.<Message>lambdaUpdate()
                .eq(Message::getUserId, userId)
                .eq(StringUtils.hasText(type), Message::getType, type)
                .set(Message::getIsRead, 1));
    }

    public void remove(Long userId, Long id) {
        Message message = messageMapper.selectById(id);
        BizException.notNull(message, "消息不存在");
        BizException.check(!message.getUserId().equals(userId), "无权操作他人消息");
        messageMapper.deleteById(id);
    }

    public void clear(Long userId, String type) {
        messageMapper.delete(Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId)
                .eq(StringUtils.hasText(type), Message::getType, type));
    }

    /** 发送一条消息 */
    public void send(Long userId, String title, String content, String type, String link) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type == null ? "system" : type);
        message.setLink(link);
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
    }
}
