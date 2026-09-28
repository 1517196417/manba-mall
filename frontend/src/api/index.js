import request from './request'

/* ============================== 认证 ============================== */
export const authApi = {
  register: (data) => request.post('/auth/register', data),
  login: (data) => request.post('/auth/login', data),
  logout: () => request.post('/auth/logout'),
  current: () => request.get('/auth/current')
}

/* ============================== 首页 ============================== */
export const homeApi = {
  index: () => request.get('/home/index'),
  banners: (position = 'carousel') => request.get('/home/banners', { params: { position } }),
  notices: (page = 1, size = 10) => request.get('/home/notice/page', { params: { page, size } }),
  noticeDetail: (id) => request.get(`/home/notice/${id}`),
  activities: () => request.get('/home/activity/list'),
  activityDetail: (id) => request.get(`/home/activity/${id}`),
  topics: (limit = 10) => request.get('/home/topic/list', { params: { limit } }),
  topicDetail: (id) => request.get(`/home/topic/${id}`),
  hotKeywords: () => request.get('/home/hot-keywords'),
  seckillList: () => request.get('/home/seckill/list'),
  helpList: (category) => request.get('/home/help/list', { params: { category } })
}

/* ============================== 分类 / 品牌 ============================== */
export const categoryApi = {
  tree: () => request.get('/category/tree'),
  list: () => request.get('/category/list'),
  detail: (id) => request.get(`/category/${id}`),
  breadcrumb: (id) => request.get(`/category/${id}/breadcrumb`)
}

export const brandApi = {
  list: () => request.get('/brand/list'),
  detail: (id) => request.get(`/brand/${id}`)
}

/* ============================== 商品 ============================== */
export const productApi = {
  page: (params) => request.get('/product/page', { params }),
  detail: (id) => request.get(`/product/${id}`),
  skus: (id) => request.get(`/product/${id}/skus`),
  specs: (id) => request.get(`/product/${id}/specs`),
  attrs: (id) => request.get(`/product/${id}/attrs`),
  related: (id) => request.get(`/product/${id}/related`),
  commentStatistics: (id) => request.get(`/product/${id}/comment-statistics`),
  recommend: (limit = 10) => request.get('/product/recommend', { params: { limit } }),
  hot: (limit = 8) => request.get('/product/hot', { params: { limit } }),
  news: (limit = 8) => request.get('/product/new', { params: { limit } })
}

/* ============================== 搜索 ============================== */
export const searchApi = {
  suggest: (keyword) => request.get('/search/suggest', { params: { keyword } }),
  hot: () => request.get('/search/hot'),
  search: (params) => request.get('/search', { params })
}

/* ============================== 购物车 ============================== */
export const cartApi = {
  list: () => request.get('/cart/list'),
  count: () => request.get('/cart/count'),
  summary: () => request.get('/cart/summary'),
  add: (data) => request.post('/cart/add', data),
  update: (data) => request.put('/cart/update', data),
  changeSku: (id, skuId) => request.put(`/cart/${id}/sku`, null, { params: { skuId } }),
  selectAll: (selected = 1) => request.put('/cart/select-all', null, { params: { selected } }),
  remove: (id) => request.delete(`/cart/${id}`),
  clear: () => request.delete('/cart/clear'),
  cleanInvalid: () => request.delete('/cart/clean-invalid'),
  settle: () => request.get('/cart/settle'),
  checkout: (couponId) => request.get('/cart/checkout', { params: { couponId } })
}

/* ============================== 收货地址 ============================== */
export const addressApi = {
  list: () => request.get('/address/list'),
  default: () => request.get('/address/default'),
  detail: (id) => request.get(`/address/${id}`),
  save: (data) => request.post('/address', data),
  update: (id, data) => request.put(`/address/${id}`, data),
  remove: (id) => request.delete(`/address/${id}`),
  setDefault: (id) => request.put(`/address/${id}/default`)
}

/* ============================== 订单 ============================== */
export const orderApi = {
  preview: (data) => request.post('/order/preview', data),
  submit: (data) => request.post('/order/submit', data),
  page: (params) => request.get('/order/page', { params }),
  detail: (id) => request.get(`/order/${id}`),
  detailByNo: (orderNo) => request.get(`/order/no/${orderNo}`),
  items: (id) => request.get(`/order/${id}/items`),
  cancel: (id, reason) => request.put(`/order/${id}/cancel`, null, { params: { reason } }),
  receive: (id) => request.put(`/order/${id}/receive`),
  remove: (id) => request.delete(`/order/${id}`),
  rebuy: (id) => request.post(`/order/${id}/rebuy`),
  statistics: () => request.get('/order/statistics'),
  statusDict: () => request.get('/order/status-dict')
}

/* ============================== 支付 ============================== */
export const payApi = {
  methods: (orderId) => request.get(`/pay/methods/${orderId}`),
  mockPay: (orderId, channel = 'wechat') =>
    request.post(`/pay/mock/${orderId}`, null, { params: { channel } }),
  result: (orderNo) => request.get(`/pay/result/${orderNo}`),
  channels: () => request.get('/pay/channels')
}

/* ============================== 会员中心 ============================== */
export const userApi = {
  profile: () => request.get('/user/profile'),
  updateProfile: (data) => request.put('/user/profile', data),
  changePassword: (data) => request.put('/user/password', data),
  dashboard: () => request.get('/user/dashboard'),
  orderStatistics: () => request.get('/user/order-statistics'),
  pointsRecords: (params) => request.get('/user/points/records', { params }),
  pointsOverview: () => request.get('/user/points/overview'),
  balanceRecords: (params) => request.get('/user/balance/records', { params }),
  favorites: (params) => request.get('/user/favorite/page', { params }),
  footprints: (limit = 60) => request.get('/user/footprint/list', { params: { limit } }),
  level: () => request.get('/user/level')
}

/* ============================== 收藏 / 足迹 ============================== */
export const favoriteApi = {
  toggle: (productId) => request.post(`/favorite/toggle/${productId}`),
  check: (productId) => request.get(`/favorite/check/${productId}`),
  remove: (productId) => request.delete(`/favorite/${productId}`)
}

export const footprintApi = {
  add: (productId) => request.post(`/footprint/${productId}`),
  list: (limit = 60) => request.get('/footprint/list', { params: { limit } }),
  clear: () => request.delete('/footprint/clear'),
  remove: (id) => request.delete(`/footprint/${id}`)
}

/* ============================== 优惠券 ============================== */
export const couponApi = {
  available: () => request.get('/coupon/available'),
  detail: (id) => request.get(`/coupon/${id}`),
  receive: (id) => request.post(`/coupon/receive/${id}`),
  my: (status) => request.get('/coupon/my', { params: { status } }),
  usable: (amount, userCouponId) =>
    request.get('/coupon/usable', { params: { amount, userCouponId } }),
  myCount: () => request.get('/coupon/my/count')
}

/* ============================== 消息 ============================== */
export const messageApi = {
  page: (params) => request.get('/message/page', { params }),
  unreadCount: () => request.get('/message/unread-count'),
  unreadSummary: () => request.get('/message/unread-summary'),
  read: (id) => request.put(`/message/${id}/read`),
  readAll: (type) => request.put('/message/read-all', null, { params: { type } }),
  remove: (id) => request.delete(`/message/${id}`),
  clear: (type) => request.delete('/message/clear', { params: { type } })
}

/* ============================== 物流 ============================== */
export const logisticsApi = {
  detail: (orderId) => request.get(`/logistics/${orderId}`),
  info: (orderId) => request.get(`/logistics/${orderId}/info`),
  traces: (orderId) => request.get(`/logistics/${orderId}/traces`),
  companies: () => request.get('/logistics/company/list')
}

/* ============================== 售后 ============================== */
export const afterSaleApi = {
  apply: (data) => request.post('/aftersale/apply', data),
  page: (params) => request.get('/aftersale/page', { params }),
  detail: (id) => request.get(`/aftersale/${id}`),
  byOrder: (orderId) => request.get(`/aftersale/order/${orderId}`),
  cancel: (id) => request.put(`/aftersale/${id}/cancel`),
  reasons: () => request.get('/aftersale/reasons')
}

/* ============================== 评价 ============================== */
export const commentApi = {
  add: (data) => request.post('/comment/add', data),
  product: (productId, params) => request.get(`/comment/product/${productId}`, { params }),
  my: (params) => request.get('/comment/my', { params }),
  pending: () => request.get('/comment/pending'),
  append: (data) => request.post('/comment/append', data),
  remove: (id) => request.delete(`/comment/${id}`),
  statistics: (productId) => request.get(`/comment/statistics/${productId}`)
}

/* ============================== 积分 / 签到 ============================== */
export const pointsApi = {
  signStatus: () => request.get('/points/sign/status'),
  sign: () => request.post('/points/sign'),
  records: (params) => request.get('/points/records', { params }),
  goods: (params) => request.get('/points/goods', { params }),
  exchange: (goodsId) => request.post(`/points/exchange/${goodsId}`),
  exchanges: () => request.get('/points/exchanges'),
  overview: () => request.get('/points/overview')
}

/* ============================== 营销 ============================== */
export const marketingApi = {
  features: () => request.get('/marketing/features'),
  seckillList: () => request.get('/marketing/seckill/list')
}

/* ============================== 帮助中心 ============================== */
export const helpApi = {
  list: (category) => request.get('/help/list', { params: { category } })
}

/* ============================== 工具 ============================== */
/** 根据文案生成占位图地址（与后端 /api/image/ph 对应） */
export function placeholderImage(text, w = 600, h = 600, c = 0) {
  return `/api/image/ph?t=${encodeURIComponent(text || 'AbcMall')}&w=${w}&h=${h}&c=${c}`
}
