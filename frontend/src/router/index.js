import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/',
    component: DefaultLayout,
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
      { path: 'ai-chat', name: 'AiChat', component: () => import('@/views/AiChat.vue'), meta: { title: 'AI聊天' } },

      /* 分类与商品 */
      { path: 'category', name: 'Category', component: () => import('@/views/Category.vue'), meta: { title: '全部分类' } },
      { path: 'category/:id', name: 'CategoryProducts', component: () => import('@/views/ProductList.vue'), meta: { title: '分类商品' } },
      { path: 'products', name: 'ProductList', component: () => import('@/views/ProductList.vue'), meta: { title: '商品列表' } },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('@/views/ProductDetail.vue'), meta: { title: '商品详情' } },
      { path: 'search', name: 'Search', component: () => import('@/views/Search.vue'), meta: { title: '搜索' } },

      /* 购物车与订单 */
      { path: 'cart', name: 'Cart', component: () => import('@/views/Cart.vue'), meta: { title: '购物车', requiresAuth: true } },
      { path: 'checkout', name: 'Checkout', component: () => import('@/views/Checkout.vue'), meta: { title: '确认订单', requiresAuth: true } },
      { path: 'pay/result/:orderNo', name: 'PayResult', component: () => import('@/views/PayResult.vue'), meta: { title: '支付结果', requiresAuth: true } },
      { path: 'pay/:orderId', name: 'Pay', component: () => import('@/views/Pay.vue'), meta: { title: '收银台', requiresAuth: true } },
      { path: 'order/:id/logistics', name: 'Logistics', component: () => import('@/views/Logistics.vue'), meta: { title: '物流跟踪', requiresAuth: true } },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('@/views/OrderDetail.vue'), meta: { title: '订单详情', requiresAuth: true } },

      /* 评价与售后 */
      { path: 'comment/create', name: 'CommentCreate', component: () => import('@/views/CommentCreate.vue'), meta: { title: '发表评价', requiresAuth: true } },
      { path: 'aftersale/apply', name: 'AfterSaleApply', component: () => import('@/views/AfterSaleApply.vue'), meta: { title: '申请售后', requiresAuth: true } },

      /* 营销与内容 */
      { path: 'coupon', name: 'CouponCenter', component: () => import('@/views/Coupon.vue'), meta: { title: '领券中心' } },
      { path: 'points-mall', name: 'PointsMall', component: () => import('@/views/PointsMall.vue'), meta: { title: '积分商城' } },
      { path: 'sign', name: 'Sign', component: () => import('@/views/Sign.vue'), meta: { title: '每日签到', requiresAuth: true } },
      { path: 'activity', name: 'ActivityList', component: () => import('@/views/ActivityList.vue'), meta: { title: '活动专区' } },
      { path: 'activity/:id', name: 'ActivityDetail', component: () => import('@/views/ActivityDetail.vue'), meta: { title: '活动详情' } },
      { path: 'promotion/:id', name: 'Promotion', component: () => import('@/views/ActivityDetail.vue'), meta: { title: '专场活动' } },
      { path: 'topic/:id', name: 'TopicDetail', component: () => import('@/views/TopicDetail.vue'), meta: { title: '专题详情' } },
      { path: 'notice', name: 'NoticeList', component: () => import('@/views/NoticeList.vue'), meta: { title: '公告中心' } },
      { path: 'notice/:id', name: 'NoticeDetail', component: () => import('@/views/NoticeDetail.vue'), meta: { title: '公告详情' } },

      /* 扫码 / 功能说明页 */
      { path: 'scan', name: 'Scan', component: () => import('@/views/Scan.vue'), meta: { title: '扫码' } },
      { path: 'features', name: 'FeatureIndex', component: () => import('@/views/FeatureIndex.vue'), meta: { title: '功能一览' } },
      { path: 'feature/:key', name: 'Feature', component: () => import('@/views/Feature.vue'), meta: { title: '功能说明' } },

      /* 会员中心 */
      {
        path: 'user',
        component: () => import('@/views/user/UserLayout.vue'),
        redirect: '/user/dashboard',
        meta: { requiresAuth: true },
        children: [
          { path: 'dashboard', name: 'UserDashboard', component: () => import('@/views/user/Dashboard.vue'), meta: { title: '会员中心', requiresAuth: true } },
          { path: 'profile', name: 'UserProfile', component: () => import('@/views/user/Profile.vue'), meta: { title: '个人资料', requiresAuth: true } },
          { path: 'address', name: 'UserAddress', component: () => import('@/views/user/Address.vue'), meta: { title: '收货地址', requiresAuth: true } },
          { path: 'orders', name: 'UserOrders', component: () => import('@/views/user/Orders.vue'), meta: { title: '我的订单', requiresAuth: true } },
          { path: 'favorites', name: 'UserFavorites', component: () => import('@/views/user/Favorites.vue'), meta: { title: '我的收藏', requiresAuth: true } },
          { path: 'footprints', name: 'UserFootprints', component: () => import('@/views/user/Footprints.vue'), meta: { title: '浏览足迹', requiresAuth: true } },
          { path: 'coupons', name: 'UserCoupons', component: () => import('@/views/user/Coupons.vue'), meta: { title: '我的优惠券', requiresAuth: true } },
          { path: 'points', name: 'UserPoints', component: () => import('@/views/user/Points.vue'), meta: { title: '我的积分', requiresAuth: true } },
          { path: 'balance', name: 'UserBalance', component: () => import('@/views/user/Balance.vue'), meta: { title: '我的余额', requiresAuth: true } },
          { path: 'messages', name: 'UserMessages', component: () => import('@/views/user/Messages.vue'), meta: { title: '消息中心', requiresAuth: true } },
          { path: 'comments', name: 'UserComments', component: () => import('@/views/user/Comments.vue'), meta: { title: '我的评价', requiresAuth: true } },
          { path: 'aftersale', name: 'UserAfterSale', component: () => import('@/views/user/AfterSale.vue'), meta: { title: '我的售后', requiresAuth: true } }
        ]
      }
    ]
  },

  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: () => import('@/views/NotFound.vue'), meta: { title: '页面不存在' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    return savedPosition || { top: 0 }
  }
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  // 首次进入时同步一次登录态
  if (!userStore.loaded) {
    await userStore.fetchCurrent()
  }

  if (to.meta && to.meta.requiresAuth && !userStore.isLogin) {
    ElMessage.warning('请先登录后再操作')
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  document.title = to.meta && to.meta.title
    ? `${to.meta.title} - AbcMall 阿帕茨商城`
    : 'AbcMall 阿帕茨商城'
  return true
})

export default router
