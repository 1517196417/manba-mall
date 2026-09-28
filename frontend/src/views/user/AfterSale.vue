<template>
  <div class="as">
    <div class="as__head">
      <div>
        <h2>我的售后</h2>
        <p>退货 / 退款 / 换货申请与处理进度都会在这里展示</p>
      </div>
      <el-button round @click="go('/user/orders')">查看我的订单</el-button>
    </div>

    <div class="as__tabs">
      <span v-for="t in tabs" :key="String(t.value)" class="as__tab"
            :class="{ active: status === t.value }" @click="changeStatus(t.value)">{{ t.label }}</span>
    </div>

    <div v-loading="loading" class="as__list">
      <div v-for="s in list" :key="s.id" class="s-item">
        <div class="s-item__head">
          <div class="s-item__head-left">
            <span class="s-item__no">售后单号：{{ s.serviceNo }}</span>
            <el-tag size="small" type="info" round>{{ s.typeText || typeText(s.type) }}</el-tag>
          </div>
          <span class="s-item__status" :class="`s-item__status--${s.status}`">
            {{ s.statusText || statusText(s.status) }}
          </span>
        </div>

        <div class="s-item__body" @click="openDetail(s)">
          <div class="s-item__img abc-img-box">
            <img :src="s.image || fallback" :alt="s.productName" />
          </div>
          <div class="s-item__info">
            <h4 class="abc-ellipsis-2">{{ s.productName }}</h4>
            <p class="s-item__reason">售后原因：{{ s.reason || '未填写' }}</p>
            <p class="s-item__time">申请时间：{{ formatTime(s.createTime) }}</p>
            <p v-if="s.refundAmount" class="s-item__refund">
              退款金额：<b>¥{{ Number(s.refundAmount).toFixed(2) }}</b>
            </p>
          </div>
        </div>

        <div class="s-item__foot">
          <span class="s-item__order">关联订单号：{{ s.orderNo }}</span>
          <div class="s-item__ops">
            <el-button size="small" round @click="openDetail(s)">查看进度</el-button>
            <el-button v-if="s.status === 0" size="small" round type="danger" plain @click="cancel(s)">
              撤销申请
            </el-button>
            <el-button v-if="s.status === 6" size="small" type="primary" round @click="go(`/order/${s.orderId}`)">
              重新申请
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="RefreshLeft" text="没有相关售后单" action-text="查看我的订单"
              @action="go('/user/orders')" />

    <div v-if="total > size" class="as__pager">
      <el-pagination background layout="prev, pager, next" :total="total"
                     :page-size="size" :current-page="page" @current-change="onPage" />
    </div>

    <!-- 售后详情 -->
    <el-drawer v-model="drawer" title="售后详情" size="560px">
      <div v-if="detail.id" class="asd">
        <div class="asd__top">
          <span class="asd__no">售后单号：{{ detail.serviceNo }}</span>
          <el-tag size="small" type="warning" round>{{ detail.statusText || statusText(detail.status) }}</el-tag>
        </div>

        <div class="asd__product">
          <div class="asd__img abc-img-box">
            <img :src="detail.image || fallback" :alt="detail.productName" />
          </div>
          <div>
            <h4>{{ detail.productName }}</h4>
            <p class="asd__line">售后类型：{{ detail.typeText || typeText(detail.type) }}</p>
            <p class="asd__line">退款金额：¥{{ Number(detail.refundAmount || 0).toFixed(2) }}</p>
          </div>
        </div>

        <el-descriptions :column="1" border class="asd__desc">
          <el-descriptions-item label="关联订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="售后原因">{{ detail.reason || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="问题描述">{{ detail.description || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="商家备注">{{ detail.handleNote || '暂无' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatTime(detail.createTime) }}</el-descriptions-item>
        </el-descriptions>

        <div class="asd__progress">
          <h4>处理进度</h4>
          <el-timeline>
            <el-timeline-item v-for="(p, i) in progress" :key="i"
                              :timestamp="formatTime(p.createTime)"
                              :type="i === progress.length - 1 ? 'primary' : ''"
                              :hollow="i !== progress.length - 1">
              <div class="asd__node">
                <b>{{ p.title || p.statusText || `节点 ${i + 1}` }}</b>
                <p v-if="p.description">{{ p.description }}</p>
              </div>
            </el-timeline-item>
            <el-timeline-item v-if="!progress.length" timestamp="">
              <div class="asd__node"><b>等待商家处理</b><p>商家将在 48 小时内响应你的售后申请</p></div>
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="asd__tip">
          <el-icon><InfoFilled /></el-icon>
          <span>「退款进度」「客服介入」「仲裁」为需求标注的功能说明页，此处展示基础处理节点。</span>
        </div>
      </div>
      <EmptyBox v-else icon="RefreshLeft" text="加载中..." />
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { afterSaleApi, placeholderImage } from '@/api'

const router = useRouter()

const fallback = placeholderImage('商品', 200, 200, 7)

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 5
const status = ref(null)

const tabs = [
  { label: '全部', value: null },
  { label: '待审核', value: 0 },
  { label: '处理中', value: 3 },
  { label: '已完成', value: 5 },
  { label: '已撤销', value: 6 }
]

const drawer = ref(false)
const detail = ref({})
const progress = ref([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await afterSaleApi.page({ status: status.value, page: page.value, size })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function changeStatus(v) {
  status.value = v
  page.value = 1
  load()
}

function onPage(p) {
  page.value = p
  load()
}

const TYPE_TEXT = { 1: '退货', 2: '退款', 3: '换货', 4: '维修' }
const STATUS_TEXT = { 0: '待审核', 1: '已同意', 2: '已拒绝', 3: '退货中', 4: '退款中', 5: '已完成', 6: '已撤销' }

function typeText(t) {
  return TYPE_TEXT[t] || '售后'
}

function statusText(s) {
  return STATUS_TEXT[s] || '处理中'
}

async function openDetail(s) {
  drawer.value = true
  detail.value = {}
  progress.value = []
  try {
    const data = (await afterSaleApi.detail(s.id)) || {}
    detail.value = data
    progress.value = data.progressList || []
  } catch (e) {
    /* 已提示 */
  }
}

async function cancel(s) {
  try {
    await ElMessageBox.confirm('确定撤销该售后申请吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await afterSaleApi.cancel(s.id)
    ElMessage.success('已撤销售后申请')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.as__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
.as__head h2 {
  margin: 0;
  font-size: 20px;
}
.as__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.as__tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.as__tab {
  padding: 6px 18px;
  border-radius: 17px;
  font-size: 13.5px;
  cursor: pointer;
  border: 1px solid var(--abc-border);
  transition: all 0.2s;
}
.as__tab:hover {
  color: var(--abc-primary);
}
.as__tab.active {
  color: #fff;
  background: var(--abc-primary);
  border-color: var(--abc-primary);
}
.as__list {
  display: grid;
  gap: 14px;
  min-height: 180px;
}
.s-item {
  border: 1px solid var(--abc-border);
  border-radius: 12px;
  overflow: hidden;
}
.s-item__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 18px;
  background: #fafbfc;
  border-bottom: 1px solid var(--abc-border);
}
.s-item__head-left {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12.5px;
  color: var(--abc-text-2);
}
.s-item__status {
  font-size: 13px;
  font-weight: 700;
  color: var(--abc-primary);
}
.s-item__status--5 {
  color: var(--abc-success);
}
.s-item__status--6,
.s-item__status--2 {
  color: var(--abc-text-3);
}
.s-item__body {
  display: flex;
  gap: 14px;
  padding: 14px 18px;
  cursor: pointer;
}
.s-item__img {
  width: 76px;
  height: 76px;
  border-radius: 8px;
  flex-shrink: 0;
}
.s-item__info {
  flex: 1;
  min-width: 0;
}
.s-item__info h4 {
  margin: 0 0 6px;
  font-size: 14px;
  line-height: 1.45;
}
.s-item__reason,
.s-item__time {
  margin: 0 0 4px;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.s-item__refund {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-2);
}
.s-item__refund b {
  color: var(--abc-price);
}
.s-item__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 12px 18px;
  border-top: 1px solid var(--abc-border);
}
.s-item__order {
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.s-item__ops {
  display: flex;
  gap: 8px;
}
.as__pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.asd__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.asd__no {
  font-size: 13px;
  color: var(--abc-text-2);
}
.asd__product {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 12px;
  background: #fafbfc;
  margin-bottom: 16px;
}
.asd__img {
  width: 70px;
  height: 70px;
  border-radius: 8px;
  flex-shrink: 0;
}
.asd__product h4 {
  margin: 0 0 8px;
  font-size: 14.5px;
}
.asd__line {
  margin: 0 0 4px;
  font-size: 12.5px;
  color: var(--abc-text-2);
}
.asd__desc {
  margin-bottom: 20px;
}
.asd__progress h4 {
  margin: 0 0 14px;
  font-size: 15px;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
.asd__node b {
  font-size: 14px;
}
.asd__node p {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.asd__tip {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff8ec;
  color: #b26a00;
  font-size: 12.5px;
  line-height: 1.7;
}
</style>
