<template>
  <div v-if="!isChatRoute" class="ai-widget">
    <!-- 展开后的面板 -->
    <transition name="widget-slide">
      <div v-if="open" class="ai-widget__panel">
        <div class="ai-widget__header">
          <div class="ai-widget__title">
            <el-icon :size="18"><ChatDotRound /></el-icon>
            <span>小艾 · AI 客服</span>
          </div>
          <div class="ai-widget__actions">
            <el-button text size="small" @click="clearHistory">
              <el-icon><DeleteFilled /></el-icon>
            </el-button>
            <el-button text size="small" @click="goFullPage">
              <el-icon><FullScreen /></el-icon>
            </el-button>
            <el-button text size="small" @click="open = false">
              <el-icon><Close /></el-icon>
            </el-button>
          </div>
        </div>

        <div ref="panelMsgRef" class="ai-widget__messages">
          <div v-if="messages.length === 0" class="ai-widget__welcome">
            <p>您好，我是小艾 AI 客服，有什么可以帮您？</p>
            <div class="ai-widget__hints">
              <el-tag
                  v-for="h in hints" :key="h"
                  size="small" effect="plain"
                  @click="sendMessage(h)"
                  class="ai-widget__hint"
              >{{ h }}</el-tag>
            </div>
          </div>

          <template v-for="(msg, i) in messages" :key="i">
            <div v-if="msg.role === 'user'" class="w-msg w-msg--user">
              <span>{{ msg.content }}</span>
            </div>
            <div v-else class="w-msg w-msg--ai">
              <!-- Agent 思考过程 -->
              <div v-if="msg.steps && msg.steps.length" class="w-steps">
                <div class="w-steps__header" @click="toggleSteps(i)">
                  <el-icon :size="12">
                    <Loading v-if="msg.pending" class="thinking-spin" />
                    <CircleCheck v-else />
                  </el-icon>
                  <span>思考过程 ({{ msg.steps.length }})</span>
                  <el-icon class="w-steps__arrow" :class="{ 'w-steps__arrow--open': stepsOpen[i] }"><ArrowDown /></el-icon>
                </div>
                <div v-show="stepsOpen[i]" class="w-steps__list">
                  <div v-for="(step, si) in msg.steps" :key="si" class="w-step">{{ step.description }}</div>
                </div>
              </div>
              <div v-if="msg.pending && (!msg.steps || !msg.steps.length)" class="typing"><span></span><span></span><span></span></div>
              <div v-else-if="msg.pending" class="typing"><span></span><span></span><span></span></div>
              <div v-else class="w-msg__text" v-html="formatText(msg.content)"></div>
              <!-- 商品 -->
              <div v-if="msg.type === 'products' && msg.items" class="w-products">
                <div v-for="p in msg.items" :key="p.id" class="w-product" @click="goTo(`/product/${p.id}`)">
                  <img :src="p.mainImage || placeholder(p.name, 80, 80)" />
                  <div class="w-product__info">
                    <div class="abc-ellipsis">{{ p.name }}</div>
                    <div class="w-product__price">¥{{ p.price }}</div>
                  </div>
                </div>
              </div>
              <!-- 订单 -->
              <div v-if="msg.type === 'orders' && msg.items" class="w-orders">
                <div v-for="o in msg.items" :key="o.orderNo" class="w-order" @click="goTo(o.url)">
                  <div class="w-order__head">
                    <span>{{ o.orderNo }}</span>
                    <el-tag size="small">{{ o.statusText }}</el-tag>
                  </div>
                  <div class="w-order__items">
                    <span v-for="(oi, idx) in (o.items || []).slice(0, 2)" :key="idx">
                      {{ oi.productName }} x{{ oi.quantity }}
                    </span>
                  </div>
                </div>
              </div>
              <!-- 导航 -->
              <div v-if="msg.type === 'navigation' && msg.items" class="w-nav">
                <div v-for="(n, idx) in msg.items" :key="idx" class="w-nav-item" @click="goTo(n.url)">
                  <el-icon><Position /></el-icon>
                  <span>{{ n.name }}</span>
                  <el-icon><ArrowRight /></el-icon>
                </div>
              </div>
            </div>
          </template>

        </div>

        <div class="ai-widget__input">
          <el-input
              v-model="inputText"
              placeholder="输入问题..."
              @keydown.enter.exact.prevent="sendMessage()"
          />
          <el-button type="primary" :loading="loading" @click="sendMessage()">
            <el-icon v-if="!loading"><Promotion /></el-icon>
          </el-button>
        </div>
      </div>
    </transition>

    <!-- 悬浮按钮 -->
    <div v-if="!open" class="ai-widget__btn" @click="open = true">
      <el-icon :size="28"><ChatDotRound /></el-icon>
      <span class="ai-widget__badge" v-if="messages.length === 0">AI</span>
    </div>
  </div>
</template>

<script setup>
import request from '@/api/request'
import { ref, reactive, computed, watch, nextTick, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {ElMessageBox} from "element-plus";
import {chatApi} from "@/api/chat.js";
const route = useRoute()
const router = useRouter()

const open = ref(false)            // 是否展开客服面板
const messages = ref([])           // 客服消息列表
const inputText = ref('')          // 输入框内容
const loading = ref(false)         // 是否正在发送消息
const sessionId = ref('')          // 客服会话ID
const panelMsgRef = ref(null)      // 客服消息面板引用
const stepsOpen = reactive({})     // 思考过程是否展开
const isChatRoute = computed(() => route.path === '/ai-chat') // 是否是客服路由

// 初始提示词
const hints = [
  '6000元笔记本推荐',
  '我的订单到哪了',
  '7天无理由退货',
  '运费谁承担'
]

onMounted(async () => {
  // 加载 SessionId（刷新页面后保持会话）
  sessionId.value = localStorage.getItem('sessionId') || ''
  // 加载客服消息历史
  await loadHistory()
})

// 当客服面板展开时，滚动到最底部
watch(open, (newVal) => {
  if (newVal) {
    scrollBottom()
  }
})

async function loadHistory() {
  if (!sessionId.value) return
  // 请求客服消息历史
  const res = await request.get('/chat/history/list', {
    params: {
      sessionId: sessionId.value,
      limit: 10,
    }
  })
  // 解释metadata为对象，合并到当前消息中
  res.forEach(msg => {
    if (msg.metadata) {
      let _metadata = JSON.parse(msg.metadata)
      Object.assign(msg, _metadata) // 合并到当前消息
    }
  })
  //加载客服消息历史
  messages.value = res || []
}

// 清除历史记录
async function clearHistory(){
  if (!sessionId.value) return
  // 询问用户确认
  const confirm = await ElMessageBox.confirm('确认清空历史记录吗？', '询问', { type: 'warning' })
  if (!confirm) return

  // 确认清除
  await request.delete('/chat/history/clear', {
    params: {
      sessionId: sessionId.value,
    }
  })

  // 这一句按需增加，清空思考过程的打开关闭状态
  Object.keys(stepsOpen).forEach(key => delete stepsOpen[key]);

  // 清空前端历史记录
  messages.value = []
}

// 发送消息
async function sendMessage(tag) {
  if (tag) inputText.value = tag
  if (!inputText.value) return
  loading.value = true
  let msg = inputText.value
  inputText.value = ''          // 清空输入框
  // 添加用户输入
  messages.value.push({ role: 'user', content: msg })

  // 预添加AI回复（占位，后续逐步填充）
  const msgIdx = messages.value.length
  messages.value.push({
    role: 'assistant', // user用户 / assistant客服 / error错误
    content: '',       // 内容
    metadata: '',      // 结构化数据字符串
    type: '',          // step工具调用步骤 / done完成 / （从metadata中提取 navigation导航 / orders订单 / products商品 / text文本）
    items: null,       // 工具调用结果（来自：结构化数据_解压）
    steps: [],         // 工具调用步骤（来自：SSE 推送）
    pending: true      // 是否正在处理中
  })
  stepsOpen[msgIdx] = true // 是否展开思考过程

  // 滚动到最底部
  scrollBottom()

  chatApi.sendStream({ sessionId: sessionId.value, message: msg}, {
    // 工具调用步骤
    onStep: (d)=>{
      messages.value[msgIdx].steps.push({
        toolName: d.toolName,
        description: d.description,
        timestamp: d.timestamp
      })
      scrollBottom()
    },
    // 最终结果推送
    onResult: (res)=>{

      // 将前端返回的结果，直接打印在控制台
      console.log("大模型回复：", res)

      messages.value[msgIdx].content = res.content || ''
      messages.value[msgIdx].type = res.type || 'text'
      messages.value[msgIdx].items = res.items || null
      messages.value[msgIdx].metadata = res.metadata || ''
      // 解释 metadata 为对象，合并到当前消息中
      if (res.metadata) {
        let _metadata = JSON.parse(res.metadata)
        Object.assign(messages.value[msgIdx], _metadata) // 合并到当前消息
      }
      // 更新 sessionId
      if (res.sessionId) {
        sessionId.value = res.sessionId
        localStorage.setItem('sessionId', res.sessionId)
      }
      messages.value[msgIdx].pending = false
      scrollBottom()
    },
    // 完成推送
    onDone: ()=>{
      messages.value[msgIdx].pending = false
      loading.value = false
      scrollBottom()
    },
    // 发生错误
    onError: (e)=>{
      messages.value[msgIdx].pending = false
      messages.value[msgIdx].content = '抱歉，服务暂时不可用。'
      messages.value[msgIdx].type = 'text'
      loading.value = false
      scrollBottom()
    },
  })
}

// 全屏显示
function goFullPage() {
  open.value = false
  router.push('/ai-chat')
}

// 转到指定URL
function goTo(url) {
  if (url) router.push(url)  // 前端路由跳转
}

// 展开某项过程
function toggleSteps(idx) {
  stepsOpen[idx] = !stepsOpen[idx]
}

// 格式化文本，处理HTML实体和换行符
function formatText(text) {
  if (!text) return ''
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>')
}

// 滚动到最底部
function scrollBottom() {
  nextTick(() => {
    const el = panelMsgRef.value
    if (el) el.scrollTop = el.scrollHeight
  })
}
</script>

<style scoped>
.ai-widget {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 999;
}

/* ---- 悬浮按钮 ---- */
.ai-widget__btn {
  width: 56px; height: 56px;
  background: var(--abc-primary);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(255, 80, 0, 0.4);
  transition: transform 0.2s;
  position: relative;
}
.ai-widget__btn:hover {
  transform: scale(1.08);
}
.ai-widget__badge {
  position: absolute;
  top: -4px; right: -4px;
  background: #fff;
  color: var(--abc-primary);
  font-size: 10px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 8px;
  border: 1px solid var(--abc-primary);
}

/* ---- 面板 ---- */
.ai-widget__panel {
  width: 380px;
  height: 520px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(15, 23, 42, 0.18);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.widget-slide-enter-active, .widget-slide-leave-active {
  transition: all 0.3s ease;
}
.widget-slide-enter-from, .widget-slide-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

/* ---- 面板头部 ---- */
.ai-widget__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: linear-gradient(135deg, var(--abc-primary), var(--abc-primary-dark));
  color: #fff;
}
.ai-widget__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
}
.ai-widget__actions {
  display: flex;
  gap: 4px;
}
.ai-widget__actions :deep(.el-button) {
  color: #fff;
}

/* ---- 面板消息区 ---- */
.ai-widget__messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: var(--abc-bg);
}
.ai-widget__messages::-webkit-scrollbar {
  width: 4px;
}
.ai-widget__messages::-webkit-scrollbar-thumb {
  background: var(--abc-text-3);
  border-radius: 2px;
}
.ai-widget__welcome {
  text-align: center;
  padding: 16px 8px;
}
.ai-widget__welcome p {
  color: var(--abc-text-2);
  margin: 0 0 12px;
  font-size: 13px;
}
.ai-widget__hints {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
}
.ai-widget__hint {
  cursor: pointer;
}

/* ---- 面板消息气泡 ---- */
.w-msg {
  margin-bottom: 10px;
  font-size: 13px;
  line-height: 1.5;
}
.w-msg--user {
  text-align: right;
}
.w-msg--user span {
  display: inline-block;
  padding: 6px 10px;
  background: var(--abc-primary);
  color: #fff;
  border-radius: 10px 10px 2px 10px;
  max-width: 85%;
  word-break: break-word;
}
.w-msg--ai {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.w-msg__text {
  padding: 6px 10px;
  background: #fff;
  border-radius: 10px 10px 10px 2px;
  border: 1px solid var(--abc-border);
  max-width: 85%;
  word-break: break-word;
}

/* ---- 面板思考过程 ---- */
.w-steps {
  margin-bottom: 4px;
}
.w-steps__header {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: var(--abc-primary-light);
  border: 1px solid var(--abc-border);
  border-radius: 6px;
  cursor: pointer;
  font-size: 11px;
  color: var(--abc-text-2);
}
.w-steps__arrow {
  margin-left: auto;
  transition: transform 0.2s;
}
.w-steps__arrow--open {
  transform: rotate(180deg);
}
.w-steps__list {
  padding: 4px 8px;
  background: #fafafa;
  border-radius: 0 0 6px 6px;
}
.w-step {
  font-size: 11px;
  color: var(--abc-text-3);
  padding: 2px 0;
}

/* ---- 面板商品 ---- */
.w-products {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.w-product {
  display: flex;
  gap: 8px;
  padding: 6px;
  background: #fff;
  border: 1px solid var(--abc-border);
  border-radius: 6px;
  cursor: pointer;
}
.w-product:hover { border-color: var(--abc-primary); }
.w-product img {
  width: 50px; height: 50px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}
.w-product__info {
  flex: 1;
  font-size: 12px;
}
.w-product__price {
  color: var(--abc-price);
  font-weight: 600;
}

/* ---- 面板订单 ---- */
.w-orders {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.w-order {
  padding: 8px;
  background: #fff;
  border: 1px solid var(--abc-border);
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
}
.w-order:hover { border-color: var(--abc-primary); }
.w-order__head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 4px;
  color: var(--abc-text-2);
}
.w-order__items {
  display: flex;
  flex-direction: column;
  gap: 2px;
  color: var(--abc-text-3);
}

/* ---- 面板导航 ---- */
.w-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.w-nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  background: #fff;
  border: 1px solid var(--abc-border);
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  color: var(--abc-primary);
}
.w-nav-item:hover { background: var(--abc-primary-light); }
.w-nav-item span { flex: 1; color: var(--abc-text); }

/* ---- 面板打字指示 ---- */
.typing {
  display: flex;
  gap: 3px;
  padding: 4px 0;
}
.typing span {
  width: 6px; height: 6px;
  background: var(--abc-text-3);
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }
@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.4; }
  30% { transform: translateY(-5px); opacity: 1; }
}
.thinking-spin {
  animation: w-spin 1s linear infinite;
}
@keyframes w-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ---- 面板输入区 ---- */
.ai-widget__input {
  display: flex;
  gap: 8px;
  padding: 10px 12px;
  border-top: 1px solid var(--abc-border);
  background: #fff;
}
.ai-widget__input :deep(.el-input__wrapper) {
  border-radius: 8px;
}
</style>
