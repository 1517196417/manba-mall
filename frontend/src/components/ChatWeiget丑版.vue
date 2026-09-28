<script setup>
import {onMounted, ref} from "vue";
import request from "@/api/request"

// 是否是显示AI聊天框
const showAIChat = ref(false)
// 输入框
const inputValue = ref("")
// 对话ID
const sessionId = ref(null)
// 聊天历史记录
const chatHistory = ref([])

onMounted(()=>{
  // 初始化对话ID（从本地存储中读取会话ID）
  sessionId.value = localStorage.getItem("sessionId")
})

// 发送消息
async function sendMessage(){
  // 获取用户输入
  let msg = inputValue.value
  if(msg.trim() === "")
    return

  // 清空输入框
  inputValue.value = ""

  // 将用户消息添加到聊天历史中
  chatHistory.value.push({
    role: "user",
    content: msg,
  })

  // 发送消息
  let res = await request.post("/chat/send", {
    sessionId: sessionId.value,
    message: msg,
  });

  // 更新对话id
  sessionId.value = res.sessionId
  // 保存会话ID到本地存储
  localStorage.setItem("sessionId", res.sessionId)

  // 将AI回复添加到聊天历史中
  chatHistory.value.push({
    role: "assistant",
    content: res.content,
  })

}

</script>

<template>
  <!-- 智能客服图标 -->
  <div v-if="!showAIChat" @click="showAIChat=!showAIChat" class="smai-icon">
    <ChatDotRound />
  </div>

  <!-- 智能客服聊天界面 -->
  <div v-else class="smai-panel">
    <div class="header">
      AI 智能客服 <el-button @click="showAIChat=!showAIChat" link icon="Close"></el-button>
    </div>
    <div class="chat-history">
      <div v-for="item in chatHistory" :key="item.role" class="chat-item">
        <div v-if="item.role === 'user'" class="user-message">
          用户：{{ item.content }}
        </div>
        <div v-else class="assistant-message">
          客服：{{ item.content }}
        </div>
      </div>
    </div>
    <div class="footer">
      <el-input v-model="inputValue" placeholder="请输入" clearable></el-input>
      <el-button type="primary" @click="sendMessage">发送</el-button>
    </div>
  </div>
</template>

<style scoped>
.smai-icon {
  z-index: 20;
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 50px;
  background-color: #FF5000;
  color: #fff;
  padding: 10px;
  border-radius: 60px;
}
.smai-panel {
  z-index: 20;
  position: fixed;
  right: 20px;
  bottom: 20px;
  border-radius: 10px;
  width: 250px;
  height: 400px;
  background-color: #FF5000;
  display: flex;
  flex-direction: column;
  /* 显示阴影 */
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.5);
}
.smai-panel > .header {
  height: 50px;
  color: #fff;
}
.smai-panel > .footer {
  height: 50px;
  background-color: #fff;
  display: flex;
  padding: 4px;
}
.smai-panel > .chat-history {
  flex: 1; /* 权重1（占剩余的全部） */
  background-color: #F5F6F8;
}
</style>