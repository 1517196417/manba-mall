<template>
  <div class="cm">
    <div class="cm__head">
      <div>
        <h2>我的评价</h2>
        <p>评价商品可获得 50 积分奖励，晒图为功能说明页</p>
      </div>
      <el-button v-if="pending.length" type="primary" round @click="active = 'pending'">
        待评价（{{ pending.length }}）
      </el-button>
    </div>

    <div class="cm__tabs">
      <span class="cm__tab" :class="{ active: active === 'pending' }" @click="active = 'pending'">
        待评价 <em v-if="pending.length">{{ pending.length }}</em>
      </span>
      <span class="cm__tab" :class="{ active: active === 'mine' }" @click="active = 'mine'">
        我的评价 <em v-if="total">{{ total }}</em>
      </span>
    </div>

    <!-- 待评价 -->
    <div v-if="active === 'pending'" v-loading="loadingPending" class="cm__pending">
      <div v-for="it in pending" :key="it.id" class="p-item">
        <div class="p-item__img abc-img-box">
          <img :src="it.image || fallback" :alt="it.productName" />
        </div>
        <div class="p-item__info">
          <h4 class="abc-ellipsis-2">{{ it.productName }}</h4>
          <p class="p-item__sku abc-ellipsis">{{ it.skuDesc || '默认规格' }} × {{ it.quantity }}</p>
          <span class="p-item__time">订单号：{{ it.orderNo }}</span>
        </div>
        <el-button type="primary" round @click="go(`/comment/create?orderItemId=${it.id}&orderId=${it.orderId}`)">
          去评价
        </el-button>
      </div>
      <EmptyBox v-if="!loadingPending && !pending.length" icon="ChatLineSquare" text="没有待评价的商品"
                action-text="查看我的评价" @action="active = 'mine'" />
    </div>

    <!-- 我的评价 -->
    <div v-else v-loading="loadingMine" class="cm__mine">
      <div v-for="c in list" :key="c.id" class="c-item">
        <div class="c-item__head">
          <div class="c-item__product" @click="go(`/product/${c.productId}`)">
            <div class="c-item__img abc-img-box">
              <img :src="c.productImage || fallback" :alt="c.productName" />
            </div>
            <div class="c-item__pname">
              <h4 class="abc-ellipsis-2">{{ c.productName || '商品' }}</h4>
              <span class="c-item__sku">{{ c.skuDesc || '默认规格' }}</span>
            </div>
          </div>
          <div class="c-item__right">
            <el-rate :model-value="c.rating" disabled />
            <span class="c-item__time">{{ formatTime(c.createTime) }}</span>
          </div>
        </div>

        <p class="c-item__content">{{ c.content || '此用户没有填写评价内容' }}</p>

        <div v-if="appendList(c).length" class="c-item__images">
          <span v-for="(img, i) in appendList(c)" :key="i" class="c-item__img-thumb abc-img-box">
            <img :src="img" alt="晒图" />
          </span>
        </div>

        <div v-if="c.reply" class="c-item__reply">
          <b>商家回复：</b>{{ c.reply }}
        </div>

        <div v-if="c.appendContent" class="c-item__append">
          <b>追评（{{ formatTime(c.appendTime) }}）：</b>{{ c.appendContent }}
        </div>

        <div class="c-item__ops">
          <el-button size="small" round @click="openAppend(c)">追评</el-button>
          <el-button size="small" round type="danger" plain @click="remove(c)">删除</el-button>
        </div>
      </div>

      <EmptyBox v-if="!loadingMine && !list.length" icon="ChatLineSquare" text="还没有发表过评价"
                action-text="去评价" @action="active = 'pending'" />
    </div>

    <el-dialog v-model="appendDialog" title="追加评价" width="520px">
      <el-input v-model="appendContent" type="textarea" :rows="4" maxlength="200" show-word-limit
                placeholder="补充你的使用感受，追评同样可获得积分奖励" />
      <template #footer>
        <el-button round @click="appendDialog = false">取消</el-button>
        <el-button type="primary" round :loading="appending" @click="submitAppend">提交追评</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import EmptyBox from '@/components/EmptyBox.vue'
import { commentApi, placeholderImage } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const fallback = placeholderImage('商品', 200, 200, 7)

const active = ref('pending')
const loadingPending = ref(true)
const loadingMine = ref(true)

const pending = ref([])
const list = ref([])
const total = ref(0)

const appendDialog = ref(false)
const appendContent = ref('')
const appending = ref(false)
const appendId = ref(null)

onMounted(async () => {
  await Promise.all([loadPending(), loadMine()])
})

watch(active, (v) => {
  if (v === 'pending') loadPending()
  else loadMine()
})

async function loadPending() {
  loadingPending.value = true
  try {
    pending.value = (await commentApi.pending()) || []
  } catch (e) {
    pending.value = []
  } finally {
    loadingPending.value = false
  }
}

async function loadMine() {
  loadingMine.value = true
  try {
    const res = await commentApi.my({ page: 1, size: 20 })
    list.value = res.list || []
    total.value = res.total || 0
  } catch (e) {
    list.value = []
  } finally {
    loadingMine.value = false
  }
}

function appendList(c) {
  if (!c.images) return []
  const raw = String(c.images).trim()
  if (!raw) return []
  try {
    const arr = JSON.parse(raw)
    return Array.isArray(arr) ? arr : [raw]
  } catch (e) {
    return raw.split(',').map((s) => s.trim()).filter(Boolean)
  }
}

function openAppend(c) {
  appendId.value = c.id
  appendContent.value = ''
  appendDialog.value = true
}

async function submitAppend() {
  if (!appendContent.value.trim()) {
    ElMessage.warning('请输入追评内容')
    return
  }
  appending.value = true
  try {
    await commentApi.append({ commentId: appendId.value, content: appendContent.value.trim() })
    ElMessage.success('追评成功')
    appendDialog.value = false
    await loadMine()
  } catch (e) {
    /* 已提示 */
  } finally {
    appending.value = false
  }
}

async function remove(c) {
  try {
    await ElMessageBox.confirm('删除后评价不可恢复，确定删除吗？', '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  try {
    await commentApi.remove(c.id)
    ElMessage.success('已删除')
    await loadMine()
  } catch (e) {
    /* 已提示 */
  }
}

function formatTime(v) {
  return v ? String(v).replace('T', ' ').slice(0, 16) : ''
}

function go(path) {
  router.push(path)
}
</script>

<style scoped>
.cm__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
}
.cm__head h2 {
  margin: 0;
  font-size: 20px;
}
.cm__head p {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--abc-text-3);
}
.cm__tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.cm__tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 18px;
  border-radius: 17px;
  font-size: 13.5px;
  cursor: pointer;
  border: 1px solid var(--abc-border);
  transition: all 0.2s;
}
.cm__tab:hover {
  color: var(--abc-primary);
}
.cm__tab.active {
  color: #fff;
  background: var(--abc-primary);
  border-color: var(--abc-primary);
}
.cm__tab em {
  font-style: normal;
  font-size: 11px;
  padding: 0 5px;
  border-radius: 8px;
  background: var(--abc-price);
  color: #fff;
}
.cm__pending,
.cm__mine {
  display: grid;
  gap: 12px;
  min-height: 160px;
}
.p-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border: 1px solid var(--abc-border);
  border-radius: 12px;
}
.p-item__img {
  width: 74px;
  height: 74px;
  border-radius: 8px;
  flex-shrink: 0;
}
.p-item__info {
  flex: 1;
  min-width: 0;
}
.p-item__info h4 {
  margin: 0 0 6px;
  font-size: 14px;
  line-height: 1.45;
}
.p-item__sku {
  margin: 0 0 4px;
  font-size: 12px;
  color: var(--abc-text-3);
}
.p-item__time {
  font-size: 12px;
  color: var(--abc-text-3);
}
.c-item {
  padding: 16px 18px;
  border: 1px solid var(--abc-border);
  border-radius: 12px;
}
.c-item__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}
.c-item__product {
  display: flex;
  gap: 12px;
  cursor: pointer;
  min-width: 0;
}
.c-item__img {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  flex-shrink: 0;
}
.c-item__pname h4 {
  margin: 0 0 4px;
  font-size: 14px;
  line-height: 1.4;
}
.c-item__sku {
  font-size: 12px;
  color: var(--abc-text-3);
}
.c-item__right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}
.c-item__time {
  font-size: 12px;
  color: var(--abc-text-3);
}
.c-item__content {
  margin: 12px 0 0;
  font-size: 13.5px;
  line-height: 1.7;
  color: var(--abc-text-2);
}
.c-item__images {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.c-item__img-thumb {
  width: 76px;
  height: 76px;
  border-radius: 8px;
}
.c-item__reply,
.c-item__append {
  margin-top: 12px;
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--abc-text-2);
}
.c-item__reply {
  background: #fafbfc;
}
.c-item__append {
  background: #fff8ec;
  color: #b26a00;
}
.c-item__ops {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}
</style>
