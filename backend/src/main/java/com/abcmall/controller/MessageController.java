package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.entity.Message;
import com.abcmall.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 消息中心：站内信 / 系统通知 / 订单通知 */
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/page")
    public Result<PageResult<Message>> page(@RequestParam(required = false) String type,
                                           @RequestParam(required = false) Integer isRead,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(messageService.page(UserContext.requireUserId(), type, isRead, page, size));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(messageService.unreadCount(UserContext.requireUserId()));
    }

    /** 各类型未读数（导航栏角标） */
    @GetMapping("/unread-summary")
    public Result<Map<String, Long>> unreadSummary() {
        return Result.success(messageService.unreadSummary(UserContext.requireUserId()));
    }

    @PutMapping("/{id}/read")
    public Result<Void> read(@PathVariable Long id) {
        messageService.read(UserContext.requireUserId(), id);
        return Result.success(null);
    }

    @PutMapping("/read-all")
    public Result<Void> readAll(@RequestParam(required = false) String type) {
        messageService.readAll(UserContext.requireUserId(), type);
        return Result.success("已全部标记为已读", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        messageService.remove(UserContext.requireUserId(), id);
        return Result.success("已删除", null);
    }

    @DeleteMapping("/clear")
    public Result<Void> clear(@RequestParam(required = false) String type) {
        messageService.clear(UserContext.requireUserId(), type);
        return Result.success("已清空", null);
    }
}
