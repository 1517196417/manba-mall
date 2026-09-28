<template>
  <div class="abc-container notice">
    <el-breadcrumb separator="/" class="notice__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>公告中心</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="notice__head">
      <h2>公告中心</h2>
      <p>平台公告、活动通知与规则更新，第一时间同步给你</p>
    </div>

    <div v-loading="loading" class="notice__list">
      <div v-for="n in list" :key="n.id" class="n-item" @click="go(`/notice/${n.id}`)">
        <div class="n-item__left">
          <span v-if="n.isTop === 1" class="n-item__top">置顶</span>
          <el-icon class="n-item__icon"><Bell /></el-icon>
          <div class="n-item__main">
            <h4 class="abc-ellipsis">{{ n.title }}</h4>
            <p class="n-item__content abc-ellipsis-2">{{ stripHtml(n.content) }}</p>
          </div>
        </div>
        <div class="n-item__right">
          <span class="n-item__type" :class="`n-item__type--${n.type}`">{{ typeText(n.type) }}</span>
          <span class="n-item__time">{{ formatDate(n.createTime) }}</span>
          <el-icon><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="Bell" text="暂时没有公告" />

    <div v-if="total > size" class="notice__pager">
      <el-pagination background layout="prev, pager, next" :total="total"
                     :page-size="size" :current-page="page" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Bell } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { homeApi } from '@/api'

const router = useRouter()

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await homeApi.notices(page.value, size)
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function onPage(p) {
  page.value = p
  load()
}

function typeText(type) {
  return type === 'activity' ? '活动' : '公告'
}

function stripHtml(html) {
  if (!html) return ''
  return String(html).replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
}

function formatDate(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.notice {
  padding-top: 16px;
}
.notice__crumb {
  margin-bottom: 14px;
}
.notice__head {
  margin-bottom: 18px;
}
.notice__head h2 {
  margin: 0;
  font-size: 24px;
}
.notice__head p {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.notice__list {
  display: grid;
  gap: 12px;
  min-height: 160px;
}
.n-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 18px 22px;
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  cursor: pointer;
  transition: box-shadow 0.25s, transform 0.25s;
}
.n-item:hover {
  box-shadow: var(--abc-shadow-hover);
  transform: translateX(3px);
}
.n-item__left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  flex: 1;
}
.n-item__top {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  color: #fff;
  background: var(--abc-primary);
}
.n-item__icon {
  font-size: 22px;
  color: var(--abc-primary);
  flex-shrink: 0;
}
.n-item__main {
  min-width: 0;
}
.n-item__main h4 {
  margin: 0 0 4px;
  font-size: 15.5px;
}
.n-item__content {
  margin: 0;
  font-size: 12.5px;
  color: var(--abc-text-3);
  line-height: 1.5;
}
.n-item__right {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
  color: var(--abc-text-3);
  font-size: 12.5px;
}
.n-item__type {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  color: var(--abc-primary);
  background: var(--abc-primary-light);
}
.n-item__type--activity {
  color: #b26a00;
  background: #fff7e6;
}
.notice__pager {
  margin-top: 22px;
  display: flex;
  justify-content: center;
}
</style>
