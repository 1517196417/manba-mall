<template>
  <div class="nf">
    <div class="nf__code">404</div>
    <h2 class="nf__title">页面走丢了</h2>
    <p class="nf__desc">你访问的页面不存在或已被移除，换个方向继续逛逛吧。</p>
    <div class="nf__actions">
      <el-button type="primary" round @click="go('/')">返回首页</el-button>
      <el-button round @click="go('/products')">去逛商品</el-button>
      <el-button round @click="back">返回上一页</el-button>
    </div>

    <div class="nf__links">
      <span>猜你想看：</span>
      <el-link v-for="f in hot" :key="f.path" :underline="false" @click="go(f.path)">{{ f.name }}</el-link>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const hot = [
  { name: '限时秒杀', path: '/feature/seckill' },
  { name: '领券中心', path: '/coupon' },
  { name: '积分商城', path: '/points-mall' },
  { name: '活动专区', path: '/activity' },
  { name: '公告中心', path: '/notice' },
  { name: '功能一览', path: '/features' }
]

function go(path) {
  router.push(path)
}

function back() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.nf {
  max-width: var(--abc-max-width);
  margin: 0 auto;
  padding: 80px 16px 100px;
  text-align: center;
}
.nf__code {
  font-size: 110px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: 6px;
  background: linear-gradient(135deg, #ff5000, #ffb800);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.nf__title {
  margin: 14px 0 8px;
  font-size: 24px;
}
.nf__desc {
  margin: 0 0 28px;
  color: var(--abc-text-3);
  font-size: 14px;
}
.nf__actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}
.nf__links {
  margin-top: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  flex-wrap: wrap;
  font-size: 13px;
  color: var(--abc-text-3);
}
</style>
