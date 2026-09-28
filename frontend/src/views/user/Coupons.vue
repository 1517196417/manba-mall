<template>
  <div class="uc2">
    <div class="uc2__head">
      <div>
        <h2>我的优惠券</h2>
        <p>未使用 {{ counts.unused || 0 }} 张 · 已使用 {{ counts.used || 0 }} 张 · 已过期 {{ counts.expired || 0 }} 张</p>
      </div>
      <el-button type="primary" round @click="go('/coupon')">去领券</el-button>
    </div>

    <el-tabs v-model="active" @tab-change="load">
      <el-tab-pane label="未使用" name="0" />
      <el-tab-pane label="已使用" name="1" />
      <el-tab-pane label="已过期" name="2" />
    </el-tabs>

    <div v-loading="loading" class="uc2__list">
      <div v-for="c in list" :key="c.id" class="cp" :class="{ 'cp--disabled': c.status !== 0 }">
        <div class="cp__left">
          <div class="cp__value">
            <template v-if="c.type === 2">
              <b>{{ Number(c.discount || 0) }}</b><i>折</i>
            </template>
            <template v-else>
              <i>¥</i><b>{{ Number(c.amount || 0) }}</b>
            </template>
          </div>
          <div class="cp__cond">
            {{ Number(c.minAmount || 0) > 0 ? `满 ${Number(c.minAmount)} 可用` : '无门槛使用' }}
          </div>
        </div>
        <div class="cp__mid">
          <h4 class="abc-ellipsis">{{ c.name }}</h4>
          <p class="cp__desc abc-ellipsis">{{ c.description || '全场通用' }}</p>
          <p class="cp__time">
            <el-icon><Clock /></el-icon>
            {{ formatDate(c.receiveTime) }} 领取 · 有效期至 {{ formatDate(c.expireTime) }}
          </p>
        </div>
        <div class="cp__right">
          <el-tag size="small" :type="tagType(c.status)" round>{{ statusText(c.status) }}</el-tag>
          <el-button v-if="c.status === 0" size="small" type="primary" round @click="go('/products')">
            去使用
          </el-button>
        </div>
      </div>
    </div>

    <EmptyBox v-if="!loading && !list.length" icon="Ticket" :text="emptyText" action-text="去领券中心"
              @action="go('/coupon')" />
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Clock } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import { couponApi } from '@/api'

const router = useRouter()

const loading = ref(true)
const list = ref([])
const active = ref('0')
const counts = ref({ unused: 0, used: 0, expired: 0 })

const emptyText = computed(() => {
  if (active.value === '0') return '还没有可用的优惠券，去领券中心看看吧'
  if (active.value === '1') return '还没有已使用的优惠券'
  return '没有已过期的优惠券'
})

onMounted(async () => {
  try {
    counts.value = (await couponApi.myCount()) || counts.value
  } catch (e) {
    /* 忽略 */
  }
  await load()
})

async function load() {
  loading.value = true
  try {
    list.value = (await couponApi.my(Number(active.value))) || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

function statusText(s) {
  return s === 0 ? '未使用' : s === 1 ? '已使用' : '已过期'
}

function tagType(s) {
  return s === 0 ? 'warning' : s === 1 ? 'info' : 'danger'
}

function formatDate(v) {
  return v ? String(v).replace('T', ' ').slice(0, 10) : '--'
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.uc2__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}
.uc2__head h2 {
  margin: 0;
  font-size: 20px;
}
.uc2__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.uc2__list {
  display: grid;
  gap: 12px;
  min-height: 160px;
}
.cp {
  display: grid;
  grid-template-columns: 150px 1fr 150px;
  align-items: center;
  border: 1px solid var(--abc-border);
  border-radius: 12px;
  overflow: hidden;
}
.cp--disabled {
  opacity: 0.66;
}
.cp__left {
  align-self: stretch;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
}
.cp--disabled .cp__left {
  background: linear-gradient(135deg, #b8bfc9, #949ba6);
}
.cp__value i {
  font-size: 14px;
  font-style: normal;
  font-weight: 600;
}
.cp__value b {
  font-size: 30px;
  font-weight: 800;
  line-height: 1;
}
.cp__cond {
  font-size: 12px;
  opacity: 0.95;
}
.cp__mid {
  padding: 14px 18px;
}
.cp__mid h4 {
  margin: 0 0 6px;
  font-size: 15px;
}
.cp__desc {
  margin: 0 0 8px;
  font-size: 12.5px;
  color: var(--abc-text-3);
}
.cp__time {
  margin: 0;
  font-size: 12px;
  color: var(--abc-text-3);
  display: flex;
  align-items: center;
  gap: 4px;
}
.cp__right {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding-right: 18px;
}
@media (max-width: 760px) {
  .cp {
    grid-template-columns: 110px 1fr;
  }
  .cp__right {
    grid-column: 1 / -1;
    flex-direction: row;
    justify-content: space-between;
    padding: 0 18px 14px;
  }
}
</style>
