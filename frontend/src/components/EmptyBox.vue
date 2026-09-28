<template>
  <div class="empty-box">
    <el-icon :size="52" class="empty-box__icon"><component :is="icon" /></el-icon>
    <p class="empty-box__text">{{ text }}</p>
    <div v-if="actionText" class="empty-box__action">
      <el-button type="primary" round @click="$emit('action')">{{ actionText }}</el-button>
    </div>
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'ShoppingCart' },
  text: { type: String, default: '这里什么都没有' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style scoped>
.empty-box {
  padding: 56px 16px;
  text-align: center;
  color: var(--abc-text-3);
}
.empty-box__icon {
  color: #d5dae2;
}
.empty-box__text {
  margin: 14px 0 0;
  font-size: 14px;
}
.empty-box__action {
  margin-top: 18px;
}
</style>
