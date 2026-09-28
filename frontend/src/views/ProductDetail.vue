<template>
  <div v-loading="loading" class="abc-container page">
    <template v-if="detail">
      <el-breadcrumb separator="/" class="page__crumb">
        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
        <el-breadcrumb-item :to="{ path: `/category/${detail.category?.id}` }">
          {{ detail.category?.name || '商品' }}
        </el-breadcrumb-item>
        <el-breadcrumb-item>商品详情</el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 主信息区 -->
      <div class="main abc-card">
        <div class="gallery">
          <div class="gallery__main abc-img-box">
            <img v-if="!showVideo" :src="activeImage" :alt="detail.product.name" />
            <video v-else :src="detail.product.video" controls autoplay class="gallery__video"></video>
          </div>
          <div class="gallery__thumbs">
            <div v-if="detail.product.video" class="gallery__thumb"
                 :class="{ active: showVideo }" @click="showVideo = true" @mouseenter="showVideo = true">
              <el-icon :size="22"><VideoPlay /></el-icon>
              <span>视频</span>
            </div>
            <div v-for="(img, idx) in detail.imageList" :key="idx" class="gallery__thumb"
                 :class="{ active: !showVideo && activeImage === img }"
                 @mouseenter="preview(img)">
              <img :src="img" :alt="`${detail.product.name}-${idx + 1}`" />
            </div>
          </div>

          <div class="gallery__actions">
            <el-button :icon="Share" @click="share">分享</el-button>
            <el-button :type="isFavorite ? 'warning' : 'default'" :icon="Star" @click="toggleFavorite">
              {{ isFavorite ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>

        <div class="info">
          <h1 class="info__name">{{ detail.product.name }}</h1>
          <p class="info__subtitle">{{ detail.product.subtitle }}</p>

          <div class="info__tags abc-tags">
            <span v-for="tag in tagList" :key="tag" class="abc-tag">{{ tag }}</span>
          </div>

          <div class="info__price-box">
            <div class="info__price-row">
              <span class="info__price-label">售价</span>
              <MoneyText :value="currentPrice" size="large" />
              <span v-if="detail.product.marketPrice > currentPrice" class="abc-market-price">
                ¥{{ Number(detail.product.marketPrice).toFixed(2) }}
              </span>
            </div>
            <div class="info__meta">
              <span>销量 <b>{{ detail.product.sales }}</b></span>
              <span>库存 <b>{{ currentStock }}</b></span>
              <span>浏览 <b>{{ detail.product.views }}</b></span>
              <span>单位 <b>{{ detail.product.unit }}</b></span>
            </div>
          </div>

          <div class="info__row">
            <span class="info__label">品牌</span>
            <span>{{ detail.brand?.name || '—' }}</span>
            <span class="info__label info__label--gap">分类</span>
            <span>{{ detail.category?.name || '—' }}</span>
          </div>

          <div v-for="spec in detail.specs" :key="spec.id" class="info__row info__row--spec">
            <span class="info__label">{{ spec.name }}</span>
            <div class="spec-values">
              <span v-for="v in spec.values" :key="v.id" class="spec-value"
                    :class="{ active: selectedMap[spec.name] === v.value }"
                    @click="selectedMap = { ...selectedMap, [spec.name]: v.value }">
                {{ v.value }}
              </span>
            </div>
          </div>

          <div class="info__row">
            <span class="info__label">数量</span>
            <el-input-number v-model="quantity" :min="1" :max="maxQuantity" />
            <span class="info__stock-tip" v-if="currentSku">
              已选：{{ currentSku.specDesc }}
            </span>
          </div>

          <div class="info__buy">
            <el-button size="large" type="warning" :icon="ShoppingCart" @click="addToCart">加入购物车</el-button>
            <el-button size="large" type="primary" @click="buyNow">立即购买</el-button>
          </div>

          <div class="info__promise">
            <span><el-icon><CircleCheck /></el-icon> 正品保障</span>
            <span><el-icon><Van /></el-icon> 满 99 包邮</span>
            <span><el-icon><RefreshLeft /></el-icon> 七天无理由</span>
            <span><el-icon><Service /></el-icon> 售后无忧</span>
          </div>
        </div>
      </div>

      <!-- 参数 + 详情 -->
      <div class="tabs abc-card">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="rich" v-html="detail.product.detailHtml || '<p>暂无图文详情</p>'"></div>
          </el-tab-pane>

          <el-tab-pane label="规格参数" name="attrs">
            <div class="attrs">
              <div v-for="g in detail.attrGroups" :key="g.groupName" class="attrs__group">
                <h4>{{ g.groupName }}</h4>
                <el-descriptions :column="2" border>
                  <el-descriptions-item v-for="a in g.attrs" :key="a.id" :label="a.name">
                    {{ a.value }}
                  </el-descriptions-item>
                </el-descriptions>
              </div>
              <EmptyBox v-if="!detail.attrGroups.length" icon="Document" text="暂无规格参数" />
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`商品评价（${commentStat.total || 0}）`" name="comments">
            <div class="comments">
              <div class="comments__summary">
                <div class="comments__score">
                  <b>{{ (commentStat.avgRating || 5).toFixed(1) }}</b>
                  <el-rate :model-value="Math.round(commentStat.avgRating || 5)" disabled />
                  <span>共 {{ commentStat.total || 0 }} 条评价</span>
                </div>
                <div class="comments__bars">
                  <div v-for="(count, star) in commentStat.starCount" :key="star" class="comments__bar">
                    <span>{{ star }} 星</span>
                    <el-progress :percentage="barPercent(count)" :show-text="false"
                                 :stroke-width="8" color="#ff9500" />
                    <span>{{ count }}</span>
                  </div>
                </div>
                <div class="comments__tags">
                  <el-tag effect="plain">好评 {{ commentStat.goodCount || 0 }}</el-tag>
                  <el-tag effect="plain" type="info">中评 {{ commentStat.midCount || 0 }}</el-tag>
                  <el-tag effect="plain" type="danger">差评 {{ commentStat.badCount || 0 }}</el-tag>
                </div>
              </div>

              <div v-for="c in comments" :key="c.id" class="comment">
                <div class="comment__avatar">
                  <img :src="c.avatar || placeholderImage(c.nickname || '匿名', 80, 80, 5)" :alt="c.nickname" />
                </div>
                <div class="comment__body">
                  <div class="comment__head">
                    <b>{{ c.nickname || '匿名用户' }}</b>
                    <el-rate :model-value="c.rating" disabled size="small" />
                  </div>
                  <p class="comment__text">{{ c.content }}</p>
                  <div v-if="c.images && c.images !== '[]'" class="comment__imgs">
                    <img v-for="(img, i) in parseImages(c.images)" :key="i" :src="img" alt="晒图" />
                  </div>
                  <div v-if="c.appendContent" class="comment__append">
                    <b>追评：</b>{{ c.appendContent }}
                  </div>
                  <div v-if="c.reply" class="comment__reply">
                    <b>商家回复：</b>{{ c.reply }}
                  </div>
                  <div class="comment__time">{{ c.skuDesc }} · {{ formatTime(c.createTime) }}</div>
                </div>
              </div>
              <EmptyBox v-if="!comments.length" icon="ChatDotSquare" text="暂无评价，快去下单抢首评" />

              <div v-if="commentTotal > 0" class="pager">
                <el-pagination background layout="prev, pager, next, total"
                               :total="commentTotal" :current-page="commentPage" :page-size="commentSize"
                               @current-change="loadComments" />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- 相关推荐 -->
      <div class="abc-section">
        <SectionTitle title="同类推荐" subtitle="看了又看" />
        <ProductGrid :products="detail.related" @add-cart="quickAdd" />
      </div>
    </template>

    <EmptyBox v-else-if="!loading" icon="WarningFilled" text="商品不存在或已下架" action-text="回到首页"
              @action="$router.push('/')" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck, RefreshLeft, Service, Share, ShoppingCart, Star, Van, VideoPlay } from '@element-plus/icons-vue'
import MoneyText from '@/components/MoneyText.vue'
import ProductGrid from '@/components/ProductGrid.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { cartApi, commentApi, favoriteApi, placeholderImage, productApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const detail = ref(null)
const activeImage = ref('')
const showVideo = ref(false)
const quantity = ref(1)
const selectedMap = ref({})
const isFavorite = ref(false)
const activeTab = ref('detail')

const comments = ref([])
const commentPage = ref(1)
const commentSize = ref(5)
const commentTotal = ref(0)
const commentStat = ref({ avgRating: 5, total: 0, starCount: {}, goodCount: 0, midCount: 0, badCount: 0 })

onMounted(load)

watch(
  () => route.params.id,
  () => {
    if (route.name === 'ProductDetail') {
      activeTab.value = 'detail'
      comments.value = []
      commentPage.value = 1
      load()
    }
  }
)

async function load() {
  loading.value = true
  try {
    const res = await productApi.detail(route.params.id)
    detail.value = res
    activeImage.value = (res.imageList || [])[0] || res.product.mainImage
    isFavorite.value = !!res.favorite
    selectedMap.value = {}
    quantity.value = 1
    if ((res.specs || []).length) {
      res.specs.forEach((s) => {
        if (s.values.length) {
          selectedMap.value[s.name] = s.values[0].value
        }
      })
    }
    await Promise.all([loadComments(1), loadStat()])
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
}

async function loadComments(page = 1) {
  commentPage.value = page
  try {
    const res = await commentApi.product(route.params.id, { page, size: commentSize.value })
    comments.value = res.list || []
    commentTotal.value = res.total || 0
  } catch (e) {
    comments.value = []
  }
}

async function loadStat() {
  try {
    commentStat.value = await commentApi.statistics(route.params.id)
  } catch (e) {
    /* 忽略 */
  }
}

const tagList = computed(() => {
  if (!detail.value?.product?.tags) return []
  return String(detail.value.product.tags).split(',').filter(Boolean)
})

const currentSku = computed(() => {
  const d = detail.value
  if (!d) return null
  const specs = d.specs || []
  if (!specs.length) return (d.skus || [])[0] || null
  if (specs.some((s) => !selectedMap.value[s.name])) return null
  return (
    (d.skus || []).find((sku) => {
      let arr = []
      try {
        arr = JSON.parse(sku.specJson || '[]')
      } catch (e) {
        return false
      }
      return arr.every((i) => selectedMap.value[i.specName] === i.value)
    }) || null
  )
})

const currentPrice = computed(() =>
  currentSku.value ? currentSku.value.price : detail.value?.product?.price || 0
)
const currentStock = computed(() =>
  currentSku.value ? currentSku.value.stock : detail.value?.product?.stock || 0
)
const maxQuantity = computed(() => Math.max(1, Math.min(currentStock.value || 1, 99)))

function preview(img) {
  showVideo.value = false
  activeImage.value = img
}

function parseImages(json) {
  try {
    return JSON.parse(json || '[]')
  } catch (e) {
    return []
  }
}

function barPercent(count) {
  const total = commentStat.value.total || 0
  if (!total) return 0
  return Math.round((count / total) * 100)
}

function formatTime(time) {
  return time ? String(time).replace('T', ' ').slice(0, 16) : ''
}

async function addToCart() {
  if (!currentSku.value) {
    ElMessage.warning('请先选择完整规格')
    return
  }
  if (currentStock.value <= 0) {
    ElMessage.warning('该规格暂时缺货')
    return
  }
  await doAdd(currentSku.value.id, quantity.value)
}

async function doAdd(skuId, qty) {
  try {
    await cartApi.add({ productId: detail.value.product.id, skuId, quantity: qty })
    ElMessage.success('已加入购物车')
    userStore.refreshBadges()
  } catch (e) {
    /* 已提示 */
  }
}

function buyNow() {
  if (!currentSku.value) {
    ElMessage.warning('请先选择完整规格')
    return
  }
  doBuyNow(currentSku.value.id, quantity.value)
}

async function doBuyNow(skuId, qty) {
  try {
    const item = await cartApi.add({ productId: detail.value.product.id, skuId, quantity: qty })
    userStore.refreshBadges()
    // 立即购买：只结算新增的这一件，因此把购物车条目ID带到结算页
    router.push({ path: '/checkout', query: { ids: item && item.id ? item.id : undefined, from: 'detail' } })
  } catch (e) {
    /* 已提示 */
  }
}

async function toggleFavorite() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录后再收藏')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  try {
    const res = await favoriteApi.toggle(detail.value.product.id)
    isFavorite.value = !!res.favorite
    ElMessage.success(isFavorite.value ? '收藏成功' : '已取消收藏')
  } catch (e) {
    /* 已提示 */
  }
}

function share() {
  const url = window.location.href
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url).then(() => ElMessage.success('商品链接已复制，快去分享吧'))
  } else {
    ElMessage.info(url)
  }
}

async function quickAdd(product) {
  try {
    const d = await productApi.detail(product.id)
    const sku = (d.skus || [])[0]
    await doAdd(sku ? sku.id : null, 1)
  } catch (e) {
    /* 已提示 */
  }
}
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.page__crumb {
  margin-bottom: 14px;
}
.main {
  display: grid;
  grid-template-columns: 440px 1fr;
  gap: 26px;
  padding: 22px;
}
.gallery__main {
  width: 100%;
  aspect-ratio: 1/1;
  border-radius: 10px;
}
.gallery__video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  background: #000;
}
.gallery__thumbs {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  flex-wrap: wrap;
}
.gallery__thumb {
  width: 62px;
  height: 62px;
  border-radius: 8px;
  border: 2px solid transparent;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f6f8;
  font-size: 11px;
  color: var(--abc-text-2);
}
.gallery__thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.gallery__thumb.active {
  border-color: var(--abc-primary);
}
.gallery__actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
}
.info__name {
  margin: 0;
  font-size: 22px;
  line-height: 32px;
}
.info__subtitle {
  margin: 8px 0 0;
  color: var(--abc-primary);
  font-size: 13px;
}
.info__tags {
  margin-top: 10px;
}
.info__price-box {
  margin-top: 16px;
  padding: 16px 18px;
  border-radius: 10px;
  background: linear-gradient(135deg, #fff6f2, #fffaf6);
}
.info__price-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}
.info__price-label {
  font-size: 13px;
  color: var(--abc-text-3);
}
.info__meta {
  display: flex;
  gap: 22px;
  margin-top: 12px;
  font-size: 13px;
  color: var(--abc-text-2);
}
.info__meta b {
  color: var(--abc-text);
}
.info__row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 16px;
  font-size: 13px;
  flex-wrap: wrap;
}
.info__row--spec {
  align-items: flex-start;
}
.info__label {
  width: 46px;
  flex-shrink: 0;
  color: var(--abc-text-3);
}
.info__label--gap {
  width: auto;
  margin-left: 18px;
}
.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.spec-value {
  padding: 5px 14px;
  border: 1px solid var(--abc-border);
  border-radius: 18px;
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}
.spec-value:hover {
  border-color: var(--abc-primary);
  color: var(--abc-primary);
}
.spec-value.active {
  border-color: var(--abc-primary);
  background: var(--abc-primary-light);
  color: var(--abc-primary);
  font-weight: 600;
}
.info__stock-tip {
  color: var(--abc-text-3);
  margin-left: 10px;
}
.info__buy {
  margin-top: 22px;
  display: flex;
  gap: 12px;
}
.info__buy .el-button {
  min-width: 160px;
  height: 46px;
  font-size: 16px;
}
.info__promise {
  margin-top: 18px;
  display: flex;
  gap: 18px;
  font-size: 12px;
  color: var(--abc-text-2);
}
.info__promise span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.tabs {
  margin-top: 16px;
  padding: 8px 22px 24px;
}
.rich {
  line-height: 1.9;
  color: var(--abc-text-2);
}
.rich :deep(img) {
  max-width: 100%;
  border-radius: 8px;
  margin: 12px 0;
}
.rich :deep(h3) {
  color: var(--abc-text);
  margin: 18px 0 8px;
}
.attrs__group {
  margin-bottom: 22px;
}
.attrs__group h4 {
  margin: 0 0 10px;
  font-size: 15px;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
.comments__summary {
  display: flex;
  align-items: center;
  gap: 40px;
  padding: 18px 20px;
  background: #fafbfc;
  border-radius: 10px;
  flex-wrap: wrap;
}
.comments__score {
  text-align: center;
}
.comments__score b {
  font-size: 38px;
  color: var(--abc-primary);
}
.comments__score span {
  display: block;
  font-size: 12px;
  color: var(--abc-text-3);
}
.comments__bars {
  flex: 1;
  min-width: 260px;
}
.comments__bar {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.comments__bar :deep(.el-progress) {
  flex: 1;
}
.comment {
  display: flex;
  gap: 14px;
  padding: 18px 0;
  border-bottom: 1px dashed var(--abc-border);
}
.comment__avatar img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
}
.comment__body {
  flex: 1;
}
.comment__head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.comment__text {
  margin: 8px 0 0;
  line-height: 1.75;
  color: var(--abc-text-2);
}
.comment__imgs {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.comment__imgs img {
  width: 82px;
  height: 82px;
  border-radius: 8px;
  object-fit: cover;
}
.comment__append,
.comment__reply {
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  font-size: 13px;
  color: var(--abc-text-2);
  background: #fafbfc;
}
.comment__reply {
  background: var(--abc-primary-light);
}
.comment__time {
  margin-top: 8px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
@media (max-width: 1000px) {
  .main {
    grid-template-columns: 1fr;
  }
}
</style>
