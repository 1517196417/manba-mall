<template>
  <div class="orders">
    <div class="orders__head">
      <div>
        <h2>我的订单</h2>
        <p>共 {{ total }} 笔订单</p>
      </div>
      <el-input v-model="keyword" placeholder="搜索订单号或商品名称" clearable
                :prefix-icon="Search" style="width: 260px" @keyup.enter="reload" @clear="reload" />
    </div>

    <el-tabs v-model="activeTab" @tab-change="reload">
      <el-tab-pane v-for="t in tabs" :key="String(t.value)" :label="t.label" :name="String(t.value)" />
    </el-tabs>

    <div v-loading="loading" class="orders__list">
      <div v-for="vo in list" :key="vo.order.id" class="o-card">
        <div class="o-card__head">
          <div class="o-card__head-left">
            <span class="o-card__no">订单号：{{ vo.order.orderNo }}</span>
            <span class="o-card__time">{{ formatTime(vo.order.createTime) }}</span>
          </div>
          <span class="o-card__status" :class="`o-card__status--${vo.order.orderStatus}`">
            {{ statusText(vo.order.orderStatus) }}
          </span>
        </div>

        <div class="o-card__items" @click="go(`/order/${vo.order.id}`)">
          <div v-for="it in vo.items" :key="it.id" class="o-item">
            <div class="o-item__img abc-img-box">
              <img :src="it.image || fallback" :alt="it.productName" />
            </div>
            <div class="o-item__info">
              <h4 class="abc-ellipsis-2">{{ it.productName }}</h4>
              <p class="o-item__sku abc-ellipsis">{{ it.skuDesc || '默认规格' }}</p>
              <div class="o-item__price">
                <MoneyText :value="it.price" size="small" />
                <span class="o-item__qty">× {{ it.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="o-card__foot">
          <div class="o-card__amount">
            <span>共 {{ vo.totalQuantity }} 件，实付</span>
            <MoneyText :value="vo.order.payAmount" />
            <span v-if="Number(vo.order.freight) > 0" class="o-card__freight">
              （含运费 ¥{{ Number(vo.order.freight).toFixed(2) }}）
            </span>
          </div>
          <div class="o-card__ops">
            <el-button size="small" round @click="go(`/order/${vo.order.id}`)">订单详情</el-button>

            <template v-if="vo.order.orderStatus === 0">
              <el-button size="small" round @click="cancel(vo)">取消订单</el-button>
              <el-button size="small" type="primary" round @click="go(`/pay/${vo.order.id}`)">去支付</el-button>
            </template>

            <template v-else-if="vo.order.orderStatus === 1">
              <el-button size="small" round @click="remind">提醒发货</el-button>
            </template>

            <template v-else-if="vo.order.orderStatus === 2">
              <el-button size="small" round @click="go(`/order/${vo.order.id}/logistics`)">查看物流</el-button>
              <el-button size="small" type="primary" round @click="receive(vo)">确认收货</el-button>
            </template>

            <template v-else-if="vo.order.orderStatus === 3">
              <el-button size="small" round @click="go(`/comment/create?orderId=${vo.order.id}`)">去评价</el-button>
            </template>

            <template v-else>
              <el-button size="small" round @click="rebuy(vo)">再次购买</el-button>
              <el-button size="small" round @click="remove(vo)">删除订单</el-button>
            </template>
          </div>
        </div>
      </div>
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="List" text="还没有相关订单" action-text="去逛逛"
              @action="go('/products')" />

    <div v-if="total > size" class="orders__pager">
      <el-pagination background layout="prev, pager, next" :total="total"
                     :page-size="size" :current-page="page" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import MoneyText from '@/components/MoneyText.vue'
import { orderApi, placeholderImage } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const fallback = placeholderImage('商品', 200, 200, 7)

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 5
const keyword = ref('')
const activeTab = ref('null')
const tabs = ref([{ label: '全部', value: null }])

onMounted(async () => {
  try {
    const dict = (await orderApi.statusDict()) || []
    tabs.value = [{ label: '全部', value: null }, ...dict.map((d) => ({ label: d.name, value: d.code }))]
  } catch (e) {
    tabs.value = [
      { label: '全部', value: null },
      { label: '待付款', value: 0 },
      { label: '待发货', value: 1 },
      { label: '待收货', value: 2 },
      { label: '待评价', value: 3 }
    ]
  }
  const qs = route.query.status
  if (qs !== undefined && qs !== null && qs !== '') {
    activeTab.value = String(qs)
  }
  await load()
})

watch(() => route.query.status, (v) => {
  if (v !== undefined && v !== null && v !== '') {
    activeTab.value = String(v)
    reload()
  }
})

async function load() {
  loading.value = true
  try {
    const status = activeTab.value === 'null' ? null : Number(activeTab.value)
    const res = await orderApi.page({ status, keyword: keyword.value || null, page: page.value, size })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function reload() {
  page.value = 1
  load()
}

function onPage(p) {
  page.value = p
  load()
}

const STATUS_TEXT = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '待评价',
  4: '已完成',
  5: '已取消',
  6: '售后中'
}

function statusText(s) {
  return STATUS_TEXT[s] || '未知状态'
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

async function cancel(vo) {
  let reason = ''
  try {
    const { value } = await ElMessageBox.prompt('请选择或填写取消原因', '取消订单', {
      inputValue: '不想要了',
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想'
    })
    reason = value || '不想要了'
  } catch (e) {
    return
  }
  try {
    await orderApi.cancel(vo.order.id, reason)
    ElMessage.success('订单已取消')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}

async function receive(vo) {
  try {
    await ElMessageBox.confirm('确认已收到商品？确认后订单将进入待评价状态。', '确认收货', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await orderApi.receive(vo.order.id)
    ElMessage.success('确认收货成功')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}

async function rebuy(vo) {
  try {
    await orderApi.rebuy(vo.order.id)
    ElMessage.success('已加入购物车')
    userStore.refreshBadges()
    router.push('/cart')
  } catch (e) {
    /* 已提示 */
  }
}

async function remove(vo) {
  try {
    await ElMessageBox.confirm('删除后订单记录不可恢复，确定删除吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await orderApi.remove(vo.order.id)
    ElMessage.success('订单已删除')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}

function remind() {
  ElMessage.success('已提醒商家尽快发货')
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.orders__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}
.orders__head h2 {
  margin: 0;
  font-size: 20px;
}
.orders__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.orders__list {
  display: grid;
  gap: 14px;
  min-height: 200px;
}
.o-card {
  border: 1px solid var(--abc-border);
  border-radius: 12px;
  overflow: hidden;
}
.o-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  background: #fafbfc;
  border-bottom: 1px solid var(--abc-border);
}
.o-card__head-left {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.o-card__no {
  color: var(--abc-text-2);
}
.o-card__status {
  font-size: 13px;
  font-weight: 700;
  color: var(--abc-primary);
}
.o-card__status--5 {
  color: var(--abc-text-3);
}
.o-card__status--4 {
  color: var(--abc-success);
}
.o-card__items {
  padding: 14px 18px;
  display: grid;
  gap: 12px;
  cursor: pointer;
}
.o-item {
  display: flex;
  gap: 12px;
}
.o-item__img {
  width: 76px;
  height: 76px;
  border-radius: 8px;
  flex-shrink: 0;
}
.o-item__info {
  flex: 1;
  min-width: 0;
}
.o-item__info h4 {
  margin: 0 0 4px;
  font-size: 14px;
  line-height: 1.45;
}
.o-item__sku {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.o-item__price {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.o-item__qty {
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.o-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 14px 18px;
  border-top: 1px solid var(--abc-border);
}
.o-card__amount {
  display: flex;
  align-items: baseline;
  gap: 6px;
  font-size: 13px;
  color: var(--abc-text-2);
}
.o-card__freight {
  font-size: 12px;
  color: var(--abc-text-3);
}
.o-card__ops {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.orders__pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
