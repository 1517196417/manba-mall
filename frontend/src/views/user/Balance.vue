<template>
  <div v-loading="loading" class="bal">
    <div class="bal__head">
      <div>
        <h2>我的余额</h2>
        <p>余额可用于订单支付，也可与其它支付方式组合使用（标注「!」的功能为功能说明页）</p>
      </div>
      <el-button type="primary" round @click="go('/feature/pay-balance')">查看余额支付说明</el-button>
    </div>

    <div class="bal__hero">
      <div class="bal__hero-left">
        <span class="bal__hero-label">账户可用余额（元）</span>
        <b class="bal__hero-value">{{ balance }}</b>
        <div class="bal__hero-ops">
          <el-button round class="bal__hero-btn" @click="go('/feature/balance-deduct')">余额抵扣说明</el-button>
          <el-button round class="bal__hero-btn" @click="rechargeHint">充值</el-button>
        </div>
      </div>
      <div class="bal__hero-right">
        <div class="bal__hero-stat">
          <span>累计收入</span>
          <b>{{ income }}</b>
        </div>
        <div class="bal__hero-stat">
          <span>累计支出</span>
          <b>{{ outcome }}</b>
        </div>
      </div>
    </div>

    <div class="bal__records">
      <SectionTitle title="余额明细" subtitle="充值 / 消费 / 退款流水" />
      <el-table :data="records" row-key="id">
        <el-table-column prop="description" label="说明" min-width="240" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)" round>{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="金额变动（元）" width="150">
          <template #default="{ row }">
            <span :class="Number(row.amount) >= 0 ? 'bal__add' : 'bal__minus'">
              {{ Number(row.amount) >= 0 ? '+' : '' }}{{ Number(row.amount).toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <template #empty>
          <EmptyBox icon="Wallet" text="暂无余额流水" />
        </template>
      </el-table>

      <div v-if="total > size" class="bal__pager">
        <el-pagination background layout="prev, pager, next" :total="total"
                       :page-size="size" :current-page="page" @current-change="onPage" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import EmptyBox from '@/components/EmptyBox.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { userApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 10

const balance = computed(() => Number(userStore.balance || 0).toFixed(2))

const income = computed(() => {
  const sum = records.value
    .filter((r) => Number(r.amount) > 0)
    .reduce((s, r) => s + Number(r.amount), 0)
  return `¥${sum.toFixed(2)}`
})

const outcome = computed(() => {
  const sum = records.value
    .filter((r) => Number(r.amount) < 0)
    .reduce((s, r) => s + Math.abs(Number(r.amount)), 0)
  return `¥${sum.toFixed(2)}`
})

onMounted(async () => {
  await userStore.refreshInfo()
  await load()
  loading.value = false
})

async function load() {
  try {
    const res = await userApi.balanceRecords({ page: page.value, size })
    records.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    records.value = []
  }
}

function onPage(p) {
  page.value = p
  load()
}

const TYPE_TEXT = { 1: '充值', 2: '消费', 3: '退款' }

function typeText(t) {
  return TYPE_TEXT[t] || '其它'
}

function typeTag(t) {
  return t === 1 ? 'success' : t === 2 ? 'primary' : 'warning'
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

function rechargeHint() {
  ElMessage.info('充值需对接支付渠道，当前为演示环境（微信/支付宝等为功能说明页）')
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.bal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.bal__head h2 {
  margin: 0;
  font-size: 20px;
}
.bal__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.bal__hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  padding: 26px 30px;
  border-radius: 12px;
  color: #fff;
  background: linear-gradient(120deg, #ff5000, #ff8a00 70%, #ffb300);
  box-shadow: 0 12px 26px rgba(255, 80, 0, 0.22);
}
.bal__hero-label {
  font-size: 12.5px;
  opacity: 0.92;
}
.bal__hero-value {
  display: block;
  margin: 6px 0 14px;
  font-size: 40px;
  font-weight: 800;
  font-family: Arial, Helvetica, sans-serif;
  line-height: 1;
}
.bal__hero-ops {
  display: flex;
  gap: 10px;
}
.bal__hero-btn {
  background: rgba(255, 255, 255, 0.95);
  border: none;
  color: var(--abc-primary);
  font-weight: 600;
}
.bal__hero-right {
  display: flex;
  gap: 28px;
}
.bal__hero-stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.bal__hero-stat span {
  font-size: 12.5px;
  opacity: 0.92;
}
.bal__hero-stat b {
  font-size: 19px;
  font-family: Arial, Helvetica, sans-serif;
}
.bal__records {
  margin-top: 26px;
}
.bal__add {
  color: var(--abc-success);
  font-weight: 700;
}
.bal__minus {
  color: var(--abc-price);
  font-weight: 700;
}
.bal__pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
</style>
