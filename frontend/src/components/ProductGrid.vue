<template>
  <div v-loading="loading">
    <div v-if="products && products.length" class="grid">
      <ProductCard v-for="p in products" :key="p.id" :product="p"
                   @add-cart="$emit('add-cart', $event)"
                   @favorite="$emit('favorite', $event)" />
    </div>
    <EmptyBox v-else-if="!loading" icon="Search" :text="emptyText" />
  </div>
</template>

<script setup>
import ProductCard from './ProductCard.vue'
import EmptyBox from './EmptyBox.vue'

defineProps({
  products: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无相关商品' }
})
defineEmits(['add-cart', 'favorite'])
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 14px;
}
@media (max-width: 1180px) {
  .grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 900px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 640px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
