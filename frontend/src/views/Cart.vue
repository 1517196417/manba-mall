<template>
  <div class="abc-container page">
    <div class="page__head">
      <h2>我的购物车</h2>
      <span class="page__sub">共 {{ cartStore.count }} 件商品，已选中 {{ cartStore.selectedQuantity }} 件</span>
    </div>

    <div v-loading="cartStore.loading" class="cart abc-card">
      <div class="cart__head">
        <el-checkbox :model-value="cartStore.allSelected" @change="onSelectAll">全选</el-checkbox>
        <span class="cart__col cart__col--goods">商品信息</span>
        <span class="cart__col cart__col--price">单价</span>
        <span class="cart__col cart__col--qty">数量</span>
        <span class="cart__col cart__col--total">小计</span>
        <span class="cart__col cart__col--op">操作</span>
      </div>

      <template v-if="validItems.length">
        <div v-for="item in validItems" :key="item.id" class="cart__row">
          <el-checkbox :model-value="item.selected === 1"
                       @change="(v) => cartStore.updateSelected(item.id, v ? 1 : 0)" />
          <div class="cart__goods" @click="$router.push(`/product/${item.productId}`)">
            <div class="cart__img abc-img-box">
              <img :src="item.image" :alt="item.productName" />
            </div>
            <div class="cart__goods-info">
              <div class="cart__name abc-ellipsis-2">{{ item.productName }}</div>
              <div class="cart__sku" @click.stop>
                <el-dropdown v-if="item.specs && item.specs.length" trigger="click"
                             @command="(skuId) => changeSku(item, skuId)">
                  <span class="cart__sku-pick">{{ item.skuDesc || '选择规格' }} <el-icon><ArrowDown /></el-icon></span>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-for="sku in skusOf(item)" :key="sku.id" :command="sku.id"
                                        :disabled="!sku.stock">
                        {{ sku.specDesc }}（库存 {{ sku.stock }}）
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
                <span v-else class="cart__sku-pick">{{ item.skuDesc || '默认规格' }}</span>
              </div>
              <div class="abc-tags" v-if="item.subtitle">
                <span class="abc-tag">{{ item.subtitle }}</span>
              </div>
            </div>
          </div>

          <div class="cart__col cart__col--price">
            <MoneyText :value="item.price" size="small" />
          </div>

          <div class="cart__col cart__col--qty">
            <el-input-number :model-value="item.quantity" :min="1" :max="Math.max(1, item.stock)"
                             size="small" @change="(v) => cartStore.updateQuantity(item.id, v)" />
          </div>

          <div class="cart__col cart__col--total">
            <MoneyText :value="Number(item.price) * item.quantity" size="small" />
          </div>

          <div class="cart__col cart__col--op">
            <el-link type="danger" :underline="false" @click="removeItem(item)">删除</el-link>
          </div>
        </div>
      </template>

      <EmptyBox v-else icon="ShoppingCart" text="购物车还是空的，快去挑几件好物吧"
                action-text="去逛逛" @action="$router.push('/products')" />

      <!-- 失效商品 -->
      <div v-if="invalidItems.length" class="cart__invalid">
        <div class="cart__invalid-head">
          <span><el-icon><WarningFilled /></el-icon> 失效商品 {{ invalidItems.length }} 件</span>
          <div>
            <el-link type="primary" :underline="false" @click="$router.push('/feature/cart-invalid')">
              失效商品处理说明
            </el-link>
            <el-button size="small" text type="danger" @click="cleanInvalid">一键清理</el-button>
          </div>
        </div>
        <div v-for="item in invalidItems" :key="item.id" class="cart__row cart__row--invalid">
          <el-tag size="small" type="info">失效</el-tag>
          <div class="cart__goods" @click="$router.push(`/product/${item.productId}`)">
            <div class="cart__img abc-img-box">
              <img :src="item.image" :alt="item.productName" />
            </div>
            <div class="cart__goods-info">
              <div class="cart__name abc-ellipsis-2">{{ item.productName }}</div>
              <div class="cart__invalid-reason">{{ item.invalidReason }}</div>
            </div>
          </div>
          <div class="cart__col cart__col--op">
            <el-link type="primary" :underline="false" @click="$router.push('/feature/cart-move-favorite')">
              移入收藏
            </el-link>
            <el-link type="danger" :underline="false" class="ml8" @click="removeItem(item)">删除</el-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部结算栏 -->
    <div class="settle abc-card">
      <div class="settle__left">
        <el-checkbox :model-value="cartStore.allSelected" @change="onSelectAll">全选</el-checkbox>
        <el-link :underline="false" @click="removeSelected">删除选中</el-link>
        <el-link :underline="false" @click="clearCart">清空购物车</el-link>
        <el-link :underline="false" type="warning" @click="$router.push('/feature/cart-invalid')">
          失效商品处理
        </el-link>
      </div>
      <div class="settle__right">
        <div class="settle__amount">
          <span>已选 <b>{{ cartStore.selectedQuantity }}</b> 件，合计：</span>
          <MoneyText :value="cartStore.selectedAmount" size="large" />
        </div>
        <el-button type="primary" size="large" :disabled="!cartStore.selectedItems.length"
                   @click="goCheckout">
          去结算
        </el-button>
      </div>
    </div>

    <div class="tips abc-card">
      <SectionTitle title="温馨提示" />
      <ul>
        <li>满 99 元包邮（新疆、西藏等偏远地区除外），未满 99 元收取 12 元运费。</li>
        <li>下单前请确认商品规格与数量，提交订单后可在「我的订单」中查看物流进度。</li>
        <li>需求中标注「!」的功能（失效商品处理、移入收藏等）提供功能说明页，点击可查看定位。</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, WarningFilled } from '@element-plus/icons-vue'
import MoneyText from '@/components/MoneyText.vue'
import EmptyBox from '@/components/EmptyBox.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { productApi } from '@/api'
import { useCartStore } from '@/store/cart'
import { useUserStore } from '@/store/user'

const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()

const validItems = computed(() => cartStore.validItems)
const invalidItems = computed(() => cartStore.invalidItems)

const skuCache = new Map()

onMounted(async () => {
  await cartStore.load()
})

function skusOf(item) {
  if (!skuCache.has(item.productId)) {
    skuCache.set(item.productId, [])
    productApi.skus(item.productId).then((list) => {
      skuCache.set(item.productId, list || [])
    })
  }
  return skuCache.get(item.productId)
}

async function onSelectAll(val) {
  await cartStore.selectAll(val ? 1 : 0)
}

async function changeSku(item, skuId) {
  try {
    await cartStore.changeSku(item.id, skuId)
    ElMessage.success('规格已切换')
  } catch (e) {
    /* 已提示 */
  }
}

async function removeItem(item) {
  try {
    await ElMessageBox.confirm(`确定要删除「${item.productName}」吗？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await cartStore.remove(item.id)
  ElMessage.success('已删除')
  userStore.refreshBadges()
}

async function removeSelected() {
  const items = cartStore.selectedItems
  if (!items.length) {
    ElMessage.warning('请先勾选要删除的商品')
    return
  }
  try {
    await ElMessageBox.confirm(`确定删除已选中的 ${items.length} 件商品吗？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  for (const item of items) {
    await cartStore.remove(item.id)
  }
  ElMessage.success('已删除选中商品')
  userStore.refreshBadges()
}

async function clearCart() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '清空确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await cartStore.clear()
  ElMessage.success('购物车已清空')
  userStore.refreshBadges()
}

async function cleanInvalid() {
  const count = await cartStore.cleanInvalid()
  ElMessage.success(`已清理 ${count} 件失效商品`)
  userStore.refreshBadges()
}

function goCheckout() {
  router.push('/checkout')
}
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.page__head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 14px;
}
.page__head h2 {
  margin: 0;
  font-size: 22px;
}
.page__sub {
  color: var(--abc-text-3);
  font-size: 13px;
}
.cart {
  padding: 4px 20px 20px;
}
.cart__head,
.cart__row {
  display: flex;
  align-items: center;
  gap: 14px;
}
.cart__head {
  height: 48px;
  border-bottom: 1px solid var(--abc-border);
  font-size: 13px;
  color: var(--abc-text-3);
}
.cart__row {
  padding: 16px 0;
  border-bottom: 1px solid #f5f6f8;
}
.cart__col--goods {
  flex: 1;
}
.cart__col--price {
  width: 100px;
  text-align: center;
}
.cart__col--qty {
  width: 130px;
  text-align: center;
}
.cart__col--total {
  width: 110px;
  text-align: center;
}
.cart__col--op {
  width: 110px;
  text-align: center;
}
.cart__goods {
  flex: 1;
  display: flex;
  gap: 12px;
  cursor: pointer;
  min-width: 0;
}
.cart__img {
  width: 86px;
  height: 86px;
  border-radius: 8px;
  flex-shrink: 0;
}
.cart__goods-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.cart__name {
  font-size: 14px;
  line-height: 20px;
}
.cart__sku-pick {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  padding: 2px 10px;
  border-radius: 12px;
  background: #f5f6f8;
  font-size: 12px;
  color: var(--abc-text-2);
  cursor: pointer;
}
.cart__invalid {
  margin-top: 18px;
  border-top: 1px dashed var(--abc-border);
  padding-top: 12px;
}
.cart__invalid-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
  color: var(--abc-text-3);
  padding-bottom: 6px;
}
.cart__invalid-head span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.cart__row--invalid {
  opacity: 0.7;
}
.cart__invalid-reason {
  font-size: 12px;
  color: var(--abc-danger, #f56c6c);
}
.ml8 {
  margin-left: 8px;
}
.settle {
  margin-top: 14px;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  bottom: 0;
  z-index: 10;
}
.settle__left {
  display: flex;
  align-items: center;
  gap: 18px;
  font-size: 13px;
}
.settle__right {
  display: flex;
  align-items: center;
  gap: 18px;
}
.settle__amount {
  display: flex;
  align-items: baseline;
  gap: 6px;
  color: var(--abc-text-2);
}
.settle__amount b {
  color: var(--abc-primary);
}
.tips {
  margin-top: 14px;
  padding: 18px 20px 22px;
}
.tips ul {
  margin: 0;
  padding-left: 20px;
  color: var(--abc-text-2);
  font-size: 13px;
  line-height: 26px;
}
</style>
