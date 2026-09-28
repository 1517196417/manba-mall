<template>
  <div class="abc-container scan">
    <el-breadcrumb separator="/" class="scan__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>扫码</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="scan__box">
      <div class="scan__left">
        <div class="scan__frame">
          <div class="scan__corner scan__corner--tl"></div>
          <div class="scan__corner scan__corner--tr"></div>
          <div class="scan__corner scan__corner--bl"></div>
          <div class="scan__corner scan__corner--br"></div>
          <div class="scan__line"></div>
          <img class="scan__qr" :src="qr" alt="扫码入口" />
        </div>
        <p class="scan__hint">打开手机微信 / 商城 App「扫一扫」，对准上方二维码</p>
      </div>

      <div class="scan__right">
        <h2>扫码入口</h2>
        <p class="scan__sub">扫码可快速进入商品详情、活动会场或门店自提核销</p>

        <div class="scan__items">
          <div v-for="item in items" :key="item.title" class="scan__item" @click="go(item.path)">
            <div class="scan__item-icon" :style="{ background: item.color }">
              <el-icon :size="22"><component :is="item.icon" /></el-icon>
            </div>
            <div class="scan__item-text">
              <h4>{{ item.title }}</h4>
              <p>{{ item.desc }}</p>
            </div>
            <el-icon class="scan__item-arrow"><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="scan__tip">
          <el-icon><InfoFilled /></el-icon>
          <span>「门店自提」「同城配送」「预约配送」为需求标注的功能说明页，此处仅提供入口说明。</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowRight, InfoFilled } from '@element-plus/icons-vue'
import { placeholderImage } from '@/api'

const router = useRouter()

const qr = placeholderImage('AbcMall 扫码入口', 260, 260, 4)

const items = [
  { title: '扫码查商品', desc: '扫描商品条码直达详情页，快速下单', icon: 'Search', color: '#ff5000', path: '/products' },
  { title: '扫码进会场', desc: '扫描活动二维码进入对应活动会场', icon: 'Present', color: '#ff9500', path: '/activity' },
  { title: '扫码领券', desc: '扫码领取线下专属优惠券', icon: 'Ticket', color: '#e04600', path: '/coupon' },
  { title: '门店自提核销', desc: '到店出示取货码完成自提核销', icon: 'Shop', color: '#28a745', path: '/feature/store-pickup' }
]

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.scan {
  padding-top: 16px;
}
.scan__crumb {
  margin-bottom: 14px;
}
.scan__box {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 20px;
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  padding: 34px 36px;
}
.scan__left {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.scan__frame {
  position: relative;
  width: 280px;
  height: 280px;
  border-radius: 14px;
  background: #fafbfc;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.scan__qr {
  width: 220px;
  height: 220px;
  border-radius: 8px;
}
.scan__corner {
  position: absolute;
  width: 28px;
  height: 28px;
  border: 3px solid var(--abc-primary);
}
.scan__corner--tl {
  top: 10px;
  left: 10px;
  border-right: none;
  border-bottom: none;
  border-top-left-radius: 8px;
}
.scan__corner--tr {
  top: 10px;
  right: 10px;
  border-left: none;
  border-bottom: none;
  border-top-right-radius: 8px;
}
.scan__corner--bl {
  bottom: 10px;
  left: 10px;
  border-right: none;
  border-top: none;
  border-bottom-left-radius: 8px;
}
.scan__corner--br {
  bottom: 10px;
  right: 10px;
  border-left: none;
  border-top: none;
  border-bottom-right-radius: 8px;
}
.scan__line {
  position: absolute;
  left: 12px;
  right: 12px;
  height: 2px;
  background: linear-gradient(90deg, transparent, var(--abc-primary), transparent);
  animation: scanmove 2.4s ease-in-out infinite;
}
@keyframes scanmove {
  0% {
    top: 16px;
  }
  50% {
    top: 258px;
  }
  100% {
    top: 16px;
  }
}
.scan__hint {
  margin: 18px 0 0;
  font-size: 12.5px;
  color: var(--abc-text-3);
  text-align: center;
}
.scan__right h2 {
  margin: 0;
  font-size: 23px;
}
.scan__sub {
  margin: 8px 0 22px;
  font-size: 13px;
  color: var(--abc-text-3);
}
.scan__items {
  display: grid;
  gap: 12px;
}
.scan__item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid var(--abc-border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.scan__item:hover {
  border-color: var(--abc-primary);
  background: #fffaf7;
  transform: translateX(3px);
}
.scan__item-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}
.scan__item-text {
  flex: 1;
  min-width: 0;
}
.scan__item-text h4 {
  margin: 0 0 3px;
  font-size: 14.5px;
}
.scan__item-text p {
  margin: 0;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.scan__item-arrow {
  color: var(--abc-text-3);
}
.scan__tip {
  margin-top: 20px;
  display: flex;
  gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #fff8ec;
  color: #b26a00;
  font-size: 12.5px;
  line-height: 1.6;
}
@media (max-width: 900px) {
  .scan__box {
    grid-template-columns: 1fr;
  }
}
</style>
