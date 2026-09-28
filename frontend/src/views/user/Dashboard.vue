<template>
  <div v-loading="loading" class="dash">
    <div class="dash__head">
      <div class="dash__hello">
        <h2>Hi，{{ info.nickname || userStore.nickname || '商城会员' }}</h2>
        <p>欢迎回到 AbcMall 阿帕茨商城会员中心</p>
      </div>
      <div class="dash__level">
        <div class="dash__level-top">
          <el-tag effect="dark" type="warning" round>{{ info.levelName || '普通会员' }}</el-tag>
          <span class="dash__growth">成长值 {{ info.growth || 0 }} / {{ info.nextLevelGrowth || 0 }}</span>
        </div>
        <el-progress :percentage="growthPercent" :stroke-width="8" :show-text="false" />
        <el-link :underline="false" type="primary" class="dash__level-link" @click="go('/user/points')">
          查看会员等级权益 &gt;
        </el-link>
      </div>
    </div>

    <!-- 资产概览 -->
    <div class="dash__assets">
      <div v-for="a in assets" :key="a.name" class="dash__asset" @click="go(a.path)">
        <div class="dash__asset-icon" :style="{ background: a.color }">
          <el-icon :size="20"><component :is="a.icon" /></el-icon>
        </div>
        <div class="dash__asset-text">
          <b>{{ a.value }}</b>
          <span>{{ a.name }}</span>
        </div>
      </div>
    </div>

    <!-- 订单状态 -->
    <div class="dash__block">
      <div class="dash__block-head">
        <h3>我的订单</h3>
        <el-link :underline="false" type="primary" @click="go('/user/orders')">全部订单 &gt;</el-link>
      </div>
      <div class="dash__orders">
        <div v-for="o in orderTabs" :key="o.name" class="dash__order" @click="goOrder(o.status)">
          <div class="dash__order-icon">
            <el-icon :size="24"><component :is="o.icon" /></el-icon>
            <el-badge v-if="o.count > 0" :value="o.count" class="dash__order-badge" />
          </div>
          <span>{{ o.name }}</span>
        </div>
      </div>
    </div>

    <!-- 常用功能 -->
    <div class="dash__block">
      <div class="dash__block-head">
        <h3>常用功能</h3>
      </div>
      <div class="dash__quick">
        <div v-for="q in quicks" :key="q.name" class="dash__quick-item" @click="go(q.path)">
          <el-icon :size="18"><component :is="q.icon" /></el-icon>
          <span>{{ q.name }}</span>
        </div>
      </div>
    </div>

    <div class="dash__tip">
      <el-icon><InfoFilled /></el-icon>
      <span>
        标注「!」的功能（如实名认证、会员等级权益、客服介入等）为功能说明页，点击后展示功能定位说明。
      </span>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { InfoFilled } from '@element-plus/icons-vue'
import { userApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const info = ref({})
const stat = ref({})
const dash = ref({})

const growthPercent = computed(() => {
  const next = Number(info.value.nextLevelGrowth || 0)
  if (!next) return 100
  return Math.min(100, Math.round((Number(info.value.growth || 0) / next) * 100))
})

const assets = computed(() => [
  { name: '积分', value: info.value.points ?? userStore.points ?? 0, icon: 'Medal', color: '#ff9500', path: '/user/points' },
  { name: '余额', value: `¥${Number(info.value.balance ?? userStore.balance ?? 0).toFixed(2)}`, icon: 'Wallet', color: '#ff5000', path: '/user/balance' },
  { name: '优惠券', value: dash.value.couponCount || 0, icon: 'Ticket', color: '#e04600', path: '/user/coupons' },
  { name: '收藏', value: dash.value.favoriteCount || 0, icon: 'Star', color: '#28a745', path: '/user/favorites' },
  { name: '评价', value: dash.value.commentCount || 0, icon: 'ChatLineSquare', color: '#3b82f6', path: '/user/comments' },
  { name: '售后', value: dash.value.afterSaleCount || 0, icon: 'RefreshLeft', color: '#8b5cf6', path: '/user/aftersale' }
])

const orderTabs = computed(() => [
  { name: '待付款', status: 0, count: stat.value.unpaid || 0, icon: 'Wallet' },
  { name: '待发货', status: 1, count: stat.value.unshipped || 0, icon: 'Box' },
  { name: '待收货', status: 2, count: stat.value.unreceived || 0, icon: 'Van' },
  { name: '待评价', status: 3, count: stat.value.uncommented || 0, icon: 'ChatDotRound' },
  { name: '售后中', status: 6, count: stat.value.afterSale || 0, icon: 'RefreshLeft' }
])

const quicks = [
  { name: '收货地址', path: '/user/address', icon: 'Location' },
  { name: '浏览足迹', path: '/user/footprints', icon: 'View' },
  { name: '每日签到', path: '/sign', icon: 'Calendar' },
  { name: '积分商城', path: '/points-mall', icon: 'Present' },
  { name: '领券中心', path: '/coupon', icon: 'Ticket' },
  { name: '功能一览', path: '/features', icon: 'Grid' }
]

onMounted(load)

async function load() {
  loading.value = true
  try {
    const data = (await userApi.dashboard()) || {}
    info.value = data.user || {}
    stat.value = data.orderStatistics || {}
    dash.value = data
  } catch (e) {
    /* 已提示 */
  } finally {
    loading.value = false
  }
}

function go(path) {
  router.push(path)
}

function goOrder(status) {
  router.push({ path: '/user/orders', query: { status } })
}
</script>

<style scoped>
.dash__head {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  padding: 22px 24px;
  border-radius: var(--abc-radius);
  color: #fff;
  background: linear-gradient(120deg, #ff5000, #ff8a00 70%, #ffb300);
  box-shadow: 0 12px 26px rgba(255, 80, 0, 0.22);
}
.dash__hello h2 {
  margin: 0;
  font-size: 22px;
}
.dash__hello p {
  margin: 8px 0 0;
  font-size: 13px;
  opacity: 0.92;
}
.dash__level {
  background: rgba(255, 255, 255, 0.16);
  border-radius: 12px;
  padding: 14px 16px;
}
.dash__level-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.dash__growth {
  font-size: 12px;
  opacity: 0.92;
}
.dash__level :deep(.el-progress-bar__outer) {
  background: rgba(255, 255, 255, 0.32);
}
.dash__level :deep(.el-progress-bar__inner) {
  background: #fff;
}
.dash__level-link {
  margin-top: 10px;
  font-size: 12.5px;
  color: #fff !important;
}
.dash__assets {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}
.dash__asset {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 14px;
  border-radius: 12px;
  background: #fafbfc;
  border: 1px solid var(--abc-border);
  cursor: pointer;
  transition: all 0.2s;
}
.dash__asset:hover {
  border-color: var(--abc-primary);
  background: #fffaf7;
  transform: translateY(-2px);
}
.dash__asset-icon {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.dash__asset-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}
.dash__asset-text b {
  font-size: 16px;
  font-family: Arial, Helvetica, sans-serif;
}
.dash__asset-text span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.dash__block {
  margin-top: 22px;
}
.dash__block-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.dash__block-head h3 {
  margin: 0;
  font-size: 17px;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
.dash__orders {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}
.dash__order {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 10px;
  border-radius: 12px;
  background: #fafbfc;
  border: 1px solid var(--abc-border);
  cursor: pointer;
  font-size: 13.5px;
  transition: all 0.2s;
}
.dash__order:hover {
  color: var(--abc-primary);
  border-color: var(--abc-primary);
  background: #fffaf7;
}
.dash__order-icon {
  position: relative;
  color: var(--abc-primary);
}
.dash__order-badge {
  position: absolute;
  top: -6px;
  right: -14px;
}
.dash__quick {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.dash__quick-item {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 20px;
  border-radius: 22px;
  background: #fafbfc;
  border: 1px solid var(--abc-border);
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.2s;
}
.dash__quick-item:hover {
  color: var(--abc-primary);
  border-color: var(--abc-primary);
  background: #fffaf7;
}
.dash__tip {
  margin-top: 24px;
  display: flex;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff8ec;
  color: #b26a00;
  font-size: 12.5px;
  line-height: 1.7;
}
@media (max-width: 1080px) {
  .dash__assets {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 860px) {
  .dash__head {
    grid-template-columns: 1fr;
  }
  .dash__orders {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
