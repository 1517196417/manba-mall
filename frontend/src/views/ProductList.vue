<template>
  <div class="abc-container page">
    <el-breadcrumb separator="/" class="page__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item :to="{ path: '/category' }">全部分类</el-breadcrumb-item>
      <el-breadcrumb-item v-for="c in breadcrumb" :key="c.id">{{ c.name }}</el-breadcrumb-item>
    </el-breadcrumb>

    <!-- 筛选面板 -->
    <div class="filter abc-card">
      <div class="filter__row">
        <span class="filter__label">分类：</span>
        <div class="filter__values">
          <span class="filter__value" :class="{ active: !query.categoryId }"
                @click="setCategory(null)">全部</span>
          <span v-for="c in siblingCategories" :key="c.id" class="filter__value"
                :class="{ active: query.categoryId === c.id }" @click="setCategory(c.id)">
            {{ c.name }}
          </span>
        </div>
      </div>

      <div class="filter__row">
        <span class="filter__label">品牌：</span>
        <div class="filter__values">
          <span class="filter__value" :class="{ active: !query.brandId }"
                @click="setBrand(null)">全部</span>
          <span v-for="b in brands" :key="b.id" class="filter__value"
                :class="{ active: query.brandId === b.id }" @click="setBrand(b.id)">
            {{ b.name }}
          </span>
        </div>
      </div>

      <div class="filter__row">
        <span class="filter__label">价格：</span>
        <div class="filter__values">
          <span v-for="range in priceRanges" :key="range.label" class="filter__value"
                :class="{ active: query.minPrice === range.min && query.maxPrice === range.max }"
                @click="setPrice(range)">
            {{ range.label }}
          </span>
          <span class="filter__price-input">
            <el-input v-model="customMin" size="small" placeholder="最低价" style="width: 82px" />
            <i>—</i>
            <el-input v-model="customMax" size="small" placeholder="最高价" style="width: 82px" />
            <el-button size="small" text type="primary" @click="applyCustomPrice">确定</el-button>
          </span>
        </div>
      </div>

      <div class="filter__row filter__row--last">
        <span class="filter__label">排序：</span>
        <div class="filter__values">
          <span v-for="s in sorts" :key="s.value" class="filter__sort"
                :class="{ active: query.sort === s.value }" @click="setSort(s.value)">
            {{ s.label }}
            <el-icon v-if="s.value === 'price_asc' && query.sort === 'price_asc'"><Top /></el-icon>
            <el-icon v-if="s.value === 'price_desc' && query.sort === 'price_desc'"><Bottom /></el-icon>
          </span>
          <span class="filter__count">共 {{ total }} 件商品</span>
        </div>
      </div>
    </div>

    <div class="abc-card list-body">
      <ProductGrid :products="products" :loading="loading" @add-cart="quickAdd" />
      <div v-if="total > 0" class="pager">
        <el-pagination background layout="prev, pager, next, jumper, total"
                       :total="total" :current-page="query.page" :page-size="query.size"
                       @current-change="onPageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Bottom, Top } from '@element-plus/icons-vue'
import ProductGrid from '@/components/ProductGrid.vue'
import { brandApi, cartApi, categoryApi, productApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const products = ref([])
const total = ref(0)
const brands = ref([])
const tree = ref([])
const breadcrumb = ref([])

const customMin = ref('')
const customMax = ref('')

const query = reactive({
  categoryId: null,
  brandId: null,
  minPrice: null,
  maxPrice: null,
  sort: 'default',
  page: 1,
  size: 15,
  isNew: null,
  isRecommend: null
})

const priceRanges = [
  { label: '0-100', min: 0, max: 100 },
  { label: '100-500', min: 100, max: 500 },
  { label: '500-1000', min: 500, max: 1000 },
  { label: '1000-5000', min: 1000, max: 5000 },
  { label: '5000 以上', min: 5000, max: null }
]

const sorts = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price_asc' },
  { label: '新品', value: 'new' }
]

const siblingCategories = ref([])

onMounted(async () => {
  try {
    const [brandList, catTree] = await Promise.all([brandApi.list(), categoryApi.tree()])
    brands.value = brandList || []
    tree.value = catTree || []
  } catch (e) {
    /* 已提示 */
  }
  await syncFromRoute()
  await load()
})

async function syncFromRoute() {
  const rq = route.query
  query.categoryId = rq.categoryId ? Number(rq.categoryId) : null
  query.brandId = rq.brandId ? Number(rq.brandId) : null
  query.minPrice = rq.minPrice !== undefined ? Number(rq.minPrice) : null
  query.maxPrice = rq.maxPrice !== undefined && rq.maxPrice !== '' ? Number(rq.maxPrice) : null
  query.sort = rq.sort || 'default'
  query.isNew = rq.isNew ? Number(rq.isNew) : null
  query.isRecommend = rq.isRecommend ? Number(rq.isRecommend) : null
  query.page = rq.page ? Number(rq.page) : 1

  if (query.categoryId) {
    breadcrumb.value = (await categoryApi.breadcrumb(query.categoryId)) || []
    const current = breadcrumb.value[breadcrumb.value.length - 1]
    if (current) {
      const parent = tree.value.find((c) => c.id === (current.parentId === 0 ? current.id : current.parentId))
      siblingCategories.value = parent ? parent.children || [] : []
    }
  } else {
    breadcrumb.value = []
    siblingCategories.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const res = await productApi.page({
      categoryId: query.categoryId,
      brandId: query.brandId,
      minPrice: query.minPrice,
      maxPrice: query.maxPrice,
      sort: query.sort,
      isNew: query.isNew,
      isRecommend: query.isRecommend,
      page: query.page,
      size: query.size
    })
    products.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    products.value = []
  } finally {
    loading.value = false
  }
}

function pushQuery(patch) {
  const next = {
    categoryId: query.categoryId,
    brandId: query.brandId,
    minPrice: query.minPrice,
    maxPrice: query.maxPrice,
    sort: query.sort,
    isNew: query.isNew,
    isRecommend: query.isRecommend,
    page: 1,
    ...patch
  }
  Object.keys(next).forEach((k) => {
    if (next[k] === null || next[k] === undefined || next[k] === '') {
      delete next[k]
    }
  })
  router.push({ path: route.path === '/search' ? '/search' : '/products', query: next })
}

function setCategory(id) {
  pushQuery({ categoryId: id })
}
function setBrand(id) {
  pushQuery({ brandId: id })
}
function setPrice(range) {
  pushQuery({ minPrice: range.min, maxPrice: range.max })
}
function applyCustomPrice() {
  pushQuery({
    minPrice: customMin.value === '' ? null : Number(customMin.value),
    maxPrice: customMax.value === '' ? null : Number(customMax.value)
  })
}
function setSort(value) {
  if (value === 'price_asc' && query.sort === 'price_asc') {
    pushQuery({ sort: 'price_desc' })
    return
  }
  pushQuery({ sort: value })
}
function onPageChange(p) {
  pushQuery({ page: p })
}

watch(
  () => route.fullPath,
  async () => {
    await syncFromRoute()
    await load()
  }
)

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
.page {
  padding-top: 16px;
}
.page__crumb {
  margin-bottom: 14px;
}
.filter {
  padding: 6px 18px 14px;
}
.filter__row {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px dashed var(--abc-border);
}
.filter__row--last {
  border-bottom: none;
}
.filter__label {
  width: 56px;
  flex-shrink: 0;
  color: var(--abc-text-3);
  font-size: 13px;
  line-height: 26px;
}
.filter__values {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
.filter__value,
.filter__sort {
  padding: 3px 12px;
  border-radius: 14px;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}
.filter__value:hover,
.filter__sort:hover {
  color: var(--abc-primary);
}
.filter__value.active,
.filter__sort.active {
  color: var(--abc-primary);
  background: var(--abc-primary-light);
  border-color: rgba(255, 80, 0, 0.3);
  font-weight: 600;
}
.filter__price-input {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.filter__price-input i {
  color: var(--abc-text-3);
}
.filter__count {
  margin-left: auto;
  font-size: 13px;
  color: var(--abc-text-3);
}
.list-body {
  margin-top: 14px;
  padding: 18px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
