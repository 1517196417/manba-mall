<template>
  <div v-loading="loading" class="abc-container page">
    <div class="page__head">
      <h2>收银台</h2>
      <el-steps :active="1" simple class="page__steps">
        <el-step title="提交订单" :icon="Document" />
        <el-step title="支付订单" :icon="Wallet" />
        <el-step title="完成" :icon="CircleCheck" />
      </el-steps>
    </div>

    <div class="pay abc-card">
      <div class="pay__amount">
        <span>订单提交成功，请尽快完成支付</span>
        <div class="pay__amount-value">
          应付金额 <MoneyText :value="info.payAmount" size="large" />
        </div>
        <div class="pay__order-no">
          订单号：{{ info.orderNo }}
          <el-link type="primary" :underline="false" class="ml8" @click="copyOrderNo">复制</el-link>
        </div>
      </div>

      <div class="pay__methods">
        <h3>选择支付方式</h3>
        <div class="pay__grid">
          <div v-for="m in methods" :key="m.type" class="pay__item"
               :class="{ active: channel === m.icon, disabled: m.icon === 'balance' && balanceInsufficient }"
               @click="selectMethod(m)">
            <div class="pay__item-icon" :class="`pay__item-icon--${m.icon}`">
              <el-icon :size="24"><component :is="iconOf(m.icon)" /></el-icon>
            </div>
            <div class="pay__item-body">
              <b>{{ m.name }}</b>
              <span>{{ m.tip }}</span>
            </div>
            <el-tag size="small" type="info" effect="plain">功能说明页</el-tag>
          </div>
        </div>

        <el-alert type="warning" :closable="false" show-icon class="pay__alert">
          <template #title>
            演示说明：微信支付 / 支付宝 / 银联 / 余额支付 / 组合支付 / 支付结果回调 / 退款 均为需求中标注「!」的功能说明页，
            不接入真实支付渠道。为了打通「下单 → 支付 → 发货 → 收货 → 评价」的完整演示链路，
            这里提供「模拟支付成功」按钮。
          </template>
        </el-alert>

        <div class="pay__actions">
          <el-button size="large" @click="$router.push(`/order/${orderId}`)">查看订单</el-button>
          <el-button size="large" type="primary" :loading="paying" @click="mockPay">
            模拟支付成功（¥{{ Number(info.payAmount || 0).toFixed(2) }}）
          </el-button>
        </div>

        <div class="pay__links">
          <el-link type="info" :underline="false" @click="$router.push('/feature/pay-wechat')">微信支付说明</el-link>
          <el-link type="info" :underline="false" @click="$router.push('/feature/pay-alipay')">支付宝说明</el-link>
          <el-link type="info" :underline="false" @click="$router.push('/feature/pay-unionpay')">银联说明</el-link>
          <el-link type="info" :underline="false" @click="$router.push('/feature/pay-combined')">组合支付说明</el-link>
          <el-link type="info" :underline="false" @click="$router.push('/feature/pay-callback')">支付回调说明</el-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, CircleCheck, CreditCard, Document, Money, Wallet } from '@element-plus/icons-vue'
import MoneyText from '@/components/MoneyText.vue'
import { payApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const orderId = Number(route.params.orderId)
const loading = ref(true)
const paying = ref(false)
const info = ref({})
const methods = ref([])
const channel = ref('wechat')

const balanceInsufficient = computed(
  () => Number(info.value.balance || 0) < Number(info.value.payAmount || 0)
)

onMounted(async () => {
  try {
    const res = await payApi.methods(orderId)
    info.value = res
    methods.value = res.methods || []
    if (res.payStatus === 1) {
      ElMessage.info('该订单已支付')
      router.replace(`/pay/result/${res.orderNo}`)
    }
  } catch (e) {
    router.replace('/user/orders')
  } finally {
    loading.value = false
  }
})

function iconOf(icon) {
  return (
    {
      wechat: ChatDotRound,
      alipay: Money,
      unionpay: CreditCard,
      balance: Wallet
    }[icon] || Wallet
  )
}

function selectMethod(m) {
  if (m.icon === 'balance' && balanceInsufficient.value) {
    ElMessage.warning('账户余额不足，请更换支付方式')
  }
  channel.value = m.icon
  // 各渠道均为功能说明页，点击后跳转说明
  if (['wechat', 'alipay', 'unionpay', 'balance'].includes(m.icon)) {
    ElMessage.info(`演示环境：${m.tip}，可直接点击下方「模拟支付成功」完成支付`)
  }
}

function copyOrderNo() {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(info.value.orderNo || '').then(() => ElMessage.success('订单号已复制'))
  }
}

async function mockPay() {
  paying.value = true
  try {
    const res = await payApi.mockPay(orderId, channel.value)
    ElMessage.success('支付成功')
    userStore.refreshInfo()
    userStore.refreshBadges()
    router.replace(`/pay/result/${res.orderNo}`)
  } catch (e) {
    /* 已提示 */
  } finally {
    paying.value = false
  }
}
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.page__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}
.page__head h2 {
  margin: 0;
  font-size: 22px;
}
.page__steps {
  width: 520px;
}
.pay {
  padding: 26px 24px 30px;
}
.pay__amount {
  text-align: center;
  padding-bottom: 24px;
  border-bottom: 1px dashed var(--abc-border);
}
.pay__amount > span {
  color: var(--abc-text-2);
  font-size: 14px;
}
.pay__amount-value {
  margin: 14px 0 10px;
  font-size: 16px;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 10px;
}
.pay__order-no {
  color: var(--abc-text-3);
  font-size: 13px;
}
.ml8 {
  margin-left: 8px;
}
.pay__methods h3 {
  margin: 24px 0 14px;
  font-size: 16px;
}
.pay__grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.pay__item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid var(--abc-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.pay__item:hover {
  border-color: var(--abc-primary);
}
.pay__item.active {
  border-color: var(--abc-primary);
  background: var(--abc-primary-light);
}
.pay__item.disabled {
  opacity: 0.55;
}
.pay__item-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #ff5000, #ff9500);
  flex-shrink: 0;
}
.pay__item-icon--wechat {
  background: linear-gradient(135deg, #07c160, #4fd98a);
}
.pay__item-icon--alipay {
  background: linear-gradient(135deg, #1677ff, #6aa9ff);
}
.pay__item-icon--unionpay {
  background: linear-gradient(135deg, #e60012, #ff6a6a);
}
.pay__item-icon--balance {
  background: linear-gradient(135deg, #9b51e0, #c9a0f0);
}
.pay__item-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.pay__item-body span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.pay__alert {
  margin-top: 18px;
}
.pay__actions {
  margin-top: 22px;
  display: flex;
  justify-content: center;
  gap: 14px;
}
.pay__links {
  margin-top: 18px;
  display: flex;
  justify-content: center;
  gap: 20px;
  flex-wrap: wrap;
  font-size: 13px;
}
@media (max-width: 900px) {
  .pay__grid {
    grid-template-columns: 1fr;
  }
  .page__steps {
    width: 100%;
  }
}
</style>
