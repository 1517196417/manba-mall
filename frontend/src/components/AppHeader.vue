<template>
  <header class="header">
    <!-- 顶部条 -->
    <div class="header__top">
      <div class="abc-container header__top-inner">
        <div class="header__welcome">
          <template v-if="userStore.isLogin">
            <span>Hi，{{ userStore.nickname }}</span>
            <el-tag size="small" effect="dark" type="warning" round>{{ userStore.levelName }}</el-tag>
          </template>
          <template v-else>
            <span>欢迎来到 AbcMall 阿帕茨商城</span>
            <el-link type="primary" :underline="false" @click="go('/login')">请登录</el-link>
            <el-link :underline="false" @click="go('/register')">免费注册</el-link>
          </template>
        </div>

        <nav class="header__links">
          <span class="header__link" @click="go('/user/orders')">我的订单</span>
          <el-badge :value="userStore.unreadCount" :hidden="!userStore.unreadCount" class="header__badge">
            <span class="header__link" @click="go('/user/messages')">消息</span>
          </el-badge>
          <span class="header__link" @click="go('/user/favorites')">我的收藏</span>
          <span class="header__link" @click="go('/coupon')">领券中心</span>
          <span class="header__link" @click="go('/sign')">每日签到</span>
          <span class="header__link" @click="go('/features')">功能一览</span>
          <el-dropdown v-if="userStore.isLogin" @command="onCommand">
            <span class="header__link header__link--drop">
              会员中心 <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="/user/dashboard">会员中心首页</el-dropdown-item>
                <el-dropdown-item command="/user/profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="/user/address">收货地址</el-dropdown-item>
                <el-dropdown-item command="/user/points">我的积分</el-dropdown-item>
                <el-dropdown-item command="/user/coupons">我的优惠券</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>
      </div>
    </div>

    <!-- 主搜索区 -->
    <div class="header__main">
      <div class="abc-container header__main-inner">
        <div class="header__logo" @click="go('/')">
          <span class="header__logo-mark">A</span>
          <div class="header__logo-text">
            <b>AbcMall</b>
            <small>阿帕茨商城</small>
          </div>
        </div>

        <div class="header__search">
          <div class="header__search-bar">
            <el-autocomplete
              v-model="keyword"
              class="header__search-input"
              placeholder="搜索商品，如：华为 Mate 60 Pro"
              :fetch-suggestions="fetchSuggestions"
              :trigger-on-focus="false"
              clearable
              popper-class="header__search-popper"
              @select="onSuggestSelect"
              @keyup.enter="doSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-autocomplete>
            <el-button type="primary" class="header__search-btn" @click="doSearch">搜索</el-button>
            <el-tooltip content="扫码入口" placement="bottom">
              <el-button class="header__scan-btn" :icon="FullScreen" @click="go('/scan')" />
            </el-tooltip>
          </div>

          <div class="header__hot">
            <span class="header__hot-label">热门搜索：</span>
            <span v-for="word in hotWords" :key="word" class="header__hot-item"
                  @click="searchKeyword(word)">{{ word }}</span>
          </div>
        </div>

        <div class="header__cart" @click="go('/cart')">
          <el-badge :value="userStore.cartCount" :hidden="!userStore.cartCount">
            <el-button class="header__cart-btn" :icon="ShoppingCart">
              购物车 <small>（{{ userStore.cartCount }}）</small>
            </el-button>
          </el-badge>
        </div>
      </div>
    </div>

    <!-- 导航 -->
    <nav class="header__nav">
      <div class="abc-container header__nav-inner">
        <el-dropdown trigger="hover" class="header__cat">
          <span class="header__cat-btn" @click="go('/category')">
            <el-icon><Menu /></el-icon> 全部商品分类
          </span>
          <template #dropdown>
            <el-dropdown-menu class="header__cat-menu">
              <el-dropdown-item v-for="cat in categories" :key="cat.id"
                                @click="go(`/category/${cat.id}`)">
                <span class="header__cat-name">{{ cat.name }}</span>
                <span class="header__cat-sub">
                  {{ (cat.children || []).slice(0, 3).map((c) => c.name).join(' / ') }}
                </span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <router-link to="/" class="header__nav-item" :class="{ active: route.path === '/' }">首页</router-link>
        <router-link to="/category" class="header__nav-item" :class="{ active: route.path.startsWith('/category') }">分类</router-link>
        <router-link to="/activity" class="header__nav-item" :class="{ active: route.path.startsWith('/activity') }">活动专区</router-link>
        <router-link to="/feature/seckill" class="header__nav-item">限时秒杀</router-link>
        <router-link to="/coupon" class="header__nav-item" :class="{ active: route.path === '/coupon' }">领券中心</router-link>
        <router-link to="/points-mall" class="header__nav-item" :class="{ active: route.path === '/points-mall' }">积分商城</router-link>
        <router-link to="/sign" class="header__nav-item" :class="{ active: route.path === '/sign' }">每日签到</router-link>
        <router-link to="/notice" class="header__nav-item" :class="{ active: route.path.startsWith('/notice') }">公告</router-link>
        <router-link to="/features" class="header__nav-item" :class="{ active: route.path.startsWith('/feature') }">功能一览</router-link>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowDown, FullScreen, Menu, Search, ShoppingCart } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { categoryApi, homeApi, searchApi } from '@/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const keyword = ref('')
const hotWords = ref([])
const categories = ref([])

const allSearchHistory = computed(() => [])

onMounted(async () => {
  try {
    const [hot, tree] = await Promise.all([homeApi.hotKeywords(), categoryApi.tree()])
    hotWords.value = (hot || []).slice(0, 6)
    categories.value = tree || []
  } catch (e) {
    /* 忽略首页数据异常 */
  }
})

async function fetchSuggestions(queryString, cb) {
  const history = getHistory()
  const result = []

  if (!queryString) {
    history.slice(0, 6).forEach((h) => result.push({ value: h, tag: '历史' }))
    hotWords.value.slice(0, 4).forEach((h) => result.push({ value: h, tag: '热门' }))
    cb(result)
    return
  }

  result.push({ value: queryString, tag: '搜索' })
  try {
    const list = await searchApi.suggest(queryString)
    ;(list || []).forEach((s) => {
      if (s !== queryString) result.push({ value: s, tag: '联想' })
    })
  } catch (e) {
    /* 忽略联想失败 */
  }
  history
    .filter((h) => h.includes(queryString))
    .slice(0, 3)
    .forEach((h) => result.push({ value: h, tag: '历史' }))
  cb(result)
}

function onSuggestSelect(item) {
  keyword.value = item.value
  doSearch()
}

function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    ElMessage.warning('请输入要搜索的商品')
    return
  }
  saveHistory(kw)
  router.push({ path: '/search', query: { keyword: kw } })
}

function searchKeyword(word) {
  keyword.value = word
  saveHistory(word)
  router.push({ path: '/search', query: { keyword: word } })
}

const HISTORY_KEY = 'abcmall_search_history'

function getHistory() {
  try {
    return JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
  } catch (e) {
    return []
  }
}

function saveHistory(word) {
  const list = getHistory().filter((h) => h !== word)
  list.unshift(word)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list.slice(0, 12)))
}

function go(path) {
  if (path.startsWith('/user') && !userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: path } })
    return
  }
  router.push(path)
}

async function onCommand(cmd) {
  if (cmd === 'logout') {
    await userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/')
    return
  }
  go(cmd)
}
</script>

<style scoped>
.header {
  background: #fff;
  box-shadow: 0 1px 6px rgba(15, 23, 42, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header__top {
  background: #f7f8fa;
  border-bottom: 1px solid var(--abc-border);
  font-size: 12px;
  color: var(--abc-text-2);
}
.header__top-inner {
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header__welcome {
  display: flex;
  align-items: center;
  gap: 8px;
}
.header__links {
  display: flex;
  align-items: center;
  gap: 18px;
}
.header__link {
  cursor: pointer;
  transition: color 0.2s;
}
.header__link:hover {
  color: var(--abc-primary);
}
.header__link--drop {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.header__main {
  padding: 16px 0;
}
.header__main-inner {
  display: flex;
  align-items: center;
  gap: 24px;
}
.header__logo {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  flex-shrink: 0;
}
.header__logo-mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  box-shadow: 0 6px 16px rgba(255, 80, 0, 0.3);
}
.header__logo-text {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}
.header__logo-text b {
  font-size: 20px;
  letter-spacing: 0.5px;
}
.header__logo-text small {
  color: var(--abc-text-3);
  font-size: 12px;
}
.header__search {
  flex: 1;
  min-width: 0;
}
.header__search-bar {
  display: flex;
  gap: 8px;
}
.header__search-input {
  flex: 1;
}
.header__search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  padding-left: 14px;
}
.header__search-btn {
  border-radius: 20px;
  padding: 0 26px;
}
.header__scan-btn {
  border-radius: 50%;
  width: 34px;
  height: 34px;
  padding: 0;
}
.header__hot {
  margin-top: 8px;
  font-size: 12px;
  color: var(--abc-text-3);
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.header__hot-label {
  color: var(--abc-text-3);
}
.header__hot-item {
  cursor: pointer;
  transition: color 0.2s;
}
.header__hot-item:hover {
  color: var(--abc-primary);
}
.header__cart {
  flex-shrink: 0;
}
.header__cart-btn {
  border-color: var(--abc-primary);
  color: var(--abc-primary);
  border-radius: 20px;
  padding: 0 18px;
  height: 38px;
}
.header__nav {
  border-top: 1px solid var(--abc-border);
}
.header__nav-inner {
  display: flex;
  align-items: center;
  gap: 4px;
  height: 42px;
}
.header__cat {
  margin-right: 12px;
}
.header__cat-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 42px;
  padding: 0 18px;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.header__cat-name {
  font-weight: 600;
  margin-right: 10px;
}
.header__cat-sub {
  color: var(--abc-text-3);
  font-size: 12px;
}
.header__nav-item {
  padding: 0 16px;
  height: 42px;
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  color: var(--abc-text);
  transition: color 0.2s;
}
.header__nav-item:hover,
.header__nav-item.active {
  color: var(--abc-primary);
  font-weight: 600;
}
</style>
