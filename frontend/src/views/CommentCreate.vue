<template>
  <div v-loading="loading" class="abc-container page">
    <div class="page__head">
      <h2>发表评价</h2>
      <span class="page__sub">评价成功可获得 50 积分奖励</span>
    </div>

    <div v-for="item in items" :key="item.id" class="abc-card block">
      <div class="goods">
        <div class="goods__img abc-img-box"><img :src="item.image" :alt="item.productName" /></div>
        <div class="goods__info">
          <div class="goods__name">{{ item.productName }}</div>
          <div class="goods__sku">{{ item.skuDesc }}</div>
          <div class="goods__meta">订单号：{{ item.orderNo }}</div>
        </div>
        <el-tag v-if="item.reviewStatus === 1" type="success" effect="plain">已评价</el-tag>
      </div>

      <template v-if="item.reviewStatus !== 1">
        <div class="form-row">
          <span class="form-row__label">商品评分</span>
          <el-rate v-model="forms[item.id].rating" show-text :texts="['很差', '较差', '一般', '满意', '非常满意']" />
        </div>

        <div class="form-row form-row--top">
          <span class="form-row__label">评价内容</span>
          <el-input v-model="forms[item.id].content" type="textarea" :rows="4" maxlength="500"
                    show-word-limit placeholder="说说这件商品的使用感受，帮助更多小伙伴做决定～" />
        </div>

        <div class="form-row">
          <span class="form-row__label">晒图</span>
          <el-link type="warning" :underline="false" @click="$router.push('/feature/comment-photo')">
            此为晒图功能
          </el-link>
          <span class="form-row__hint">（需求标注为功能说明页，可在评价中通过接口字段保留）</span>
        </div>

        <div class="form-row">
          <span class="form-row__label">匿名评价</span>
          <el-switch v-model="forms[item.id].anonymous" />
        </div>

        <div class="form-row form-row--submit">
          <el-button type="primary" size="large" :loading="submitting === item.id"
                     @click="submit(item)">提交评价</el-button>
        </div>
      </template>
    </div>

    <EmptyBox v-if="!loading && !items.length" icon="ChatDotSquare" text="暂无可评价的商品"
              action-text="查看我的订单" @action="$router.push('/user/orders')" />

    <div class="abc-card block links-card">
      <SectionTitle title="相关功能" />
      <div class="links">
        <el-link type="primary" :underline="false" @click="$router.push('/user/comments')">我的评价</el-link>
        <el-link type="primary" :underline="false" @click="$router.push('/feature/comment-photo')">晒图</el-link>
        <el-link type="primary" :underline="false" @click="$router.push('/feature/community')">种草社区</el-link>
        <el-link type="primary" :underline="false" @click="$router.push('/feature/video')">短视频</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import EmptyBox from '@/components/EmptyBox.vue'
import SectionTitle from '@/components/SectionTitle.vue'
import { commentApi, orderApi } from '@/api'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const items = ref([])
const forms = reactive({})
const submitting = ref(null)

onMounted(async () => {
  try {
    if (route.query.itemId) {
      const orderId = route.query.orderId
      if (orderId) {
        const order = await orderApi.detail(orderId)
        items.value = (order.items || []).filter((i) => String(i.id) === String(route.query.itemId))
      } else {
        const pending = await commentApi.pending()
        items.value = (pending || []).filter((i) => String(i.id) === String(route.query.itemId))
      }
    } else if (route.query.orderId) {
      const order = await orderApi.detail(route.query.orderId)
      items.value = order.items || []
    } else {
      items.value = (await commentApi.pending()) || []
    }
  } catch (e) {
    items.value = []
  } finally {
    items.value.forEach((i) => {
      forms[i.id] = { rating: 5, content: '', anonymous: false }
    })
    loading.value = false
  }
})

async function submit(item) {
  const form = forms[item.id]
  if (!form.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  submitting.value = item.id
  try {
    await commentApi.add({
      orderItemId: item.id,
      rating: form.rating,
      content: form.content,
      isAnonymous: form.anonymous ? 1 : 0,
      images: '[]'
    })
    ElMessage.success('评价成功，获得 50 积分奖励')
    item.reviewStatus = 1
    userStore.refreshInfo()
    setTimeout(() => router.push('/user/comments'), 600)
  } catch (e) {
    /* 已提示 */
  } finally {
    submitting.value = null
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
.goods {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 16px;
  border-bottom: 1px dashed var(--abc-border);
}
.goods__img {
  width: 76px;
  height: 76px;
  border-radius: 8px;
  flex-shrink: 0;
}
.goods__info {
  flex: 1;
  min-width: 0;
}
.goods__name {
  font-size: 14px;
}
.goods__sku,
.goods__meta {
  font-size: 12px;
  color: var(--abc-text-3);
  margin-top: 4px;
}
.form-row {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
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
</style>
