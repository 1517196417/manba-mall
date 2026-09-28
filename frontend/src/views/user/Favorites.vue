<template>
  <div class="fav">
    <div class="fav__head">
      <div>
        <h2>我的收藏</h2>
        <p>共收藏 {{ total }} 件商品</p>
      </div>
      <el-button v-if="list.length" round @click="go('/products')">继续逛逛</el-button>
    </div>

    <div v-loading="loading" class="fav__grid">
      <ProductCard v-for="p in list" :key="p.id" :product="p"
                   @add-cart="addCart" @favorite="removeFav" />
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="Star" text="还没有收藏任何商品" action-text="去逛逛"
              @action="go('/products')" />

    <div v-if="total > size" class="fav__pager">
      <el-pagination background layout="prev, pager, next" :total="total"
                     :page-size="size" :current-page="page" @current-change="onPage" />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyBox from '@/components/EmptyBox.vue'
import ProductCard from '@/components/ProductCard.vue'
import { cartApi, favoriteApi, productApi, userApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 10

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await userApi.favorites({ page: page.value, size })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function onPage(p) {
  page.value = p
  load()
}

async function addCart(product) {
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

async function removeFav(product) {
  try {
    await ElMessageBox.confirm(`确定取消收藏「${product.name}」吗？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await favoriteApi.remove(product.id)
    ElMessage.success('已取消收藏')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.fav__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.fav__head h2 {
  margin: 0;
  font-size: 20px;
}
.fav__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.fav__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
  min-height: 180px;
}
.fav__pager {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
@media (max-width: 1180px) {
  .fav__grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 820px) {
  .fav__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
