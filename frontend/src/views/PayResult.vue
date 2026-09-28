<template>
  <div v-loading="loading" class="abc-container page">
    <div class="result abc-card">
      <template v-if="result.payStatus === 1">
        <el-icon class="result__icon result__icon--ok" :size="64"><CircleCheckFilled /></el-icon>
        <h2>支付成功</h2>
        <p class="result__tip">我们已收到您的付款，商品将尽快为您打包发货</p>
      </template>
      <template v-else-if="result.payStatus === 0">
        <el-icon class="result__icon result__icon--wait" :size="64"><Clock /></el-icon>
        <h2>等待支付</h2>
        <p class="result__tip">订单尚未完成支付，请尽快付款，超时订单将自动取消</p>
      </template>
      <template v-else>
        <el-icon class="result__icon result__icon--info" :size="64"><InfoFilled /></el-icon>
        <h2>订单状态异常</h2>
        <p class="result__tip">请前往订单详情查看最新状态</p>
      </template>

      <div class="result__detail">
        <div class="result__line"><span>订单号</span><b>{{ result.orderNo }}</b></div>
        <div class="result__line">
          <span>支付金额</span>
          <MoneyText :value="result.payAmount" />
        </div>
        <div class="result__line"><span>支付方式</span><b>{{ result.payTypeText || '未支付' }}</b></div>
        <div class="result__line"><span>支付时间</span><b>{{ formatTime(result.payTime) || '—' }}</b></div>
      </div>

      <div class="result__actions">
        <el-button size="large" @click="$router.push('/')">继续购物</el-button>
        <el-button size="large" @click="$router.push(`/order/${result.orderId}`)">查看订单详情</el-button>
        <el-button v-if="result.payStatus === 0" size="large" type="primary"
                   @click="$router.push(`/pay/${result.orderId}`)">去支付</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { CircleCheckFilled, Clock, InfoFilled } from '@element-plus/icons-vue'
import MoneyText from '@/components/MoneyText.vue'
import { payApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const userStore = useUserStore()
const loading = ref(true)
const result = ref({})

onMounted(async () => {
  try {
    result.value = await payApi.result(route.params.orderNo)
    userStore.refreshInfo()
  } catch (e) {
    /* 已提示 */
  } finally {
    loading.value = false
  }
})

function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 19) : ''
}
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.result {
  padding: 46px 24px 34px;
  text-align: center;
}
.result__icon--ok {
  color: var(--abc-success);
}
.result__icon--wait {
  color: var(--abc-warning);
}
.result__icon--info {
  color: var(--abc-text-3);
}
.result h2 {
  margin: 16px 0 6px;
  font-size: 24px;
}
.result__tip {
  color: var(--abc-text-3);
  font-size: 13px;
  margin: 0;
}
.result__detail {
  max-width: 460px;
  margin: 28px auto 0;
  padding: 18px 20px;
  background: #fafbfc;
  border-radius: 10px;
  text-align: left;
}
.result__line {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 0;
  font-size: 13px;
  color: var(--abc-text-2);
}
.result__line b {
  color: var(--abc-text);
}
.result__actions {
  margin-top: 26px;
  display: flex;
  justify-content: center;
  gap: 14px;
  flex-wrap: wrap;
}
</style>
