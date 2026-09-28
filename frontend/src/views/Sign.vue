<template>
  <div class="abc-container sign">
    <el-breadcrumb separator="/" class="sign__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>每日签到</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="sign__box">
      <!-- 左：签到卡 -->
      <div class="sign__card">
        <h2>每日签到</h2>
        <p class="sign__card-sub">连续签到天数越多，单日奖励积分越高</p>

        <div class="sign__points">
          <div class="sign__points-main">
            <span>当前积分</span>
            <b>{{ status.totalPoints || 0 }}</b>
          </div>
          <div class="sign__points-main">
            <span>已连续签到</span>
            <b>{{ status.continuousDays || 0 }}<i>天</i></b>
          </div>
          <div class="sign__points-main">
            <span>今日可得</span>
            <b>{{ status.todayPoints || 0 }}<i>分</i></b>
          </div>
        </div>

        <el-button type="primary" class="sign__btn" :loading="signing"
                   :disabled="status.signedToday" @click="doSign">
          {{ status.signedToday ? '今日已签到' : '立即签到' }}
        </el-button>

        <p class="sign__tip">
          连续签到满 {{ status.continuousThreshold || 7 }} 天，额外奖励
          {{ status.continuousPoints || 100 }} 积分
        </p>
      </div>

      <!-- 右：月历 -->
      <div class="sign__calendar">
        <div class="sign__cal-head">
          <b>{{ year }} 年 {{ month }} 月</b>
          <span>本月已签到 {{ status.monthSignedCount || 0 }} 天</span>
        </div>

        <div class="sign__week">
          <span v-for="w in weekNames" :key="w">{{ w }}</span>
        </div>

        <div class="sign__days">
          <span v-for="(cell, idx) in cells" :key="idx" class="sign__day"
                :class="{
                  'sign__day--empty': !cell,
                  'sign__day--signed': cell && signedSet.has(cell),
                  'sign__day--today': cell && cell === todayDate
                }">
            <template v-if="cell">
              <i>{{ cell }}</i>
              <em v-if="signedSet.has(cell)">已签</em>
            </template>
          </span>
        </div>

        <div class="sign__legend">
          <span><i class="dot dot--signed"></i>已签到</span>
          <span><i class="dot dot--today"></i>今天</span>
          <span><i class="dot"></i>未签到</span>
        </div>
      </div>
    </div>

    <div class="sign__rule abc-card">
      <h3>签到规则</h3>
      <ul>
        <li>每天可签到 1 次，签到即得积分，连续签到积分递增。</li>
        <li>连续签到中断后，连续天数从 1 重新计算。</li>
        <li>连续签到满 {{ status.continuousThreshold || 7 }} 天可获得额外奖励积分。</li>
        <li>积分可用于积分商城兑换商品，或在结算时抵扣（标注「!」的余额抵扣为功能说明页）。</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pointsApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const status = ref({
  signedToday: false,
  continuousDays: 0,
  todayPoints: 0,
  totalPoints: 0,
  signedDates: [],
  monthSignedCount: 0,
  continuousThreshold: 7,
  continuousPoints: 100
})
const signing = ref(false)

const now = new Date()
const year = now.getFullYear()
const month = now.getMonth() + 1
const todayDate = now.getDate()

const weekNames = ['日', '一', '二', '三', '四', '五', '六']

/** 本月签到日期集合（只取「日」） */
const signedSet = computed(() => {
  const set = new Set()
  ;(status.value.signedDates || []).forEach((d) => {
    const parts = String(d).split('-')
    if (parts.length === 3 && Number(parts[1]) === month) {
      set.add(Number(parts[2]))
    }
  })
  return set
})

/** 生成日历格子：前面补空格 + 本月日期 */
const cells = computed(() => {
  const firstWeekday = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()
  const arr = []
  for (let i = 0; i < firstWeekday; i++) {
    arr.push(null)
  }
  for (let d = 1; d <= daysInMonth; d++) {
    arr.push(d)
  }
  return arr
})

onMounted(load)

async function load() {
  try {
    status.value = (await pointsApi.signStatus()) || status.value
  } catch (e) {
    /* 401 时拦截器已处理 */
  }
}

async function doSign() {
  if (status.value.signedToday) return
  signing.value = true
  try {
    status.value = (await pointsApi.sign()) || status.value
    ElMessage.success(`签到成功，获得 ${status.value.todayPoints || 0} 积分`)
    await userStore.refreshInfo()
  } catch (e) {
    /* 已提示 */
  } finally {
    signing.value = false
  }
}
</script>

<style scoped>
.sign {
  padding-top: 16px;
}
.sign__crumb {
  margin-bottom: 14px;
}
.sign__box {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
}
.sign__card {
  padding: 30px 26px;
  border-radius: var(--abc-radius);
  color: #fff;
  background: linear-gradient(150deg, #ff5000, #ff9500 65%, #ffb300);
  box-shadow: 0 14px 32px rgba(255, 80, 0, 0.26);
  text-align: center;
}
.sign__card h2 {
  margin: 0;
  font-size: 24px;
  letter-spacing: 2px;
}
.sign__card-sub {
  margin: 8px 0 24px;
  font-size: 12.5px;
  opacity: 0.92;
}
.sign__points {
  display: grid;
  gap: 12px;
  margin-bottom: 24px;
}
.sign__points-main {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 10px 16px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.16);
}
.sign__points-main span {
  font-size: 13px;
}
.sign__points-main b {
  font-size: 24px;
  font-weight: 800;
  font-family: Arial, Helvetica, sans-serif;
}
.sign__points-main b i {
  font-size: 12px;
  font-style: normal;
  font-weight: 400;
  margin-left: 2px;
}
.sign__btn {
  width: 100%;
  height: 46px;
  border-radius: 23px;
  font-size: 17px;
  letter-spacing: 3px;
  background: #fff;
  border-color: #fff;
  color: var(--abc-primary);
  font-weight: 700;
}
.sign__btn:hover:not(.is-disabled) {
  background: #fff7f2;
  border-color: #fff7f2;
  color: var(--abc-primary-dark);
}
.sign__btn.is-disabled {
  background: rgba(255, 255, 255, 0.4);
  border-color: transparent;
  color: #fff;
}
.sign__tip {
  margin: 16px 0 0;
  font-size: 12px;
  opacity: 0.92;
}
.sign__calendar {
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  padding: 24px 26px;
}
.sign__cal-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}
.sign__cal-head b {
  font-size: 18px;
}
.sign__cal-head span {
  font-size: 13px;
  color: var(--abc-text-3);
}
.sign__week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  margin-bottom: 8px;
}
.sign__week span {
  text-align: center;
  font-size: 12px;
  color: var(--abc-text-3);
  padding: 6px 0;
}
.sign__days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
}
.sign__day {
  aspect-ratio: 1 / 0.82;
  border-radius: 10px;
  border: 1px solid var(--abc-border);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  background: #fafbfc;
}
.sign__day i {
  font-style: normal;
  font-weight: 600;
}
.sign__day em {
  font-style: normal;
  font-size: 10px;
  color: var(--abc-primary);
  margin-top: 2px;
}
.sign__day--empty {
  border: none;
  background: transparent;
}
.sign__day--signed {
  color: #fff;
  border-color: transparent;
  background: linear-gradient(135deg, #ff5000, #ff9500);
}
.sign__day--signed em {
  color: #fff;
}
.sign__day--today {
  box-shadow: 0 0 0 2px rgba(255, 80, 0, 0.35);
}
.sign__legend {
  margin-top: 18px;
  display: flex;
  gap: 22px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.sign__legend span {
  display: flex;
  align-items: center;
  gap: 6px;
}
.dot {
  width: 12px;
  height: 12px;
  border-radius: 3px;
  border: 1px solid var(--abc-border);
  background: #fafbfc;
  display: inline-block;
}
.dot--signed {
  border-color: transparent;
  background: linear-gradient(135deg, #ff5000, #ff9500);
}
.dot--today {
  box-shadow: 0 0 0 2px rgba(255, 80, 0, 0.35);
}
.sign__rule {
  margin-top: 20px;
  padding: 20px 24px;
}
.sign__rule h3 {
  margin: 0 0 10px;
  font-size: 16px;
}
.sign__rule ul {
  margin: 0;
  padding-left: 18px;
  color: var(--abc-text-2);
  font-size: 13px;
  line-height: 1.9;
}
@media (max-width: 900px) {
  .sign__box {
    grid-template-columns: 1fr;
  }
}
</style>
