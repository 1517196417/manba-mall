<template>
  <div class="sec-head">
    <div class="sec-head__left">
      <h3 class="sec-head__title">{{ title }}</h3>
      <span v-if="subtitle" class="sec-head__subtitle">{{ subtitle }}</span>
    </div>
    <div class="sec-head__right">
      <slot name="extra" />
      <span v-if="moreText" class="sec-head__more" @click="$emit('more')">
        {{ moreText }} <el-icon><ArrowRight /></el-icon>
      </span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  moreText: { type: String, default: '' }
})
defineEmits(['more'])
</script>

<style scoped>
.sec-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}
.sec-head__left {
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.sec-head__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
}
.sec-head__title::before {
  content: '';
  width: 4px;
  height: 18px;
  border-radius: 2px;
  background: linear-gradient(180deg, var(--abc-primary), #ff9500);
}
.sec-head__subtitle {
  color: var(--abc-text-3);
  font-size: 12px;
}
.sec-head__right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.sec-head__more {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--abc-text-2);
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
}
.sec-head__more:hover {
  color: var(--abc-primary);
}
</style>
