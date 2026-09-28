<template>
  <div class="abc-container coupon">
    <el-breadcrumb separator="/" class="coupon__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>领券中心</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="coupon__banner">
      <div class="coupon__banner-text">
        <h2>领券中心</h2>
        <p>先领券再下单，满减券 / 折扣券 / 无门槛券天天领</p>
      </div>
      <el-button class="coupon__banner-btn" round @click="go('/user/coupons')">我的优惠券</el-button>
    </div>

    <div class="coupon__tabs">
      <span v-for="t in tabs" :key="String(t.value)" class="coupon__tab"
            :class="{ active: active === t.value }" @click="active = t.value">
        {{ t.label }}
      </span>
      <span class="coupon__count">共 {{ shown.length }} 张可领</span>
    </div>

    <div v-loading="loading" class="coupon__list">
      <div v-for="c in shown" :key="c.id" class="c-item" :class="{ 'c-item--sold': c.soldOut }">
        <div class="c-item__left">
          <div class="c-item__value">
            <template v-if="c.type === 2">
              <b>{{ Number(c.discount || 0) }}</b><i>折</i>
            </template>
            <template v-else>
              <i>¥</i><b>{{ Number(c.amount || 0) }}</b>
            </template>
          </div>
          <div class="c-item__cond">
            {{ Number(c.minAmount || 0) > 0 ? `满 ${Number(c.minAmount)} 可用` : '无门槛使用' }}
          </div>
        </div>

        <div class="c-item__mid">
          <h4 class="abc-ellipsis">{{ c.name }}</h4>
          <p class="c-item__desc abc-ellipsis">{{ c.description || '全场通用' }}</p>
          <p class="c-item__time">
            <el-icon><Clock /></el-icon>
            {{ formatDate(c.startTime) }} ~ {{ formatDate(c.endTime) }}
          </p>
          <el-progress :percentage="usedPercent(c)" :stroke-width="6"
                       :show-text="false" class="c-item__progress" />
          <span class="c-item__stock">已领 {{ c.received || 0 }} / {{ c.total || 0 }}</span>
        </div>

        <div class="c-item__right">
          <el-button v-if="c.soldOut" round disabled>已领完</el-button>
          <el-button v-else type="primary" round :disabled="c.receivedCount >= c.perLimit"
                     @click="receive(c)">
            {{ c.receivedCount >= c.perLimit ? '已领取' : '立即领取' }}
          </el-button>
          <span class="c-item__limit">每人限领 {{ c.perLimit || 1 }} 张</span>
        </div>
      </div>

      <EmptyBox v-if="!loading && !shown.length" icon="Ticket" text="该分类下暂时没有可领取的优惠券"
                action-text="返回首页" @action="go('/')" />
    </div>

    <div class="coupon__rule abc-card">
      <h3>使用规则</h3>
      <ul>
        <li>每张优惠券仅可在符合门槛的订单中使用一次，不找零、不兑现。</li>
        <li>满减券与折扣券可参与平台活动叠加（标注「!」的营销功能为功能说明页）。</li>
        <li>优惠券过期后自动失效，可在「我的优惠券」中查看未使用 / 已使用 / 已过期。</li>
        <li>订单发生退款时，已使用的优惠券是否退回以售后审核结果为准。</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { couponApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const list = ref([])
const active = ref(null)

const tabs = [
  { label: '全部', value: null },
  { label: '满减券', value: 1 },
  { label: '折扣券', value: 2 },
  { label: '无门槛', value: 3 }
]

const shown = computed(() =>
  active.value === null ? list.value : list.value.filter((c) => c.type === active.value)
)

onMounted(load)

async function load() {
  loading.value = true
  try {
    list.value = (await couponApi.available()) || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function receive(c) {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: '/coupon' } })
    return
  }
  try {
    await couponApi.receive(c.id)
    ElMessage.success('领取成功，可在「我的优惠券」查看')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}

function usedPercent(c) {
  const total = Number(c.total || 0)
  if (!total) return 0
  const used = Number(c.received || 0)
  return Math.min(100, Math.round((used / total) * 100))
}

function formatDate(v) {
  if (!v) return '长期有效'
  return String(v).replace('T', ' ').slice(0, 10)
}

function go(path) {
  if (path.startsWith('/user') && !userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: path } })
    return
  }
  router.push(path)
}
</script>

<style scoped>
.coupon {
  padding-top: 16px;
}
.coupon__crumb {
  margin-bottom: 14px;
}
.coupon__banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26px 30px;
  border-radius: var(--abc-radius);
  color: #fff;
  background: linear-gradient(120deg, #ff5000, #ff8a00 70%, #ffb300);
  box-shadow: 0 12px 28px rgba(255, 80, 0, 0.24);
}
.coupon__banner-text h2 {
  margin: 0;
  font-size: 24px;
  letter-spacing: 1px;
}
.coupon__banner-text p {
  margin: 8px 0 0;
  font-size: 13px;
  opacity: 0.92;
}
.coupon__banner-btn {
  background: rgba(255, 255, 255, 0.94);
  border: none;
  color: var(--abc-primary);
  font-weight: 600;
}
.coupon__tabs {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 20px 0 14px;
}
.coupon__tab {
  padding: 7px 18px;
  border-radius: 18px;
  background: #fff;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid var(--abc-border);
  transition: all 0.2s;
}
.coupon__tab:hover {
  color: var(--abc-primary);
}
.coupon__tab.active {
  color: #fff;
  background: var(--abc-primary);
  border-color: var(--abc-primary);
}
.coupon__count {
  margin-left: auto;
  font-size: 13px;
  color: var(--abc-text-3);
}
.coupon__list {
  display: grid;
  gap: 14px;
  min-height: 160px;
}
.c-item {
  display: grid;
  grid-template-columns: 168px 1fr 180px;
  align-items: center;
  background: #fff;
  border-radius: var(--abc-radius);
  overflow: hidden;
  box-shadow: var(--abc-shadow);
  transition: box-shadow 0.25s, transform 0.25s;
}
.c-item:hover {
  box-shadow: var(--abc-shadow-hover);
  transform: translateY(-2px);
}
.c-item--sold {
  opacity: 0.62;
}
.c-item__left {
  align-self: stretch;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
}
.c-item__value i {
  font-size: 15px;
  font-style: normal;
  font-weight: 600;
}
.c-item__value b {
  font-size: 34px;
  font-weight: 800;
  line-height: 1;
}
.c-item__cond {
  font-size: 12px;
  opacity: 0.95;
}
.c-item__mid {
  padding: 16px 20px;
}
.c-item__mid h4 {
  margin: 0 0 6px;
  font-size: 16px;
}
.c-item__desc {
  margin: 0 0 8px;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.c-item__time {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--abc-text-3);
  display: flex;
  align-items: center;
  gap: 4px;
}
.c-item__progress {
  max-width: 320px;
}
.c-item__stock {
  font-size: 12px;
  color: var(--abc-text-3);
}
.c-item__right {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding-right: 20px;
}
.c-item__limit {
  font-size: 12px;
  color: var(--abc-text-3);
}
.coupon__rule {
  margin-top: 22px;
  padding: 20px 24px;
}
.coupon__rule h3 {
  margin: 0 0 10px;
  font-size: 16px;
}
.coupon__rule ul {
  margin: 0;
  padding-left: 18px;
  color: var(--abc-text-2);
  font-size: 13px;
  line-height: 1.9;
}
@media (max-width: 860px) {
  .c-item {
    grid-template-columns: 120px 1fr;
  }
  .c-item__right {
    grid-column: 1 / -1;
    flex-direction: row;
    justify-content: space-between;
    padding: 0 20px 16px;
  }
}
</style>
