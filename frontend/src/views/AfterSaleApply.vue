<template>
  <div v-loading="loading" class="abc-container page">
    <div class="page__head">
      <h2>申请售后</h2>
      <span class="page__sub">支持退货 / 退款 / 换货 / 维修</span>
    </div>

    <div class="abc-card block">
      <h3 class="block__title">选择售后商品</h3>
      <div class="goods-list">
        <div v-for="item in items" :key="item.id" class="goods" :class="{ active: selectedItemId === item.id }"
             @click="selectItem(item)">
          <div class="goods__img abc-img-box"><img :src="item.image" :alt="item.productName" /></div>
          <div class="goods__info">
            <div class="goods__name abc-ellipsis-2">{{ item.productName }}</div>
            <div class="goods__sku">{{ item.skuDesc }}</div>
            <div class="goods__meta">实付 ¥{{ Number(item.totalPrice || 0).toFixed(2) }} × {{ item.quantity }}</div>
          </div>
          <el-icon v-if="selectedItemId === item.id" class="goods__check"><CircleCheckFilled /></el-icon>
        </div>
      </div>
      <EmptyBox v-if="!loading && !items.length" icon="Box" text="该订单暂无可申请售后的商品" />
    </div>

    <div class="abc-card block">
      <h3 class="block__title">售后类型</h3>
      <div class="type-grid">
        <div v-for="t in typeOptions" :key="t.value" class="type-item"
             :class="{ active: type === t.value }" @click="selectType(t)">
          <el-icon :size="22"><component :is="t.icon" /></el-icon>
          <b>{{ t.label }}</b>
          <span>{{ t.desc }}</span>
        </div>
      </div>
      <div class="hint-row">
        <el-link type="warning" :underline="false" @click="$router.push('/feature/repair')">此为维修功能</el-link>
        <el-link type="warning" :underline="false" @click="$router.push('/feature/complaint')">此为投诉功能</el-link>
        <el-link type="warning" :underline="false" @click="$router.push('/feature/ticket')">此为工单功能</el-link>
        <el-link type="warning" :underline="false" @click="$router.push('/feature/agent-intervene')">此为客服介入功能</el-link>
        <el-link type="warning" :underline="false" @click="$router.push('/feature/arbitration')">此为仲裁功能</el-link>
      </div>
    </div>

    <div class="abc-card block">
      <h3 class="block__title">售后信息</h3>
      <div class="form-row">
        <span class="form-row__label">售后原因</span>
        <el-select v-model="reason" placeholder="请选择售后原因" style="width: 320px">
          <el-option v-for="r in reasons" :key="r" :value="r" :label="r" />
        </el-select>
      </div>
      <div class="form-row form-row--top">
        <span class="form-row__label">问题描述</span>
        <el-input v-model="description" type="textarea" :rows="4" maxlength="500" show-word-limit
                  placeholder="请描述商品具体问题，便于客服快速处理" />
      </div>
      <div class="form-row">
        <span class="form-row__label">退款金额</span>
        <el-input-number v-model="refundAmount" :min="0" :precision="2" :step="1" />
        <span class="form-row__hint">默认按商品实付金额退款</span>
      </div>
      <div class="form-row">
        <span class="form-row__label">凭证图片</span>
        <el-link type="warning" :underline="false" @click="$router.push('/feature/comment-photo')">
          此为晒图/凭证上传功能
        </el-link>
      </div>
      <div class="form-row form-row--submit">
        <el-button size="large" @click="$router.back()">返回</el-button>
        <el-button size="large" type="primary" :loading="submitting" @click="submit">提交申请</el-button>
      </div>
    </div>

    <div class="abc-card block links-card">
      <SectionTitle title="相关功能" />
      <div class="links">
        <el-link type="primary" :underline="false" @click="$router.push('/user/aftersale')">我的售后</el-link>
        <el-link type="primary" :underline="false" @click="$router.push('/feature/refund')">退款</el-link>
        <el-link type="primary" :underline="false" @click="$router.push('/feature/refund-partial')">部分退款</el-link>
        <el-link type="primary" :underline="false" @click="$router.push('/feature/refund-progress')">退款进度</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheckFilled, Money, Refresh, RefreshLeft, Switch } from '@element-plus/icons-vue'
import EmptyBox from '@/components/EmptyBox.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { afterSaleApi, orderApi } from '@/api'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const submitting = ref(false)
const items = ref([])
const selectedItemId = ref(null)
const reasons = ref([])
const type = ref(1)
const reason = ref('')
const description = ref('')
const refundAmount = ref(0)

const typeOptions = [
  { value: 1, label: '退货', icon: RefreshLeft, desc: '退回商品并退款' },
  { value: 2, label: '仅退款', icon: Money, desc: '不退货直接退款' },
  { value: 3, label: '换货', icon: Switch, desc: '更换同款商品' },
  { value: 4, label: '维修', icon: Refresh, desc: '功能说明页' }
]

onMounted(async () => {
  try {
    const [reasonList] = await Promise.all([afterSaleApi.reasons().catch(() => [])])
    reasons.value = reasonList || []
    reason.value = reasons.value[0] || ''

    let list = []
    if (route.query.orderId) {
      const order = await orderApi.detail(route.query.orderId)
      list = order.items || []
    } else {
      list = (await orderApi.page({ page: 1, size: 20 })).list.flatMap((o) => o.items || [])
    }
    items.value = list.filter((i) => i.refundStatus !== 1)
    if (route.query.itemId) {
      const target = items.value.find((i) => String(i.id) === String(route.query.itemId))
      if (target) selectItem(target)
    } else if (items.value.length) {
      selectItem(items.value[0])
    }
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
})

function selectItem(item) {
  selectedItemId.value = item.id
  refundAmount.value = Number(item.totalPrice || 0)
}

function selectType(t) {
  if (t.value === 4) {
    router.push('/feature/repair')
    return
  }
  type.value = t.value
}

async function submit() {
  if (!selectedItemId.value) {
    ElMessage.warning('请选择要申请售后的商品')
    return
  }
  if (!reason.value) {
    ElMessage.warning('请选择售后原因')
    return
  }
  submitting.value = true
  try {
    await afterSaleApi.apply({
      orderItemId: selectedItemId.value,
      type: type.value,
      reason: reason.value,
      description: description.value,
      refundAmount: refundAmount.value
    })
    ElMessage.success('售后申请已提交，平台将在 24 小时内审核')
    router.push('/user/aftersale')
  } catch (e) {
    /* 已提示 */
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.page {
  padding-top: 16px;
}
.page__head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 14px;
}
.page__head h2 {
  margin: 0;
  font-size: 22px;
}
.page__sub {
  color: var(--abc-text-3);
  font-size: 13px;
}
.block {
  margin-bottom: 14px;
  padding: 20px;
}
.block__title {
  margin: 0 0 16px;
  font-size: 16px;
  padding-left: 10px;
  border-left: 3px solid var(--abc-primary);
}
.goods-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
.goods {
  position: relative;
  display: flex;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--abc-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}
.goods:hover {
  border-color: var(--abc-primary);
}
.goods.active {
  border-color: var(--abc-primary);
  background: var(--abc-primary-light);
}
.goods__img {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  flex-shrink: 0;
}
.goods__info {
  min-width: 0;
  flex: 1;
}
.goods__name {
  font-size: 13px;
  line-height: 19px;
}
.goods__sku,
.goods__meta {
  font-size: 12px;
  color: var(--abc-text-3);
  margin-top: 4px;
}
.goods__check {
  position: absolute;
  right: 8px;
  top: 8px;
  color: var(--abc-primary);
}
.type-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.type-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 10px;
  border: 1px solid var(--abc-border);
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}
.type-item:hover {
  border-color: var(--abc-primary);
  color: var(--abc-primary);
}
.type-item.active {
  border-color: var(--abc-primary);
  background: var(--abc-primary-light);
  color: var(--abc-primary);
}
.type-item span {
  font-size: 12px;
  color: var(--abc-text-3);
}
.hint-row {
  margin-top: 16px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  font-size: 13px;
}
.form-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
}
.form-row--top {
  align-items: flex-start;
}
.form-row__label {
  width: 76px;
  flex-shrink: 0;
  color: var(--abc-text-3);
  font-size: 13px;
}
.form-row__hint {
  font-size: 12px;
  color: var(--abc-text-3);
}
.form-row--submit {
  justify-content: flex-end;
  border-top: 1px dashed var(--abc-border);
  margin-top: 8px;
  padding-top: 18px;
}
.links-card {
  margin-top: 14px;
}
.links {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
  font-size: 13px;
}
@media (max-width: 900px) {
  .goods-list,
  .type-grid {
    grid-template-columns: 1fr;
  }
}
</style>
