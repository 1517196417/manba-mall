<template>
  <div class="msg">
    <div class="msg__head">
      <div>
        <h2>消息中心</h2>
        <p>站内信、系统通知与订单消息都会在这里汇总</p>
      </div>
      <div class="msg__head-ops">
        <el-button round @click="readAll">全部已读</el-button>
        <el-button round type="danger" plain @click="clearAll">清空当前分类</el-button>
      </div>
    </div>

    <div class="msg__tabs">
      <span v-for="t in tabs" :key="t.value" class="msg__tab"
            :class="{ active: type === t.value }" @click="changeType(t.value)">
        {{ t.label }}
        <em v-if="unreadOf(t.value) > 0">{{ unreadOf(t.value) }}</em>
      </span>
    </div>

    <div v-loading="loading" class="msg__list">
      <div v-for="m in list" :key="m.id" class="m-item" :class="{ 'm-item--unread': m.isRead === 0 }"
           @click="open(m)">
        <div class="m-item__icon" :class="`m-item__icon--${m.type}`">
          <el-icon :size="18"><component :is="iconOf(m.type)" /></el-icon>
        </div>
        <div class="m-item__main">
          <div class="m-item__top">
            <h4 class="abc-ellipsis">
              <span v-if="m.isRead === 0" class="m-item__dot"></span>
              {{ m.title }}
            </h4>
            <span class="m-item__time">{{ formatTime(m.createTime) }}</span>
          </div>
          <p class="m-item__content abc-ellipsis-2">{{ stripHtml(m.content) }}</p>
          <div class="m-item__foot">
            <el-tag size="small" type="info" round>{{ typeText(m.type) }}</el-tag>
            <el-link v-if="m.link" :underline="false" type="primary" @click.stop="go(m.link)">查看详情 &gt;</el-link>
          </div>
        </div>
        <el-button class="m-item__del" link :icon="Delete" @click.stop="remove(m)" />
      </div>
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="Bell" text="暂无消息" />

    <div v-if="total > size" class="msg__pager">
      <el-pagination background layout="prev, pager, next" :total="total"
                     :page-size="size" :current-page="page" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { messageApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10
const type = ref(null)
const summary = ref({})

const tabs = [
  { label: '全部', value: null },
  { label: '系统通知', value: 'system' },
  { label: '订单消息', value: 'order' },
  { label: '活动消息', value: 'activity' },
  { label: '站内信', value: 'private' }
]

onMounted(async () => {
  await Promise.all([loadSummary(), load()])
  loading.value = false
})

async function loadSummary() {
  try {
    summary.value = (await messageApi.unreadSummary()) || {}
  } catch (e) {
    summary.value = {}
  }
}

async function load() {
  loading.value = true
  try {
    const res = await messageApi.page({ type: type.value, page: page.value, size })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
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

function unreadOf(t) {
  if (t === null) {
    return Object.values(summary.value || {}).reduce((s, v) => s + Number(v || 0), 0)
  }
  return Number((summary.value || {})[t] || 0)
}

const TYPE_TEXT = { system: '系统', order: '订单', activity: '活动', private: '站内信' }

function typeText(t) {
  return TYPE_TEXT[t] || '通知'
}

function iconOf(t) {
  if (t === 'order') return 'Van'
  if (t === 'activity') return 'Present'
  if (t === 'private') return 'ChatDotRound'
  return 'Bell'
}

async function open(m) {
  if (m.isRead === 0) {
    try {
      await messageApi.read(m.id)
      m.isRead = 1
      userStore.refreshBadges()
      loadSummary()
    } catch (e) {
      /* 忽略 */
    }
  }
  if (m.link) {
    go(m.link)
  }
}

async function readAll() {
  try {
    await messageApi.readAll(type.value)
    ElMessage.success('已全部标记为已读')
    await Promise.all([loadSummary(), load()])
    userStore.refreshBadges()
  } catch (e) {
    /* 已提示 */
  }
}

async function clearAll() {
  try {
    await ElMessageBox.confirm('确定清空当前分类下的全部消息吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await messageApi.clear(type.value)
    ElMessage.success('已清空')
    await Promise.all([loadSummary(), load()])
    userStore.refreshBadges()
  } catch (e) {
    /* 已提示 */
  }
}

async function remove(m) {
  try {
    await messageApi.remove(m.id)
    await Promise.all([loadSummary(), load()])
    userStore.refreshBadges()
  } catch (e) {
    /* 已提示 */
  }
}

function stripHtml(html) {
  if (!html) return ''
  return String(html).replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.msg__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.msg__head h2 {
  margin: 0;
  font-size: 20px;
}
.msg__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.msg__head-ops {
  display: flex;
  gap: 10px;
}
.msg__tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.msg__tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border-radius: 17px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid var(--abc-border);
  transition: all 0.2s;
}
.msg__tab:hover {
  color: var(--abc-primary);
}
.msg__tab.active {
  color: #fff;
  background: var(--abc-primary);
  border-color: var(--abc-primary);
}
.msg__tab em {
  font-style: normal;
  font-size: 11px;
  padding: 0 5px;
  border-radius: 8px;
  background: var(--abc-price);
  color: #fff;
}
.msg__list {
  display: grid;
  gap: 12px;
  min-height: 160px;
}
.m-item {
  display: flex;
  gap: 14px;
  padding: 16px 18px;
  border: 1px solid var(--abc-border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.m-item:hover {
  border-color: var(--abc-primary);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}
.m-item--unread {
  background: #fffaf7;
}
.m-item__icon {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: #c8ced8;
  flex-shrink: 0;
}
.m-item__icon--system {
  background: #3b82f6;
}
.m-item__icon--order {
  background: #ff5000;
}
.m-item__icon--activity {
  background: #ff9500;
}
.m-item__icon--private {
  background: #28a745;
}
.m-item__main {
  flex: 1;
  min-width: 0;
}
.m-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.m-item__top h4 {
  margin: 0;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}
.m-item__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--abc-price);
  flex-shrink: 0;
}
.m-item__time {
  font-size: 12px;
  color: var(--abc-text-3);
  flex-shrink: 0;
}
.m-item__content {
  margin: 6px 0 10px;
  font-size: 13px;
  color: var(--abc-text-2);
  line-height: 1.6;
}
.m-item__foot {
  display: flex;
  align-items: center;
  gap: 12px;
}
.m-item__del {
  align-self: flex-start;
  color: var(--abc-text-3);
}
.m-item__del:hover {
  color: var(--abc-price);
}
.msg__pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
</style>
