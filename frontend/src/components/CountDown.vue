<template>
  <span class="countdown">
    <template v-if="!finished">
      <em v-for="(unit, idx) in units" :key="idx">
        <i>{{ unit }}</i><b v-if="idx < 2">:</b>
      </em>
    </template>
    <span v-else class="countdown__over">{{ overText }}</span>
  </span>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'

const props = defineProps({
  endTime: { type: String, required: true },
  overText: { type: String, default: '已结束' }
})

const remain = ref(0)
let timer = null

const finished = computed(() => remain.value <= 0)

const units = computed(() => {
  const total = Math.max(0, Math.floor(remain.value / 1000))
  const h = String(Math.floor(total / 3600)).padStart(2, '0')
  const m = String(Math.floor((total % 3600) / 60)).padStart(2, '0')
  const s = String(total % 60).padStart(2, '0')
  return [h, m, s]
})

function tick() {
  // 兼容后端返回的 ISO 时间（2026-09-30T23:59:59）与「Y-m-d H:i:s」
  const normalized = String(props.endTime).replace('T', ' ').replace(/-/g, '/')
  const end = new Date(normalized).getTime()
  remain.value = Number.isNaN(end) ? 0 : end - Date.now()
}

onMounted(() => {
  tick()
  timer = setInterval(tick, 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.countdown {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-family: Arial, Helvetica, sans-serif;
}
.countdown em {
  display: inline-flex;
  align-items: center;
}
.countdown i {
  display: inline-block;
  min-width: 24px;
  padding: 0 4px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 4px;
  background: #1f2329;
  color: #fff;
  font-style: normal;
  font-size: 13px;
}
.countdown b {
  margin: 0 3px;
  color: #1f2329;
}
.countdown__over {
  color: var(--abc-text-3);
  font-size: 13px;
}
</style>
