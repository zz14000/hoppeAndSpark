<template>
  <div class="onboarding-page">
    <div class="chat-container glass-panel">
      <div class="chat-header">
        <div class="spark-icon">✨</div>
        <h2>动态画像构建中</h2>
        <p>让 Nebula 引擎更懂你</p>
      </div>

      <div class="chat-messages" ref="msgList">
        <div 
          v-for="(msg, index) in messages" 
          :key="index" 
          class="msg-bubble" 
          :class="msg.role"
        >
          <div v-if="msg.role === 'ai'" class="avatar">🤖</div>
          <div class="content">{{ msg.content }}</div>
          <div v-if="msg.role === 'user'" class="avatar user-avatar">我</div>
        </div>
        
        <div v-if="isTyping" class="msg-bubble ai typing">
          <div class="avatar">🤖</div>
          <div class="content">
            <span class="dot"></span><span class="dot"></span><span class="dot"></span>
          </div>
        </div>
      </div>

      <div class="chat-input-area" v-if="!isFinished">
        <div v-if="currentOptions.length > 0" class="quick-options">
          <button 
            v-for="(opt, idx) in currentOptions" 
            :key="idx" 
            class="btn quick-btn" 
            @click="selectOption(opt)"
          >
            {{ opt }}
          </button>
        </div>
        <div class="input-wrapper" v-else>
          <input 
            type="text" 
            v-model="inputText" 
            @keyup.enter="sendText" 
            placeholder="输入您的回答..." 
            :disabled="isTyping"
          >
          <button class="btn btn-primary send-btn" @click="sendText" :disabled="!inputText.trim() || isTyping">
            发送
          </button>
        </div>
      </div>

      <div class="chat-finish-area" v-else>
        <button class="btn btn-primary enter-btn" @click="enterDashboard">
          画像生成完毕，进入专属学习空间 🚀
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { completeOnboarding } from '../../api/onboarding.js'

const router = useRouter()
const msgList = ref(null)

const messages = ref([])
const isTyping = ref(false)
const inputText = ref('')
const currentOptions = ref([])
const isFinished = ref(false)

const steps = [
  {
    ai: "你好！我是 Nebula 引擎。为了给你生成最合适的学习资源和路径，我们需要简单聊聊。请问你目前的【年级或阶段】是什么？",
    options: ["高中生", "大学生", "职场新人", "资深开发者"]
  },
  {
    ai: "了解了！那么你接下来主要想专注的【学习领域】是哪一块呢？",
    options: ["前端开发", "后端架构", "人工智能", "数据结构与算法"]
  },
  {
    ai: "太棒了！在这个领域，你目前具备怎样的【基础知识】呢？",
    options: ["零基础小白", "了解一些概念", "写过简单Demo", "有实战经验"]
  },
  {
    ai: "收到。你希望通过这次学习达成的【最终目标】是什么？",
    options: ["应对期末考试", "准备求职面试", "完成项目实战", "兴趣驱动探索"]
  },
  {
    ai: "最后，你觉得自己的【自律性】如何？这将帮助 Strict 为你定制合适的计划强度。",
    options: ["极度自律", "需要偶尔提醒", "重度拖延症"]
  }
]

let currentStep = 0

const scrollToBottom = async () => {
  await nextTick()
  if (msgList.value) {
    msgList.value.scrollTop = msgList.value.scrollHeight
  }
}

const showAiMessage = (text, options = []) => {
  isTyping.value = true
  scrollToBottom()
  setTimeout(() => {
    isTyping.value = false
    messages.value.push({ role: 'ai', content: text })
    currentOptions.value = options
    scrollToBottom()
  }, 1000)
}

const handleUserReply = (text) => {
  messages.value.push({ role: 'user', content: text })
  currentOptions.value = []
  inputText.value = ''
  scrollToBottom()

  currentStep++
  if (currentStep < steps.length) {
    showAiMessage(steps[currentStep].ai, steps[currentStep].options)
  } else {
    finishOnboarding()
  }
}

const selectOption = (opt) => {
  handleUserReply(opt)
}

const sendText = () => {
  if (inputText.value.trim()) {
    handleUserReply(inputText.value.trim())
  }
}

const finishOnboarding = async () => {
  isTyping.value = true
  scrollToBottom()
  try {
    await completeOnboarding()
  } catch (e) {
    console.error('画像生成接口调用失败:', e)
  }
  setTimeout(() => {
    isTyping.value = false
    messages.value.push({ role: 'ai', content: "🎉 分析完毕！您的专属 Spark 画像已生成。Strict 已为您规划好学习路径，Nebula 已准备好资源包。我们开始吧！" })
    isFinished.value = true
    localStorage.setItem('spark_onboarded', 'true')
    scrollToBottom()
  }, 1500)
}

const enterDashboard = () => {
  router.push('/app/dashboard')
}

onMounted(() => {
  showAiMessage(steps[0].ai, steps[0].options)
})
</script>

<style scoped>
.onboarding-page {
  height: 100vh; width: 100%;
  display: flex; align-items: center; justify-content: center;
  background: radial-gradient(circle at center, rgba(186,230,253,0.2) 0%, transparent 60%);
}

.chat-container {
  width: 600px; height: 80vh; max-height: 800px;
  display: flex; flex-direction: column;
  padding: 0; overflow: hidden;
  box-shadow: 0 20px 50px rgba(0,0,0,0.1);
  border-radius: 24px;
}

.chat-header {
  text-align: center; padding: 25px 20px;
  background: rgba(255,255,255,0.4);
  border-bottom: 1px solid rgba(0,0,0,0.05);
}
.spark-icon { font-size: 32px; margin-bottom: 5px; }
.chat-header h2 { font-size: 20px; font-weight: 700; margin-bottom: 4px; }
.chat-header p { font-size: 13px; color: var(--text-sub); }

.chat-messages {
  flex: 1; padding: 30px; overflow-y: auto;
  display: flex; flex-direction: column; gap: 20px;
}

.msg-bubble {
  display: flex; align-items: flex-end; gap: 12px;
  max-width: 85%; animation: slideIn 0.3s ease;
}
.msg-bubble.ai { align-self: flex-start; }
.msg-bubble.user { align-self: flex-end; flex-direction: row; }

.avatar {
  width: 36px; height: 36px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: rgba(255,255,255,0.8); font-size: 18px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.05);
}
.user-avatar { background: var(--accent); color: white; font-size: 14px; font-weight: 600; }

.content {
  background: rgba(255,255,255,0.7);
  padding: 12px 18px; border-radius: 18px;
  font-size: 15px; line-height: 1.5; color: var(--text-main);
  box-shadow: 0 4px 10px rgba(0,0,0,0.02);
}
.msg-bubble.ai .content { border-bottom-left-radius: 4px; }
.msg-bubble.user .content { background: var(--accent); color: white; border-bottom-right-radius: 4px; }

/* Typing indicator */
.typing .content { display: flex; gap: 4px; padding: 16px 20px; }
.dot { width: 6px; height: 6px; background: var(--text-muted); border-radius: 50%; animation: blink 1.4s infinite; }
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

.chat-input-area {
  padding: 20px; background: rgba(255,255,255,0.5);
  border-top: 1px solid rgba(0,0,0,0.05);
}
.quick-options {
  display: flex; flex-wrap: wrap; gap: 10px; justify-content: center;
}
.quick-btn {
  background: #fff; color: var(--text-main); border: 1px solid rgba(0,0,0,0.05);
  border-radius: 50px; padding: 10px 20px; transition: 0.2s;
}
.quick-btn:hover { background: var(--accent); color: white; transform: translateY(-2px); }

.input-wrapper {
  display: flex; gap: 15px;
}
.input-wrapper input {
  flex: 1; padding: 12px 20px; border-radius: 50px;
  border: 1px solid rgba(0,0,0,0.1); outline: none; font-size: 15px;
  background: rgba(255,255,255,0.8);
}
.send-btn { border-radius: 50px; padding: 0 25px; }

.chat-finish-area {
  padding: 30px; display: flex; justify-content: center;
  background: rgba(255,255,255,0.5); border-top: 1px solid rgba(0,0,0,0.05);
}
.enter-btn { font-size: 16px; padding: 15px 40px; border-radius: 50px; animation: popIn 0.5s ease; }

@keyframes slideIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes blink {
  0%, 100% { opacity: 0.2; }
  50% { opacity: 1; }
}
@keyframes popIn {
  0% { transform: scale(0.9); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}
</style>
