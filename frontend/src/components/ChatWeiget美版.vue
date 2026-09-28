<script setup>
import { nextTick, onMounted, ref, watch } from "vue";
import request from "@/api/request";

// 是否显示 AI 聊天框
const showAIChat = ref(false);
// 输入框
const inputValue = ref("");
// 对话 ID
const sessionId = ref(null);
// 聊天历史记录
const chatHistory = ref([]);
// 是否正在等待 AI 回复
const sending = ref(false);
// 滚动容器
const scrollRef = ref(null);

onMounted(() => {
  // 初始化对话 ID（从本地存储中读取会话 ID）
  sessionId.value = localStorage.getItem("sessionId");
});

// 滚动到底部
function scrollToBottom() {
  nextTick(() => {
    const el = scrollRef.value;
    if (el) el.scrollTop = el.scrollHeight;
  });
}

// 有新消息时自动滚到底部
watch(chatHistory, scrollToBottom, { deep: true });

// 发送消息
async function sendMessage() {
  const msg = inputValue.value.trim();
  if (!msg || sending.value) return;

  // 清空输入框
  inputValue.value = "";

  // 将用户消息添加到聊天历史中
  chatHistory.value.push({
    role: "user",
    content: msg,
  });

  sending.value = true;
  try {
    // 发送消息
    const res = await request.post("/chat/send", {
      sessionId: sessionId.value,
      message: msg,
    });

    // 更新对话 id
    sessionId.value = res.sessionId;
    // 保存会话 ID 到本地存储
    localStorage.setItem("sessionId", res.sessionId);

    // 将 AI 回复添加到聊天历史中
    chatHistory.value.push({
      role: "assistant",
      content: res.content,
    });
  } catch (err) {
    chatHistory.value.push({
      role: "assistant",
      content: "抱歉，服务暂时不可用，请稍后再试～",
    });
  } finally {
    sending.value = false;
  }
}
</script>

<template>
  <!-- 智能客服图标 -->
  <Transition name="pop">
    <button
        v-if="!showAIChat"
        class="smai-icon"
        type="button"
        aria-label="打开智能客服"
        @click="showAIChat = true"
    >
      <el-icon :size="24"><ChatDotRound /></el-icon>
    </button>
  </Transition>

  <!-- 智能客服聊天界面 -->
  <Transition name="slide-up">
    <section v-if="showAIChat" class="smai-panel">
      <!-- 头部 -->
      <header class="header">
        <div class="header-avatar">
          <el-icon :size="20"><ChatDotRound /></el-icon>
        </div>
        <div class="header-info">
          <p class="header-title">AI 智能客服</p>
          <p class="header-sub">
            <span class="dot"></span>
            在线 · 随时为你服务
          </p>
        </div>
        <button
            class="header-close"
            type="button"
            aria-label="关闭"
            @click="showAIChat = false"
        >
          <el-icon :size="16"><Close /></el-icon>
        </button>
      </header>

      <!-- 聊天记录 -->
      <div ref="scrollRef" class="chat-history">
        <!-- 空状态 -->
        <div v-if="!chatHistory.length" class="empty">
          <div class="empty-avatar">
            <el-icon :size="26"><ChatDotRound /></el-icon>
          </div>
          <p class="empty-title">你好，我是 AI 客服 👋</p>
          <p class="empty-desc">有什么可以帮你的？直接告诉我吧～</p>
        </div>

        <div
            v-for="(item, index) in chatHistory"
            :key="index"
            class="chat-item"
            :class="item.role"
        >
          <div class="avatar">{{ item.role === "user" ? "我" : "AI" }}</div>
          <div class="bubble">{{ item.content }}</div>
        </div>

        <!-- 正在输入 -->
        <div v-if="sending" class="chat-item assistant">
          <div class="avatar">AI</div>
          <div class="bubble typing">
            <i></i><i></i><i></i>
          </div>
        </div>
      </div>

      <!-- 输入区 -->
      <footer class="footer">
        <textarea
            v-model="inputValue"
            class="input"
            rows="1"
            placeholder="请输入你的问题…"
            @keydown.enter.exact.prevent="sendMessage"
        ></textarea>
        <button
            class="send-btn"
            type="button"
            :disabled="!inputValue.trim() || sending"
            aria-label="发送"
            @click="sendMessage"
        >
          <el-icon :size="17"><Promotion /></el-icon>
        </button>
      </footer>
    </section>
  </Transition>
</template>

<style scoped>
/* ---------------- 悬浮按钮 ---------------- */
.smai-icon {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  padding: 0;
  border: none;
  border-radius: 50%;
  color: #fff;
  cursor: pointer;
  background: linear-gradient(135deg, #ff8a3d 0%, #ff5000 100%);
  box-shadow: 0 8px 24px rgba(255, 80, 0, 0.38);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.smai-icon:hover {
  transform: translateY(-3px) scale(1.05);
  box-shadow: 0 14px 30px rgba(255, 80, 0, 0.48);
}

.smai-icon:active {
  transform: translateY(0) scale(0.98);
}

/* 呼吸光圈 */
.smai-icon::after {
  content: "";
  position: absolute;
  inset: -4px;
  border: 2px solid rgba(255, 80, 0, 0.45);
  border-radius: 50%;
  animation: ripple 2.2s ease-out infinite;
  pointer-events: none;
}

@keyframes ripple {
  0% {
    transform: scale(0.85);
    opacity: 0.9;
  }
  100% {
    transform: scale(1.35);
    opacity: 0;
  }
}

/* ---------------- 聊天面板 ---------------- */
.smai-panel {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 999;
  display: flex;
  flex-direction: column;
  width: 360px;
  height: 540px;
  max-height: calc(100vh - 48px);
  overflow: hidden;
  border-radius: 18px;
  background: #fff;
  transform-origin: right bottom;
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.16), 0 2px 8px rgba(0, 0, 0, 0.06);
}

/* 头部 */
.header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  color: #fff;
  background: linear-gradient(135deg, #ff8a3d 0%, #ff5000 100%);
}

.header-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.22);
}

.header-info {
  flex: 1;
  min-width: 0;
}

.header-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.3px;
}

.header-sub {
  display: flex;
  align-items: center;
  gap: 5px;
  margin: 3px 0 0;
  font-size: 11px;
  opacity: 0.92;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #4ade80;
  animation: pulse 1.8s ease-out infinite;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(74, 222, 128, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(74, 222, 128, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(74, 222, 128, 0);
  }
}

.header-close {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  padding: 0;
  color: #fff;
  cursor: pointer;
  border: none;
  border-radius: 50%;
  background: transparent;
  transition: background 0.2s ease, transform 0.2s ease;
}

.header-close:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: rotate(90deg);
}

/* ---------------- 聊天记录 ---------------- */
.chat-history {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 14px;
  padding: 16px;
  overflow-y: auto;
  background: #f7f8fa;
  scroll-behavior: smooth;
}

.chat-history::-webkit-scrollbar {
  width: 5px;
}

.chat-history::-webkit-scrollbar-thumb {
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.14);
}

.chat-history::-webkit-scrollbar-track {
  background: transparent;
}

/* 空状态 */
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex: 1;
  padding: 20px;
  text-align: center;
}

.empty-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  margin-bottom: 14px;
  color: #fff;
  border-radius: 18px;
  background: linear-gradient(135deg, #ff8a3d 0%, #ff5000 100%);
  box-shadow: 0 8px 20px rgba(255, 80, 0, 0.3);
}

.empty-title {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.empty-desc {
  margin: 0;
  font-size: 12.5px;
  line-height: 1.6;
  color: #999;
}

/* 消息条目 */
.chat-item {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  animation: fade-in 0.28s ease both;
}

.chat-item.user {
  flex-direction: row-reverse;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(6px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.avatar {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 50%;
}

.chat-item.assistant .avatar {
  color: #fff;
  background: linear-gradient(135deg, #ff8a3d, #ff5000);
  box-shadow: 0 3px 8px rgba(255, 80, 0, 0.25);
}

.chat-item.user .avatar {
  color: #666;
  background: #e6e8eb;
}

.bubble {
  max-width: 74%;
  padding: 9px 13px;
  font-size: 13.5px;
  line-height: 1.65;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
  background: #fff;
  border-radius: 14px 14px 14px 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.chat-item.user .bubble {
  color: #fff;
  background: linear-gradient(135deg, #ff8a3d 0%, #ff5000 100%);
  border-radius: 14px 14px 4px 14px;
  box-shadow: 0 4px 12px rgba(255, 80, 0, 0.26);
}

/* 正在输入 */
.typing {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 13px 14px;
}

.typing i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #ffb28a;
  animation: typing 1.2s ease-in-out infinite;
}

.typing i:nth-child(2) {
  animation-delay: 0.2s;
}

.typing i:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.45;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

/* ---------------- 输入区 ---------------- */
.footer {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 10px 12px;
  background: #fff;
  border-top: 1px solid #f0f1f3;
}

.input {
  flex: 1;
  min-width: 0;
  height: 38px;
  max-height: 80px;
  padding: 9px 12px;
  font-family: inherit;
  font-size: 13.5px;
  line-height: 1.45;
  color: #333;
  resize: none;
  overflow-y: auto;
  outline: none;
  border: 1px solid #e6e8eb;
  border-radius: 12px;
  background: #f7f8fa;
  transition: border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.input::placeholder {
  color: #b0b4bb;
}

.input:focus {
  border-color: #ff5000;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(255, 80, 0, 0.1);
}

.send-btn {
  display: grid;
  place-items: center;
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  padding: 0;
  color: #fff;
  cursor: pointer;
  border: none;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff8a3d 0%, #ff5000 100%);
  box-shadow: 0 4px 12px rgba(255, 80, 0, 0.3);
  transition: transform 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 80, 0, 0.42);
}

.send-btn:active:not(:disabled) {
  transform: translateY(0);
}

.send-btn:disabled {
  opacity: 0.42;
  cursor: not-allowed;
  box-shadow: none;
}

/* ---------------- 过渡动画 ---------------- */
.pop-enter-active,
.pop-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.pop-enter-from,
.pop-leave-to {
  opacity: 0;
  transform: scale(0.6);
}

.slide-up-enter-active {
  transition: opacity 0.25s ease, transform 0.3s cubic-bezier(0.34, 1.35, 0.64, 1);
}

.slide-up-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(16px) scale(0.94);
}

/* ---------------- 移动端适配 ---------------- */
@media (max-width: 480px) {
  .smai-icon {
    right: 16px;
    bottom: 16px;
  }

  .smai-panel {
    right: 0;
    bottom: 0;
    width: 100vw;
    height: 100vh;
    height: 100dvh;
    max-height: 100vh;
    max-height: 100dvh;
    border-radius: 0;
  }
}
</style>