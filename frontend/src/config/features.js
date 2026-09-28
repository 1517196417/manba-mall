/**
 * 需求中标注「!」的功能（只做功能说明页，不真正开发）
 *
 * 统一约定：访问 /feature/:key 会渲染 Feature.vue，
 * 页面展示「此为 XXX 功能」+ 功能定位说明 + 相关业务入口。
 */
export const FEATURES = {
  /* ---------------- 购物车 ---------------- */
  'cart-invalid': {
    name: '失效商品处理',
    group: '购物车',
    icon: 'WarningFilled',
    desc: '自动识别已下架、已售罄或规格失效的商品，支持「一键清理」把它们从购物车移除，避免影响正常结算。'
  },
  'cart-move-favorite': {
    name: '移入收藏',
    group: '购物车',
    icon: 'Star',
    desc: '把暂时不想买、但又不舍得删掉的购物车商品一键移入收藏夹，之后可在「我的收藏」中随时找回。'
  },

  /* ---------------- 订单与结算 ---------------- */
  'pickup-point': {
    name: '自提点',
    group: '订单与结算',
    icon: 'OfficeBuilding',
    desc: '结算时选择离自己最近的门店或合作自提点，商品到达后凭取货码自提，省下运费也更快拿到手。'
  },
  'balance-deduct': {
    name: '余额抵扣',
    group: '订单与结算',
    icon: 'Wallet',
    desc: '结算时可用账户余额直接抵扣订单金额；若余额不足还可与其它支付方式组合使用。'
  },
  'invoice': {
    name: '发票信息',
    group: '订单与结算',
    icon: 'Document',
    desc: '提交订单时填写发票抬头与纳税人识别号，支持开具电子普通发票与增值税专用发票。'
  },
  'order-remark': {
    name: '订单备注',
    group: '订单与结算',
    icon: 'EditPen',
    desc: '给商家留言，例如「工作日送货」「不要放丰巢」「需要开发票」等个性化要求。'
  },

  /* ---------------- 支付 ---------------- */
  'pay-wechat': {
    name: '微信支付',
    group: '支付',
    icon: 'ChatDotRound',
    desc: '调用微信支付 SDK 生成预支付订单，用户在微信内完成付款，支付结果通过异步回调通知商城。'
  },
  'pay-alipay': {
    name: '支付宝',
    group: '支付',
    icon: 'Money',
    desc: '调用支付宝手机网站支付/APP 支付接口，支持花呗分期，支付完成后由支付宝异步回调通知。'
  },
  'pay-unionpay': {
    name: '银联支付',
    group: '支付',
    icon: 'CreditCard',
    desc: '接入银联在线支付网关，支持境内外主流银行卡支付。'
  },
  'pay-balance': {
    name: '余额支付',
    group: '支付',
    icon: 'Wallet',
    desc: '使用商城账户余额直接付款，余额来自充值、退款与活动返现。'
  },
  'pay-combined': {
    name: '组合支付',
    group: '支付',
    icon: 'Connection',
    desc: '余额不足时组合多种支付方式，例如「余额 + 微信」共同支付同一笔订单。'
  },
  'pay-callback': {
    name: '支付结果回调',
    group: '支付',
    icon: 'Bell',
    desc: '支付渠道将支付结果异步推送到商城服务端，服务端校验签名后更新订单状态，保证支付状态最终一致。'
  },
  'refund': {
    name: '退款',
    group: '支付',
    icon: 'RefreshLeft',
    desc: '按原支付渠道把订单金额退回给用户，退款到账时间取决于支付渠道（通常 1-7 个工作日）。'
  },
  'refund-partial': {
    name: '部分退款',
    group: '支付',
    icon: 'ScaleToOriginal',
    desc: '多商品订单中仅对部分商品退款，系统按商品实付金额比例分摊优惠券与运费后计算退款金额。'
  },
  'refund-progress': {
    name: '退款进度',
    group: '支付',
    icon: 'Clock',
    desc: '实时展示退款的受理、审核、渠道处理、到账四个阶段，进度可随时查询。'
  },

  /* ---------------- 会员中心 ---------------- */
  'third-login': {
    name: '第三方登录',
    group: '会员中心',
    icon: 'Link',
    desc: '支持微信、QQ、Apple 等第三方账号一键登录，首次登录时自动完成账号绑定。'
  },
  'bind-phone': {
    name: '手机绑定',
    group: '会员中心',
    icon: 'Iphone',
    desc: '绑定手机号用于登录、找回密码与订单通知，绑定时需通过短信验证码校验。'
  },
  'change-password': {
    name: '密码修改',
    group: '会员中心',
    icon: 'Lock',
    desc: '校验原密码后设置新密码；出于安全考虑，修改成功后需要重新登录。'
  },
  'real-name': {
    name: '实名认证',
    group: '会员中心',
    icon: 'Postcard',
    desc: '提交姓名与身份证号完成实名认证，认证后可享受更高额度的支付与开票服务。'
  },
  'member-level': {
    name: '会员等级',
    group: '会员中心',
    icon: 'Medal',
    desc: '按成长值划分为普通、银卡、金卡、白金、钻石五个等级，等级越高专享权益越多。'
  },
  'growth': {
    name: '成长值',
    group: '会员中心',
    icon: 'TrendCharts',
    desc: '购物、签到、评价、晒单等行为都会累积成长值，成长值决定会员等级。'
  },

  /* ---------------- 营销互动 ---------------- */
  'full-reduce': {
    name: '满减',
    group: '营销互动',
    icon: 'Discount',
    desc: '阶梯式满减规则，例如满 299 减 50、满 599 减 100，可与优惠券叠加使用。'
  },
  'discount': {
    name: '折扣',
    group: '营销互动',
    icon: 'PriceTag',
    desc: '对指定商品或分类设置限时折扣，例如数码专场 9 折、美妆两件 8 折。'
  },
  'full-gift': {
    name: '满赠',
    group: '营销互动',
    icon: 'Present',
    desc: '订单满指定金额即赠送赠品，赠品库存独立管理，送完即止。'
  },
  'seckill': {
    name: '秒杀',
    group: '营销互动',
    icon: 'AlarmClock',
    desc: '每日整点开启限时秒杀场次，商品低至 5 折，每人限购固定件数，售完即止。'
  },
  'groupon': {
    name: '拼团',
    group: '营销互动',
    icon: 'UserFilled',
    desc: '开团后邀请好友参团，成团后以团购价发货；未成团自动退款。'
  },
  'bargain': {
    name: '砍价',
    group: '营销互动',
    icon: 'Scissor',
    desc: '发起砍价后邀请好友助力，价格逐次降低，最低可 0 元拿商品。'
  },
  'presale': {
    name: '预售',
    group: '营销互动',
    icon: 'Van',
    desc: '支付定金锁定新品，尾款阶段定金膨胀抵扣，付清后按约定时间发货。'
  },
  'combo': {
    name: '套餐',
    group: '营销互动',
    icon: 'Box',
    desc: '多个商品组合成套餐售卖，套餐价低于单独购买的价格之和。'
  },
  'gift': {
    name: '赠品',
    group: '营销互动',
    icon: 'TakeawayBox',
    desc: '购买指定商品或达到指定数量时，随单赠送配套小样或配件。'
  },
  'lottery': {
    name: '抽奖',
    group: '营销互动',
    icon: 'Trophy',
    desc: '使用积分参与大转盘抽奖，奖品包含优惠券、积分与实物好礼。'
  },
  'invite': {
    name: '邀请有礼',
    group: '营销互动',
    icon: 'Share',
    desc: '邀请好友注册并完成首单，邀请人与被邀请人双方均可得优惠券奖励。'
  },
  'distribution': {
    name: '分销',
    group: '营销互动',
    icon: 'Money',
    desc: '把商品分享出去，好友通过分享链接下单后，分享者可获得一定比例的佣金。'
  },
  'live': {
    name: '直播',
    group: '营销互动',
    icon: 'VideoCamera',
    desc: '直播间边看边买，主播讲解商品并挂载购物袋，观众可实时下单。'
  },
  'video': {
    name: '短视频',
    group: '营销互动',
    icon: 'Film',
    desc: '短视频种草内容流，刷到心仪商品可一键加入购物车。'
  },
  'community': {
    name: '种草社区',
    group: '营销互动',
    icon: 'ChatLineSquare',
    desc: '用户晒单与达人种草内容的社区广场，支持点赞、评论与商品挂链。'
  },

  /* ---------------- 售后与评价 ---------------- */
  'comment-photo': {
    name: '晒图',
    group: '售后与评价',
    icon: 'Camera',
    desc: '评价时上传实拍照片或视频，晒图评价可获得额外积分奖励。'
  },
  'repair': {
    name: '维修',
    group: '售后与评价',
    icon: 'Tools',
    desc: '在保修期内的商品可申请维修服务，平台安排寄修或上门维修。'
  },
  'complaint': {
    name: '投诉',
    group: '售后与评价',
    icon: 'Warning',
    desc: '对商家服务、商品质量或物流体验发起投诉，平台客服介入核实处理。'
  },
  'ticket': {
    name: '工单',
    group: '售后与评价',
    icon: 'Tickets',
    desc: '以工单形式跟踪问题处理进度，包含受理、处理中、已解决、已关闭四种状态。'
  },
  'agent-intervene': {
    name: '客服介入',
    group: '售后与评价',
    icon: 'Service',
    desc: '买卖双方协商未果时可申请平台客服介入，客服根据举证材料给出处理结论。'
  },
  'arbitration': {
    name: '仲裁',
    group: '售后与评价',
    icon: 'Stamp',
    desc: '争议升级后由平台仲裁团队依据规则做出最终裁决，双方需按裁决结果执行。'
  },

  /* ---------------- 客服与消息 ---------------- */
  'online-service': {
    name: '在线客服',
    group: '客服与消息',
    icon: 'Headset',
    desc: '人工客服在线实时答疑，支持发送订单、商品卡片，服务时间 9:00-22:00。'
  },
  'ai-service': {
    name: '智能客服',
    group: '客服与消息',
    icon: 'MagicStick',
    desc: '7×24 小时智能机器人，通过意图识别自动回答常见问题并可转接人工。'
  },
  'help-center': {
    name: '帮助中心',
    group: '客服与消息',
    icon: 'QuestionFilled',
    desc: '按「常见问题 / 配送与物流 / 售后服务 / 账户与安全」分类整理的自助帮助文档。'
  },
  'faq': {
    name: '常见问题',
    group: '客服与消息',
    icon: 'Document',
    desc: '以问答形式呈现高频问题，例如如何申请退款、多久发货、运费如何计算等。'
  },
  'app-push': {
    name: 'APP 推送',
    group: '客服与消息',
    icon: 'Promotion',
    desc: '通过移动端推送通道（APNs / 厂商通道）把订单、物流与活动消息推送到手机通知栏。'
  },

  /* ---------------- 物流配送 ---------------- */
  'store-pickup': {
    name: '门店自提',
    group: '物流配送',
    icon: 'Shop',
    desc: '下单时选择自提门店，商品到店后收到取货通知，凭取货码与手机号到店自提。'
  },
  'city-delivery': {
    name: '同城配送',
    group: '物流配送',
    icon: 'Bicycle',
    desc: '由同城骑手配送，支持小时级送达，适合生鲜、鲜花等对时效要求高的品类。'
  },
  'appointment-delivery': {
    name: '预约配送',
    group: '物流配送',
    icon: 'Calendar',
    desc: '下单时选择期望的配送日期与时间段，适合大件家电等需要在家签收的商品。'
  },
  'delivery-range': {
    name: '配送范围',
    group: '物流配送',
    icon: 'Location',
    desc: '按行政区划或电子围栏维护可配送区域，超出范围的订单会提示暂不支持配送。'
  },
  'freight-template': {
    name: '运费模板',
    group: '物流配送',
    icon: 'Coin',
    desc: '按件数、重量或体积设置阶梯运费，支持指定地区包邮与偏远地区加价规则。'
  }
}

/** 按分组整理，供「功能一览」页面使用 */
export const FEATURE_GROUPS = (() => {
  const groups = {}
  Object.entries(FEATURES).forEach(([key, item]) => {
    if (!groups[item.group]) {
      groups[item.group] = []
    }
    groups[item.group].push({ key, ...item })
  })
  return groups
})()

export function getFeature(key) {
  return FEATURES[key] || null
}

export default FEATURES
