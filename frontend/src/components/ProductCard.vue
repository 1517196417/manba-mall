<template>
  <div class="p-card abc-hover-zoom" @click="goDetail">
    <div class="p-card__img abc-img-box">
      <img :src="product.mainImage || fallback" :alt="product.name" @error="onImgError" />
      <span v-if="product.isNew === 1" class="p-card__badge p-card__badge--new">新品</span>
      <span v-else-if="product.isHot === 1" class="p-card__badge p-card__badge--hot">热销</span>
      <div class="p-card__hover" @click.stop>
        <el-button class="p-card__cart" type="primary" circle :icon="ShoppingCart"
                   @click.stop="$emit('add-cart', product)" />
        <el-button class="p-card__fav" circle :icon="Star" @click.stop="$emit('favorite', product)" />
      </div>
    </div>

    <div class="p-card__body">
      <div class="p-card__price-row">
        <MoneyText :value="product.price" />
        <span v-if="product.marketPrice > product.price" class="abc-market-price">
          ¥{{ Number(product.marketPrice).toFixed(2) }}
        </span>
      </div>
      <h4 class="p-card__name abc-ellipsis-2" :title="product.name">{{ product.name }}</h4>
      <p class="p-card__subtitle abc-ellipsis">{{ product.subtitle || '正品保障 · 极速发货' }}</p>
      <div class="p-card__foot">
        <div class="abc-tags">
          <span v-for="tag in tagList" :key="tag" class="abc-tag">{{ tag }}</span>
        </div>
        <span class="p-card__sales">已售 {{ product.sales || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, Star } from '@element-plus/icons-vue'
import MoneyText from './MoneyText.vue'
import { placeholderImage } from '@/api'

const props = defineProps({
  product: { type: Object, required: true }
})
defineEmits(['add-cart', 'favorite'])

const router = useRouter()
const fallback = placeholderImage('AbcMall', 400, 400, 1)

const tagList = computed(() => {
  if (!props.product.tags) return []
  return String(props.product.tags)
    .split(',')
    .filter(Boolean)
    .slice(0, 2)
})

function goDetail() {
  router.push(`/product/${props.product.id}`)
}

function onImgError(e) {
  e.target.src = fallback
}
</script>

<style scoped>
.p-card {
  background: #fff;
  border-radius: var(--abc-radius);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.25s ease, transform 0.25s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.p-card:hover {
  box-shadow: var(--abc-shadow-hover);
  transform: translateY(-3px);
}
.p-card__img {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
}
.p-card__badge {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 11px;
  color: #fff;
}
.p-card__badge--new {
  background: linear-gradient(135deg, #2d9cdb, #56ccf2);
}
.p-card__badge--hot {
  background: linear-gradient(135deg, #ff5000, #ff9500);
}
.p-card__hover {
  position: absolute;
  right: 8px;
  bottom: 8px;
  display: flex;
  gap: 6px;
  opacity: 0;
  transform: translateY(6px);
  transition: all 0.25s ease;
}
.p-card:hover .p-card__hover {
  opacity: 1;
  transform: translateY(0);
}
.p-card__body {
  padding: 10px 12px 12px;
  display: flex;
  flex-direction: column;
  flex: 1;
}
.p-card__price-row {
  display: flex;
  align-items: baseline;
}
.p-card__name {
  margin: 6px 0 0;
  font-size: 14px;
  font-weight: 500;
  line-height: 20px;
  min-height: 40px;
  color: var(--abc-text);
}
.p-card__subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--abc-text-3);
}
.p-card__foot {
  margin-top: auto;
  padding-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
}
.p-card__sales {
  font-size: 12px;
  color: var(--abc-text-3);
  white-space: nowrap;
}
</style>
