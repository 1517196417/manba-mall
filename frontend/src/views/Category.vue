<template>
  <div class="abc-container page">
    <el-breadcrumb separator="/" class="page__crumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>全部分类</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-loading="loading" class="cat-page">
      <aside class="cat-page__side">
        <div v-for="cat in tree" :key="cat.id" class="cat-page__side-item"
             :class="{ active: cat.id === activeId }" @click="selectCategory(cat.id)">
          {{ cat.name }}
        </div>
      </aside>

      <section class="cat-page__main">
        <template v-for="cat in tree" :key="cat.id">
          <div v-if="cat.id === activeId" class="cat-detail">
            <h2 class="cat-detail__title">{{ cat.name }}</h2>
            <p class="cat-detail__desc">{{ cat.description }}</p>

            <div v-for="sub in cat.children" :key="sub.id" class="cat-detail__group">
              <div class="cat-detail__group-head">
                <b>{{ sub.name }}</b>
                <el-link type="primary" :underline="false" @click="go(sub.id)">查看全部 &gt;</el-link>
              </div>
              <ProductGrid :products="productsMap[sub.id] || []" :loading="false"
                           empty-text="该分类下暂无商品" @add-cart="quickAdd" />
            </div>
          </div>
        </template>
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductGrid from '@/components/ProductGrid.vue'
import { categoryApi, productApi, cartApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const tree = ref([])
const activeId = ref(null)
const productsMap = ref({})

onMounted(async () => {
  try {
    tree.value = (await categoryApi.tree()) || []
    const queryId = Number(route.query.id)
    activeId.value = queryId || (tree.value[0] ? tree.value[0].id : null)
    await loadProducts(activeId.value)
  } catch (e) {
    /* 已提示 */
  } finally {
    loading.value = false
  }
})

async function selectCategory(id) {
  activeId.value = id
  router.replace({ path: '/category', query: { id } })
  await loadProducts(id)
}

async function loadProducts(catId) {
  const cat = tree.value.find((c) => c.id === catId)
  if (!cat) return
  const map = {}
  await Promise.all(
    (cat.children || []).map(async (sub) => {
      try {
        const res = await productApi.page({ categoryId: sub.id, page: 1, size: 5 })
        map[sub.id] = res.list || []
      } catch (e) {
        map[sub.id] = []
      }
    })
  )
  productsMap.value = map
}

function go(categoryId) {
  router.push(`/category/${categoryId}`)
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

watch(() => route.query.id, (val) => {
  if (val && Number(val) !== activeId.value) {
    activeId.value = Number(val)
    loadProducts(activeId.value)
  }
})
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.page__crumb {
  margin-bottom: 14px;
}
.cat-page {
  display: grid;
  grid-template-columns: 180px 1fr;
  gap: 16px;
}
.cat-page__side {
  background: #fff;
  border-radius: var(--abc-radius);
  padding: 10px 0;
  box-shadow: var(--abc-shadow);
  height: fit-content;
  position: sticky;
  top: 190px;
}
.cat-page__side-item {
  padding: 12px 18px;
  font-size: 14px;
  cursor: pointer;
  border-left: 3px solid transparent;
  transition: all 0.2s;
}
.cat-page__side-item:hover {
  color: var(--abc-primary);
  background: #fafbfc;
}
.cat-page__side-item.active {
  color: var(--abc-primary);
  font-weight: 700;
  background: var(--abc-primary-light);
  border-left-color: var(--abc-primary);
}
.cat-page__main {
  background: #fff;
  border-radius: var(--abc-radius);
  padding: 20px;
  box-shadow: var(--abc-shadow);
  min-height: 400px;
}
.cat-detail__title {
  margin: 0;
  font-size: 22px;
}
.cat-detail__desc {
  color: var(--abc-text-3);
  font-size: 13px;
  margin: 6px 0 18px;
}
.cat-detail__group {
  margin-bottom: 26px;
}
.cat-detail__group-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
</style>
