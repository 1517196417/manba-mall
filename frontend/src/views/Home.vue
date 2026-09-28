<template>
  <div v-loading="loading" class="home">
    <!-- 轮播 + 右侧公告 -->
    <section class="abc-container hero">
      <div class="hero__carousel">
        <el-carousel height="380px" :interval="4500" arrow="hover">
          <el-carousel-item v-for="b in data.carousels" :key="b.id">
            <div class="hero__slide" @click="go(b.link)">
              <img :src="b.image" :alt="b.title" />
              <div class="hero__slide-title">{{ b.title }}</div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </div>

      <div class="hero__side">
        <div class="hero__notice">
          <div class="hero__notice-head">
            <el-icon><Bell /></el-icon>
            <span>商城公告</span>
            <el-link type="primary" :underline="false" @click="go('/notice')">更多</el-link>
          </div>
          <ul>
            <li v-for="n in data.notices.slice(0, 5)" :key="n.id" @click="go(`/notice/${n.id}`)">
              <el-tag v-if="n.isTop === 1" size="small" type="danger" effect="plain">置顶</el-tag>
              <span class="abc-ellipsis">{{ n.title }}</span>
            </li>
          </ul>
        </div>

        <div v-if="data.sideAds.length" class="hero__side-ad" @click="go(data.sideAds[0].link)">
          <img :src="data.sideAds[0].image" :alt="data.sideAds[0].title" />
        </div>
      </div>
    </section>

    <!-- 分类入口 -->
    <section class="abc-container abc-section">
      <SectionTitle title="全部分类" subtitle="按品类快速找到心仪好物" more-text="查看全部" @more="go('/category')" />
      <div class="cat-grid">
        <div v-for="cat in data.categories" :key="cat.id" class="cat-item" @click="go(`/category/${cat.id}`)">
          <img :src="cat.icon" :alt="cat.name" class="cat-item__icon" />
          <b>{{ cat.name }}</b>
          <span class="abc-ellipsis">{{ (cat.children || []).slice(0, 2).map((c) => c.name).join('·') }}</span>
        </div>
      </div>
    </section>

    <!-- 广告位 -->
    <section v-if="data.ads.length" class="abc-container abc-section">
      <div class="ad-grid">
        <div v-for="ad in data.ads" :key="ad.id" class="ad-item" @click="go(ad.link)">
          <img :src="ad.image" :alt="ad.title" />
        </div>
      </div>
    </section>

    <!-- 活动专区 -->
    <section class="abc-container abc-section">
      <SectionTitle title="活动专区" subtitle="平台大促 · 专场优惠" more-text="全部活动" @more="go('/activity')" />
      <div class="act-grid">
        <div v-for="act in data.activities" :key="act.id" class="act-item" @click="go(act.link)">
          <img :src="act.image" :alt="act.title" />
          <div class="act-item__mask">
            <b>{{ act.title }}</b>
            <span>{{ act.subtitle }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 限时秒杀（功能说明页入口） -->
    <section class="abc-container abc-section">
      <SectionTitle title="限时秒杀" subtitle="每日整点开抢" more-text="进入秒杀" @more="go('/feature/seckill')" />
      <div class="seckill">
        <div class="seckill__head">
          <el-icon :size="22"><AlarmClock /></el-icon>
          <div>
            <b>今日场次</b>
            <span>低至 5 折，售完即止</span>
          </div>
          <el-button type="primary" round @click="go('/feature/seckill')">查看场次</el-button>
        </div>
        <div class="seckill__list">
          <div v-for="s in seckillList" :key="s.id" class="seckill__item" @click="go(`/product/${s.productId}`)">
            <img :src="s.productImage" :alt="s.productName" />
            <div class="seckill__name abc-ellipsis">{{ s.productName }}</div>
            <div class="seckill__price">
              <MoneyText :value="s.seckillPrice" size="small" />
              <span class="abc-market-price">¥{{ Number(s.originPrice || 0).toFixed(2) }}</span>
            </div>
            <el-progress :percentage="percent(s)" :show-text="false" :stroke-width="6" color="#ff5000" />
            <div class="seckill__left">已抢 {{ s.sold }} / {{ s.stock + s.sold }}</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 推荐商品 -->
    <section class="abc-container abc-section">
      <SectionTitle title="为你推荐" subtitle="精选高性价比好物" more-text="更多商品"
                    @more="go('/products')" />
      <ProductGrid :products="data.recommends" :loading="loading" @add-cart="quickAdd" @favorite="toggleFavorite" />
    </section>

    <!-- 热销榜单 + 新品 -->
    <section class="abc-container abc-section">
      <div class="two-col">
        <div class="rank-card">
          <SectionTitle title="热销榜单" subtitle="实时销量 TOP 8" />
          <div class="rank-list">
            <div v-for="(p, idx) in data.hots" :key="p.id" class="rank-item"
                 @click="go(`/product/${p.id}`)">
              <span class="rank-item__no" :class="`rank-item__no--${idx < 3 ? idx + 1 : 'n'}`">{{ idx + 1 }}</span>
              <img :src="p.mainImage" :alt="p.name" />
              <div class="rank-item__info">
                <div class="abc-ellipsis-2">{{ p.name }}</div>
                <div class="rank-item__meta">
                  <MoneyText :value="p.price" size="small" />
                  <span>已售 {{ p.sales }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="topic-card">
          <SectionTitle title="新品上架" subtitle="每周三上新" more-text="更多新品" @more="go('/products?isNew=1')" />
          <ProductGrid :products="data.news.slice(0, 4)" :loading="loading"
                       @add-cart="quickAdd" @favorite="toggleFavorite" />
        </div>
      </div>
    </section>

    <!-- 专题 -->
    <section class="abc-container abc-section">
      <SectionTitle title="精选专题" subtitle="深度内容 · 选购指南" />
      <div class="topic-grid">
        <div v-for="t in data.topics" :key="t.id" class="topic-item" @click="go(`/topic/${t.id}`)">
          <img :src="t.cover" :alt="t.title" />
          <div class="topic-item__body">
            <b class="abc-ellipsis">{{ t.title }}</b>
            <span class="abc-ellipsis">{{ t.subtitle }}</span>
            <small><el-icon><View /></el-icon> {{ t.views }} 阅读</small>
          </div>
        </div>
      </div>
    </section>

    <SkuPicker v-model="skuVisible" :detail="skuDetail" confirm-text="加入购物车" @confirm="onSkuConfirm" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { AlarmClock, Bell, View } from '@element-plus/icons-vue'
import SectionTitle from '@/components/SectionTitle.vue'
import ProductGrid from '@/components/ProductGrid.vue'
import MoneyText from '@/components/MoneyText.vue'
import SkuPicker from '@/components/SkuPicker.vue'
import { cartApi, favoriteApi, homeApi, productApi } from '@/api'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const loading = ref(true)
const seckillList = ref([])
const data = reactive({
  carousels: [],
  ads: [],
  sideAds: [],
  notices: [],
  activities: [],
  topics: [],
  categories: [],
  recommends: [],
  hots: [],
  news: []
})

const skuVisible = ref(false)
const skuDetail = ref(null)
const pendingProductId = ref(null)

onMounted(async () => {
  try {
    const [home, seckill] = await Promise.all([
      homeApi.index(),
      homeApi.seckillList().catch(() => [])
    ])
    Object.assign(data, home || {})
    seckillList.value = (seckill || []).slice(0, 4)
  } catch (e) {
    ElMessage.error('首页数据加载失败，请确认后端服务已启动')
  } finally {
    loading.value = false
  }
})

function go(link) {
  if (!link) return
  if (/^https?:/.test(link)) {
    window.open(link, '_blank')
    return
  }
  router.push(link)
}

function percent(s) {
  const total = (s.stock || 0) + (s.sold || 0)
  if (!total) return 0
  return Math.min(100, Math.round(((s.sold || 0) / total) * 100))
}

async function quickAdd(product) {
  pendingProductId.value = product.id
  if (product.hasSpec === false) {
    await cartStore.add(product.id, null, 1)
    ElMessage.success('已加入购物车')
    userStore.refreshBadges()
    return
  }
  try {
    const detail = await productApi.detail(product.id)
    const specs = detail.specs || []
    if (!specs.length) {
      await cartStore.add(product.id, (detail.skus[0] || {}).id, 1)
      ElMessage.success('已加入购物车')
      userStore.refreshBadges()
      return
    }
    skuDetail.value = detail
    skuVisible.value = true
  } catch (e) {
    /* 已提示 */
  }
}

async function onSkuConfirm({ sku, quantity }) {
  try {
    await cartStore.add(pendingProductId.value, sku.id, quantity)
    ElMessage.success('已加入购物车')
    userStore.refreshBadges()
  } catch (e) {
    /* 已提示 */
  }
}

async function toggleFavorite(product) {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再收藏')
    router.push({ path: '/login', query: { redirect: `/product/${product.id}` } })
    return
  }
  try {
    const res = await favoriteApi.toggle(product.id)
    ElMessage.success(res && res.favorite ? '收藏成功' : '已取消收藏')
  } catch (e) {
    /* 已提示 */
  }
}
</script>

<style scoped>
.home {
  padding-top: 16px;
}
.hero {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 14px;
}
.hero__carousel {
  border-radius: var(--abc-radius);
  overflow: hidden;
  box-shadow: var(--abc-shadow);
}
.hero__slide {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;
}
.hero__slide img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hero__slide-title {
  position: absolute;
  left: 30px;
  bottom: 28px;
  padding: 8px 18px;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  border-radius: 20px;
  font-size: 15px;
  backdrop-filter: blur(4px);
}
.hero__side {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.hero__notice {
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  padding: 14px 16px;
  flex: 1;
  min-height: 0;
}
.hero__notice-head {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  padding-bottom: 10px;
  border-bottom: 1px dashed var(--abc-border);
}
.hero__notice-head span {
  flex: 1;
}
.hero__notice ul {
  list-style: none;
  margin: 8px 0 0;
  padding: 0;
}
.hero__notice li {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--abc-text-2);
  line-height: 32px;
  cursor: pointer;
  transition: color 0.2s;
}
.hero__notice li:hover {
  color: var(--abc-primary);
}
.hero__side-ad {
  border-radius: var(--abc-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--abc-shadow);
}
.hero__side-ad img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}
.cat-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 10px;
  background: #fff;
  border-radius: var(--abc-radius);
  padding: 18px 12px;
  box-shadow: var(--abc-shadow);
}
.cat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 4px;
  border-radius: 10px;
  transition: background 0.2s, transform 0.2s;
}
.cat-item:hover {
  background: var(--abc-primary-light);
  transform: translateY(-3px);
}
.cat-item__icon {
  width: 52px;
  height: 52px;
  border-radius: 50%;
}
.cat-item b {
  font-size: 13px;
}
.cat-item span {
  font-size: 11px;
  color: var(--abc-text-3);
  max-width: 100%;
}
.ad-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}
.ad-item {
  border-radius: var(--abc-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--abc-shadow);
  transition: transform 0.25s;
}
.ad-item:hover {
  transform: translateY(-3px);
}
.ad-item img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}
.act-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.act-item {
  position: relative;
  border-radius: var(--abc-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--abc-shadow);
}
.act-item img {
  width: 100%;
  height: 170px;
  object-fit: cover;
  transition: transform 0.35s;
}
.act-item:hover img {
  transform: scale(1.05);
}
.act-item__mask {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 22px 14px 12px;
  background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.68));
  color: #fff;
  display: flex;
  flex-direction: column;
}
.act-item__mask b {
  font-size: 15px;
}
.act-item__mask span {
  font-size: 12px;
  opacity: 0.85;
}
.seckill {
  background: #fff;
  border-radius: var(--abc-radius);
  padding: 18px;
  box-shadow: var(--abc-shadow);
}
.seckill__head {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--abc-primary);
  padding-bottom: 14px;
  border-bottom: 1px dashed var(--abc-border);
}
.seckill__head div {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.seckill__head b {
  color: var(--abc-text);
}
.seckill__head span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.seckill__list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  margin-top: 16px;
}
.seckill__item {
  cursor: pointer;
  border-radius: 10px;
  padding: 10px;
  transition: background 0.2s;
}
.seckill__item:hover {
  background: #fafbfc;
}
.seckill__item img {
  width: 100%;
  aspect-ratio: 1/1;
  object-fit: cover;
  border-radius: 8px;
}
.seckill__name {
  margin-top: 8px;
  font-size: 13px;
}
.seckill__price {
  display: flex;
  align-items: baseline;
  margin: 4px 0 6px;
}
.seckill__left {
  margin-top: 6px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.two-col {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
}
.rank-card,
.topic-card {
  background: #fff;
  border-radius: var(--abc-radius);
  padding: 18px;
  box-shadow: var(--abc-shadow);
}
.rank-list {
  display: flex;
  flex-direction: column;
}
.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 0;
  cursor: pointer;
  border-bottom: 1px dashed #f2f4f7;
}
.rank-item:last-child {
  border-bottom: none;
}
.rank-item__no {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  background: #c3cad6;
  flex-shrink: 0;
}
.rank-item__no--1 {
  background: linear-gradient(135deg, #ff5000, #ff9500);
}
.rank-item__no--2 {
  background: linear-gradient(135deg, #f2994a, #f7c58b);
}
.rank-item__no--3 {
  background: linear-gradient(135deg, #9b51e0, #c9a0f0);
}
.rank-item img {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  object-fit: cover;
  flex-shrink: 0;
}
.rank-item__info {
  flex: 1;
  min-width: 0;
  font-size: 13px;
}
.rank-item__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 4px;
}
.rank-item__meta span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.topic-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.topic-item {
  background: #fff;
  border-radius: var(--abc-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--abc-shadow);
  transition: transform 0.25s;
}
.topic-item:hover {
  transform: translateY(-3px);
}
.topic-item img {
  width: 100%;
  height: 150px;
  object-fit: cover;
}
.topic-item__body {
  padding: 12px 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.topic-item__body b {
  font-size: 15px;
}
.topic-item__body span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.topic-item__body small {
  margin-top: 4px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--abc-text-3);
  font-size: 12px;
}
@media (max-width: 1180px) {
  .cat-grid {
    grid-template-columns: repeat(5, 1fr);
  }
  .act-grid,
  .topic-grid,
  .seckill__list {
    grid-template-columns: repeat(2, 1fr);
  }
  .two-col {
    grid-template-columns: 1fr;
  }
}
@media (max-width: 900px) {
  .hero {
    grid-template-columns: 1fr;
  }
  .ad-grid {
    grid-template-columns: 1fr;
  }
}
</style>
