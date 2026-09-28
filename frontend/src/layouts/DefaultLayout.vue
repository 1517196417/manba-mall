<template>
  <div class="layout">
    <AppHeader />
    <main class="layout__main">
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter />
    <el-backtop :right="28" :bottom="90" />
  </div>
</template>

<script setup>
import AppHeader from '@/components/AppHeader.vue'
import AppFooter from '@/components/AppFooter.vue'
</script>

<style scoped>
.layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
.layout__main {
  flex: 1;
  padding-bottom: 24px;
}
</style>
