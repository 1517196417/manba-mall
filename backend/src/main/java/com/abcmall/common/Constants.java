package com.abcmall.common;

/**
 * 业务常量
 */
public interface Constants {

    /* 会话 */
    String SESSION_USER_KEY = "ABCMALL_USER_ID";

    /* ------------------ 订单状态 ------------------ */
    int ORDER_UNPAID = 0;        // 待付款
    int ORDER_UNSHIPPED = 1;     // 待发货
    int ORDER_UNRECEIVED = 2;    // 待收货
    int ORDER_UNCOMMENTED = 3;   // 待评价
    int ORDER_FINISHED = 4;      // 已完成
    int ORDER_CANCELED = 5;      // 已取消
    int ORDER_AFTER_SALE = 6;    // 售后中

    /* ------------------ 支付 ------------------ */
    int PAY_STATUS_UNPAID = 0;
    int PAY_STATUS_PAID = 1;
    int PAY_STATUS_REFUNDED = 2;

    int PAY_TYPE_WECHAT = 1;     // 微信支付
    int PAY_TYPE_ALIPAY = 2;     // 支付宝
    int PAY_TYPE_UNIONPAY = 3;   // 银联
    int PAY_TYPE_BALANCE = 4;    // 余额支付

    /* ------------------ 配送方式 ------------------ */
    int DELIVERY_EXPRESS = 1;    // 快递配送
    int DELIVERY_SELF_PICK = 2;  // 门店自提
    int DELIVERY_CITY = 3;       // 同城配送

    /* ------------------ 优惠券 ------------------ */
    int COUPON_TYPE_FULL_REDUCE = 1;  // 满减
    int COUPON_TYPE_DISCOUNT = 2;     // 折扣
    int COUPON_TYPE_NO_THRESHOLD = 3; // 无门槛

    int USER_COUPON_UNUSED = 0;
    int USER_COUPON_USED = 1;
    int USER_COUPON_EXPIRED = 2;

    /* ------------------ 售后 ------------------ */
    int AS_TYPE_RETURN = 1;   // 退货
    int AS_TYPE_REFUND = 2;   // 退款
    int AS_TYPE_EXCHANGE = 3; // 换货
    int AS_TYPE_REPAIR = 4;   // 维修

    int AS_STATUS_PENDING = 0;   // 待审核
    int AS_STATUS_AGREED = 1;    // 已同意
    int AS_STATUS_REJECTED = 2;  // 已拒绝
    int AS_STATUS_RETURNING = 3; // 退货中
    int AS_STATUS_REFUNDING = 4; // 退款中
    int AS_STATUS_FINISHED = 5;  // 已完成
    int AS_STATUS_CANCELED = 6;  // 已撤销

    /* ------------------ 评价 ------------------ */
    int REVIEW_NOT_YET = 0;
    int REVIEW_DONE = 1;
}
