<template>
  <div class="abc-container mall">
    <el-breadcrumb separator="/" class="mall__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>积分商城</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="mall__head">
      <div class="mall__head-left">
        <h2>积分商城</h2>
        <p>购物、签到、评价都能攒积分，积分可直接兑换好物</p>
      </div>
      <div class="mall__head-right">
        <div class="mall__stat">
          <span class="mall__stat-label">我的积分</span>
          <b>{{ userStore.isLogin ? (overview.points ?? userStore.points) : '--' }}</b>
        </div>
        <div class="mall__stat">
          <span class="mall__stat-label">已签到</span>
          <b>{{ userStore.isLogin ? (overview.continuousDays ?? 0) : '--' }}<i>天</i></b>
        </div>
        <div class="mall__stat">
          <span class="mall__stat-label">兑换记录</span>
          <b>{{ userStore.isLogin ? (overview.exchangeCount ?? 0) : '--' }}<i>笔</i></b>
        </div>
        <el-button type="primary" round @click="go('/sign')">去签到攒积分</el-button>
      </div>
    </div>

    <div class="mall__toolbar">
      <span class="mall__count">共 {{ total }} 件可兑换商品</span>
      <el-link :underline="false" type="primary" @click="showExchange = true">
        我的兑换记录（{{ exchanges.length }}）
      </el-link>
    </div>

    <div v-loading="loading" class="mall__grid">
      <div v-for="g in list" :key="g.id" class="g-item">
        <div class="g-item__img abc-img-box">
          <img :src="g.image || fallback" :alt="g.name" />
          <span class="g-item__stock">剩余 {{ g.stock }} 件</span>
        </div>
        <div class="g-item__body">
          <h4 class="abc-ellipsis-2">{{ g.name }}</h4>
          <p class="g-item__desc abc-ellipsis">{{ g.description || '积分好礼，先兑先得' }}</p>
          <div class="g-item__price">
            <span class="g-item__points">{{ g.points }}<i>积分</i></span>
            <span class="abc-market-price">¥{{ Number(g.marketPrice || 0).toFixed(2) }}</span>
          </div>
          <div class="g-item__foot">
            <span class="g-item__exchanged">已兑 {{ g.exchanged || 0 }}</span>
            <el-button size="small" type="primary" round :disabled="g.stock <= 0" @click="exchange(g)">
              立即兑换
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="Present" text="积分商城暂时没有可兑换的商品" />
    <div v-if="total > size" class="mall__pager">
      <el-pagination background layout="prev, pager, next" :total="total"
                     :page-size="size" :current-page="page" @current-change="onPage" />
    </div>

    <!-- 兑换记录 -->
    <el-drawer v-model="showExchange" title="我的兑换记录" size="520px">
      <div v-if="exchanges.length" class="ex-list">
        <div v-for="e in exchanges" :key="e.id" class="ex-item">
          <div class="ex-item__img abc-img-box">
            <img :src="e.image || fallback" :alt="e.goodsName" />
          </div>
          <div class="ex-item__info">
            <h5 class="abc-ellipsis">{{ e.goodsName }}</h5>
            <p class="ex-item__time">{{ formatTime(e.createTime) }}</p>
            <p class="ex-item__express" v-if="e.expressNo">快递单号：{{ e.expressNo }}</p>
          </div>
          <div class="ex-item__right">
            <span class="ex-item__points">{{ e.points }} 积分</span>
            <el-tag size="small" :type="e.status === 0 ? 'warning' : e.status === 1 ? 'primary' : 'success'">
              {{ e.status === 0 ? '待发货' : e.status === 1 ? '已发货' : '已完成' }}
            </el-tag>
          </div>
        </div>
      </div>
      <EmptyBox v-else icon="Present" text="还没有兑换记录" action-text="去兑换" @action="showExchange = false" />
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import EmptyBox from '@/components/EmptyBox.vue'
import { placeholderImage, pointsApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const fallback = placeholderImage('积分好礼', 400, 400, 6)

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 12

const overview = ref({})
const exchanges = ref([])
const showExchange = ref(false)

onMounted(async () => {
  await Promise.all([loadGoods(), loadMine()])
})

async function loadGoods() {
  loading.value = true
  try {
    const res = await pointsApi.goods({ page: page.value, size })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function loadMine() {
  if (!userStore.isLogin) return
  try {
    overview.value = (await pointsApi.overview()) || {}
    exchanges.value = (await pointsApi.exchanges()) || []
  } catch (e) {
    /* 忽略 */
  }
}

async function exchange(g) {
  if (!userStore.isLogin) {
    router.push({ path: '/login', query: { redirect: '/points-mall' } })
    return
  }
  const points = Number(overview.value.points ?? userStore.points ?? 0)
  if (points < g.points) {
    ElMessage.warning(`积分不足，还差 ${g.points - points} 积分`)
    return
  }
  try {
    await pointsApi.exchange(g.id)
    ElMessage.success('兑换成功，请留意发货通知')
    await Promise.all([loadGoods(), loadMine()])
    await userStore.refreshInfo()
  } catch (e) {
    /* 已提示 */
  }
}

function onPage(p) {
  page.value = p
  loadGoods()
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.mall {
  padding-top: 16px;
}
.mall__crumb {
  margin-bottom: 14px;
}
.mall__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  padding: 24px 28px;
  border-radius: var(--abc-radius);
  color: #fff;
  background: linear-gradient(120deg, #ff7a00, #ffb300 70%, #ffd54f);
  box-shadow: 0 12px 28px rgba(255, 149, 0, 0.26);
}
.mall__head-left h2 {
  margin: 0;
  font-size: 24px;
  letter-spacing: 1px;
}
.mall__head-left p {
  margin: 8px 0 0;
  font-size: 13px;
  opacity: 0.94;
}
.mall__head-right {
  display: flex;
  align-items: center;
  gap: 26px;
  flex-wrap: wrap;
}
.mall__stat {
  display: flex;
  flex-direction: column;
}
.mall__stat-label {
  font-size: 12px;
  opacity: 0.9;
}
.mall__stat b {
  font-size: 22px;
  font-weight: 800;
}
.mall__stat b i {
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  margin-left: 2px;
}
.mall__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 20px 0 14px;
  font-size: 13px;
  color: var(--abc-text-3);
}
.mall__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 180px;
}
.g-item {
  background: #fff;
  border-radius: var(--abc-radius);
  overflow: hidden;
  box-shadow: var(--abc-shadow);
  transition: box-shadow 0.25s, transform 0.25s;
}
.g-item:hover {
  box-shadow: var(--abc-shadow-hover);
  transform: translateY(-3px);
}
.g-item__img {
  height: 190px;
  position: relative;
}
.g-item__stock {
  position: absolute;
  left: 8px;
  bottom: 8px;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
}
.g-item__body {
  padding: 12px 14px 14px;
}
.g-item__body h4 {
  margin: 0 0 6px;
  font-size: 14px;
  line-height: 1.4;
  min-height: 39px;
}
.g-item__desc {
  margin: 0 0 10px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.g-item__price {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.g-item__points {
  color: var(--abc-price);
  font-weight: 800;
  font-size: 20px;
  font-family: Arial, Helvetica, sans-serif;
}
.g-item__points i {
  font-size: 12px;
  font-style: normal;
  font-weight: 500;
  margin-left: 2px;
}
.g-item__foot {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.g-item__exchanged {
  font-size: 12px;
  color: var(--abc-text-3);
}
.mall__pager {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}
.ex-list {
  display: grid;
  gap: 12px;
}
.ex-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--abc-border);
  border-radius: 10px;
}
.ex-item__img {
  width: 62px;
  height: 62px;
  border-radius: 8px;
  flex-shrink: 0;
}
.ex-item__info {
  flex: 1;
  min-width: 0;
}
.ex-item__info h5 {
  margin: 0 0 4px;
  font-size: 14px;
}
.ex-item__time,
.ex-item__express {
  margin: 0;
  font-size: 12px;
  color: var(--abc-text-3);
}
.ex-item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}
.ex-item__points {
  font-size: 13px;
  font-weight: 700;
  color: var(--abc-price);
}
@media (max-width: 1180px) {
  .mall__grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 820px) {
  .mall__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
