<template>
  <div class="abc-container nd">
    <el-breadcrumb separator="/" class="nd__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/notice' }">公告中心</el-breadcrumb-item>
      <el-breadcrumb-item>{{ detail.title || '公告详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-loading="loading" class="nd__card abc-card">
      <template v-if="detail.id">
        <h1 class="nd__title">{{ detail.title }}</h1>
        <div class="nd__meta">
          <el-tag size="small" :type="detail.type === 'activity' ? 'warning' : 'primary'">
            {{ detail.type === 'activity' ? '活动通知' : '平台公告' }}
          </el-tag>
          <span><el-icon><Clock /></el-icon> {{ formatTime(detail.createTime) }}</span>
        </div>
        <el-divider />
        <div class="nd__body" v-html="contentHtml"></div>

        <div class="nd__foot">
          <el-button round @click="go('/notice')">返回公告列表</el-button>
          <el-button type="primary" round @click="go('/')">返回首页</el-button>
        </div>
      </template>

      <EmptyBox v-else-if="!loading" icon="Bell" text="公告不存在或已下线" action-text="返回公告列表"
                @action="go('/notice')" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Clock } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { homeApi } from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const detail = ref({})

const contentHtml = computed(() => {
  const c = detail.value.content || ''
  if (!c) return ''
  return /<[a-z][\s\S]*>/i.test(c) ? c : c.replace(/\n/g, '<br/>')
})

onMounted(load)
watch(() => route.params.id, load)

async function load() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    detail.value = (await homeApi.noticeDetail(id)) || {}
  } catch (e) {
    detail.value = {}
  } finally {
    loading.value = false
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
.nd {
  padding-top: 16px;
}
.nd__crumb {
  margin-bottom: 14px;
}
.nd__card {
  max-width: 900px;
  margin: 0 auto;
  padding: 34px 40px 30px;
}
.nd__title {
  margin: 0 0 16px;
  font-size: 25px;
  line-height: 1.45;
}
.nd__meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.nd__meta span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.nd__body {
  font-size: 14.5px;
  line-height: 2;
  color: var(--abc-text-2);
  min-height: 120px;
}
.nd__body :deep(img) {
  border-radius: 10px;
  margin: 14px 0;
}
.nd__body :deep(h2),
.nd__body :deep(h3),
.nd__body :deep(p) {
  margin: 12px 0;
}
.nd__foot {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
