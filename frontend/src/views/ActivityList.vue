<template>
  <div class="abc-container act">
    <el-breadcrumb separator="/" class="act__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>活动专区</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="act__head">
      <h2>活动专区</h2>
      <p>专场促销 / 限时秒杀 / 拼团 / 新品首发，好价一次逛够</p>
    </div>

    <div v-loading="loading" class="act__grid">
      <div v-for="a in list" :key="a.id" class="a-card" @click="open(a)">
        <div class="a-card__img abc-img-box">
          <img :src="a.image || placeholderImage(a.title, 640, 360, a.id % 8)" :alt="a.title" />
          <span class="a-card__type">{{ typeText(a.type) }}</span>
          <div class="a-card__countdown" v-if="a.endTime">
            <span class="a-card__countdown-label">距结束</span>
            <CountDown :end-time="a.endTime" />
          </div>
        </div>
        <div class="a-card__body">
          <h3 class="abc-ellipsis">{{ a.title }}</h3>
          <p class="a-card__sub abc-ellipsis">{{ a.subtitle || '活动进行中，先到先得' }}</p>
          <div class="a-card__foot">
            <span class="a-card__time">{{ formatRange(a.startTime, a.endTime) }}</span>
            <el-button size="small" type="primary" round>进入会场</el-button>
          </div>
        </div>
      </div>
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="Present" text="当前没有进行中的活动" action-text="返回首页"
              @action="go('/')" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import CountDown from '@/components/CountDown.vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { homeApi, placeholderImage } from '@/api'

const router = useRouter()

const loading = ref(true)
const list = ref([])

const TYPE_TEXT = {
  promotion: '专场',
  seckill: '秒杀',
  groupon: '拼团',
  new: '新品',
  season: '季节'
}

onMounted(async () => {
  try {
    list.value = (await homeApi.activities()) || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
})

function typeText(type) {
  return TYPE_TEXT[type] || '活动'
}

function formatRange(start, end) {
  const s = start ? String(start).replace('T', ' ').slice(0, 10) : ''
  const e = end ? String(end).replace('T', ' ').slice(0, 10) : '长期'
  return `${s} ~ ${e}`
}

function open(a) {
  if (a.link && a.link.startsWith('/')) {
    router.push(a.link)
    return
  }
  router.push(`/activity/${a.id}`)
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.act {
  padding-top: 16px;
}
.act__crumb {
  margin-bottom: 14px;
}
.act__head {
  margin-bottom: 18px;
}
.act__head h2 {
  margin: 0;
  font-size: 24px;
}
.act__head p {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.act__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  min-height: 180px;
}
.a-card {
  background: #fff;
  border-radius: var(--abc-radius);
  overflow: hidden;
  box-shadow: var(--abc-shadow);
  cursor: pointer;
  transition: box-shadow 0.25s, transform 0.25s;
}
.a-card:hover {
  box-shadow: var(--abc-shadow-hover);
  transform: translateY(-3px);
}
.a-card__img {
  height: 176px;
  position: relative;
}
.a-card__type {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  border-radius: 11px;
  font-size: 12px;
  color: #fff;
  background: rgba(255, 80, 0, 0.92);
}
.a-card__countdown {
  position: absolute;
  right: 10px;
  bottom: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 10px;
  border-radius: 12px;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 12px;
}
.a-card__countdown-label {
  opacity: 0.85;
}
.a-card__body {
  padding: 14px 16px 16px;
}
.a-card__body h3 {
  margin: 0 0 6px;
  font-size: 17px;
}
.a-card__sub {
  margin: 0 0 12px;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.a-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.a-card__time {
  font-size: 12px;
  color: var(--abc-text-3);
}
@media (max-width: 980px) {
  .act__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .act__grid {
    grid-template-columns: 1fr;
  }
}
</style>
