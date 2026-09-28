<template>
  <div v-loading="loading" class="abc-container page">
    <el-breadcrumb separator="/" class="page__crumb">
      <el-breadcrumb-item :to="{ path: '/user/orders' }">我的订单</el-breadcrumb-item>
      <el-breadcrumb-item>订单详情</el-breadcrumb-item>
    </el-breadcrumb>

    <template v-if="data.order">
      <!-- 状态条 -->
      <div class="status abc-card">
        <div class="status__left">
          <el-icon :size="40" class="status__icon"><component :is="statusIcon" /></el-icon>
          <div>
            <h2>{{ statusText }}<span v-if="needPay" class="status__tip">请在 30 分钟内完成支付</span></h2>
            <p>{{ statusDesc }}</p>
          </div>
        </div>
        <div class="status__actions">
          <el-button v-if="needPay" type="primary" @click="$router.push(`/pay/${orderId}`)">立即支付</el-button>
          <el-button v-if="needPay" @click="cancelOrder">取消订单</el-button>
          <el-button v-if="canReceive" type="primary" @click="confirmReceive">确认收货</el-button>
          <el-button v-if="canComment" type="primary" @click="goComment">去评价</el-button>
          <el-button v-if="canTrack" @click="$router.push(`/order/${orderId}/logistics`)">查看物流</el-button>
          <el-button v-if="canAfterSale" @click="goAfterSale">申请售后</el-button>
          <el-button @click="rebuy">再次购买</el-button>
          <el-button v-if="canDelete" type="danger" plain @click="deleteOrder">删除订单</el-button>
        </div>
      </div>

      <!-- 物流摘要 -->
      <div v-if="data.logistics" class="abc-card block logistics-tip">
        <el-icon><Van /></el-icon>
        <div>
          <b>{{ data.logistics.company }} {{ data.logistics.trackingNo }}</b>
          <span>{{ latestTrace || '暂无轨迹信息' }}</span>
        </div>
        <el-button size="small" @click="$router.push(`/order/${orderId}/logistics`)">查看详情</el-button>
      </div>

      <!-- 进度 -->
      <div class="abc-card block">
        <h3 class="block__title">订单进度</h3>
        <el-steps :active="stepActive" align-center class="progress">
          <el-step title="提交订单" :description="formatTime(data.order.createTime)" />
          <el-step title="付款成功" :description="formatTime(data.order.payTime) || '待支付'" />
          <el-step title="商品出库" :description="formatTime(data.order.deliveryTime) || '待发货'" />
          <el-step title="确认收货" :description="formatTime(data.order.receiveTime) || '待收货'" />
          <el-step title="交易完成" :description="formatTime(data.order.finishTime) || '待完成'" />
        </el-steps>
      </div>

      <!-- 商品明细 -->
      <div class="abc-card block">
        <div class="block__head">
          <h3 class="block__title">商品明细</h3>
          <span class="block__sub">共 {{ data.totalQuantity }} 件</span>
        </div>
        <div v-for="item in data.items" :key="item.id" class="goods">
          <div class="goods__img abc-img-box" @click="$router.push(`/product/${item.productId}`)">
            <img :src="item.image" :alt="item.productName" />
          </div>
          <div class="goods__info">
            <div class="goods__name" @click="$router.push(`/product/${item.productId}`)">
              {{ item.productName }}
            </div>
            <div class="goods__sku">{{ item.skuDesc }}</div>
            <div class="goods__tags">
              <el-tag v-if="item.reviewStatus === 1" size="small" type="success" effect="plain">已评价</el-tag>
              <el-tag v-else-if="['3', '4'].includes(String(orderStatus))" size="small" type="warning" effect="plain">
                待评价
              </el-tag>
              <el-tag v-if="item.refundStatus === 1" size="small" type="danger" effect="plain">售后中</el-tag>
            </div>
          </div>
          <div class="goods__price">
            <MoneyText :value="item.price" size="small" />
            <span class="goods__qty">× {{ item.quantity }}</span>
          </div>
          <div class="goods__subtotal"><MoneyText :value="item.totalPrice" /></div>
          <div class="goods__op">
            <el-button v-if="['3', '4'].includes(String(orderStatus)) && item.reviewStatus !== 1"
                       size="small" type="primary" plain @click="goComment(item)">评价</el-button>
            <el-button v-if="['2', '3', '4'].includes(String(orderStatus))" size="small"
                       @click="goAfterSale(item)">申请售后</el-button>
          </div>
        </div>
      </div>

      <!-- 金额与信息 -->
      <div class="two-col">
        <div class="abc-card block">
          <h3 class="block__title">收货信息</h3>
          <div class="kv"><span>收货人</span><b>{{ data.order.receiver }}</b></div>
          <div class="kv"><span>联系电话</span><b>{{ data.order.receiverPhone }}</b></div>
          <div class="kv"><span>收货地址</span><b>{{ data.order.receiverAddr }}</b></div>
          <div class="kv"><span>配送方式</span><b>{{ deliveryText }}</b></div>
          <div class="kv"><span>订单备注</span><b>{{ data.order.remark || '无' }}</b></div>
          <div class="kv"><span>发票信息</span><b>{{ data.order.invoiceTitle || '未开具' }}</b></div>
        </div>

        <div class="abc-card block">
          <h3 class="block__title">订单信息</h3>
          <div class="kv"><span>订单编号</span><b>{{ data.order.orderNo }}</b></div>
          <div class="kv"><span>下单时间</span><b>{{ formatTime(data.order.createTime) }}</b></div>
          <div class="kv"><span>支付方式</span><b>{{ payTypeText }}</b></div>
          <div class="kv"><span>商品总额</span><b>¥{{ money(data.order.totalAmount) }}</b></div>
          <div class="kv"><span>运费</span><b>¥{{ money(data.order.freight) }}</b></div>
          <div class="kv"><span>优惠抵扣</span><b class="deduct">- ¥{{ money(data.order.discountAmount) }}</b></div>
          <div class="kv kv--total">
            <span>实付金额</span>
            <MoneyText :value="data.order.payAmount" size="large" />
          </div>
        </div>
      </div>
    </template>

    <EmptyBox v-else-if="!loading" icon="WarningFilled" text="订单不存在" action-text="返回订单列表"
              @action="$router.push('/user/orders')" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CircleCheckFilled, Clock, CloseBold, Money, Van, Wallet
} from '@element-plus/icons-vue'
import MoneyText from '@/components/MoneyText.vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { logisticsApi, orderApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const data = ref({ items: [] })
const latestTrace = ref('')

const orderId = computed(() => Number(route.params.id))
const orderStatus = computed(() => String(data.value.order?.orderStatus ?? ''))

const statusText = computed(
  () =>
    ({
      0: '等待付款',
      1: '等待发货',
      2: '卖家已发货',
      3: '交易成功',
      4: '交易完成',
      5: '订单已取消',
      6: '售后处理中'
    }[orderStatus.value] || '订单状态')
)

const statusDesc = computed(
  () =>
    ({
      0: '请在下单后 30 分钟内完成支付，超时订单将自动取消',
      1: '商家正在为您打包商品，请耐心等待',
      2: '商品已在路上，请留意物流信息并及时签收',
      3: '感谢您的购买，评价可获得 50 积分奖励',
      4: '订单已完成，期待您的再次光临',
      5: `订单已取消${data.value.order?.cancelReason ? '：' + data.value.order.cancelReason : ''}`,
      6: '售后正在处理中，请留意消息通知'
    }[orderStatus.value] || '')
)

const statusIcon = computed(
  () =>
    ({
      0: Clock,
      1: Wallet,
      2: Van,
      3: CircleCheckFilled,
      4: CircleCheckFilled,
      5: CloseBold,
      6: Money
    }[orderStatus.value] || Clock)
)

const needPay = computed(() => orderStatus.value === '0')
const canReceive = computed(() => orderStatus.value === '2')
const canComment = computed(() => ['3', '4'].includes(orderStatus.value))
const canTrack = computed(() => ['2', '3', '4'].includes(orderStatus.value))
const canAfterSale = computed(() => ['2', '3', '4'].includes(orderStatus.value))
const canDelete = computed(() => ['4', '5'].includes(orderStatus.value))

const stepActive = computed(() => {
  const map = { 0: 1, 1: 2, 2: 3, 3: 4, 4: 5, 5: 1, 6: 4 }
  return map[orderStatus.value] || 1
})

const deliveryText = computed(
  () => ({ 1: '快递配送', 2: '门店自提（功能说明页）', 3: '同城配送（功能说明页）' }[String(data.value.order?.deliveryType)] || '快递配送')
)

const payTypeText = computed(
  () =>
    ({
      1: '微信支付',
      2: '支付宝',
      3: '银联支付',
      4: '余额支付'
    }[String(data.value.order?.payType)] || '未支付')
)

onMounted(load)
watch(() => route.params.id, () => route.name === 'OrderDetail' && load())

async function load() {
  loading.value = true
  try {
    data.value = await orderApi.detail(orderId.value)
    if (data.value.logistics) {
      try {
        const traces = await logisticsApi.traces(orderId.value)
        latestTrace.value = traces && traces[0] ? traces[0].context : ''
      } catch (e) {
        latestTrace.value = ''
      }
    }
  } catch (e) {
    data.value = {}
  } finally {
    loading.value = false
  }
}

function money(v) {
  return Number(v || 0).toFixed(2)
}

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}

async function cancelOrder() {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？取消后优惠券与积分会退回。', '取消订单', {
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await orderApi.cancel(orderId.value, '用户主动取消')
  ElMessage.success('订单已取消')
  userStore.refreshInfo()
  await load()
}

async function confirmReceive() {
  try {
    await ElMessageBox.confirm('请确认已收到商品，确认后订单将进入待评价状态。', '确认收货', {
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await orderApi.receive(orderId.value)
  ElMessage.success('确认收货成功，快去评价赚积分吧')
  userStore.refreshInfo()
  await load()
}

async function deleteOrder() {
  try {
    await ElMessageBox.confirm('确定要删除该订单吗？删除后不可恢复。', '删除订单', { type: 'warning' })
  } catch (e) {
    return
  }
  await orderApi.remove(orderId.value)
  ElMessage.success('订单已删除')
  router.replace('/user/orders')
}

async function rebuy() {
  const res = await orderApi.rebuy(orderId.value)
  ElMessage.success(`已加入购物车 ${res.addedCount} 件商品`)
  userStore.refreshBadges()
  router.push('/cart')
}

function goComment(item) {
  if (item) {
    router.push({ path: '/comment/create', query: { itemId: item.id } })
    return
  }
  router.push({ path: '/comment/create', query: { orderId: orderId.value } })
}

function goAfterSale(item) {
  router.push({
    path: '/aftersale/apply',
    query: item ? { itemId: item.id } : { orderId: orderId.value }
  })
}
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.page__crumb {
  margin-bottom: 14px;
}
.status {
  padding: 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
  background: linear-gradient(135deg, #fff6f2, #fffaf6);
}
.status__left {
  display: flex;
  align-items: center;
  gap: 16px;
}
.status__icon {
  color: var(--abc-primary);
}
.status h2 {
  margin: 0;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.status__tip {
  font-size: 12px;
  color: #f56c6c;
  font-weight: 400;
}
.status p {
  margin: 6px 0 0;
  color: var(--abc-text-2);
  font-size: 13px;
}
.status__actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.block {
  margin-top: 14px;
  padding: 18px 20px;
}
.block__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.block__title {
  margin: 0 0 12px;
  font-size: 16px;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
.block__sub {
  color: var(--abc-text-3);
  font-size: 13px;
}
.logistics-tip {
  display: flex;
  align-items: center;
  gap: 12px;
  color: var(--abc-primary);
}
.logistics-tip div {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.logistics-tip b {
  color: var(--abc-text);
  font-size: 14px;
}
.logistics-tip span {
  font-size: 13px;
  color: var(--abc-text-2);
  margin-top: 2px;
}
.progress {
  margin-top: 10px;
}
.goods {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f5f6f8;
}
.goods:last-child {
  border-bottom: none;
}
.goods__img {
  width: 84px;
  height: 84px;
  border-radius: 8px;
  flex-shrink: 0;
  cursor: pointer;
}
.goods__info {
  flex: 1;
  min-width: 0;
}
.goods__name {
  font-size: 14px;
  line-height: 20px;
  cursor: pointer;
  transition: color 0.2s;
}
.goods__name:hover {
  color: var(--abc-primary);
}
.goods__sku {
  margin-top: 4px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.goods__tags {
  margin-top: 6px;
  display: flex;
  gap: 6px;
}
.goods__price {
  width: 120px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.goods__qty {
  font-size: 12px;
  color: var(--abc-text-3);
  margin-top: 2px;
}
.goods__subtotal {
  width: 120px;
  text-align: center;
}
.goods__op {
  width: 160px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
}
.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.kv {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  padding: 7px 0;
  font-size: 13px;
  color: var(--abc-text-2);
}
.kv b {
  color: var(--abc-text);
  text-align: right;
}
.kv--total {
  border-top: 1px dashed var(--abc-border);
  margin-top: 8px;
  padding-top: 12px;
  font-size: 15px;
  color: var(--abc-text);
}
.deduct {
  color: var(--abc-primary);
}
@media (max-width: 900px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}
</style>
