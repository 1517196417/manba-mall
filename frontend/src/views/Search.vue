<template>
  <div class="abc-container page">
    <div class="search-box abc-card">
      <div class="search-box__bar">
        <el-input v-model="keyword" size="large" placeholder="搜索商品名称、品牌、关键词"
                  @keyup.enter="doSearch" clearable>
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" @click="doSearch">搜索</el-button>
      </div>

      <div class="search-box__row">
        <span class="search-box__label">搜索历史：</span>
        <template v-if="history.length">
          <el-tag v-for="h in history" :key="h" closable class="search-box__tag"
                  @close="removeHistory(h)" @click="searchKeyword(h)">{{ h }}</el-tag>
          <el-link :underline="false" @click="clearHistory">清空</el-link>
        </template>
        <span v-else class="search-box__empty">暂无搜索历史</span>
      </div>

      <div class="search-box__row">
        <span class="search-box__label">热门搜索：</span>
        <el-tag v-for="h in hotWords" :key="h" type="danger" effect="plain"
                class="search-box__tag" @click="searchKeyword(h)">{{ h }}</el-tag>
      </div>
    </div>

    <template v-if="searched">
      <div class="result-head abc-card">
        <div>
          搜索「<b class="highlight">{{ resultKeyword }}</b>」，共找到
          <b class="highlight">{{ total }}</b> 件商品
        </div>
        <div class="result-head__sorts">
          <span v-for="s in sorts" :key="s.value" class="sort-item"
                :class="{ active: sort === s.value }" @click="setSort(s.value)">{{ s.label }}</span>
        </div>
      </div>

      <div class="abc-card list-body">
        <ProductGrid :products="products" :loading="loading"
                     empty-text="没有找到相关商品，换个关键词试试" @add-cart="quickAdd" />
        <div v-if="total > 0" class="pager">
          <el-pagination background layout="prev, pager, next, total"
                         :total="total" :current-page="page" :page-size="size"
                         @current-change="onPageChange" />
        </div>
      </div>
    </template>

    <div v-else class="search-tip abc-card">
      <el-icon :size="46"><Search /></el-icon>
      <p>输入关键词开始搜索，支持商品名称、副标题与促销标签</p>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import ProductGrid from '@/components/ProductGrid.vue'
import { cartApi, homeApi, productApi, searchApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const HISTORY_KEY = 'abcmall_search_history'

const keyword = ref('')
const resultKeyword = ref('')
const history = ref([])
const hotWords = ref([])
const products = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(15)
const sort = ref('default')
const loading = ref(false)
const searched = ref(false)

const sorts = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格升序', value: 'price_asc' },
  { label: '价格降序', value: 'price_desc' },
  { label: '新品', value: 'new' }
]

onMounted(async () => {
  history.value = readHistory()
  try {
    hotWords.value = (await searchApi.hot()) || []
  } catch (e) {
    /* 忽略 */
  }
  await syncAndSearch()
})

function readHistory() {
  try {
    return JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]')
  } catch (e) {
    return []
  }
}

function saveHistory(word) {
  const list = readHistory().filter((h) => h !== word)
  list.unshift(word)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list.slice(0, 12)))
  history.value = list.slice(0, 12)
}

function removeHistory(word) {
  const list = readHistory().filter((h) => h !== word)
  localStorage.setItem(HISTORY_KEY, JSON.stringify(list))
  history.value = list
}

function clearHistory() {
  localStorage.removeItem(HISTORY_KEY)
  history.value = []
}

async function syncAndSearch() {
  keyword.value = route.query.keyword || ''
  sort.value = route.query.sort || 'default'
  page.value = route.query.page ? Number(route.query.page) : 1
  if (keyword.value) {
    resultKeyword.value = keyword.value
    searched.value = true
    await load()
  } else {
    searched.value = false
    products.value = []
    total.value = 0
  }
}

async function load() {
  loading.value = true
  try {
    const res = await searchApi.search({
      keyword: resultKeyword.value,
      sort: sort.value,
      page: page.value,
      size: size.value
    })
    products.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  saveHistory(kw)
  router.push({ path: '/search', query: { keyword: kw } })
}

function searchKeyword(word) {
  keyword.value = word
  doSearch()
}

function setSort(value) {
  router.push({ path: '/search', query: { keyword: resultKeyword.value, sort: value, page: 1 } })
}

function onPageChange(p) {
  router.push({ path: '/search', query: { keyword: resultKeyword.value, sort: sort.value, page: p } })
}

watch(
  () => route.fullPath,
  async () => {
    if (route.path === '/search') {
      await syncAndSearch()
    }
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
.search-box {
  padding: 20px;
}
.search-box__bar {
  display: flex;
  gap: 10px;
}
.search-box__row {
  margin-top: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  font-size: 13px;
}
.search-box__label {
  color: var(--abc-text-3);
}
.search-box__tag {
  cursor: pointer;
}
.search-box__empty {
  color: var(--abc-text-3);
}
.result-head {
  margin-top: 14px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 10px;
}
.highlight {
  color: var(--abc-primary);
}
.result-head__sorts {
  display: flex;
  gap: 4px;
}
.sort-item {
  padding: 4px 14px;
  border-radius: 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.sort-item:hover {
  color: var(--abc-primary);
}
.sort-item.active {
  color: var(--abc-primary);
  background: var(--abc-primary-light);
  font-weight: 600;
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
.search-tip {
  margin-top: 14px;
  padding: 70px 20px;
  text-align: center;
  color: var(--abc-text-3);
}
.search-tip p {
  margin-top: 12px;
  font-size: 13px;
}
</style>
