<template>
  <div class="abc-container fi">
    <el-breadcrumb separator="/" class="fi__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>功能一览</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="fi__head">
      <h2>功能一览</h2>
      <p>
        以下为需求中标注「!」的功能，均以「功能说明页」形式呈现：点击可查看该功能的定位与说明，
        不涉及真实的后端支付、物流与第三方对接。
      </p>
      <div class="fi__stat">共 {{ total }} 项功能 · {{ groupCount }} 个分组</div>
    </div>

    <div v-for="(items, group) in groups" :key="group" class="fi__group">
      <div class="fi__group-head">
        <span class="fi__group-bar"></span>
        <h3>{{ group }}</h3>
        <span class="fi__group-count">{{ items.length }} 项</span>
      </div>
      <div class="fi__grid">
        <div v-for="item in items" :key="item.key" class="fi__cell" @click="go(`/feature/${item.key}`)">
          <div class="fi__cell-icon">
            <el-icon :size="20"><component :is="item.icon" /></el-icon>
          </div>
          <div class="fi__cell-text">
            <h4>{{ item.name }}</h4>
            <p class="abc-ellipsis-2">{{ item.desc }}</p>
          </div>
          <el-icon class="fi__cell-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import { FEATURES, FEATURE_GROUPS } from '@/config/features'

const router = useRouter()

const groups = computed(() => FEATURE_GROUPS)
const total = computed(() => Object.keys(FEATURES).length)
const groupCount = computed(() => Object.keys(FEATURE_GROUPS).length)

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.fi {
  padding-top: 16px;
}
.fi__crumb {
  margin-bottom: 14px;
}
.fi__head {
  padding: 24px 28px;
  border-radius: var(--abc-radius);
  color: #fff;
  background: linear-gradient(120deg, #ff5000, #ff8a00 70%, #ffb300);
  box-shadow: 0 12px 28px rgba(255, 80, 0, 0.24);
}
.fi__head h2 {
  margin: 0;
  font-size: 24px;
  letter-spacing: 1px;
}
.fi__head p {
  margin: 10px 0 14px;
  font-size: 13px;
  line-height: 1.8;
  opacity: 0.94;
  max-width: 900px;
}
.fi__stat {
  font-size: 12.5px;
  opacity: 0.92;
}
.fi__group {
  margin-top: 26px;
}
.fi__group-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.fi__group-bar {
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--abc-primary), #ff9500);
}
.fi__group-head h3 {
  margin: 0;
  font-size: 18px;
}
.fi__group-count {
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.fi__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.fi__cell {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  cursor: pointer;
  transition: box-shadow 0.25s, transform 0.25s;
}
.fi__cell:hover {
  box-shadow: var(--abc-shadow-hover);
  transform: translateY(-3px);
}
.fi__cell-icon {
  width: 40px;
  height: 40px;
  border-radius: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  flex-shrink: 0;
}
.fi__cell-text {
  flex: 1;
  min-width: 0;
}
.fi__cell-text h4 {
  margin: 0 0 5px;
  font-size: 14.5px;
}
.fi__cell-text p {
  margin: 0;
  font-size: 12.5px;
  color: var(--abc-text-3);
  line-height: 1.55;
}
.fi__cell-arrow {
  color: #c8ced8;
  align-self: center;
}
@media (max-width: 1024px) {
  .fi__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 680px) {
  .fi__grid {
    grid-template-columns: 1fr;
  }
}
</style>
