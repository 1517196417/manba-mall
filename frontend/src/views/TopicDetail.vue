<template>
  <div class="abc-container topic">
    <el-breadcrumb separator="/" class="topic__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>专题详情</el-breadcrumb-item>
      <el-breadcrumb-item>{{ topic.title || '专题' }}</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-loading="loading">
      <div class="topic__hero" :style="heroStyle">
        <div class="topic__hero-mask">
          <span class="topic__badge">专题</span>
          <h1>{{ topic.title || '精选专题' }}</h1>
          <p v-if="topic.subtitle">{{ topic.subtitle }}</p>
          <div class="topic__meta">
            <span><el-icon><View /></el-icon> {{ topic.views || 0 }} 次浏览</span>
            <span v-if="topic.createTime"><el-icon><Clock /></el-icon> {{ formatTime(topic.createTime) }}</span>
          </div>
        </div>
      </div>

      <div v-if="topic.content" class="topic__content abc-card" v-html="topic.content"></div>

      <SectionTitle title="专题推荐商品" subtitle="本期专题为你挑选的好物" class="topic__sec" />
      <ProductGrid :products="products" :loading="loading" empty-text="专题商品准备中" @add-cart="quickAdd" />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, View } from '@element-plus/icons-vue'
import ProductGrid from '@/components/ProductGrid.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { cartApi, homeApi, placeholderImage, productApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()

const loading = ref(true)
const topic = ref({})
const products = ref([])

const heroStyle = computed(() => {
  const img = topic.value.cover
  if (img) {
    return { backgroundImage: `url(${img})` }
  }
  return { backgroundImage: `url(${placeholderImage(topic.value.title || '专题', 1200, 340, 2)})` }
})

onMounted(load)
watch(() => route.params.id, load)

async function load() {
  const id = route.params.id
  if (!id) return
  loading.value = true
  try {
    const data = await homeApi.topicDetail(id)
    topic.value = (data && data.topic) || {}
    products.value = (data && data.products) || []
  } catch (e) {
    topic.value = {}
    products.value = []
  } finally {
    loading.value = false
  }
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
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
</script>

<style scoped>
.topic {
  padding-top: 16px;
}
.topic__crumb {
  margin-bottom: 14px;
}
.topic__hero {
  height: 300px;
  border-radius: var(--abc-radius);
  background-size: cover;
  background-position: center;
  overflow: hidden;
  box-shadow: var(--abc-shadow);
}
.topic__hero-mask {
  height: 100%;
  padding: 36px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
  background: linear-gradient(90deg, rgba(15, 23, 42, 0.8), rgba(15, 23, 42, 0.2));
}
.topic__badge {
  width: fit-content;
  padding: 3px 12px;
  border-radius: 11px;
  font-size: 12px;
  background: rgba(255, 80, 0, 0.92);
  margin-bottom: 14px;
}
.topic__hero-mask h1 {
  margin: 0;
  font-size: 32px;
  letter-spacing: 1px;
}
.topic__hero-mask p {
  margin: 10px 0 0;
  font-size: 14px;
  opacity: 0.92;
  max-width: 640px;
}
.topic__meta {
  margin-top: 20px;
  display: flex;
  gap: 20px;
  font-size: 12.5px;
  opacity: 0.9;
}
.topic__meta span {
  display: inline-flex;
  align-items: center;
  gap: 5px;
}
.topic__content {
  margin-top: 18px;
  padding: 26px 30px;
  font-size: 14.5px;
  line-height: 1.9;
  color: var(--abc-text-2);
}
.topic__content :deep(img) {
  border-radius: 10px;
  margin: 14px 0;
}
.topic__content :deep(h2),
.topic__content :deep(h3) {
  color: var(--abc-text);
}
.topic__sec {
  margin-top: 24px;
}
</style>
