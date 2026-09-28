<template>
  <div class="abc-container actd">
    <el-breadcrumb separator="/" class="actd__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/activity' }">活动专区</el-breadcrumb-item>
      <el-breadcrumb-item>{{ activity.title || '活动详情' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-loading="loading">
      <div class="actd__banner" :style="bannerStyle">
        <div class="actd__banner-mask">
          <el-tag effect="dark" class="actd__tag">{{ typeText(activity.type) }}</el-tag>
          <h1>{{ activity.title }}</h1>
          <p v-if="activity.subtitle">{{ activity.subtitle }}</p>
          <div class="actd__banner-foot">
            <span class="actd__time">
              <el-icon><Clock /></el-icon>
              {{ formatTime(activity.startTime) }} ~ {{ formatTime(activity.endTime) }}
            </span>
            <div v-if="activity.endTime" class="actd__cd">
              <span>距结束</span>
              <CountDown :end-time="activity.endTime" />
            </div>
          </div>
        </div>
      </div>

      <div class="actd__bar">
        <div class="actd__bar-left">
          <el-icon><Discount /></el-icon>
          <span>活动商品专场，先领券再下单更划算</span>
        </div>
        <div class="actd__bar-right">
          <el-button round @click="go('/coupon')">去领券</el-button>
          <el-button type="primary" round @click="enter">进入活动会场</el-button>
        </div>
      </div>

      <SectionTitle title="活动精选商品" subtitle="为你挑选的会场好物" class="actd__sec" />
      <ProductGrid :products="products" :loading="loadingProducts" empty-text="活动商品准备中"
                   @add-cart="quickAdd" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Discount } from '@element-plus/icons-vue'
import CountDown from '@/components/CountDown.vue'
import ProductGrid from '@/components/ProductGrid.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { cartApi, homeApi, placeholderImage, productApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const loadingProducts = ref(true)
const activity = ref({})
const products = ref([])

const TYPE_TEXT = {
  promotion: '专场活动',
  seckill: '限时秒杀',
  groupon: '拼团活动',
  new: '新品首发',
  season: '季节活动'
}

const bannerStyle = computed(() => {
  const img = activity.value.image
  if (img) {
    return { backgroundImage: `url(${img})` }
  }
  return { backgroundImage: `url(${placeholderImage(activity.value.title || '活动', 1200, 360, 1)})` }
})

onMounted(load)
watch(() => route.params.id, load)

async function load() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  loadingProducts.value = true
  try {
    activity.value = (await homeApi.activityDetail(id)) || {}
  } catch (e) {
    activity.value = {}
  } finally {
    loading.value = false
  }
  try {
    const res = await productApi.page({ page: 1, size: 10, sort: 'hot' })
    products.value = res.list || []
  } catch (e) {
    products.value = []
  } finally {
    loadingProducts.value = false
  }
}

function typeText(type) {
  return TYPE_TEXT[type] || '活动'
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : '长期有效'
}

function enter() {
  const link = activity.value.link
  if (link && link.startsWith('/')) {
    router.push(link)
    return
  }
  ElMessage.info('活动会场入口已加载当前会场商品，可直接下单')
}

async function quickAdd(product) {
  try {
    const detail = await productApi.detail(product.id)
    const sku = (detail.skus || [])[0]
    await cartApi.add({ productId: product.id, skuId: sku ? sku.id : null, quantity: 1 })
    ElMessage.success('已加入购物车')
    userStore.refreshBadges()
  } catch (e) {
    /* 已提示 */
  }
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.actd {
  padding-top: 16px;
}
.actd__crumb {
  margin-bottom: 14px;
}
.actd__banner {
  height: 300px;
  border-radius: var(--abc-radius);
  background-size: cover;
  background-position: center;
  overflow: hidden;
  box-shadow: var(--abc-shadow);
}
.actd__banner-mask {
  height: 100%;
  padding: 34px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
  background: linear-gradient(90deg, rgba(15, 23, 42, 0.82), rgba(15, 23, 42, 0.25));
}
.actd__tag {
  width: fit-content;
  margin-bottom: 14px;
  background: var(--abc-primary);
  border-color: var(--abc-primary);
}
.actd__banner-mask h1 {
  margin: 0;
  font-size: 32px;
  letter-spacing: 1px;
}
.actd__banner-mask p {
  margin: 10px 0 0;
  font-size: 14px;
  opacity: 0.92;
  max-width: 620px;
}
.actd__banner-foot {
  margin-top: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.actd__time {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  opacity: 0.92;
}
.actd__cd {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 5px 14px;
  border-radius: 16px;
  background: rgba(0, 0, 0, 0.35);
  font-size: 12.5px;
}
.actd__bar {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  padding: 16px 22px;
  border-radius: var(--abc-radius);
  background: #fff;
  box-shadow: var(--abc-shadow);
}
.actd__bar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--abc-primary);
  font-size: 14px;
  font-weight: 600;
}
.actd__sec {
  margin-top: 22px;
}
</style>
