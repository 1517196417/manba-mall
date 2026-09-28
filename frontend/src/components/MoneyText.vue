<template>
  <span class="abc-price" :class="sizeClass">
    <span class="abc-price__symbol">¥</span>
    <span class="abc-price__value">{{ formatted }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: [Number, String], default: 0 },
  size: { type: String, default: 'default' } // default | large | small
})

const formatted = computed(() => {
  const num = Number(props.value || 0)
  if (Number.isNaN(num)) return '0.00'
  return num.toFixed(2)
})

const sizeClass = computed(() => (props.size === 'default' ? '' : `abc-price--${props.size}`))
</script>
