<template>
  <div class="abc-container uc">
    <el-breadcrumb separator="/" class="uc__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>会员中心</el-breadcrumb-item>
      <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="uc__body">
      <!-- 左侧栏 -->
      <aside class="uc__side">
        <div class="uc__card">
          <div class="uc__avatar abc-img-box">
            <img :src="userStore.avatar || fallbackAvatar" :alt="userStore.nickname" />
          </div>
          <h3 class="uc__name">{{ userStore.nickname || userStore.info?.username || '商城会员' }}</h3>
          <el-tag size="small" effect="dark" type="warning" round>{{ userStore.levelName || '普通会员' }}</el-tag>
          <div class="uc__assets">
            <div class="uc__asset" @click="go('/user/points')">
              <b>{{ userStore.points || 0 }}</b>
              <span>积分</span>
            </div>
            <div class="uc__asset" @click="go('/user/balance')">
              <b>{{ Number(userStore.balance || 0).toFixed(2) }}</b>
              <span>余额</span>
            </div>
            <div class="uc__asset" @click="go('/user/coupons')">
              <b>{{ couponCount }}</b>
              <span>优惠券</span>
            </div>
          </div>
        </div>

        <nav class="uc__menu">
          <template v-for="m in menus" :key="m.path">
            <div class="uc__menu-group" v-if="m.group">{{ m.group }}</div>
            <router-link :to="m.path" class="uc__menu-item"
                         :class="{ active: route.path === m.path }">
              <el-icon :size="17"><component :is="m.icon" /></el-icon>
              <span>{{ m.name }}</span>
              <el-badge v-if="m.badge && badgeOf(m.badge)" :value="badgeOf(m.badge)" class="uc__menu-badge" />
            </router-link>
          </template>
        </nav>
      </aside>

      <!-- 右侧内容 -->
      <section class="uc__main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { couponApi, messageApi, placeholderImage } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const fallbackAvatar = placeholderImage('会员', 120, 120, 3)

const couponCount = ref(0)
const unread = ref(0)

const menus = [
  { group: '交易管理', name: '会员中心首页', path: '/user/dashboard', icon: 'HomeFilled' },
  { name: '我的订单', path: '/user/orders', icon: 'List' },
  { name: '我的售后', path: '/user/aftersale', icon: 'RefreshLeft' },
  { name: '我的评价', path: '/user/comments', icon: 'ChatLineSquare' },
  { group: '我的资产', name: '我的积分', path: '/user/points', icon: 'Medal' },
  { name: '我的余额', path: '/user/balance', icon: 'Wallet' },
  { name: '我的优惠券', path: '/user/coupons', icon: 'Ticket' },
  { group: '我的收藏', name: '我的收藏', path: '/user/favorites', icon: 'Star' },
  { name: '浏览足迹', path: '/user/footprints', icon: 'View' },
  { group: '账户设置', name: '个人资料', path: '/user/profile', icon: 'User' },
  { name: '收货地址', path: '/user/address', icon: 'Location' },
  { name: '消息中心', path: '/user/messages', icon: 'Bell', badge: 'unread' }
]

const currentTitle = computed(() => {
  const hit = menus
    .filter((m) => m.path)
    .find((m) => m.path === route.path)
  return hit ? hit.name : '会员中心'
})

function badgeOf(key) {
  return key === 'unread' ? unread.value : 0
}

onMounted(async () => {
  await userStore.refreshInfo()
  try {
    const [cnt, u] = await Promise.all([
      couponApi.myCount().catch(() => null),
      messageApi.unreadCount().catch(() => 0)
    ])
    couponCount.value = cnt ? cnt.unused || 0 : 0
    unread.value = u || 0
  } catch (e) {
    /* 忽略 */
  }
})

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.uc {
  padding-top: 16px;
}
.uc__crumb {
  margin-bottom: 14px;
}
.uc__body {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 16px;
  align-items: start;
}
.uc__side {
  position: sticky;
  top: 190px;
}
.uc__card {
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  padding: 24px 18px 18px;
  text-align: center;
}
.uc__avatar {
  width: 74px;
  height: 74px;
  border-radius: 50%;
  margin: 0 auto 12px;
  overflow: hidden;
  border: 3px solid var(--abc-primary-light);
}
.uc__name {
  margin: 0 0 8px;
  font-size: 16px;
}
.uc__assets {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border-top: 1px solid var(--abc-border);
  padding-top: 14px;
}
.uc__asset {
  display: flex;
  flex-direction: column;
  gap: 2px;
  cursor: pointer;
  transition: color 0.2s;
}
.uc__asset:hover b {
  color: var(--abc-primary);
}
.uc__asset b {
  font-size: 15px;
  color: var(--abc-price);
  font-family: Arial, Helvetica, sans-serif;
}
.uc__asset span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.uc__menu {
  margin-top: 14px;
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  padding: 8px 0 12px;
}
.uc__menu-group {
  padding: 12px 20px 6px;
  font-size: 11.5px;
  color: var(--abc-text-3);
  letter-spacing: 1px;
}
.uc__menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 20px;
  font-size: 14px;
  color: var(--abc-text);
  border-left: 3px solid transparent;
  transition: all 0.2s;
}
.uc__menu-item:hover {
  color: var(--abc-primary);
  background: #fafbfc;
}
.uc__menu-item.active {
  color: var(--abc-primary);
  font-weight: 700;
  background: var(--abc-primary-light);
  border-left-color: var(--abc-primary);
}
.uc__menu-item span {
  flex: 1;
}
.uc__menu-badge {
  margin-right: 2px;
}
.uc__main {
  min-height: 460px;
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  padding: 22px 24px;
}
@media (max-width: 900px) {
  .uc__body {
    grid-template-columns: 1fr;
  }
  .uc__side {
    position: static;
  }
  .uc__menu {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
  }
  .uc__menu-group {
    grid-column: 1 / -1;
  }
}
</style>
