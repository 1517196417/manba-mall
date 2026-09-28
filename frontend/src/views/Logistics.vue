<template>
  <div v-loading="loading" class="abc-container page">
    <el-breadcrumb separator="/" class="page__crumb">
      <el-breadcrumb-item :to="{ path: '/user/orders' }">我的订单</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: `/order/${orderId}` }">订单详情</el-breadcrumb-item>
      <el-breadcrumb-item>物流跟踪</el-breadcrumb-item>
    </el-breadcrumb>

    <template v-if="info.logistics">
      <div class="abc-card block head">
        <div class="head__left">
          <div class="head__icon"><el-icon :size="26"><Van /></el-icon></div>
          <div>
            <h2>{{ info.logistics.company }}</h2>
            <p>运单号：{{ info.logistics.trackingNo }}</p>
          </div>
        </div>
        <div class="head__right">
          <el-tag :type="statusType" effect="dark" size="large">
            {{ info.statusText || '运输中' }}
          </el-tag>
          <el-button size="small" @click="copyTracking">复制运单号</el-button>
        </div>
      </div>

      <div class="two-col">
        <div class="abc-card block">
          <h3 class="block__title">物流轨迹</h3>
          <el-timeline class="timeline">
            <el-timeline-item v-for="(t, idx) in info.traces" :key="t.id"
                              :timestamp="formatTime(t.traceTime)"
                              :type="idx === 0 ? 'primary' : ''"
                              :hollow="idx !== 0"
                              placement="top">
              <div class="trace" :class="{ 'trace--active': idx === 0 }">{{ t.context }}</div>
            </el-timeline-item>
          </el-timeline>
          <EmptyBox v-if="!info.traces || !info.traces.length" icon="Van" text="暂无物流轨迹" />
        </div>

        <div class="abc-card block">
          <h3 class="block__title">收货信息</h3>
          <div class="kv"><span>收货人</span><b>{{ info.receiver }}</b></div>
          <div class="kv"><span>联系电话</span><b>{{ info.receiverPhone }}</b></div>
          <div class="kv kv--wrap"><span>收货地址</span><b>{{ info.receiverAddr }}</b></div>
          <div class="kv"><span>订单编号</span><b>{{ info.orderNo }}</b></div>

          <h3 class="block__title block__title--gap">快递公司</h3>
          <div class="companies">
            <el-tag v-for="c in companies" :key="c.code" :type="c.name === info.logistics.company ? 'primary' : 'info'"
                    effect="plain">{{ c.name }}</el-tag>
          </div>

          <el-alert type="info" :closable="false" class="tip" show-icon
                    title="需求中「门店自提 / 同城配送 / 预约配送 / 配送范围 / 运费模板」标注为功能说明页，点击下方链接可查看功能定位。" />
          <div class="links">
            <el-link type="primary" :underline="false" @click="$router.push('/feature/store-pickup')">门店自提</el-link>
            <el-link type="primary" :underline="false" @click="$router.push('/feature/city-delivery')">同城配送</el-link>
            <el-link type="primary" :underline="false" @click="$router.push('/feature/appointment-delivery')">预约配送</el-link>
            <el-link type="primary" :underline="false" @click="$router.push('/feature/delivery-range')">配送范围</el-link>
            <el-link type="primary" :underline="false" @click="$router.push('/feature/freight-template')">运费模板</el-link>
          </div>
        </div>
      </div>
    </template>

    <EmptyBox v-else-if="!loading" icon="Van" text="该订单暂无物流信息" action-text="返回订单"
              @action="$router.push(`/order/${orderId}`)" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Van } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { logisticsApi } from '@/api'

const route = useRoute()
const router = useRouter()

const orderId = computed(() => Number(route.params.id))
const loading = ref(true)
const info = ref({ traces: [] })
const companies = ref([])

const statusType = computed(
  () => ({ 已签收: 'success', 派送中: 'warning', 运输中: 'primary', 已揽收: 'info' }[info.value.statusText] || 'primary')
)

onMounted(async () => {
  try {
    const [detail, list] = await Promise.all([
      logisticsApi.detail(orderId.value),
      logisticsApi.companies().catch(() => [])
    ])
    info.value = detail
    companies.value = list || []
  } catch (e) {
    info.value = {}
  } finally {
    loading.value = false
  }
})

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}

function copyTracking() {
  const no = info.value.logistics?.trackingNo || ''
  if (navigator.clipboard) {
    navigator.clipboard.writeText(no).then(() => ElMessage.success('运单号已复制'))
  }
}
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.page__crumb {
  margin-bottom: 14px;
}
.block {
  margin-top: 14px;
  padding: 18px 20px;
}
.block__title {
  margin: 0 0 14px;
  font-size: 16px;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
.block__title--gap {
  margin-top: 22px;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.head__left {
  display: flex;
  align-items: center;
  gap: 14px;
}
.head__icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
}
.head h2 {
  margin: 0;
  font-size: 18px;
}
.head p {
  margin: 4px 0 0;
  color: var(--abc-text-3);
  font-size: 13px;
}
.head__right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.timeline {
  padding-left: 4px;
}
.trace {
  font-size: 13px;
  color: var(--abc-text-2);
  line-height: 20px;
}
.trace--active {
  color: var(--abc-primary);
  font-weight: 600;
}
.two-col {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
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
.kv--wrap b {
  max-width: 60%;
}
.companies {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.tip {
  margin-top: 20px;
}
.links {
  margin-top: 12px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
}
@media (max-width: 900px) {
  .two-col {
    grid-template-columns: 1fr;
  }
}
</style>
