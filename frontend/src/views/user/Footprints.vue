<template>
  <div class="fp">
    <div class="fp__head">
      <div>
        <h2>浏览足迹</h2>
        <p>最近浏览过的商品，按日期分组展示</p>
      </div>
      <el-button v-if="groups.length" round type="danger" plain @click="clearAll">清空足迹</el-button>
    </div>

    <div v-loading="loading" class="fp__body">
      <div v-for="g in groups" :key="g.date" class="fp__group">
        <div class="fp__date">
          <el-icon><Calendar /></el-icon>
          <span>{{ formatDate(g.date) }}</span>
          <em>{{ g.items.length }} 件</em>
        </div>
        <div class="fp__grid">
          <div v-for="it in g.items" :key="it.footprintId" class="fp__item">
            <div class="fp__img abc-img-box" @click="go(`/product/${it.product.id}`)">
              <img :src="it.product.mainImage || fallback" :alt="it.product.name" />
            </div>
            <div class="fp__info">
              <h4 class="abc-ellipsis-2" @click="go(`/product/${it.product.id}`)">{{ it.product.name }}</h4>
              <div class="fp__price">
                <MoneyText :value="it.product.price" size="small" />
              </div>
              <div class="fp__ops">
                <el-button size="small" type="primary" round @click="addCart(it.product)">加购</el-button>
                <el-button size="small" round @click="remove(it)">删除</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <EmptyBox v-if="!loading && !groups.length" icon="View" text="还没有浏览记录" action-text="去逛逛"
              @action="go('/products')" />
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Calendar } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import MoneyText from '@/components/MoneyText.vue'
import { cartApi, footprintApi, placeholderImage, productApi, userApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const fallback = placeholderImage('商品', 200, 200, 7)

const loading = ref(true)
const groups = ref([])

onMounted(load)

async function load() {
  loading.value = true
  try {
    groups.value = (await userApi.footprints(60)) || []
  } catch (e) {
    groups.value = []
  } finally {
    loading.value = false
  }
}

function formatDate(d) {
  if (!d) return '更早'
  const s = String(d)
  if (s === '更早') return s
  const today = new Date()
  const t = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
  if (s === t) return `今天 · ${s}`
  return s
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

async function remove(it) {
  try {
    await footprintApi.remove(it.footprintId)
    ElMessage.success('已删除该足迹')
    await load()
  } catch (e) {
    /* 已提示 */
  }
}

async function clearAll() {
  try {
    await ElMessageBox.confirm('清空后所有浏览足迹将被删除，确定继续吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await footprintApi.clear()
    ElMessage.success('足迹已清空')
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
.fp__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}
.fp__head h2 {
  margin: 0;
  font-size: 20px;
}
.fp__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.fp__body {
  min-height: 180px;
}
.fp__group {
  margin-bottom: 22px;
}
.fp__date {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 600;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
.fp__date em {
  font-style: normal;
  font-size: 12px;
  font-weight: 400;
  color: var(--abc-text-3);
}
.fp__grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
.fp__item {
  border: 1px solid var(--abc-border);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.2s;
}
.fp__item:hover {
  border-color: var(--abc-primary);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.08);
}
.fp__img {
  height: 150px;
  cursor: pointer;
}
.fp__info {
  padding: 10px 12px 12px;
}
.fp__info h4 {
  margin: 0 0 8px;
  font-size: 13.5px;
  line-height: 1.4;
  min-height: 38px;
  cursor: pointer;
}
.fp__price {
  margin-bottom: 10px;
}
.fp__ops {
  display: flex;
  gap: 8px;
}
@media (max-width: 1080px) {
  .fp__grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 760px) {
  .fp__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
