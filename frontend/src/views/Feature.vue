<template>
  <div class="abc-container feat">
    <el-breadcrumb separator="/" class="feat__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/features' }">功能一览</el-breadcrumb-item>
      <el-breadcrumb-item>{{ feature ? feature.name : '功能说明' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <template v-if="feature">
      <div class="feat__card">
        <div class="feat__icon">
          <el-icon :size="38"><component :is="feature.icon" /></el-icon>
        </div>
        <el-tag class="feat__group" round>{{ feature.group }}</el-tag>
        <h1 class="feat__title">此为「{{ feature.name }}」功能</h1>
        <p class="feat__desc">{{ feature.desc }}</p>

        <div class="feat__notice">
          <el-icon><InfoFilled /></el-icon>
          <span>
            该功能为需求标注的「!」功能，仅提供功能说明，不包含真实的后端对接与业务实现。
            以下入口可继续浏览商城其它已实现的模块。
          </span>
        </div>

        <div class="feat__actions">
          <el-button type="primary" round @click="go('/')">返回首页</el-button>
          <el-button round @click="go('/features')">查看全部功能</el-button>
        </div>
      </div>

      <div class="feat__related">
        <SectionTitle title="相关业务入口" subtitle="继续体验商城已实现的模块" />
        <div class="feat__links">
          <div v-for="l in related" :key="l.path" class="feat__link" @click="go(l.path)">
            <el-icon :size="18"><component :is="l.icon" /></el-icon>
            <span>{{ l.name }}</span>
          </div>
        </div>
      </div>

      <div class="feat__group-list">
        <SectionTitle :title="`同组功能 · ${feature.group}`" :subtitle="`共 ${sameGroup.length} 项`" />
        <div class="feat__grid">
          <div v-for="item in sameGroup" :key="item.key" class="feat__cell"
               :class="{ active: item.key === featureKey }" @click="go(`/feature/${item.key}`)">
            <el-icon :size="17"><component :is="item.icon" /></el-icon>
            <span>{{ item.name }}</span>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="feat__empty abc-card">
      <EmptyBox icon="QuestionFilled" :text="`未找到功能「${featureKey}」`" action-text="返回功能一览"
                @action="go('/features')" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { InfoFilled } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { FEATURE_GROUPS, getFeature } from '@/config/features'

const route = useRoute()
const router = useRouter()

const featureKey = computed(() => String(route.params.key || ''))
const feature = computed(() => getFeature(featureKey.value))

const sameGroup = computed(() => {
  if (!feature.value) return []
  return FEATURE_GROUPS[feature.value.group] || []
})

const related = [
  { name: '限时秒杀', path: '/feature/seckill', icon: 'AlarmClock' },
  { name: '领券中心', path: '/coupon', icon: 'Ticket' },
  { name: '积分商城', path: '/points-mall', icon: 'Present' },
  { name: '每日签到', path: '/sign', icon: 'Calendar' },
  { name: '活动专区', path: '/activity', icon: 'Present' },
  { name: '会员中心', path: '/user/dashboard', icon: 'User' },
  { name: '购物车', path: '/cart', icon: 'ShoppingCart' },
  { name: '公告中心', path: '/notice', icon: 'Bell' }
]

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.feat {
  padding-top: 16px;
}
.feat__crumb {
  margin-bottom: 14px;
}
.feat__card {
  padding: 42px 40px 36px;
  border-radius: var(--abc-radius);
  background: #fff;
  box-shadow: var(--abc-shadow);
  text-align: center;
}
.feat__icon {
  width: 84px;
  height: 84px;
  margin: 0 auto 18px;
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  box-shadow: 0 14px 30px rgba(255, 80, 0, 0.28);
}
.feat__group {
  margin-bottom: 12px;
}
.feat__title {
  margin: 6px 0 0;
  font-size: 27px;
  letter-spacing: 1px;
}
.feat__desc {
  margin: 16px auto 0;
  max-width: 720px;
  font-size: 14.5px;
  line-height: 1.95;
  color: var(--abc-text-2);
}
.feat__notice {
  margin: 26px auto 0;
  max-width: 760px;
  display: flex;
  gap: 10px;
  text-align: left;
  padding: 14px 18px;
  border-radius: 12px;
  background: #fff8ec;
  color: #b26a00;
  font-size: 12.5px;
  line-height: 1.75;
}
.feat__actions {
  margin-top: 26px;
  display: flex;
  justify-content: center;
  gap: 12px;
}
.feat__related,
.feat__group-list {
  margin-top: 28px;
}
.feat__links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.feat__link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 11px 20px;
  border-radius: 22px;
  background: #fff;
  box-shadow: var(--abc-shadow);
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.2s;
}
.feat__link:hover {
  color: var(--abc-primary);
  box-shadow: var(--abc-shadow-hover);
  transform: translateY(-2px);
}
.feat__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.feat__cell {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: var(--abc-shadow);
  font-size: 13.5px;
  cursor: pointer;
  transition: all 0.2s;
}
.feat__cell:hover {
  color: var(--abc-primary);
  transform: translateY(-2px);
}
.feat__cell.active {
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  box-shadow: 0 10px 22px rgba(255, 80, 0, 0.26);
}
.feat__empty {
  padding: 20px;
}
@media (max-width: 980px) {
  .feat__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 640px) {
  .feat__grid {
    grid-template-columns: 1fr;
  }
  .feat__card {
    padding: 30px 20px 26px;
  }
}
</style>
