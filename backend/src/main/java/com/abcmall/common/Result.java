package com.abcmall.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结构
 *
 * 约定：
 *   200  成功
 *   401  未登录 / 会话失效
 *   500  业务异常
 *   400  参数校验失败
 */
@Data
public class Result<T> implements Serializable {

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_UNAUTHORIZED = 401;
    public static final int CODE_BAD_REQUEST = 400;
    public static final int CODE_ERROR = 500;

    private Integer code;
    private String message;
    private T data;
    private Long timestamp = System.currentTimeMillis();

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(CODE_SUCCESS, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(CODE_SUCCESS, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(CODE_SUCCESS, message, data);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(CODE_ERROR, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> unauthorized() {
        return new Result<>(CODE_UNAUTHORIZED, "您还未登录，请先登录", null);
    }

    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(CODE_UNAUTHORIZED, message, null);
    }

    public boolean isSuccess() {
        return CODE_SUCCESS == (code == null ? -1 : code);
    }
}
