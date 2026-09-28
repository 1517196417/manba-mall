/* ==========================================================================
 *  AbcMall 阿帕茨商城 —— 数据库初始化脚本
 *  DB   : MySQL 8.x
 *  Charset: utf8mb4
 *  说明 : 执行本脚本即可得到完整的库表结构 + 演示种子数据
 *  用法 : mysql -uroot -proot < abcmall.sql
 * ========================================================================== */

DROP DATABASE IF EXISTS `abcmall`;
CREATE DATABASE `abcmall` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `abcmall`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

/* =========================== 1. 用户与地址 =========================== */

DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL COMMENT '登录账号',
  `password`    VARCHAR(64)  NOT NULL COMMENT '密码(MD5)',
  `nickname`    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
  `avatar`      VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `email`       VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `gender`      TINYINT      DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  `birthday`    DATE         DEFAULT NULL COMMENT '生日',
  `real_name`   VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
  `id_card`     VARCHAR(30)  DEFAULT NULL COMMENT '身份证号',
  `is_verified` TINYINT      DEFAULT 0 COMMENT '是否实名 0否 1是',
  `level`       TINYINT      DEFAULT 1 COMMENT '会员等级 1-5',
  `growth`      INT          DEFAULT 0 COMMENT '成长值',
  `points`      INT          DEFAULT 0 COMMENT '积分',
  `balance`     DECIMAL(12,2) DEFAULT 0.00 COMMENT '账户余额',
  `status`      TINYINT      DEFAULT 1 COMMENT '状态 0禁用 1正常',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL COMMENT '用户ID',
  `receiver`    VARCHAR(50) NOT NULL COMMENT '收货人',
  `phone`       VARCHAR(20) NOT NULL COMMENT '联系电话',
  `province`    VARCHAR(50) DEFAULT NULL,
  `city`        VARCHAR(50) DEFAULT NULL,
  `district`    VARCHAR(50) DEFAULT NULL,
  `detail`      VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `zip_code`    VARCHAR(10)  DEFAULT NULL,
  `tag`         VARCHAR(20)  DEFAULT NULL COMMENT '标签 家/公司/学校',
  `is_default`  TINYINT      DEFAULT 0 COMMENT '是否默认地址',
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

DROP TABLE IF EXISTS `balance_record`;
CREATE TABLE `balance_record` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT        NOT NULL,
  `amount`      DECIMAL(12,2) NOT NULL COMMENT '变动金额 正为收入 负为支出',
  `type`        TINYINT       DEFAULT 1 COMMENT '1充值 2消费 3退款',
  `description` VARCHAR(200)  DEFAULT NULL,
  `create_time` DATETIME      DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='余额流水表';

/* =========================== 2. 商品基础数据 =========================== */

DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `parent_id`   BIGINT      DEFAULT 0 COMMENT '父级ID,0为顶级',
  `name`        VARCHAR(50) NOT NULL,
  `level`       TINYINT     DEFAULT 1 COMMENT '层级 1/2',
  `icon`        VARCHAR(255) DEFAULT NULL COMMENT '图标',
  `banner`      VARCHAR(255) DEFAULT NULL COMMENT '分类页banner',
  `description` VARCHAR(255) DEFAULT NULL,
  `sort`        INT         DEFAULT 0,
  `status`      TINYINT     DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

DROP TABLE IF EXISTS `product_brand`;
CREATE TABLE `product_brand` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(50) NOT NULL,
  `logo`        VARCHAR(255) DEFAULT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `initial`     CHAR(1)     DEFAULT NULL COMMENT '首字母',
  `sort`        INT         DEFAULT 0,
  `status`      TINYINT     DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `name`          VARCHAR(200)  NOT NULL COMMENT '商品名称',
  `subtitle`      VARCHAR(255)  DEFAULT NULL COMMENT '副标题/卖点',
  `category_id`   BIGINT        NOT NULL COMMENT '分类ID',
  `brand_id`      BIGINT        DEFAULT NULL COMMENT '品牌ID',
  `main_image`    VARCHAR(500)  DEFAULT NULL COMMENT '主图',
  `images`        TEXT          DEFAULT NULL COMMENT '图片集 JSON 数组',
  `video`         VARCHAR(500)  DEFAULT NULL COMMENT '主视频',
  `detail_html`   TEXT          DEFAULT NULL COMMENT '图文详情',
  `price`         DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '售价',
  `market_price`  DECIMAL(12,2) DEFAULT 0 COMMENT '划线价',
  `cost_price`    DECIMAL(12,2) DEFAULT 0 COMMENT '成本价',
  `stock`         INT           DEFAULT 0 COMMENT '总库存',
  `sales`         INT           DEFAULT 0 COMMENT '销量',
  `views`         INT           DEFAULT 0 COMMENT '浏览量',
  `unit`          VARCHAR(10)   DEFAULT '件',
  `weight`        DECIMAL(10,2) DEFAULT 0 COMMENT '重量kg',
  `tags`          VARCHAR(255)  DEFAULT NULL COMMENT '促销标签,逗号分隔',
  `is_recommend`  TINYINT       DEFAULT 0 COMMENT '是否推荐',
  `is_hot`        TINYINT       DEFAULT 0 COMMENT '是否热销',
  `is_new`        TINYINT       DEFAULT 0 COMMENT '是否新品',
  `status`        TINYINT       DEFAULT 1 COMMENT '0下架 1上架',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_brand` (`brand_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SPU表';

DROP TABLE IF EXISTS `product_spec`;
CREATE TABLE `product_spec` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT      NOT NULL,
  `name`       VARCHAR(50) NOT NULL COMMENT '规格名 如 颜色',
  `sort`       INT         DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格项表';

DROP TABLE IF EXISTS `product_spec_value`;
CREATE TABLE `product_spec_value` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT      NOT NULL,
  `spec_id`    BIGINT      NOT NULL COMMENT '所属规格项',
  `value`      VARCHAR(50) NOT NULL COMMENT '规格值 如 黑色',
  `sort`       INT         DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_spec` (`spec_id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格值表';

DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `product_id`   BIGINT        NOT NULL,
  `sku_code`     VARCHAR(64)   DEFAULT NULL,
  `spec_json`    VARCHAR(500)  DEFAULT NULL COMMENT '规格组合 JSON',
  `spec_desc`    VARCHAR(255)  DEFAULT NULL COMMENT '规格描述 如 黑色 256GB',
  `price`        DECIMAL(12,2) NOT NULL DEFAULT 0,
  `market_price` DECIMAL(12,2) DEFAULT 0,
  `stock`        INT           DEFAULT 0,
  `sales`        INT           DEFAULT 0,
  `image`        VARCHAR(500)  DEFAULT NULL,
  `status`       TINYINT       DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品SKU表';

DROP TABLE IF EXISTS `product_attr`;
CREATE TABLE `product_attr` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `product_id` BIGINT       NOT NULL,
  `group_name` VARCHAR(50)  DEFAULT '主体' COMMENT '参数分组',
  `name`       VARCHAR(50)  NOT NULL COMMENT '参数名',
  `value`      VARCHAR(255) DEFAULT NULL COMMENT '参数值',
  `sort`       INT          DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品参数表';

/* =========================== 3. 首页运营位 =========================== */

DROP TABLE IF EXISTS `home_banner`;
CREATE TABLE `home_banner` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `title`      VARCHAR(100) DEFAULT NULL,
  `image`      VARCHAR(500) NOT NULL,
  `link`       VARCHAR(255) DEFAULT NULL COMMENT '跳转地址',
  `position`   VARCHAR(30)  DEFAULT 'carousel' COMMENT 'carousel轮播 ad_home广告位 ad_side侧栏',
  `sort`       INT          DEFAULT 0,
  `status`     TINYINT      DEFAULT 1,
  `start_time` DATETIME     DEFAULT NULL,
  `end_time`   DATETIME     DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图/广告位表';

DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `title`       VARCHAR(200) NOT NULL,
  `content`     TEXT         DEFAULT NULL,
  `type`        VARCHAR(20)  DEFAULT 'notice' COMMENT 'notice公告 activity活动',
  `is_top`      TINYINT      DEFAULT 0,
  `sort`        INT          DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

DROP TABLE IF EXISTS `activity`;
CREATE TABLE `activity` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `title`       VARCHAR(100) NOT NULL,
  `subtitle`    VARCHAR(200) DEFAULT NULL,
  `image`       VARCHAR(500) DEFAULT NULL,
  `type`        VARCHAR(30)  DEFAULT 'promotion' COMMENT 'promotion专场 seckill秒杀 groupon拼团 new新品 season季节',
  `link`        VARCHAR(255) DEFAULT NULL,
  `start_time`  DATETIME     DEFAULT NULL,
  `end_time`    DATETIME     DEFAULT NULL,
  `sort`        INT          DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='活动专区表';

DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `title`       VARCHAR(100) NOT NULL,
  `subtitle`    VARCHAR(200) DEFAULT NULL,
  `cover`       VARCHAR(500) DEFAULT NULL,
  `content`     TEXT         DEFAULT NULL COMMENT '专题图文内容 HTML',
  `product_ids` VARCHAR(500) DEFAULT NULL COMMENT '关联商品ID,逗号分隔',
  `views`       INT          DEFAULT 0,
  `sort`        INT          DEFAULT 0,
  `status`      TINYINT      DEFAULT 1,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专题页面表';

DROP TABLE IF EXISTS `search_hot`;
CREATE TABLE `search_hot` (
  `id`      BIGINT      NOT NULL AUTO_INCREMENT,
  `keyword` VARCHAR(50) NOT NULL,
  `sort`    INT         DEFAULT 0,
  `is_hot`  TINYINT     DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热门搜索词表';

/* =========================== 4. 购物车 =========================== */

DROP TABLE IF EXISTS `cart_item`;
CREATE TABLE `cart_item` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `product_id`  BIGINT   NOT NULL,
  `sku_id`      BIGINT   DEFAULT NULL,
  `quantity`    INT      DEFAULT 1,
  `selected`    TINYINT  DEFAULT 1 COMMENT '是否选中',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

/* =========================== 5. 订单 =========================== */

DROP TABLE IF EXISTS `shop_order`;
CREATE TABLE `shop_order` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `order_no`       VARCHAR(40)   NOT NULL COMMENT '订单号',
  `user_id`        BIGINT        NOT NULL,
  `total_amount`   DECIMAL(12,2) DEFAULT 0 COMMENT '商品总额',
  `freight`        DECIMAL(10,2) DEFAULT 0 COMMENT '运费',
  `discount_amount` DECIMAL(12,2) DEFAULT 0 COMMENT '优惠金额',
  `pay_amount`     DECIMAL(12,2) DEFAULT 0 COMMENT '应付金额',
  `user_coupon_id` BIGINT        DEFAULT NULL COMMENT '使用的优惠券',
  `points_deduct`  DECIMAL(10,2) DEFAULT 0 COMMENT '积分抵扣金额',
  `points_used`    INT           DEFAULT 0 COMMENT '使用积分',
  `balance_used`   DECIMAL(12,2) DEFAULT 0 COMMENT '余额抵扣',
  `pay_type`       TINYINT       DEFAULT NULL COMMENT '1微信 2支付宝 3银联 4余额',
  `pay_status`     TINYINT       DEFAULT 0 COMMENT '0未支付 1已支付 2已退款',
  `order_status`   TINYINT       DEFAULT 0 COMMENT '0待付款 1待发货 2待收货 3待评价 4已完成 5已取消 6售后中',
  `delivery_type`  TINYINT       DEFAULT 1 COMMENT '1快递 2自提 3同城配送',
  `address_id`     BIGINT        DEFAULT NULL,
  `receiver`       VARCHAR(50)   DEFAULT NULL,
  `receiver_phone` VARCHAR(20)   DEFAULT NULL,
  `receiver_addr`  VARCHAR(500)  DEFAULT NULL,
  `remark`         VARCHAR(255)  DEFAULT NULL COMMENT '订单备注',
  `invoice_title`  VARCHAR(100)  DEFAULT NULL COMMENT '发票抬头',
  `invoice_tax_no` VARCHAR(50)   DEFAULT NULL COMMENT '税号',
  `cancel_reason`  VARCHAR(255)  DEFAULT NULL,
  `create_time`    DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `pay_time`       DATETIME      DEFAULT NULL,
  `delivery_time`  DATETIME      DEFAULT NULL,
  `receive_time`   DATETIME      DEFAULT NULL,
  `finish_time`    DATETIME      DEFAULT NULL,
  `cancel_time`    DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT        NOT NULL,
  `order_no`      VARCHAR(40)   DEFAULT NULL,
  `product_id`    BIGINT        NOT NULL,
  `sku_id`        BIGINT        DEFAULT NULL,
  `product_name`  VARCHAR(200)  DEFAULT NULL,
  `sku_desc`      VARCHAR(255)  DEFAULT NULL,
  `image`         VARCHAR(500)  DEFAULT NULL,
  `price`         DECIMAL(12,2) DEFAULT 0,
  `quantity`      INT           DEFAULT 1,
  `total_price`   DECIMAL(12,2) DEFAULT 0,
  `review_status` TINYINT       DEFAULT 0 COMMENT '0未评价 1已评价',
  `refund_status` TINYINT       DEFAULT 0 COMMENT '0无 1售后中 2已完成',
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

DROP TABLE IF EXISTS `order_logistics`;
CREATE TABLE `order_logistics` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
  `order_id`     BIGINT      NOT NULL,
  `order_no`     VARCHAR(40) DEFAULT NULL,
  `company`      VARCHAR(50) DEFAULT NULL COMMENT '快递公司',
  `company_code` VARCHAR(30) DEFAULT NULL,
  `tracking_no`  VARCHAR(50) DEFAULT NULL COMMENT '运单号',
  `receiver_name` VARCHAR(50) DEFAULT NULL,
  `receiver_phone` VARCHAR(20) DEFAULT NULL,
  `receiver_addr` VARCHAR(500) DEFAULT NULL,
  `status`       TINYINT     DEFAULT 1 COMMENT '1已揽收 2运输中 3派送中 4已签收',
  `create_time`  DATETIME    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流单表';

DROP TABLE IF EXISTS `logistics_trace`;
CREATE TABLE `logistics_trace` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `order_id`    BIGINT       NOT NULL,
  `trace_time`  DATETIME     DEFAULT NULL,
  `context`     VARCHAR(500) DEFAULT NULL COMMENT '轨迹内容',
  `status`      TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物流轨迹表';

/* =========================== 6. 评价与售后 =========================== */

DROP TABLE IF EXISTS `order_comment`;
CREATE TABLE `order_comment` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `order_id`      BIGINT       NOT NULL,
  `order_item_id` BIGINT       DEFAULT NULL,
  `product_id`    BIGINT       NOT NULL,
  `user_id`       BIGINT       NOT NULL,
  `sku_desc`      VARCHAR(255) DEFAULT NULL,
  `rating`        TINYINT      DEFAULT 5 COMMENT '评分 1-5',
  `content`       VARCHAR(1000) DEFAULT NULL,
  `images`        VARCHAR(1000) DEFAULT NULL COMMENT '晒图 JSON 数组',
  `is_anonymous`  TINYINT      DEFAULT 0,
  `reply`         VARCHAR(500) DEFAULT NULL COMMENT '商家回复',
  `append_content` VARCHAR(1000) DEFAULT NULL COMMENT '追评内容',
  `append_time`   DATETIME     DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

DROP TABLE IF EXISTS `after_sale`;
CREATE TABLE `after_sale` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `service_no`    VARCHAR(40)   NOT NULL COMMENT '售后单号',
  `order_id`      BIGINT        NOT NULL,
  `order_no`      VARCHAR(40)   DEFAULT NULL,
  `order_item_id` BIGINT        DEFAULT NULL,
  `user_id`       BIGINT        NOT NULL,
  `product_name`  VARCHAR(200)  DEFAULT NULL,
  `image`         VARCHAR(500)  DEFAULT NULL,
  `type`          TINYINT       DEFAULT 1 COMMENT '1退货 2退款 3换货 4维修',
  `reason`        VARCHAR(200)  DEFAULT NULL,
  `description`   VARCHAR(1000) DEFAULT NULL,
  `images`        VARCHAR(1000) DEFAULT NULL,
  `refund_amount` DECIMAL(12,2) DEFAULT 0,
  `status`        TINYINT       DEFAULT 0 COMMENT '0待审核 1已同意 2已拒绝 3退货中 4退款中 5已完成 6已撤销',
  `handle_note`   VARCHAR(500)  DEFAULT NULL COMMENT '处理意见',
  `create_time`   DATETIME      DEFAULT CURRENT_TIMESTAMP,
  `handle_time`   DATETIME      DEFAULT NULL,
  `finish_time`   DATETIME      DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_service_no` (`service_no`),
  KEY `idx_user` (`user_id`),
  KEY `idx_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后单表';

DROP TABLE IF EXISTS `after_sale_progress`;
CREATE TABLE `after_sale_progress` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `after_sale_id` BIGINT       NOT NULL,
  `title`         VARCHAR(100) DEFAULT NULL,
  `content`       VARCHAR(500) DEFAULT NULL,
  `operator`      VARCHAR(50)  DEFAULT NULL,
  `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_as` (`after_sale_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后进度表';

/* =========================== 7. 营销 =========================== */

DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT,
  `name`         VARCHAR(100)  NOT NULL,
  `type`         TINYINT       DEFAULT 1 COMMENT '1满减券 2折扣券 3无门槛',
  `amount`       DECIMAL(10,2) DEFAULT 0 COMMENT '面额',
  `discount`     DECIMAL(4,2)  DEFAULT 10 COMMENT '折扣 如8.5',
  `min_amount`   DECIMAL(10,2) DEFAULT 0 COMMENT '使用门槛',
  `total`        INT           DEFAULT 0 COMMENT '发行总量',
  `received`     INT           DEFAULT 0 COMMENT '已领取',
  `per_limit`    INT           DEFAULT 1 COMMENT '每人限领',
  `category_id`  BIGINT        DEFAULT NULL COMMENT '限定分类,空为全场',
  `description`  VARCHAR(255)  DEFAULT NULL,
  `start_time`   DATETIME      DEFAULT NULL,
  `end_time`     DATETIME      DEFAULT NULL,
  `status`       TINYINT       DEFAULT 1,
  `create_time`  DATETIME      DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT      NOT NULL,
  `coupon_id`    BIGINT      NOT NULL,
  `status`       TINYINT     DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
  `order_id`     BIGINT      DEFAULT NULL,
  `receive_time` DATETIME    DEFAULT CURRENT_TIMESTAMP,
  `use_time`     DATETIME    DEFAULT NULL,
  `expire_time`  DATETIME    DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

DROP TABLE IF EXISTS `points_record`;
CREATE TABLE `points_record` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `points`      INT          NOT NULL COMMENT '正为获得 负为消耗',
  `type`        VARCHAR(20)  DEFAULT 'other' COMMENT 'sign签到 order订单 exchange兑换',
  `description` VARCHAR(200) DEFAULT NULL,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分记录表';

DROP TABLE IF EXISTS `sign_record`;
CREATE TABLE `sign_record` (
  `id`               BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`          BIGINT   NOT NULL,
  `sign_date`        DATE     NOT NULL COMMENT '签到日期',
  `continuous_days`  INT      DEFAULT 1 COMMENT '连续签到天数',
  `points`           INT      DEFAULT 0 COMMENT '获得积分',
  `create_time`      DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

DROP TABLE IF EXISTS `points_goods`;
CREATE TABLE `points_goods` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100) NOT NULL,
  `image`       VARCHAR(500) DEFAULT NULL,
  `points`      INT          DEFAULT 0 COMMENT '所需积分',
  `market_price` DECIMAL(10,2) DEFAULT 0 COMMENT '参考价',
  `stock`       INT          DEFAULT 0,
  `exchanged`   INT          DEFAULT 0,
  `description` VARCHAR(500) DEFAULT NULL,
  `status`      TINYINT      DEFAULT 1,
  `sort`        INT          DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分商品表';

DROP TABLE IF EXISTS `points_exchange`;
CREATE TABLE `points_exchange` (
  `id`              BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT      NOT NULL,
  `points_goods_id` BIGINT      NOT NULL,
  `goods_name`      VARCHAR(100) DEFAULT NULL,
  `image`           VARCHAR(500) DEFAULT NULL,
  `points`          INT         DEFAULT 0,
  `status`          TINYINT     DEFAULT 0 COMMENT '0待发货 1已发货 2已完成',
  `express_no`      VARCHAR(50)  DEFAULT NULL,
  `create_time`     DATETIME    DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分兑换记录表';

DROP TABLE IF EXISTS `seckill_activity`;
CREATE TABLE `seckill_activity` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT,
  `title`         VARCHAR(100)  DEFAULT NULL,
  `product_id`    BIGINT        NOT NULL,
  `sku_id`        BIGINT        DEFAULT NULL,
  `seckill_price` DECIMAL(12,2) DEFAULT 0 COMMENT '秒杀价',
  `stock`         INT           DEFAULT 0,
  `sold`          INT           DEFAULT 0,
  `limit_count`   INT           DEFAULT 1 COMMENT '每人限购',
  `start_time`    DATETIME      DEFAULT NULL,
  `end_time`      DATETIME      DEFAULT NULL,
  `status`        TINYINT       DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

/* =========================== 8. 互动 =========================== */

DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `product_id`  BIGINT   NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

DROP TABLE IF EXISTS `footprint`;
CREATE TABLE `footprint` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL,
  `product_id`  BIGINT   NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览足迹表';

DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL,
  `title`       VARCHAR(200) NOT NULL,
  `content`     VARCHAR(1000) DEFAULT NULL,
  `type`        VARCHAR(20)  DEFAULT 'system' COMMENT 'system系统 order订单 activity活动 private站内信',
  `link`        VARCHAR(255) DEFAULT NULL,
  `is_read`     TINYINT      DEFAULT 0,
  `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

DROP TABLE IF EXISTS `help_article`;
CREATE TABLE `help_article` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT,
  `category`   VARCHAR(50)  DEFAULT '常见问题',
  `question`   VARCHAR(200) NOT NULL,
  `answer`     TEXT         DEFAULT NULL,
  `sort`       INT          DEFAULT 0,
  `status`     TINYINT      DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帮助中心表';

SET FOREIGN_KEY_CHECKS = 1;

/* ==========================================================================
 *                              种子数据
 * ========================================================================== */

/* ---------- 用户 ---------- */
INSERT INTO `sys_user`
(`id`,`username`,`password`,`nickname`,`avatar`,`phone`,`email`,`gender`,`birthday`,`level`,`growth`,`points`,`balance`,`status`) VALUES
(1,'zhangsan','e10adc3949ba59abbe56e057f20f883e','张三','/api/image/ph?t=ZS&w=120&h=120&c=3','13800138000','zhangsan@abcmall.com',1,'1995-06-18',3,2400,860,1580.50,1),
(2,'lisi','e10adc3949ba59abbe56e057f20f883e','李四','/api/image/ph?t=LS&w=120&h=120&c=5','13900139000','lisi@abcmall.com',2,'1998-03-02',2,860,320,200.00,1);

INSERT INTO `user_address`
(`id`,`user_id`,`receiver`,`phone`,`province`,`city`,`district`,`detail`,`zip_code`,`tag`,`is_default`) VALUES
(1,1,'张三','13800138000','广东省','深圳市','南山区','科技园南区深南大道9988号阿帕茨大厦A座1801','518057','公司',1),
(2,1,'张三','13800138000','广东省','广州市','天河区','珠江新城华夏路16号富力中心2503','510623','家',0),
(3,2,'李四','13900139000','浙江省','杭州市','西湖区','文三路508号天苑花园3幢602','310012','家',1);

INSERT INTO `balance_record` (`user_id`,`amount`,`type`,`description`) VALUES
(1,2000.00,1,'账户充值'),
(1,-419.50,2,'支付订单 20260913100001'),
(2,200.00,1,'新用户注册赠送');

/* ---------- 分类 ---------- */
INSERT INTO `product_category` (`id`,`parent_id`,`name`,`level`,`icon`,`description`,`sort`) VALUES
(1,0,'数码电子',1,'/api/image/ph?t=数码&w=120&h=120&c=1','手机 电脑 影音 智能设备',1),
(2,0,'家用电器',1,'/api/image/ph?t=家电&w=120&h=120&c=2','冰箱 空调 厨房 生活电器',2),
(3,0,'服饰鞋包',1,'/api/image/ph?t=服饰&w=120&h=120&c=3','男装 女装 鞋靴 箱包',3),
(4,0,'美妆护肤',1,'/api/image/ph?t=美妆&w=120&h=120&c=4','护肤 彩妆 香水 洗护',4),
(5,0,'食品生鲜',1,'/api/image/ph?t=食品&w=120&h=120&c=5','零食 粮油 乳品 生鲜',5),
(6,0,'家居家装',1,'/api/image/ph?t=家居&w=120&h=120&c=6','床品 家具 厨具 灯饰',6),
(7,0,'运动户外',1,'/api/image/ph?t=运动&w=120&h=120&c=7','运动鞋服 健身器材 户外装备',7),
(8,0,'母婴玩具',1,'/api/image/ph?t=母婴&w=120&h=120&c=8','奶粉辅食 纸尿裤 玩具 童装',8),
(9,0,'图书文娱',1,'/api/image/ph?t=图书&w=120&h=120&c=9','图书 文具 乐器',9),
(10,0,'汽车用品',1,'/api/image/ph?t=汽车&w=120&h=120&c=10','车载电器 养护 内饰',10);

INSERT INTO `product_category` (`id`,`parent_id`,`name`,`level`,`icon`,`description`,`sort`) VALUES
(101,1,'手机通讯',2,NULL,NULL,1),
(102,1,'电脑办公',2,NULL,NULL,2),
(103,1,'平板电脑',2,NULL,NULL,3),
(104,1,'耳机音响',2,NULL,NULL,4),
(105,1,'智能穿戴',2,NULL,NULL,5),
(106,1,'摄影摄像',2,NULL,NULL,6),
(201,2,'冰箱洗衣机',2,NULL,NULL,1),
(202,2,'空调',2,NULL,NULL,2),
(203,2,'厨房电器',2,NULL,NULL,3),
(204,2,'生活电器',2,NULL,NULL,4),
(301,3,'男装',2,NULL,NULL,1),
(302,3,'女装',2,NULL,NULL,2),
(303,3,'鞋靴',2,NULL,NULL,3),
(304,3,'箱包',2,NULL,NULL,4),
(401,4,'面部护肤',2,NULL,NULL,1),
(402,4,'彩妆',2,NULL,NULL,2),
(403,4,'香水',2,NULL,NULL,3),
(404,4,'洗护清洁',2,NULL,NULL,4),
(501,5,'休闲零食',2,NULL,NULL,1),
(502,5,'粮油调味',2,NULL,NULL,2),
(503,5,'乳品饮料',2,NULL,NULL,3),
(504,5,'生鲜果蔬',2,NULL,NULL,4),
(601,6,'床上用品',2,NULL,NULL,1),
(602,6,'家具',2,NULL,NULL,2),
(603,6,'厨具',2,NULL,NULL,3),
(604,6,'灯饰照明',2,NULL,NULL,4),
(701,7,'运动鞋服',2,NULL,NULL,1),
(702,7,'健身器材',2,NULL,NULL,2),
(703,7,'户外装备',2,NULL,NULL,3),
(801,8,'奶粉辅食',2,NULL,NULL,1),
(802,8,'纸尿裤',2,NULL,NULL,2),
(803,8,'玩具',2,NULL,NULL,3),
(804,8,'童装',2,NULL,NULL,4),
(901,9,'图书',2,NULL,NULL,1),
(902,9,'文具',2,NULL,NULL,2),
(903,9,'乐器',2,NULL,NULL,3),
(1001,10,'车载电器',2,NULL,NULL,1),
(1002,10,'汽车养护',2,NULL,NULL,2),
(1003,10,'内饰用品',2,NULL,NULL,3);

/* ---------- 品牌 ---------- */
INSERT INTO `product_brand` (`id`,`name`,`logo`,`description`,`initial`,`sort`) VALUES
(1,'华为','/api/image/ph?t=华为&w=200&h=100&c=1','构建万物互联的智能世界','H',1),
(2,'小米','/api/image/ph?t=小米&w=200&h=100&c=2','让每个人都能享受科技的乐趣','X',2),
(3,'Apple','/api/image/ph?t=Apple&w=200&h=100&c=3','Think different','A',3),
(4,'联想','/api/image/ph?t=联想&w=200&h=100&c=4','智慧中国 联想未来','L',4),
(5,'戴森','/api/image/ph?t=戴森&w=200&h=100&c=5','重新定义家用电器','D',5),
(6,'李宁','/api/image/ph?t=李宁&w=200&h=100&c=6','一切皆有可能','L',6),
(7,'兰蔻','/api/image/ph?t=兰蔻&w=200&h=100&c=7','来自巴黎的高端美妆','L',7),
(8,'三只松鼠','/api/image/ph?t=三只松鼠&w=200&h=100&c=8','让天下人都爱上吃零食','S',8),
(9,'南极人','/api/image/ph?t=南极人&w=200&h=100&c=9','舒适家居生活','N',9),
(10,'飞利浦','/api/image/ph?t=飞利浦&w=200&h=100&c=10','创新与你同行','F',10);

/* ---------- 商品 SPU ---------- */
INSERT INTO `product`
(`id`,`name`,`subtitle`,`category_id`,`brand_id`,`main_image`,`images`,`video`,`detail_html`,`price`,`market_price`,`stock`,`sales`,`views`,`unit`,`tags`,`is_recommend`,`is_hot`,`is_new`) VALUES
(1,'华为 Mate 60 Pro 12GB+512GB 雅川青','超可靠玄武架构 卫星通话 第二代昆仑玻璃',101,1,
 '/api/image/ph?t=华为Mate60Pro&w=600&h=600&c=1',
 '["/api/image/ph?t=Mate60-1&w=800&h=800&c=1","/api/image/ph?t=Mate60-2&w=800&h=800&c=2","/api/image/ph?t=Mate60-3&w=800&h=800&c=3","/api/image/ph?t=Mate60-4&w=800&h=800&c=4"]',
 '/video/demo/mate60.mp4',
 '<h3>旗舰影像 · 卫星通话</h3><p>华为 Mate 60 Pro 搭载超可靠玄武架构，支持双向北斗卫星消息，第二代昆仑玻璃，抗摔能力提升 10 倍。</p><p>全焦段超清影像：5000 万像素超光变摄像头 + 1200 万像素超广角 + 4800 万像素超微距长焦。</p><img src="/api/image/ph?t=详情图1&w=800&h=400&c=1"/>',
 6999.00,7499.00,520,3860,15230,'台','热销,新品,分期免息',1,1,1),
(2,'小米 14 Pro 16GB+512GB 岩石青','徕卡光学镜头 骁龙8Gen3 2K超色准屏',101,2,
 '/api/image/ph?t=小米14Pro&w=600&h=600&c=2',
 '["/api/image/ph?t=Mi14-1&w=800&h=800&c=2","/api/image/ph?t=Mi14-2&w=800&h=800&c=5"]',
 NULL,
 '<h3>徕卡光学 · 骁龙旗舰</h3><p>第三代骁龙 8 移动平台，全等深微曲屏，小米澎湃 OS 全生态互联。</p>',
 4999.00,5499.00,860,5210,18320,'台','热销,分期免息',1,1,1),
(3,'Apple iPhone 15 Pro 256GB 原色钛金属','A17 Pro 芯片 钛金属设计 USB-C 接口',101,3,
 '/api/image/ph?t=iPhone15Pro&w=600&h=600&c=3',
 '["/api/image/ph?t=IP15-1&w=800&h=800&c=3","/api/image/ph?t=IP15-2&w=800&h=800&c=6"]',
 NULL,
 '<h3>钛金属 · A17 Pro</h3><p>航空级钛金属边框，更轻更坚固；A17 Pro 芯片带来主机级游戏体验。</p>',
 8499.00,8999.00,300,2180,20150,'台','热销',1,1,0),
(4,'联想拯救者 Y9000P 2024 电竞笔记本','i9-14900HX 32G 1TB RTX4060 2.5K 240Hz',102,4,
 '/api/image/ph?t=拯救者Y9000P&w=600&h=600&c=4',
 '["/api/image/ph?t=Y9000P-1&w=800&h=800&c=4","/api/image/ph?t=Y9000P-2&w=800&h=800&c=7"]',
 NULL,
 '<h3>性能怪兽</h3><p>16 英寸 2.5K 240Hz 电竞屏，霜刃 Pro 散热系统 5.0，性能释放可达 190W。</p>',
 9299.00,9999.00,180,760,6420,'台','分期免息,以旧换新',1,1,1),
(5,'Apple iPad Air 11 英寸 M2 128GB','轻薄设计 全面屏 支持 Apple Pencil Pro',103,3,
 '/api/image/ph?t=iPadAir&w=600&h=600&c=5',
 '["/api/image/ph?t=iPad-1&w=800&h=800&c=5"]',
 NULL,'<h3>M2 芯片 · 轻若无物</h3><p>11 英寸 Liquid 视网膜显示屏，支持妙控键盘与 Apple Pencil Pro。</p>',
 4799.00,4999.00,420,980,5230,'台','新品',1,0,1),
(6,'索尼 WH-1000XM5 头戴式降噪耳机','双芯降噪 30小时续航 Hi-Res 音质',104,10,
 '/api/image/ph?t=WH1000XM5&w=600&h=600&c=6',
 '["/api/image/ph?t=XM5-1&w=800&h=800&c=6"]',
 NULL,'<h3>静界之作</h3><p>8 麦克风双芯片降噪系统，智能免摘对话，30 小时长续航。</p>',
 2199.00,2899.00,650,1540,8760,'副','热销,降价',1,1,0),
(7,'华为 WATCH GT4 智能手表 46mm','8天长续航 血氧监测 100+运动模式',105,1,
 '/api/image/ph?t=WATCHGT4&w=600&h=600&c=7',
 '["/api/image/ph?t=GT4-1&w=800&h=800&c=7"]',
 NULL,'<h3>腕上健康管家</h3><p>全新心率监测技术，支持月经周期管理、睡眠呼吸暂停监测。</p>',
 1488.00,1688.00,780,2210,9120,'块','热销',1,1,0),
(8,'戴森 V12 Detect Slim 无绳吸尘器','激光探测 智能感应 轻量机身',204,5,
 '/api/image/ph?t=戴森V12&w=600&h=600&c=8',
 '["/api/image/ph?t=V12-1&w=800&h=800&c=8"]',
 NULL,'<h3>激光显尘</h3><p>激光探测让微尘无处遁形，压电式声学传感器实时统计尘量。</p>',
 3990.00,4490.00,260,430,4120,'台','降价',1,0,0),
(9,'海尔 501L 十字对开门冰箱 一级能效','风冷无霜 干湿分储 母婴空间',201,10,
 '/api/image/ph?t=海尔501L冰箱&w=600&h=600&c=9',
 '["/api/image/ph?t=Haier-1&w=800&h=800&c=9"]',
 NULL,'<h3>干湿分储</h3><p>501 升大容量，一级能效双变频，-20℃ 深冷速冻。</p>',
 4299.00,5299.00,120,320,2810,'台','以旧换新',1,1,0),
(10,'美的 1.5匹 变频冷暖空调 新一级能效','智能语音 自清洁 无风感',202,2,
 '/api/image/ph?t=美的空调&w=600&h=600&c=10',
 '["/api/image/ph?t=Midea-1&w=800&h=800&c=10"]',
 NULL,'<h3>无风感舒适风</h3><p>新一级能效，一键自清洁，APP 远程控制。</p>',
 2699.00,3299.00,340,860,5310,'台','以旧换新,包安装',0,1,0),
(11,'九阳 破壁料理机 家用多功能','静音降噪 12大功能 一键清洗',203,10,
 '/api/image/ph?t=九阳破壁机&w=600&h=600&c=11',
 '["/api/image/ph?t=Joyoung-1&w=800&h=800&c=11"]',
 NULL,'<h3>静音破壁</h3><p>3.5 万转高速电机，八叶精钢刀，冷热双打。</p>',
 699.00,999.00,560,1320,6210,'台','满减',1,0,0),
(12,'李宁 男子运动卫衣 加绒宽松','国潮设计 亲肤保暖 吸湿排汗',301,6,
 '/api/image/ph?t=李宁卫衣&w=600&h=600&c=12',
 '["/api/image/ph?t=LiNing-1&w=800&h=800&c=12"]',
 NULL,'<h3>国潮加绒</h3><p>320g 加绒面料，宽松版型，四季可穿。</p>',
 259.00,399.00,1200,3420,11200,'件','满减,热销',1,1,1),
(13,'南极人 全棉四件套 1.8m 床','100支长绒棉 亲肤裸睡级',601,9,
 '/api/image/ph?t=四件套&w=600&h=600&c=13',
 '["/api/image/ph?t=Bed-1&w=800&h=800&c=13"]',
 NULL,'<h3>100 支长绒棉</h3><p>高密织造，柔软透气，A 类婴幼儿标准。</p>',
 399.00,699.00,880,2140,7320,'套','满减',1,1,0),
(14,'兰蔻 小黑瓶精华肌底液 50ml','修护屏障 紧致淡纹 敏感肌适用',401,7,
 '/api/image/ph?t=兰蔻小黑瓶&w=600&h=600&c=14',
 '["/api/image/ph?t=Lancome-1&w=800&h=800&c=14"]',
 NULL,'<h3>第二代小黑瓶</h3><p>二裂酵母发酵产物溶胞物，稳定肌肤微生态。</p>',
 1080.00,1280.00,420,1680,9420,'瓶','正品保障',1,1,0),
(15,'三只松鼠 每日坚果 750g/30包','六种坚果三种果干 独立小包',501,8,
 '/api/image/ph?t=每日坚果&w=600&h=600&c=15',
 '["/api/image/ph?t=Nuts-1&w=800&h=800&c=15"]',
 NULL,'<h3>每日一包</h3><p>科学配比，锁鲜独立包装，孕妇儿童均可食用。</p>',
 89.90,139.00,2600,8620,21400,'箱','满减,热销',1,1,1),
(16,'安佳 全脂纯牛奶 250ml*24盒','新西兰进口 高钙优质蛋白',503,10,
 '/api/image/ph?t=安佳牛奶&w=600&h=600&c=16',
 '["/api/image/ph?t=Milk-1&w=800&h=800&c=16"]',
 NULL,'<h3>新西兰进口</h3><p>3.6g 优质乳蛋白，120mg 原生高钙。</p>',
 79.00,109.00,3200,5240,13400,'箱','满减',1,1,0),
(17,'小米 米家智能台灯 Pro','无频闪 无极调光 护眼读写',604,2,
 '/api/image/ph?t=米家台灯&w=600&h=600&c=17',
 '["/api/image/ph?t=Lamp-1&w=800&h=800&c=17"]',
 NULL,'<h3>国AA级护眼</h3><p>Ra95 高显色，无蓝光危害，智能感光调节。</p>',
 349.00,399.00,760,1120,4310,'台','满减',1,0,0),
(18,'小米 米家跑步机 家用静音折叠','智能调速 减震折叠 静音电机',702,2,
 '/api/image/ph?t=米家跑步机&w=600&h=600&c=18',
 '["/api/image/ph?t=Treadmill-1&w=800&h=800&c=18"]',
 NULL,'<h3>折叠静音</h3><p>1.25HP 静音电机，可折叠收纳，连接米家 App。</p>',
 1899.00,2499.00,180,420,3120,'台','满减',0,0,1),
(19,'李宁 羽毛球拍 碳素超轻双拍','攻守兼备 附赠球拍包',703,6,
 '/api/image/ph?t=李宁球拍&w=600&h=600&c=19',
 '["/api/image/ph?t=Racket-1&w=800&h=800&c=19"]',
 NULL,'<h3>碳素超轻</h3><p>一体成型碳纤维，拍框抗扭强，附赠 3 球 1 包。</p>',
 169.00,299.00,980,1620,5210,'副','满减',0,1,0),
(20,'飞利浦 电动牙刷 声波震动成人款','5种模式 智能压力感应 30天续航',404,10,
 '/api/image/ph?t=飞利浦牙刷&w=600&h=600&c=20',
 '["/api/image/ph?t=Toothbrush-1&w=800&h=800&c=20"]',
 NULL,'<h3>声波洁净</h3><p>每分钟 31000 次声波震动，压力感应保护牙龈。</p>',
 399.00,599.00,1100,2860,8140,'支','满减,热销',1,1,0);

/* ---------- 商品规格（规格项 + 规格值） ---------- */
INSERT INTO `product_spec` (`id`,`product_id`,`name`,`sort`) VALUES
(1,1,'颜色',1),(2,1,'版本',2),
(3,2,'颜色',1),(4,2,'版本',2),
(5,3,'颜色',1),(6,3,'容量',2),
(7,4,'配置',1),
(8,12,'颜色',1),(9,12,'尺码',2),
(10,13,'颜色',1),(11,13,'尺寸',2),
(12,15,'规格',1);

INSERT INTO `product_spec_value` (`id`,`product_id`,`spec_id`,`value`,`sort`) VALUES
(1,1,1,'雅川青',1),(2,1,1,'曜金黑',2),(3,1,1,'白沙银',3),
(4,1,2,'12GB+256GB',1),(5,1,2,'12GB+512GB',2),(6,1,2,'12GB+1TB',3),
(7,2,3,'岩石青',1),(8,2,3,'黑色',2),(9,2,3,'白色',3),
(10,2,4,'12GB+256GB',1),(11,2,4,'16GB+512GB',2),(12,2,4,'16GB+1TB',3),
(13,3,5,'原色钛金属',1),(14,3,5,'黑色钛金属',2),(15,3,5,'白色钛金属',3),(16,3,5,'蓝色钛金属',4),
(17,3,6,'128GB',1),(18,3,6,'256GB',2),(19,3,6,'512GB',3),(20,3,6,'1TB',4),
(21,4,7,'i9/32G/1TB/RTX4060',1),(22,4,7,'i7/16G/1TB/RTX4060',2),(23,4,7,'i9/32G/2TB/RTX4070',3),
(24,12,8,'藏青',1),(25,12,8,'黑色',2),(26,12,8,'米白',3),
(27,12,9,'M',1),(28,12,9,'L',2),(29,12,9,'XL',3),(30,12,9,'XXL',4),
(31,13,10,'月光白',1),(32,13,10,'雾霾蓝',2),(33,13,10,'豆沙粉',3),
(34,13,11,'1.5m 床',1),(35,13,11,'1.8m 床',2),(36,13,11,'2.0m 床',3),
(37,15,12,'30包/750g',1),(38,15,12,'60包/1500g',2);

/* ---------- 商品 SKU ---------- */
INSERT INTO `product_sku`
(`product_id`,`sku_code`,`spec_json`,`spec_desc`,`price`,`market_price`,`stock`,`sales`,`image`) VALUES
(1,'HW-M60-256-QC','[{"specName":"颜色","value":"雅川青"},{"specName":"版本","value":"12GB+256GB"}]','雅川青 12GB+256GB',6499.00,6999.00,120,860,'/api/image/ph?t=Mate60-青&w=600&h=600&c=1'),
(1,'HW-M60-512-QC','[{"specName":"颜色","value":"雅川青"},{"specName":"版本","value":"12GB+512GB"}]','雅川青 12GB+512GB',6999.00,7499.00,180,1620,'/api/image/ph?t=Mate60-青&w=600&h=600&c=1'),
(1,'HW-M60-1T-QC','[{"specName":"颜色","value":"雅川青"},{"specName":"版本","value":"12GB+1TB"}]','雅川青 12GB+1TB',7999.00,8499.00,60,120,'/api/image/ph?t=Mate60-青&w=600&h=600&c=1'),
(1,'HW-M60-256-BK','[{"specName":"颜色","value":"曜金黑"},{"specName":"版本","value":"12GB+256GB"}]','曜金黑 12GB+256GB',6499.00,6999.00,90,420,'/api/image/ph?t=Mate60-黑&w=600&h=600&c=2'),
(1,'HW-M60-512-BK','[{"specName":"颜色","value":"曜金黑"},{"specName":"版本","value":"12GB+512GB"}]','曜金黑 12GB+512GB',6999.00,7499.00,110,530,'/api/image/ph?t=Mate60-黑&w=600&h=600&c=2'),
(1,'HW-M60-1T-BK','[{"specName":"颜色","value":"曜金黑"},{"specName":"版本","value":"12GB+1TB"}]','曜金黑 12GB+1TB',7999.00,8499.00,20,86,'/api/image/ph?t=Mate60-黑&w=600&h=600&c=2'),
(1,'HW-M60-256-SV','[{"specName":"颜色","value":"白沙银"},{"specName":"版本","value":"12GB+256GB"}]','白沙银 12GB+256GB',6499.00,6999.00,100,210,'/api/image/ph?t=Mate60-银&w=600&h=600&c=3'),
(1,'HW-M60-512-SV','[{"specName":"颜色","value":"白沙银"},{"specName":"版本","value":"12GB+512GB"}]','白沙银 12GB+512GB',6999.00,7499.00,80,124,'/api/image/ph?t=Mate60-银&w=600&h=600&c=3'),
(1,'HW-M60-1T-SV','[{"specName":"颜色","value":"白沙银"},{"specName":"版本","value":"12GB+1TB"}]','白沙银 12GB+1TB',7999.00,8499.00,30,0,'/api/image/ph?t=Mate60-银&w=600&h=600&c=3'),
(2,'MI-14P-256-RK','[{"specName":"颜色","value":"岩石青"},{"specName":"版本","value":"12GB+256GB"}]','岩石青 12GB+256GB',4599.00,4999.00,200,1200,'/api/image/ph?t=Mi14-青&w=600&h=600&c=2'),
(2,'MI-14P-512-RK','[{"specName":"颜色","value":"岩石青"},{"specName":"版本","value":"16GB+512GB"}]','岩石青 16GB+512GB',4999.00,5499.00,220,2100,'/api/image/ph?t=Mi14-青&w=600&h=600&c=2'),
(2,'MI-14P-512-BK','[{"specName":"颜色","value":"黑色"},{"specName":"版本","value":"16GB+512GB"}]','黑色 16GB+512GB',4999.00,5499.00,180,860,'/api/image/ph?t=Mi14-黑&w=600&h=600&c=5'),
(2,'MI-14P-1T-WT','[{"specName":"颜色","value":"白色"},{"specName":"版本","value":"16GB+1TB"}]','白色 16GB+1TB',5499.00,5999.00,60,180,'/api/image/ph?t=Mi14-白&w=600&h=600&c=6'),
(3,'AP-IP15P-128-NA','[{"specName":"颜色","value":"原色钛金属"},{"specName":"容量","value":"128GB"}]','原色钛金属 128GB',7999.00,8499.00,80,320,'/api/image/ph?t=IP15-原色&w=600&h=600&c=3'),
(3,'AP-IP15P-256-NA','[{"specName":"颜色","value":"原色钛金属"},{"specName":"容量","value":"256GB"}]','原色钛金属 256GB',8499.00,8999.00,120,980,'/api/image/ph?t=IP15-原色&w=600&h=600&c=3'),
(3,'AP-IP15P-512-NA','[{"specName":"颜色","value":"原色钛金属"},{"specName":"容量","value":"512GB"}]','原色钛金属 512GB',9999.00,10499.00,40,120,'/api/image/ph?t=IP15-原色&w=600&h=600&c=3'),
(3,'AP-IP15P-256-BK','[{"specName":"颜色","value":"黑色钛金属"},{"specName":"容量","value":"256GB"}]','黑色钛金属 256GB',8499.00,8999.00,90,420,'/api/image/ph?t=IP15-黑&w=600&h=600&c=4'),
(3,'AP-IP15P-1T-WT','[{"specName":"颜色","value":"白色钛金属"},{"specName":"容量","value":"1TB"}]','白色钛金属 1TB',11499.00,11999.00,10,32,'/api/image/ph?t=IP15-白&w=600&h=600&c=5'),
(4,'LN-Y9000P-I9','[{"specName":"配置","value":"i9/32G/1TB/RTX4060"}]','i9/32G/1TB/RTX4060',9299.00,9999.00,100,520,'/api/image/ph?t=Y9000P-高配&w=600&h=600&c=4'),
(4,'LN-Y9000P-I7','[{"specName":"配置","value":"i7/16G/1TB/RTX4060"}]','i7/16G/1TB/RTX4060',7999.00,8699.00,60,180,'/api/image/ph?t=Y9000P-中配&w=600&h=600&c=4'),
(4,'LN-Y9000P-I9-4070','[{"specName":"配置","value":"i9/32G/2TB/RTX4070"}]','i9/32G/2TB/RTX4070',11499.00,12499.00,20,60,'/api/image/ph?t=Y9000P-顶配&w=600&h=600&c=7'),
(12,'LN-HOOD-M-NV','[{"specName":"颜色","value":"藏青"},{"specName":"尺码","value":"M"}]','藏青 M',259.00,399.00,300,820,'/api/image/ph?t=卫衣-藏青&w=600&h=600&c=12'),
(12,'LN-HOOD-L-NV','[{"specName":"颜色","value":"藏青"},{"specName":"尺码","value":"L"}]','藏青 L',259.00,399.00,300,960,'/api/image/ph?t=卫衣-藏青&w=600&h=600&c=12'),
(12,'LN-HOOD-XL-NV','[{"specName":"颜色","value":"藏青"},{"specName":"尺码","value":"XL"}]','藏青 XL',259.00,399.00,200,540,'/api/image/ph?t=卫衣-藏青&w=600&h=600&c=12'),
(12,'LN-HOOD-L-BK','[{"specName":"颜色","value":"黑色"},{"specName":"尺码","value":"L"}]','黑色 L',259.00,399.00,260,680,'/api/image/ph?t=卫衣-黑&w=600&h=600&c=13'),
(12,'LN-HOOD-XXL-WT','[{"specName":"颜色","value":"米白"},{"specName":"尺码","value":"XXL"}]','米白 XXL',279.00,419.00,140,420,'/api/image/ph?t=卫衣-米白&w=600&h=600&c=14'),
(13,'NJR-BED15-WH','[{"specName":"颜色","value":"月光白"},{"specName":"尺寸","value":"1.5m 床"}]','月光白 1.5m 床',399.00,699.00,200,620,'/api/image/ph?t=四件套-白&w=600&h=600&c=13'),
(13,'NJR-BED18-WH','[{"specName":"颜色","value":"月光白"},{"specName":"尺寸","value":"1.8m 床"}]','月光白 1.8m 床',429.00,729.00,240,880,'/api/image/ph?t=四件套-白&w=600&h=600&c=13'),
(13,'NJR-BED18-BL','[{"specName":"颜色","value":"雾霾蓝"},{"specName":"尺寸","value":"1.8m 床"}]','雾霾蓝 1.8m 床',429.00,729.00,180,420,'/api/image/ph?t=四件套-蓝&w=600&h=600&c=15'),
(13,'NJR-BED20-PK','[{"specName":"颜色","value":"豆沙粉"},{"specName":"尺寸","value":"2.0m 床"}]','豆沙粉 2.0m 床',459.00,769.00,120,220,'/api/image/ph?t=四件套-粉&w=600&h=600&c=16'),
(15,'SSS-NUT-30','[{"specName":"规格","value":"30包/750g"}]','30包/750g',89.90,139.00,1600,5200,'/api/image/ph?t=坚果-30包&w=600&h=600&c=15'),
(15,'SSS-NUT-60','[{"specName":"规格","value":"60包/1500g"}]','60包/1500g',169.00,259.00,1000,3420,'/api/image/ph?t=坚果-60包&w=600&h=600&c=15');

/* 单规格商品补齐默认 SKU */
INSERT INTO `product_sku` (`product_id`,`sku_code`,`spec_json`,`spec_desc`,`price`,`market_price`,`stock`,`sales`,`image`)
SELECT p.id, CONCAT('DEF-', p.id), '[]', '默认规格', p.price, p.market_price, p.stock, p.sales, p.main_image
FROM `product` p
WHERE p.id NOT IN (1,2,3,4,12,13,15);

/* ---------- 商品参数 ---------- */
INSERT INTO `product_attr` (`product_id`,`group_name`,`name`,`value`,`sort`) VALUES
(1,'主体','品牌','华为',1),(1,'主体','型号','Mate 60 Pro',2),(1,'主体','上市年份','2023年',3),
(1,'屏幕','屏幕尺寸','6.82英寸',1),(1,'屏幕','分辨率','FHD+ 2720 × 1260 像素',2),(1,'屏幕','刷新率','1-120Hz LTPO 自适应',3),
(1,'性能','处理器','麒麟 9000S',1),(1,'性能','运行内存','12GB',2),(1,'性能','机身存储','256GB / 512GB / 1TB',3),
(1,'影像','后置摄像头','5000万超光变+1200万超广角+4800万长焦微距',1),(1,'影像','前置摄像头','1300万像素超广角',2),
(1,'电池','电池容量','5000mAh',1),(1,'电池','充电功率','88W 有线 / 50W 无线',2),
(2,'主体','品牌','小米',1),(2,'主体','型号','小米 14 Pro',2),
(2,'屏幕','屏幕尺寸','6.73英寸',1),(2,'屏幕','分辨率','2K 3200 × 1440',2),
(2,'性能','处理器','第三代骁龙 8',1),(2,'性能','运行内存','12GB / 16GB',2),
(2,'影像','后置摄像头','徕卡光学 Summilux 镜头 5000万×3',1),
(3,'主体','品牌','Apple',1),(3,'主体','型号','iPhone 15 Pro',2),
(3,'屏幕','屏幕尺寸','6.1英寸',1),(3,'性能','处理器','A17 Pro 芯片',1),
(3,'影像','后置摄像头','4800万主摄 + 1200万超广角 + 1200万长焦',1),
(4,'主体','品牌','联想',1),(4,'主体','型号','拯救者 Y9000P 2024',2),
(4,'屏幕','屏幕尺寸','16英寸',1),(4,'屏幕','刷新率','240Hz',2),
(4,'性能','处理器','Intel Core i9-14900HX',1),(4,'性能','显卡','NVIDIA RTX 4060 8GB',2),
(12,'主体','品牌','李宁',1),(12,'面料','面料成分','棉 80% 聚酯纤维 20%',1),(12,'版型','版型','宽松',1),
(13,'主体','品牌','南极人',1),(13,'面料','面料成分','100% 长绒棉',1),(13,'规格','件数','四件套（被套*1 床单*1 枕套*2）',1),
(14,'主体','品牌','兰蔻',1),(14,'规格','净含量','50ml',1),(14,'功效','主要功效','修护 紧致 淡纹',1),
(15,'主体','品牌','三只松鼠',1),(15,'规格','净含量','750g / 1500g',1),(15,'保质期','保质期','180天',1);

/* ---------- 轮播图 / 广告位 ---------- */
INSERT INTO `home_banner` (`id`,`title`,`image`,`link`,`position`,`sort`) VALUES
(1,'华为 Mate 60 Pro 重磅开售','/api/image/ph?t=华为Mate60Pro开售&w=1200&h=420&c=1','/product/1','carousel',1),
(2,'小米 14 Pro 徕卡影像','/api/image/ph?t=小米14Pro徕卡影像&w=1200&h=420&c=2','/product/2','carousel',2),
(3,'iPhone 15 Pro 钛金属','/api/image/ph?t=iPhone15Pro钛金属&w=1200&h=420&c=3','/product/3','carousel',3),
(4,'戴森无绳吸尘器 立减500','/api/image/ph?t=戴森立减500&w=1200&h=420&c=8','/product/8','carousel',4),
(5,'三只松鼠 每日坚果 满199减60','/api/image/ph?t=坚果满199减60&w=1200&h=420&c=15','/product/15','carousel',5),
(6,'新人专享 · 首单立减20元','/api/image/ph?t=新人首单立减&w=600&h=300&c=6','/coupon','ad_home',1),
(7,'家电以旧换新 最高补贴15%','/api/image/ph?t=家电以旧换新&w=600&h=300&c=9','/category/2','ad_home',2),
(8,'服饰满399减120','/api/image/ph?t=服饰满399减120&w=600&h=300&c=12','/category/3','ad_home',3),
(9,'数码专场 · 分期免息','/api/image/ph?t=数码分期免息&w=400&h=600&c=4','/activity/1','ad_side',1);

/* ---------- 公告 ---------- */
INSERT INTO `announcement` (`id`,`title`,`content`,`type`,`is_top`,`sort`) VALUES
(1,'【重要】阿帕茨商城 9 月大促活动规则说明','9 月 13 日 00:00 - 9 月 20 日 24:00 全场满 299 减 50，可与优惠券叠加使用，单个订单最多叠加 2 张。','notice',1,1),
(2,'关于国庆节期间物流发货时效的公告','受国庆假期影响，10 月 1 日至 10 月 3 日期间的订单将于 10 月 4 日起陆续发货，给您带来的不便敬请谅解。','notice',0,2),
(3,'平台新增七天无理由退货服务','自即日起，除特殊商品外，平台所有商品支持签收后 7 天内无理由退货。','notice',0,3),
(4,'积分商城上新：米家保温杯 500ml 仅需 1200 积分','积分商城本周上新，米家保温杯 500ml 限量 500 件，先到先得。','activity',0,4),
(5,'谨防冒充电商客服的诈骗电话','平台不会以任何理由索要您的银行卡、验证码信息。如遇可疑来电请通过在线客服核实。','notice',1,5),
(6,'会员日：每月 8 日签到双倍积分','每月 8 日签到可得双倍积分，连续签到 7 天额外赠送 100 积分。','activity',0,6);

/* ---------- 活动专区 ---------- */
INSERT INTO `activity` (`id`,`title`,`subtitle`,`image`,`type`,`link`,`start_time`,`end_time`,`sort`) VALUES
(1,'数码狂欢节','爆款直降 至高立省 1000 元','/api/image/ph?t=数码狂欢节&w=600&h=340&c=1','promotion','/promotion/1','2026-09-01 00:00:00','2026-09-30 23:59:59',1),
(2,'家电焕新季','以旧换新 最高补贴 15%','/api/image/ph?t=家电焕新季&w=600&h=340&c=9','promotion','/promotion/2','2026-09-01 00:00:00','2026-10-15 23:59:59',2),
(3,'服饰秋季新品','国潮风起 满 399 减 120','/api/image/ph?t=服饰秋季新品&w=600&h=340&c=12','season','/promotion/3','2026-09-01 00:00:00','2026-10-31 23:59:59',3),
(4,'零食囤货节','满 199 减 60 包邮到家','/api/image/ph?t=零食囤货节&w=600&h=340&c=15','promotion','/promotion/4','2026-09-05 00:00:00','2026-09-25 23:59:59',4),
(5,'每日 10 点 限时秒杀','爆款好物 低至 5 折','/api/image/ph?t=限时秒杀&w=600&h=340&c=5','seckill','/feature/seckill','2026-09-13 00:00:00','2026-09-30 23:59:59',5),
(6,'美妆大牌日','正品保障 满 1000 减 200','/api/image/ph?t=美妆大牌日&w=600&h=340&c=14','promotion','/promotion/6','2026-09-10 00:00:00','2026-09-30 23:59:59',6),
(7,'新人专区','新人首单立减 20 元','/api/image/ph?t=新人专区&w=600&h=340&c=6','promotion','/coupon','2026-01-01 00:00:00','2026-12-31 23:59:59',7),
(8,'运动户外季','装备焕新 满 299 减 80','/api/image/ph?t=运动户外季&w=600&h=340&c=19','season','/promotion/8','2026-09-01 00:00:00','2026-10-20 23:59:59',8);

/* ---------- 专题 ---------- */
INSERT INTO `topic` (`id`,`title`,`subtitle`,`cover`,`content`,`product_ids`,`views`,`sort`) VALUES
(1,'国产旗舰手机横评','华为 / 小米 / 荣耀 谁才是 2026 年度机皇','/api/image/ph?t=国产旗舰横评&w=900&h=400&c=1',
 '<h3>写在前面</h3><p>2026 年国产旗舰在影像、续航、AI 能力上全面进阶。本文从性能、影像、续航、系统四个维度横向对比。</p><h3>影像对比</h3><p>华为 Mate 60 Pro 的可变光圈与小米 14 Pro 的徕卡色彩各有所长……</p><h3>购买建议</h3><p>追求影像与商务可选华为，追求性价比与性能可选小米。</p>',
 '1,2,3',8620,1),
(2,'小户型家电选购指南','20 件提升幸福感的实用家电','/api/image/ph?t=小户型家电指南&w=900&h=400&c=9',
 '<h3>前言</h3><p>小户型空间有限，家电选购要兼顾功能与占地。本文精选 20 件高性价比家电。</p><h3>厨房篇</h3><p>破壁机、空气炸锅、电饭煲是提升效率的三件套。</p>',
 '8,9,10,11',3420,2),
(3,'秋冬穿搭灵感合集','一衣多穿 通勤休闲都好看','/api/image/ph?t=秋冬穿搭灵感&w=900&h=400&c=12',
 '<h3>基础款的力量</h3><p>一件加绒卫衣可以搭配出通勤、休闲、运动三种风格。</p>',
 '12,13',2140,3),
(4,'每日坚果怎么挑','配料表里的小秘密','/api/image/ph?t=坚果怎么挑&w=900&h=400&c=15',
 '<h3>看配料表</h3><p>优先选择无添加糖、无盐焗的坚果，注意坚果与果干的比例。</p>',
 '15,16',5210,4);

/* ---------- 热门搜索 ---------- */
INSERT INTO `search_hot` (`keyword`,`sort`,`is_hot`) VALUES
('华为 Mate 60 Pro',1,1),('iPhone 15',2,1),('小米 14',3,1),('笔记本电脑',4,1),
('无线耳机',5,1),('保温杯',6,0),('空气炸锅',7,0),('跑步鞋',8,0),
('面霜推荐',9,0),('坚果礼盒',10,0);

/* ---------- 优惠券 ---------- */
INSERT INTO `coupon` (`id`,`name`,`type`,`amount`,`discount`,`min_amount`,`total`,`received`,`per_limit`,`description`,`start_time`,`end_time`) VALUES
(1,'新人专享券',3,20.00,10.00,0.00,10000,3200,1,'无门槛立减 20 元','2026-01-01 00:00:00','2026-12-31 23:59:59'),
(2,'全场满 299 减 50',1,50.00,10.00,299.00,5000,1860,1,'全场通用，特价商品除外','2026-09-01 00:00:00','2026-09-30 23:59:59'),
(3,'家电专享 满 1000 减 150',1,150.00,10.00,1000.00,2000,760,1,'仅限家用电器分类使用','2026-09-01 00:00:00','2026-10-15 23:59:59'),
(4,'数码 9 折券',2,0.00,9.00,500.00,3000,1120,2,'数码电子分类 9 折，最高减 300 元','2026-09-01 00:00:00','2026-09-30 23:59:59'),
(5,'服饰满 399 减 120',1,120.00,10.00,399.00,4000,2200,1,'服饰鞋包分类可用','2026-09-01 00:00:00','2026-10-31 23:59:59'),
(6,'零食满 199 减 60',1,60.00,10.00,199.00,6000,3100,1,'食品生鲜分类可用','2026-09-05 00:00:00','2026-09-25 23:59:59'),
(7,'美妆满 1000 减 200',1,200.00,10.00,1000.00,1500,520,1,'美妆护肤分类可用','2026-09-10 00:00:00','2026-09-30 23:59:59'),
(8,'满 599 减 100',1,100.00,10.00,599.00,5000,980,1,'全场通用，大促专享','2026-09-13 00:00:00','2026-09-20 23:59:59');

INSERT INTO `user_coupon` (`user_id`,`coupon_id`,`status`,`expire_time`) VALUES
(1,1,0,'2026-12-31 23:59:59'),
(1,2,0,'2026-09-30 23:59:59'),
(1,3,0,'2026-10-15 23:59:59'),
(1,4,0,'2026-09-30 23:59:59'),
(1,5,2,'2026-08-31 23:59:59'),
(2,1,0,'2026-12-31 23:59:59');

/* ---------- 积分 ---------- */
INSERT INTO `points_goods` (`id`,`name`,`image`,`points`,`market_price`,`stock`,`exchanged`,`description`,`sort`) VALUES
(1,'米家保温杯 500ml','/api/image/ph?t=米家保温杯&w=400&h=400&c=2',1200,99.00,500,120,'316L 不锈钢内胆，保温 12 小时',1),
(2,'小米移动电源 10000mAh','/api/image/ph?t=小米充电宝&w=400&h=400&c=3',1800,129.00,300,86,'双向快充，轻薄便携',2),
(3,'阿帕茨定制帆布袋','/api/image/ph?t=帆布袋&w=400&h=400&c=4',800,39.00,1000,320,'环保加厚帆布，大容量',3),
(4,'不锈钢焖烧杯 800ml','/api/image/ph?t=焖烧杯&w=400&h=400&c=5',2600,199.00,200,42,'长效保温，适合上班族带饭',4),
(5,'无线蓝牙耳机','/api/image/ph?t=蓝牙耳机&w=400&h=400&c=6',5000,299.00,100,18,'半入耳设计，30 小时续航',5),
(6,'阿帕茨定制鼠标垫','/api/image/ph?t=鼠标垫&w=400&h=400&c=7',500,29.00,2000,760,'加厚防滑，超大尺寸',6);

INSERT INTO `points_record` (`user_id`,`points`,`type`,`description`,`create_time`) VALUES
(1,100,'sign','连续签到 1 天','2026-09-06 09:12:00'),
(1,100,'sign','连续签到 2 天','2026-09-07 08:55:00'),
(1,200,'sign','连续签到 3 天','2026-09-08 09:03:00'),
(1,860,'order','订单 20260901000001 消费返积分','2026-09-01 14:20:00'),
(1,-1200,'exchange','兑换 米家保温杯 500ml','2026-09-05 16:40:00'),
(1,800,'sign','连续签到 7 天额外奖励','2026-09-12 09:20:00');

INSERT INTO `sign_record` (`user_id`,`sign_date`,`continuous_days`,`points`) VALUES
(1,'2026-09-06',1,100),(1,'2026-09-07',2,100),(1,'2026-09-08',3,200),
(1,'2026-09-09',4,100),(1,'2026-09-10',5,100),(1,'2026-09-11',6,100),
(1,'2026-09-12',7,800);

INSERT INTO `points_exchange` (`user_id`,`points_goods_id`,`goods_name`,`image`,`points`,`status`,`create_time`) VALUES
(1,1,'米家保温杯 500ml','/api/image/ph?t=米家保温杯&w=400&h=400&c=2',1200,1,'2026-09-05 16:40:00'),
(1,3,'阿帕茨定制帆布袋','/api/image/ph?t=帆布袋&w=400&h=400&c=4',800,2,'2026-08-20 10:12:00');

/* ---------- 秒杀 ---------- */
INSERT INTO `seckill_activity` (`product_id`,`title`,`seckill_price`,`stock`,`sold`,`limit_count`,`start_time`,`end_time`) VALUES
(15,'每日坚果 30 包 限时秒杀',59.90,200,168,2,'2026-09-13 10:00:00','2026-09-13 12:00:00'),
(11,'九阳破壁机 限时秒杀',499.00,100,72,1,'2026-09-13 10:00:00','2026-09-13 12:00:00'),
(6,'索尼降噪耳机 限时秒杀',1699.00,50,31,1,'2026-09-13 10:00:00','2026-09-13 12:00:00'),
(20,'飞利浦电动牙刷 限时秒杀',299.00,150,96,2,'2026-09-13 14:00:00','2026-09-13 16:00:00');

/* ---------- 购物车 ---------- */
INSERT INTO `cart_item` (`user_id`,`product_id`,`sku_id`,`quantity`,`selected`) VALUES
(1,1,(SELECT id FROM product_sku WHERE sku_code='HW-M60-512-QC'),1,1),
(1,15,(SELECT id FROM product_sku WHERE sku_code='SSS-NUT-30'),2,1),
(1,12,(SELECT id FROM product_sku WHERE sku_code='LN-HOOD-L-NV'),1,0),
(2,3,(SELECT id FROM product_sku WHERE sku_code='AP-IP15P-256-NA'),1,1);

/* ---------- 收藏 / 足迹 ---------- */
INSERT INTO `favorite` (`user_id`,`product_id`,`create_time`) VALUES
(1,1,'2026-09-10 10:20:00'),(1,3,'2026-09-11 15:31:00'),(1,6,'2026-09-12 20:12:00'),
(2,2,'2026-09-12 09:10:00');

INSERT INTO `footprint` (`user_id`,`product_id`,`create_time`) VALUES
(1,1,'2026-09-13 09:40:00'),(1,15,'2026-09-13 09:12:00'),(1,12,'2026-09-12 21:30:00'),
(1,8,'2026-09-12 18:02:00'),(1,14,'2026-09-11 12:22:00'),(1,3,'2026-09-11 09:05:00'),
(2,2,'2026-09-13 08:41:00'),(2,16,'2026-09-12 19:00:00');

/* ---------- 订单 ---------- */
INSERT INTO `shop_order`
(`id`,`order_no`,`user_id`,`total_amount`,`freight`,`discount_amount`,`pay_amount`,`pay_type`,`pay_status`,`order_status`,`delivery_type`,`address_id`,`receiver`,`receiver_phone`,`receiver_addr`,`remark`,`create_time`,`pay_time`,`delivery_time`,`receive_time`,`finish_time`) VALUES
(1,'20260901000001',1,6999.00,0.00,50.00,6949.00,1,1,4,1,1,'张三','13800138000','广东省 深圳市 南山区 科技园南区深南大道9988号阿帕茨大厦A座1801',NULL,'2026-09-01 14:08:00','2026-09-01 14:20:00','2026-09-02 10:00:00','2026-09-04 09:30:00','2026-09-04 09:30:00'),
(2,'20260910000002',1,259.00,0.00,0.00,259.00,4,1,3,1,1,'张三','13800138000','广东省 深圳市 南山区 科技园南区深南大道9988号阿帕茨大厦A座1801','请尽快发货','2026-09-10 20:15:00','2026-09-10 20:16:00','2026-09-11 09:40:00','2026-09-12 18:20:00',NULL),
(3,'20260912000003',1,169.00,12.00,0.00,181.00,NULL,0,0,1,2,'张三','13800138000','广东省 广州市 天河区 珠江新城华夏路16号富力中心2503',NULL,'2026-09-12 21:45:00',NULL,NULL,NULL,NULL),
(4,'20260913000004',1,89.90,0.00,60.00,29.90,4,1,2,1,1,'张三','13800138000','广东省 深圳市 南山区 科技园南区深南大道9988号阿帕茨大厦A座1801',NULL,'2026-09-13 09:02:00','2026-09-13 09:03:00','2026-09-13 11:20:00',NULL,NULL),
(5,'20260913000005',1,429.00,0.00,0.00,429.00,NULL,0,5,1,1,'张三','13800138000','广东省 深圳市 南山区 科技园南区深南大道9988号阿帕茨大厦A座1801',NULL,'2026-09-13 09:30:00',NULL,NULL,NULL,NULL),
(6,'20260905000006',2,4999.00,0.00,0.00,4999.00,2,1,1,1,3,'李四','13900139000','浙江省 杭州市 西湖区 文三路508号天苑花园3幢602',NULL,'2026-09-05 11:20:00','2026-09-05 11:22:00',NULL,NULL,NULL);

UPDATE `shop_order` SET `cancel_reason`='不想要了', `cancel_time`='2026-09-13 09:45:00' WHERE `id`=5;

INSERT INTO `order_item`
(`order_id`,`order_no`,`product_id`,`sku_id`,`product_name`,`sku_desc`,`image`,`price`,`quantity`,`total_price`,`review_status`) VALUES
(1,'20260901000001',1,(SELECT id FROM product_sku WHERE sku_code='HW-M60-512-QC'),'华为 Mate 60 Pro 12GB+512GB 雅川青','雅川青 12GB+512GB','/api/image/ph?t=Mate60-青&w=600&h=600&c=1',6999.00,1,6999.00,1),
(2,'20260910000002',12,(SELECT id FROM product_sku WHERE sku_code='LN-HOOD-L-NV'),'李宁 男子运动卫衣 加绒宽松','藏青 L','/api/image/ph?t=卫衣-藏青&w=600&h=600&c=12',259.00,1,259.00,0),
(3,'20260912000003',15,(SELECT id FROM product_sku WHERE sku_code='SSS-NUT-60'),'三只松鼠 每日坚果 750g/30包','60包/1500g','/api/image/ph?t=坚果-60包&w=600&h=600&c=15',169.00,1,169.00,0),
(4,'20260913000004',15,(SELECT id FROM product_sku WHERE sku_code='SSS-NUT-30'),'三只松鼠 每日坚果 750g/30包','30包/750g','/api/image/ph?t=坚果-30包&w=600&h=600&c=15',89.90,1,89.90,0),
(4,'20260913000004',16,(SELECT id FROM product_sku WHERE sku_code='DEF-16'),'安佳 全脂纯牛奶 250ml*24盒','默认规格','/api/image/ph?t=安佳牛奶&w=600&h=600&c=16',79.00,1,79.00,0),
(5,'20260913000005',13,(SELECT id FROM product_sku WHERE sku_code='NJR-BED18-WH'),'南极人 全棉四件套 1.8m 床','月光白 1.8m 床','/api/image/ph?t=四件套-白&w=600&h=600&c=13',429.00,1,429.00,0),
(6,'20260905000006',2,(SELECT id FROM product_sku WHERE sku_code='MI-14P-512-RK'),'小米 14 Pro 16GB+512GB 岩石青','岩石青 16GB+512GB','/api/image/ph?t=Mi14-青&w=600&h=600&c=2',4999.00,1,4999.00,0);

INSERT INTO `order_logistics`
(`order_id`,`order_no`,`company`,`company_code`,`tracking_no`,`receiver_name`,`receiver_phone`,`receiver_addr`,`status`) VALUES
(1,'20260901000001','顺丰速运','SF','SF1234567890123','张三','13800138000','广东省 深圳市 南山区 科技园南区深南大道9988号阿帕茨大厦A座1801',4),
(2,'20260910000002','圆通速递','YTO','YT9876543210987','张三','13800138000','广东省 深圳市 南山区 科技园南区深南大道9988号阿帕茨大厦A座1801',4),
(4,'20260913000004','中通快递','ZTO','ZT5566778899001','张三','13800138000','广东省 深圳市 南山区 科技园南区深南大道9988号阿帕茨大厦A座1801',3),
(6,'20260905000006','京东物流','JD','JD1122334455667','李四','13900139000','浙江省 杭州市 西湖区 文三路508号天苑花园3幢602',1);

INSERT INTO `logistics_trace` (`order_id`,`trace_time`,`context`,`status`) VALUES
(1,'2026-09-02 10:00:00','【深圳市】您的订单已从 深圳南山仓 发出',1),
(1,'2026-09-02 16:30:00','【深圳市】快件已到达 深圳集散中心',2),
(1,'2026-09-03 09:15:00','【深圳市】快件已到达 南山科技园营业点',2),
(1,'2026-09-04 08:40:00','【深圳市】快递员 王师傅（138****8888）正在派送，请保持电话畅通',3),
(1,'2026-09-04 09:30:00','【深圳市】您的快递已签收，感谢使用顺丰速运',4),
(2,'2026-09-11 09:40:00','【深圳市】您的订单已从 深圳龙华仓 发出',1),
(2,'2026-09-11 18:20:00','【深圳市】快件已到达 深圳转运中心',2),
(2,'2026-09-12 10:05:00','【深圳市】快递员 李师傅（139****9999）正在派送',3),
(2,'2026-09-12 18:20:00','【深圳市】您的快递已签收，感谢使用圆通速递',4),
(4,'2026-09-13 11:20:00','【广州市】您的订单已从 广州白云仓 发出',1),
(4,'2026-09-13 15:40:00','【广州市】快件已到达 广州天河营业点',2),
(4,'2026-09-13 17:10:00','【广州市】快递员 陈师傅（137****7777）正在派送，请保持电话畅通',3),
(6,'2026-09-05 13:10:00','【杭州市】您的订单已从 杭州西湖仓 发出',1);

/* ---------- 评价 ---------- */
INSERT INTO `order_comment`
(`order_id`,`order_item_id`,`product_id`,`user_id`,`sku_desc`,`rating`,`content`,`images`,`is_anonymous`,`reply`,`create_time`) VALUES
(1,(SELECT id FROM order_item WHERE order_no='20260901000001' LIMIT 1),1,1,'雅川青 12GB+512GB',5,'手机手感非常好，昆仑玻璃很扎实，卫星通话在山区实测可用。拍照素质比上一代提升明显，夜景很干净。','["/api/image/ph?t=晒图1&w=400&h=400&c=1","/api/image/ph?t=晒图2&w=400&h=400&c=3"]',0,'感谢您的支持，祝您使用愉快！','2026-09-04 12:10:00'),
(1,NULL,3,2,'原色钛金属 256GB',4,'钛金属边框确实轻了不少，A17 Pro 打游戏很流畅。就是价格有点贵，希望后续能多搞活动。','[]',1,NULL,'2026-09-06 20:30:00');

/* ---------- 售后 ---------- */
INSERT INTO `after_sale`
(`id`,`service_no`,`order_id`,`order_no`,`order_item_id`,`user_id`,`product_name`,`image`,`type`,`reason`,`description`,`refund_amount`,`status`,`handle_note`,`create_time`,`handle_time`,`finish_time`) VALUES
(1,'AS20260906000001',1,'20260901000001',(SELECT id FROM order_item WHERE order_no='20260901000001' LIMIT 1),1,'华为 Mate 60 Pro 12GB+512GB 雅川青','/api/image/ph?t=Mate60-青&w=600&h=600&c=1',3,'收到商品与描述不符','屏幕左上角有一处细微划痕，希望换货处理。',0.00,4,'已为您寄出换货商品，运单号 SF7788990011223','2026-09-06 10:20:00','2026-09-06 14:00:00','2026-09-09 11:00:00');

INSERT INTO `after_sale_progress` (`after_sale_id`,`title`,`content`,`operator`,`create_time`) VALUES
(1,'提交申请','用户提交换货申请，原因：收到商品与描述不符','用户','2026-09-06 10:20:00'),
(1,'平台受理','客服已受理，正在审核中','客服小帕','2026-09-06 11:00:00'),
(1,'审核通过','审核通过，请将商品寄回指定地址','客服小帕','2026-09-06 14:00:00'),
(1,'用户寄回','用户已寄回商品，运单号 SF7788990011223','用户','2026-09-07 09:30:00'),
(1,'已换新','平台已寄出换货商品，运单号 SF7788990011223','客服小帕','2026-09-09 11:00:00');

/* ---------- 消息 ---------- */
INSERT INTO `message` (`user_id`,`title`,`content`,`type`,`link`,`is_read`,`create_time`) VALUES
(1,'订单已签收','您的订单 20260901000001 已签收，快去评价赚积分吧！','order','/order/1',0,'2026-09-04 09:31:00'),
(1,'优惠券到账','恭喜您获得「全场满 299 减 50」优惠券，9 月 30 日前有效。','activity','/user/coupons',0,'2026-09-01 00:05:00'),
(1,'签到提醒','您已连续签到 7 天，今日签到可获得 100 积分。','system','/sign',1,'2026-09-13 08:00:00'),
(1,'系统通知','阿帕茨商城 9 月大促开始，全场满 299 减 50，可与优惠券叠加。','system','/coupon',0,'2026-09-13 00:00:00'),
(1,'换货进度更新','您的售后单 AS20260906000001 已寄出换货商品。','order','/user/aftersale',1,'2026-09-09 11:01:00'),
(2,'欢迎加入阿帕茨','欢迎加入阿帕茨商城，新人专享 20 元无门槛券已放入您的账户。','system','/user/coupons',0,'2026-09-05 10:00:00');

/* ---------- 帮助中心 ---------- */
INSERT INTO `help_article` (`category`,`question`,`answer`,`sort`) VALUES
('常见问题','如何申请退款？','在「我的订单」中找到对应订单，点击「申请售后」选择退货或退款，填写原因并提交即可。平台将在 24 小时内审核。',1),
('常见问题','优惠券为什么不能使用？','优惠券有使用门槛、适用分类和有效期限制，请检查订单金额是否达到门槛、商品是否在适用分类范围内。',2),
('常见问题','多久可以发货？','现货商品在您付款后 24 小时内发出；预售商品以商品详情页标注的发货时间为准。',3),
('常见问题','如何修改收货地址？','订单提交前可在结算页修改或新增地址；订单提交后如需修改，请联系客服协助处理。',4),
('常见问题','运费怎么计算？','全场满 99 元包邮（新疆、西藏等偏远地区除外），未满 99 元收取 12 元运费。',5),
('配送与物流','物流信息多久更新一次？','物流信息由快递公司同步，一般在揽收后 2-4 小时更新一次，偏远地区可能延迟。',6),
('配送与物流','支持指定时间配送吗？','部分地区支持预约配送，具体以订单结算页展示的可选时间为准。',7),
('售后服务','七天无理由退货的条件是什么？','商品签收后 7 天内，商品及包装完好、不影响二次销售、附件齐全即可申请无理由退货。',8),
('售后服务','换货需要多久？','换货申请审核通过并收到您寄回的商品后，平台将在 3 个工作日内寄出换货商品。',9),
('账户与安全','忘记密码怎么办？','当前版本可通过客服协助重置密码，后续将开放短信验证码自助重置。',10);

/* ==========================================================================
 *  数据校验
 * ========================================================================== */
SELECT '用户' AS 表, COUNT(*) AS 数量 FROM sys_user
UNION ALL SELECT '分类', COUNT(*) FROM product_category
UNION ALL SELECT '品牌', COUNT(*) FROM product_brand
UNION ALL SELECT '商品SPU', COUNT(*) FROM product
UNION ALL SELECT '商品SKU', COUNT(*) FROM product_sku
UNION ALL SELECT '商品参数', COUNT(*) FROM product_attr
UNION ALL SELECT '订单', COUNT(*) FROM shop_order
UNION ALL SELECT '订单明细', COUNT(*) FROM order_item
UNION ALL SELECT '物流轨迹', COUNT(*) FROM logistics_trace
UNION ALL SELECT '优惠券', COUNT(*) FROM coupon
UNION ALL SELECT '积分商品', COUNT(*) FROM points_goods
UNION ALL SELECT '帮助文章', COUNT(*) FROM help_article;
