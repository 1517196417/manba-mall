package com.abcmall.controller;

import com.abcmall.common.PageResult;
import com.abcmall.common.Result;
import com.abcmall.common.UserContext;
import com.abcmall.dto.AppendCommentDTO;
import com.abcmall.dto.CommentDTO;
import com.abcmall.entity.OrderComment;
import com.abcmall.entity.OrderItem;
import com.abcmall.service.CommentService;
import com.abcmall.vo.CommentStatisticsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 商品评价：评价 / 追评 / 评分统计 */
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 发表评价 */
    @PostMapping("/add")
    public Result<OrderComment> add(@Valid @RequestBody CommentDTO dto) {
        return Result.success("评价成功，获得 50 积分奖励", commentService.add(UserContext.requireUserId(), dto));
    }

    /** 商品评价列表（公开） */
    @GetMapping("/product/{productId}")
    public Result<PageResult<OrderComment>> productComments(@PathVariable Long productId,
                                                           @RequestParam(required = false) Integer rating,
                                                           @RequestParam(defaultValue = "1") Integer page,
                                                           @RequestParam(defaultValue = "5") Integer size) {
        return Result.success(commentService.productComments(productId, rating, page, size));
    }

    /** 我的评价 */
    @GetMapping("/my")
    public Result<PageResult<OrderComment>> my(@RequestParam(defaultValue = "1") Integer page,
                                              @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(commentService.myComments(UserContext.requireUserId(), page, size));
    }

    /** 待评价商品 */
    @GetMapping("/pending")
    public Result<List<OrderItem>> pending() {
        return Result.success(commentService.pendingItems(UserContext.requireUserId()));
    }

    /** 追评 */
    @PostMapping("/append")
    public Result<Void> append(@Valid @RequestBody AppendCommentDTO dto) {
        commentService.append(UserContext.requireUserId(), dto);
        return Result.success("追评成功", null);
    }

    /** 删除评价 */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        commentService.remove(UserContext.requireUserId(), id);
        return Result.success("已删除", null);
    }

    /** 评分统计 */
    @GetMapping("/statistics/{productId}")
    public Result<CommentStatisticsVO> statistics(@PathVariable Long productId) {
        return Result.success(commentService.statistics(productId));
    }
}
