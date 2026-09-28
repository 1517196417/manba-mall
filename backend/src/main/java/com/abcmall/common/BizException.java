package com.abcmall.common;

/**
 * 业务异常，统一由 GlobalExceptionHandler 处理
 */
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        super(message);
        this.code = Result.CODE_ERROR;
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /* ---------- 便捷静态方法 ---------- */

    public static BizException of(String message) {
        return new BizException(message);
    }

    /** 条件成立时抛出 */
    public static void check(boolean condition, String message) {
        if (condition) {
            throw new BizException(message);
        }
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new BizException(message);
        }
    }
}
