<template>
  <div v-loading="loading" class="pt">
    <div class="pt__head">
      <div>
        <h2>我的积分</h2>
        <p>积分可通过购物、签到、评价获得，可在积分商城兑换商品</p>
      </div>
      <el-button type="primary" round @click="go('/points-mall')">去积分商城</el-button>
    </div>

    <div class="pt__cards">
      <div class="pt__card pt__card--main">
        <span class="pt__card-label">当前积分</span>
        <b class="pt__card-value">{{ overview.points || 0 }}</b>
        <span class="pt__card-sub">今日签到可得 {{ signPoints }} 积分</span>
        <el-button v-if="!overview.signedToday" size="small" round class="pt__card-btn" @click="go('/sign')">
          去签到
        </el-button>
        <el-tag v-else size="small" round effect="dark" class="pt__card-btn">今日已签到</el-tag>
      </div>
      <div class="pt__card">
        <span class="pt__card-label">会员等级</span>
        <b class="pt__card-value pt__card-value--sm">{{ levelName }}</b>
        <div class="pt__growth">
          <el-progress :percentage="growthPercent" :stroke-width="8" :show-text="false" />
          <span>成长值 {{ overview.growth || 0 }} / {{ overview.nextLevelGrowth || 0 }}</span>
        </div>
      </div>
      <div class="pt__card">
        <span class="pt__card-label">连续签到</span>
        <b class="pt__card-value pt__card-value--sm">{{ overview.continuousDays || 0 }} 天</b>
        <span class="pt__card-sub">已兑换 {{ overview.exchangeCount || 0 }} 件好礼</span>
      </div>
    </div>

    <!-- 等级权益 -->
    <div class="pt__levels">
      <SectionTitle title="会员等级权益" subtitle="成长值越高，等级越高，专享权益越多" />
      <div class="pt__level-grid">
        <div v-for="l in levels" :key="l.level" class="pt__level"
             :class="{ active: l.level === (overview.level || 1) }">
          <el-icon :size="22"><Medal /></el-icon>
          <b>{{ l.name }}</b>
          <span>成长值 {{ l.growth }}</span>
        </div>
      </div>
      <p class="pt__level-tip">
        「会员等级」「成长值」为需求标注的功能说明页，此处展示等级划分规则。
        <el-link :underline="false" type="primary" @click="go('/feature/member-level')">查看说明 &gt;</el-link>
      </p>
    </div>

    <!-- 积分明细 -->
    <div class="pt__records">
      <div class="pt__records-head">
        <SectionTitle title="积分明细" subtitle="最近 12 个月内的积分变动" />
        <div class="pt__filters">
          <span v-for="f in filters" :key="String(f.value)" class="pt__filter"
                :class="{ active: type === f.value }" @click="changeType(f.value)">{{ f.label }}</span>
        </div>
      </div>

      <el-table :data="records" class="pt__table" row-key="id">
        <el-table-column prop="description" label="说明" min-width="220" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)" round>{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="积分变动" width="130">
          <template #default="{ row }">
            <span :class="row.points >= 0 ? 'pt__add' : 'pt__minus'">
              {{ row.points >= 0 ? '+' : '' }}{{ row.points }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <template #empty>
          <EmptyBox icon="Medal" text="暂无积分记录" />
        </template>
      </el-table>

      <div v-if="total > size" class="pt__pager">
        <el-pagination background layout="prev, pager, next" :total="total"
                       :page-size="size" :current-page="page" @current-change="onPage" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Medal } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { pointsApi, userApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const overview = ref({})
const levels = ref([])
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const type = ref(null)

const filters = [
  { label: '全部', value: null },
  { label: '签到', value: 'sign' },
  { label: '订单', value: 'order' },
  { label: '兑换', value: 'exchange' }
]

const levelName = computed(() => overview.value.levelName || userStore.levelName || '普通会员')
const signPoints = computed(() => overview.value.signedToday ? 0 : 5)

const growthPercent = computed(() => {
  const next = Number(overview.value.nextLevelGrowth || 0)
  if (!next) return 100
  return Math.min(100, Math.round((Number(overview.value.growth || 0) / next) * 100))
})

onMounted(async () => {
  try {
    const [ov, lv] = await Promise.all([pointsApi.overview(), userApi.level()])
    overview.value = ov || {}
    levels.value = (lv && lv.levels) || []
  } catch (e) {
    /* 忽略 */
  }
  await load()
  loading.value = false
})

async function load() {
  try {
    const res = await pointsApi.records({ type: type.value, page: page.value, size })
    records.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    records.value = []
  }
}

function changeType(v) {
  type.value = v
  page.value = 1
  load()
}

function onPage(p) {
  page.value = p
  load()
}

const TYPE_TEXT = { sign: '签到', order: '订单', exchange: '兑换' }

function typeText(t) {
  return TYPE_TEXT[t] || '其它'
}

function typeTag(t) {
  return t === 'sign' ? 'success' : t === 'order' ? 'primary' : 'warning'
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.pt__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.pt__head h2 {
  margin: 0;
  font-size: 20px;
}
.pt__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.pt__cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.pt__card {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 20px 22px;
  border-radius: 12px;
  border: 1px solid var(--abc-border);
  background: #fafbfc;
}
.pt__card--main {
  color: #fff;
  border: none;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  box-shadow: 0 10px 24px rgba(255, 80, 0, 0.22);
}
.pt__card-label {
  font-size: 12.5px;
  opacity: 0.92;
}
.pt__card-value {
  font-size: 34px;
  font-weight: 800;
  font-family: Arial, Helvetica, sans-serif;
  line-height: 1.1;
}
.pt__card-value--sm {
  font-size: 22px;
}
.pt__card-sub {
  font-size: 12.5px;
  opacity: 0.92;
}
.pt__card-btn {
  width: fit-content;
  margin-top: 4px;
  background: #fff;
  border-color: #fff;
  color: var(--abc-primary);
  font-weight: 600;
}
.pt__growth {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.pt__growth span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.pt__levels {
  margin-top: 26px;
}
.pt__level-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}
.pt__level {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 18px 10px;
  border-radius: 12px;
  border: 1px solid var(--abc-border);
  background: #fafbfc;
  color: var(--abc-text-2);
}
.pt__level b {
  font-size: 14px;
  color: var(--abc-text);
}
.pt__level span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.pt__level.active {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  box-shadow: 0 10px 22px rgba(255, 80, 0, 0.24);
}
.pt__level.active b,
.pt__level.active span {
  color: #fff;
}
.pt__level-tip {
  margin: 14px 0 0;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.pt__records {
  margin-top: 26px;
}
.pt__records-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}
.pt__filters {
  display: flex;
  gap: 8px;
}
.pt__filter {
  padding: 5px 14px;
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid var(--abc-border);
  transition: all 0.2s;
}
.pt__filter:hover {
  color: var(--abc-primary);
}
.pt__filter.active {
  color: #fff;
  background: var(--abc-primary);
  border-color: var(--abc-primary);
}
.pt__add {
  color: var(--abc-success);
  font-weight: 700;
}
.pt__minus {
  color: var(--abc-price);
  font-weight: 700;
}
.pt__pager {
  margin-top: 16px;
  display: flex;
  justify-content: center;
}
@media (max-width: 980px) {
  .pt__cards {
    grid-template-columns: 1fr;
  }
  .pt__level-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
