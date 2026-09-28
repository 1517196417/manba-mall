<template>
  <div class="chat-page">
    <div class="chat-container">
      <!-- 头部 -->
      <div class="chat-header">
        <div class="chat-header__info">
          <div class="chat-header__avatar">
            <el-icon :size="28"><ChatDotRound /></el-icon>
          </div>
          <div>
            <div class="chat-header__name">小艾 · AI 智能客服</div>
            <div class="chat-header__status">
              <span class="status-dot"></span> 7×24 在线
            </div>
          </div>
        </div>
        <el-button text @click="clearHistory">
          <el-icon><Refresh /></el-icon> 新对话
        </el-button>
      </div>

      <!-- 消息列表 -->
      <div ref="msgListRef" class="chat-messages">
        <!-- 欢迎语 -->
        <div v-if="messages.length === 0" class="chat-welcome">
          <div class="chat-welcome__icon">
            <el-icon :size="48"><ChatDotRound /></el-icon>
          </div>
          <h2>您好，我是小艾</h2>
          <p>AbcMall AI 智能客服，随时为您服务</p>
          <div class="chat-welcome__hints">
            <div class="hint-card" v-for="h in hints" :key="h" @click="sendMessage(h)">
              {{ h }}
            </div>
          </div>
        </div>

        <template v-for="(msg, i) in messages" :key="i">
          <!-- 用户消息 -->
          <div v-if="msg.role === 'user'" class="msg msg--user">
            <div class="msg__bubble msg__bubble--user">{{ msg.content }}</div>
          </div>

          <!-- AI 消息 -->
          <div v-else class="msg msg--ai">
            <div class="msg__avatar">
              <el-icon :size="20"><ChatDotRound /></el-icon>
            </div>
            <div class="msg__content">
              <!-- Agent 思考过程 -->
              <div v-if="msg.steps && msg.steps.length" class="msg__steps">
                <div class="steps-header" @click="toggleSteps(i)">
                  <el-icon :size="14">
                    <Loading v-if="msg.pending" class="thinking-spin" />
                    <CircleCheck v-else />
                  </el-icon>
                  <span>思考过程 ({{ msg.steps.length }} 步)</span>
                  <el-icon class="steps-arrow" :class="{ 'steps-arrow--open': stepsExpanded[i] }">
                    <ArrowDown />
                  </el-icon>
                </div>
                <transition name="steps-collapse">
                  <div v-show="stepsExpanded[i]" class="steps-list">
                    <div v-for="(step, si) in msg.steps" :key="si" class="step-item">
                      <span class="step-item__icon">
                        <el-icon :size="10"><Loading v-if="si === msg.steps.length - 1 && msg.pending" /><CircleCheck v-else /></el-icon>
                      </span>
                      <span class="step-item__text">{{ step.description }}</span>
                    </div>
                  </div>
                </transition>
              </div>

              <div class="msg__bubble msg__bubble--ai">
                <div v-if="msg.pending && (!msg.steps || !msg.steps.length)" class="thinking-panel">
                  <div class="thinking-panel__icon">
                    <el-icon class="thinking-spin"><Loading /></el-icon>
                  </div>
                  <span class="thinking-panel__text">小艾正在思考...</span>
                </div>
                <div v-else-if="msg.pending" class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
                <div v-else class="msg__text" v-html="formatText(msg.content)"></div>
              </div>

              <!-- 商品推荐卡片 -->
              <div v-if="msg.type === 'products' && msg.items" class="msg__cards">
                <div
                  v-for="p in msg.items" :key="p.id"
                  class="product-card"
                  @click="goTo(`/product/${p.id}`)"
                >
                  <img :src="p.mainImage || placeholder(p.name)" :alt="p.name" class="product-card__img" />
                  <div class="product-card__info">
                    <div class="product-card__name abc-ellipsis-2">{{ p.name }}</div>
                    <div v-if="p.subtitle" class="product-card__sub abc-ellipsis">{{ p.subtitle }}</div>
                    <div class="product-card__price">¥{{ p.price }}</div>
                  </div>
                </div>
              </div>

              <!-- 订单卡片 -->
              <div v-if="msg.type === 'orders' && msg.items" class="msg__orders">
                <div
                  v-for="o in msg.items" :key="o.orderNo"
                  class="order-card"
                  @click="goTo(o.url)"
                >
                  <div class="order-card__head">
                    <span class="order-card__no">订单号: {{ o.orderNo }}</span>
                    <el-tag size="small" :type="orderTagType(o.statusText)">{{ o.statusText }}</el-tag>
                  </div>
                  <div class="order-card__items">
                    <div v-for="(oi, idx) in (o.items || []).slice(0, 3)" :key="idx" class="order-card__item">
                      <img :src="oi.image || placeholder(oi.productName, 60, 60)" class="order-card__item-img" />
                      <span class="order-card__item-name abc-ellipsis">{{ oi.productName }}</span>
                      <span class="order-card__item-qty">x{{ oi.quantity }}</span>
                      <span class="order-card__item-price">¥{{ oi.price }}</span>
                    </div>
                  </div>
                  <div class="order-card__foot">
                    <span>共 {{ o.items?.length || 0 }} 件商品</span>
                    <span class="order-card__total">实付 ¥{{ o.payAmount }}</span>
                  </div>
                </div>
              </div>

              <!-- 导航卡片 -->
              <div v-if="msg.type === 'navigation' && msg.items" class="msg__nav">
                <div
                  v-for="(n, idx) in msg.items" :key="idx"
                  class="nav-card"
                  @click="goTo(n.url)"
                >
                  <el-icon><Position /></el-icon>
                  <div class="nav-card__info">
                    <div class="nav-card__name">{{ n.name }}</div>
                    <div v-if="n.description" class="nav-card__desc">{{ n.description }}</div>
                  </div>
                  <el-icon class="nav-card__arrow"><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </div>
        </template>

      </div>

      <!-- 输入区 -->
      <div class="chat-input-area">
        <el-input
          ref="inputRef"
          v-model="inputText"
          type="textarea"
          :autosize="{ minRows: 1, maxRows: 4 }"
          placeholder="输入您的问题，按 Enter 发送..."
          resize="none"
          @keydown.enter.exact.prevent="sendMessage()"
        />
        <el-button
          type="primary"
          :loading="loading"
          :disabled="!inputText.trim()"
          @click="sendMessage()"
        >
          <el-icon v-if="!loading"><Promotion /></el-icon>
          {{ loading ? '思考中' : '发送' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/api/request'
import { chatApi } from '@/api/chat'
import { ElMessageBox } from 'element-plus'


const router = useRouter()

const messages = ref([])
const inputText = ref('')
const loading = ref(false)
const sessionId = ref('')
const msgListRef = ref(null)
const stepsExpanded = reactive({})

const hints = [
  '6000元左右的笔记本电脑推荐',
  '我的订单什么时候到货？',
  '7天无理由退货的详细条款',
  '运费谁承担？',
  '我要修改密码'
]

function placeholder(text, w = 200, h = 200) {
  return `/api/image/ph?t=${encodeURIComponent(text || '商品')}&w=${w}&h=${h}`
}

onMounted(() => {
  // 从 localStorage 恢复 sessionId
  const saved = localStorage.getItem('sessionId')
  if (saved) {
    sessionId.value = saved
    loadHistory()
  }
})

async function loadHistory() {
  if (!sessionId.value) return
  try {
    const list = await request.get('/chat/history/list', {
      params: { sessionId: sessionId.value, limit: 10 }
    })
    messages.value = (list || []).map(msg => parseMetadata(msg))
    scrollToBottom()
  } catch (e) { /* ignore */ }
}

// 清空历史记录
async function clearHistory() {
  if (!sessionId.value) return
  // 询问用户确认
  const confirm = await ElMessageBox.confirm('确认清空历史记录吗？', '询问', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  if (!confirm) return

  // 请求清空历史记录
  await request.delete('/chat/history/clear', {
    params: {
      sessionId: sessionId.value,
    }
  })
  // 清空历史记录
  messages.value = []
  Object.keys(stepsOpen).forEach(key => delete stepsOpen[key]);

  // 滚动到最末尾
  scrollBottom()
}

async function sendMessage(text) {
  const content = (text || inputText.value).trim()
  if (!content || loading.value) return

  inputText.value = ''
  messages.value.push({ role: 'user', content })

  // 立即推入 AI 占位消息，SSE 逐步填充
  const msgIdx = messages.value.length
  messages.value.push({
    role: 'assistant',
    content: '',
    type: '',
    items: null,
    steps: [],
    pending: true
  })
  stepsExpanded[msgIdx] = true

  loading.value = true
  scrollToBottom()

  chatApi.sendStream(
    { message: content, sessionId: sessionId.value },
    {
      onStep: (d) => {
        messages.value[msgIdx].steps.push({
          toolName: d.toolName,
          description: d.description,
          timestamp: d.timestamp
        })
        scrollToBottom()
      },
      onResult: (d) => {
        messages.value[msgIdx].content = d.content || ''
        messages.value[msgIdx].type = d.type || 'text'
        messages.value[msgIdx].items = d.items || null
        messages.value[msgIdx].metadata = d.metadata || ''
        parseMetadata(messages.value[msgIdx])
        if (d.sessionId) {
          sessionId.value = d.sessionId
          localStorage.setItem('sessionId', sessionId.value)
        }
        messages.value[msgIdx].pending = false
        scrollToBottom()
      },
      onDone: () => {
        messages.value[msgIdx].pending = false
        loading.value = false
        scrollToBottom()
      },
      onError: () => {
        messages.value[msgIdx].pending = false
        messages.value[msgIdx].content = '抱歉，服务暂时不可用，请稍后再试。'
        messages.value[msgIdx].type = 'text'
        loading.value = false
        scrollToBottom()
      }
    }
  )
}

function newSession() {

}

function parseMetadata(msg) {
  if (!msg?.metadata) return msg
  try {
    Object.assign(msg, JSON.parse(msg.metadata))
  } catch { /* 忽略不合法的历史元数据 */ }
  return msg
}

function toggleSteps(idx) {
  stepsExpanded[idx] = !stepsExpanded[idx]
}

function goTo(url) {
  if (url) router.push(url)
}

function formatText(text) {
  if (!text) return ''
  let html = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  html = html.replace(/\n/g, '<br>')
  // 将 /product/xxx /order/xxx /user/xxx 等路径转为可点击链接
  html = html.replace(/(\/(?:product|order|user|cart|sign|coupon|points-mall|category|search|ai-chat)\/?\S*)/g,
    (match) => `<a href="${match}" class="chat-link" onclick="event.preventDefault();window.__chatNav('${match}')">${match}</a>`)
  return html
}

// 暴露给 onclick 的全局导航函数
window.__chatNav = (url) => router.push(url)

function orderTagType(statusText) {
  const map = {
    '待付款': 'warning',
    '待发货': 'info',
    '待收货': '',
    '待评价': 'info',
    '已完成': 'success',
    '已取消': 'danger',
    '售后中': 'danger'
  }
  return map[statusText] || ''
}

function scrollToBottom() {
  nextTick(() => {
    const el = msgListRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}
</script>

<style scoped>
.chat-page {
  min-height: calc(100vh - 140px);
  padding: 20px 0;
}
.chat-container {
  max-width: 860px;
  margin: 0 auto;
  background: #fff;
  border-radius: var(--abc-radius);
  box-shadow: var(--abc-shadow);
  display: flex;
  flex-direction: column;
  height: calc(100vh - 180px);
  overflow: hidden;
}

/* ---- 头部 ---- */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--abc-border);
  background: linear-gradient(135deg, var(--abc-primary), var(--abc-primary-dark));
}
.chat-header__info {
  display: flex;
  align-items: center;
  gap: 12px;
}
.chat-header__avatar {
  width: 44px; height: 44px;
  background: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--abc-primary);
}
.chat-header__name {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}
.chat-header__status {
  color: rgba(255,255,255,0.85);
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}
.status-dot {
  width: 8px; height: 8px;
  background: #4caf50;
  border-radius: 50%;
  display: inline-block;
}
.chat-header :deep(.el-button) {
  color: #fff;
}
.chat-header :deep(.el-button:hover) {
  background: rgba(255,255,255,0.15);
}

/* ---- 消息列表 ---- */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: var(--abc-bg);
}
.chat-messages::-webkit-scrollbar {
  width: 6px;
}
.chat-messages::-webkit-scrollbar-thumb {
  background: var(--abc-text-3);
  border-radius: 3px;
}

/* ---- 欢迎语 ---- */
.chat-welcome {
  text-align: center;
  padding: 40px 0;
}
.chat-welcome__icon {
  width: 80px; height: 80px;
  margin: 0 auto 16px;
  background: var(--abc-primary-light);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--abc-primary);
}
.chat-welcome h2 {
  font-size: 22px;
  margin: 0 0 8px;
}
.chat-welcome p {
  color: var(--abc-text-2);
  margin: 0 0 24px;
}
.chat-welcome__hints {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  max-width: 560px;
  margin: 0 auto;
}
.hint-card {
  padding: 12px 16px;
  background: #fff;
  border: 1px solid var(--abc-border);
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--abc-text-2);
  transition: all 0.2s;
}
.hint-card:hover {
  border-color: var(--abc-primary);
  color: var(--abc-primary);
  background: var(--abc-primary-light);
}

/* ---- 消息气泡 ---- */
.msg {
  display: flex;
  margin-bottom: 16px;
}
.msg--user {
  justify-content: flex-end;
}
.msg--ai {
  justify-content: flex-start;
  gap: 10px;
}
.msg__avatar {
  width: 36px; height: 36px;
  background: var(--abc-primary);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.msg__content {
  max-width: 70%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.msg__bubble {
  padding: 10px 16px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}
.msg__bubble--user {
  background: var(--abc-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg__bubble--ai {
  background: #fff;
  color: var(--abc-text);
  border: 1px solid var(--abc-border);
  border-bottom-left-radius: 4px;
}
:deep(.chat-link) {
  color: var(--abc-primary);
  text-decoration: underline;
  cursor: pointer;
}

/* ---- 商品卡片 ---- */
.msg__cards {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 4px;
}
.product-card {
  width: 160px;
  background: #fff;
  border: 1px solid var(--abc-border);
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.2s;
  flex-shrink: 0;
}
.product-card:hover {
  box-shadow: var(--abc-shadow-hover);
}
.product-card__img {
  width: 100%;
  height: 120px;
  object-fit: cover;
  background: var(--abc-bg);
}
.product-card__info {
  padding: 8px;
}
.product-card__name {
  font-size: 13px;
  font-weight: 600;
  line-height: 1.3;
  margin-bottom: 4px;
}
.product-card__sub {
  font-size: 11px;
  color: var(--abc-text-3);
  margin-bottom: 4px;
}
.product-card__price {
  color: var(--abc-price);
  font-weight: 700;
  font-size: 15px;
}

/* ---- 订单卡片 ---- */
.msg__orders {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}
.order-card {
  background: #fff;
  border: 1px solid var(--abc-border);
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.order-card:hover {
  box-shadow: var(--abc-shadow-hover);
}
.order-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}
.order-card__no {
  font-size: 12px;
  color: var(--abc-text-2);
}
.order-card__items {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.order-card__item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}
.order-card__item-img {
  width: 40px; height: 40px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}
.order-card__item-name {
  flex: 1;
  color: var(--abc-text-2);
}
.order-card__item-qty {
  color: var(--abc-text-3);
}
.order-card__item-price {
  color: var(--abc-text-2);
}
.order-card__foot {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--abc-border);
  font-size: 12px;
  color: var(--abc-text-2);
}
.order-card__total {
  color: var(--abc-price);
  font-weight: 600;
}

/* ---- 导航卡片 ---- */
.msg__nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 100%;
}
.nav-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border: 1px solid var(--abc-border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  color: var(--abc-primary);
}
.nav-card:hover {
  background: var(--abc-primary-light);
  border-color: var(--abc-primary);
}
.nav-card__info {
  flex: 1;
}
.nav-card__name {
  font-size: 14px;
  font-weight: 600;
  color: var(--abc-text);
}
.nav-card__desc {
  font-size: 12px;
  color: var(--abc-text-3);
  margin-top: 2px;
}
.nav-card__arrow {
  color: var(--abc-text-3);
}

/* ---- Agent 思考过程 ---- */
.msg__steps {
  margin-bottom: 4px;
  border-radius: 8px;
  overflow: hidden;
}
.steps-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background: var(--abc-primary-light);
  border: 1px solid var(--abc-border);
  border-radius: 8px;
  cursor: pointer;
  font-size: 12px;
  color: var(--abc-text-2);
  user-select: none;
}
.steps-header:hover {
  border-color: var(--abc-primary);
}
.steps-arrow {
  margin-left: auto;
  transition: transform 0.2s;
  color: var(--abc-text-3);
}
.steps-arrow--open {
  transform: rotate(180deg);
}
.steps-list {
  padding: 8px 10px;
  background: #fafafa;
  border: 1px solid var(--abc-border);
  border-top: none;
  border-radius: 0 0 8px 8px;
}
.step-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 0;
  font-size: 12px;
  color: var(--abc-text-2);
}
.step-item__icon {
  color: var(--abc-primary);
  display: flex;
  align-items: center;
}
.step-item__text {
  flex: 1;
}
.steps-collapse-enter-active, .steps-collapse-leave-active {
  transition: all 0.2s ease;
  overflow: hidden;
}
.steps-collapse-enter-from, .steps-collapse-leave-to {
  opacity: 0;
  max-height: 0;
}

/* ---- 思考面板 ---- */
.thinking-panel {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0 8px;
  border-bottom: 1px dashed var(--abc-border);
  margin-bottom: 8px;
}
.thinking-panel__icon {
  color: var(--abc-primary);
  display: flex;
  align-items: center;
}
.thinking-spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.thinking-panel__text {
  font-size: 13px;
  color: var(--abc-text-2);
}

/* ---- 打字指示器 ---- */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}
.typing-indicator span {
  width: 8px; height: 8px;
  background: var(--abc-text-3);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-6px); opacity: 1; }
}

/* ---- 输入区 ---- */
.chat-input-area {
  display: flex;
  gap: 10px;
  padding: 12px 20px;
  border-top: 1px solid var(--abc-border);
  background: #fff;
}
.chat-input-area :deep(.el-textarea__inner) {
  border-radius: 8px;
  padding: 8px 12px;
}
</style>
