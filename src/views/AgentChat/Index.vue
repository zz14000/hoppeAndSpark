<template>
  <div class="chat-layout">
    <!-- 左侧切换助手 -->
    <div class="sidebar glass-panel">
      <div class="agent-icon" @click="currentAgent = 'ava'" :class="{ active: currentAgent==='ava' }">👩‍🏫</div>
      <div class="agent-icon" @click="currentAgent = 'sage'" :class="{ active: currentAgent==='sage' }">🧙‍♂️</div>
      <div class="agent-icon" @click="currentAgent = 'coach'" :class="{ active: currentAgent==='coach' }">🤺</div>
      <div class="agent-icon" @click="currentAgent = 'strict'" :class="{ active: currentAgent==='strict' }">📋</div>
      <div class="agent-icon" @click="currentAgent = 'oldmoney'" :class="{ active: currentAgent==='oldmoney' }">💼</div>
    </div>
    
    <!-- 助手简介面板 -->
    <div class="agent-info-panel glass-panel">
      <div class="avatar-big">{{ agentData[currentAgent].icon }}</div>
      <h2>{{ agentData[currentAgent].name }}</h2>
      <p class="role">{{ agentData[currentAgent].role }}</p>
      <div class="tags">
        <span class="tag" v-for="t in agentData[currentAgent].tags" :key="t">{{ t }}</span>
      </div>
    </div>

    <!-- 中间对话核心区 -->
    <div class="chat-center-wrapper">
      <div class="chat-header-bar">
        <div class="agent-search-bar glass-panel">
          <span>🔍</span>
          <input type="text" placeholder="在当前智能体语境中搜索...">
        </div>
        <div class="chat-top-tools">
          <span class="tool-icon" title="知识社区">🌐</span>
          <span class="tool-icon" title="帮助与支持">🎧</span>
        </div>
      </div>

      <div class="chat-main glass-panel">
        <div class="msg-list">
          <div class="msg-bubble ai">
            {{ agentData[currentAgent].welcomeMsg }}
          </div>
          <!-- 动态消息列表 -->
          <div v-for="(msg, i) in messages" :key="i" class="msg-bubble" :class="msg.role">
            {{ msg.content }}
          </div>
        </div>
        
        <div class="input-row">
          <div class="input-box-wrapper glass-panel">
            <input type="text" v-model="inputText" @keyup.enter="sendMsg" placeholder="向智能体描述您的问题或提交代码片段...">
            <span class="input-icon">📎</span>
            <span class="input-icon">🎙️</span>
          </div>
          <button class="btn btn-primary send-btn" @click="sendMsg">✈️</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentAgent = ref(route.query.agent || 'sage')

// 模拟不同智能体的数据
const agentData = {
  ava: { icon:'👩‍🏫', name: 'Ava', role: '潜意识唤醒与激励', tags: ['情绪干预','学习动力'], welcomeMsg: 'Spark你好！我看你的大脑活跃度处于巅峰，我们一鼓作气拿下这个知识点吧！' },
  sage: { icon:'🧙‍♂️', name: 'Sage', role: '苏格拉底式启发体系', tags: ['层层反问','不给直接答案'], welcomeMsg: 'Spark你好，我是 Sage。当你遇到难题时，我会通过反问帮你理清逻辑脉络。今天遇到什么瓶颈了吗？' },
  coach: { icon:'🤺', name: 'Coach', role: '深水区实战模拟', tags: ['压力测试','项目实战'], welcomeMsg: '理论看够了，来点真格的。这里有一份系统改造需求，给你 60 分钟出方案。' },
  strict: { icon:'📋', name: 'Strict', role: '动态算力调度与规划', tags: ['路径规划','遗忘曲线'], welcomeMsg: '你的掌握速度超出了我的预期 15%。我已经为你裁剪了下一章的冗余复习。' },
  oldmoney: { icon:'💼', name: 'Old Money', role: '行业壁垒与经验萃取', tags: ['甲方模拟','避坑指南'], welcomeMsg: '这种写法在硅谷 5 年前就被抛弃了。你以为能过得去，但并发一到 10 万立马熔断。' }
}

const inputText = ref('')
const messages = ref([])

// 切换智能体时清空记录
watch(currentAgent, () => {
  messages.value = []
})

const sendMsg = () => {
  if (!inputText.value.trim()) return
  messages.value.push({ role: 'user', content: inputText.value })
  const text = inputText.value
  inputText.value = ''
  
  // 模拟 AI 回复
  setTimeout(() => {
    let reply = ''
    if (currentAgent.value === 'sage') {
      reply = `我注意到你提到了“${text}”。但是，如果我们在极端边界条件下测试，你觉得这个逻辑还会成立吗？试着从数据结构的最坏时间复杂度来分析一下，不要直接给我代码。`
    } else if (currentAgent.value === 'oldmoney') {
      reply = `老实说，在生产环境中这种“${text}”的思路是不及格的。这会导致严重的内存泄漏，你考虑过高并发场景下的连接池复用吗？重写一遍，给我一个更成熟的方案。`
    } else if (currentAgent.value === 'coach') {
      reply = `收到你的解答。逻辑基本通顺，但在容错处理上扣 20 分。真实业务中，网络超时是常态。现在请你补充一段具备指数退避的重试机制代码。`
    } else if (currentAgent.value === 'ava') {
      reply = `哇！这个想法很有创意！💡 虽然可能还有一些小瑕疵，但我看到你正在迅速进步。保持这个状态，我们继续攻克下一个难点吧！`
    } else if (currentAgent.value === 'strict') {
      reply = `根据你刚才的回答，评估显示你对该知识点的掌握度已达 90%。我已自动将“基础回顾”从你的今日路径中移除，为你节省了 45 分钟。`
    } else {
      reply = `[${agentData[currentAgent.value].name} 思考中] 关于你说的“${text}”，让我们换个角度来看看...`
    }
    messages.value.push({ role: 'ai', content: reply })
    
    // 自动滚动到底部
    const msgListEl = document.querySelector('.msg-list')
    if(msgListEl) {
      setTimeout(() => {
        msgListEl.scrollTop = msgListEl.scrollHeight
      }, 50)
    }
  }, 1000)
}
</script>

<style scoped>
.chat-layout { display: flex; gap: 30px; height: 100%; width: 100%; }

/* 左侧栏 */
.sidebar { flex: 0 0 80px; padding: 30px 0; display: flex; flex-direction: column; align-items: center; gap: 20px; }
.agent-icon {
  width: 48px; height: 48px; border-radius: 16px; display: flex; justify-content: center; align-items: center; font-size: 24px; cursor: pointer; transition: 0.3s; background: rgba(255,255,255,0.4);
}
.agent-icon.active { background: #fff; box-shadow: 0 4px 15px rgba(0,0,0,0.1); border: 2px solid var(--accent); }

/* 助手信息板 */
.agent-info-panel { flex: 0 0 240px; display: flex; flex-direction: column; align-items: center; padding: 40px 20px; text-align: center; }
.avatar-big { width: 90px; height: 90px; border-radius: 50%; font-size: 45px; display: flex; align-items: center; justify-content: center; background: #fff; margin-bottom: 20px; box-shadow: 0 10px 30px rgba(0,0,0,0.06); }
h2 { margin-bottom: 8px; font-weight: 700; font-size: 22px; }
.role { font-size: 13px; color: var(--text-sub); margin-bottom: 30px; font-weight: 500; }
.tags { display: flex; gap: 10px; flex-wrap: wrap; justify-content: center; padding-top: 20px; border-top: 1px solid rgba(0,0,0,0.05); width: 100%; }
.tag { background: rgba(0,0,0,0.04); padding: 4px 12px; border-radius: 999px; font-size: 11px; font-weight: 600; color: var(--text-main); }

/* 中间对话核心区 */
.chat-center-wrapper { flex: 1; display: flex; flex-direction: column; gap: 20px; min-width: 0; }
.chat-header-bar { display: flex; justify-content: space-between; align-items: center; height: 50px; }
.agent-search-bar { flex: 0 0 350px; display: flex; align-items: center; padding: 0 20px; border-radius: 999px; height: 100%; }
.agent-search-bar input { flex: 1; background: transparent; border: none; outline: none; margin-left: 10px; }
.chat-top-tools { display: flex; gap: 20px; }
.tool-icon { font-size: 20px; cursor: pointer; opacity: 0.7; transition: 0.2s; }
.tool-icon:hover { opacity: 1; transform: scale(1.1); }

.chat-main { flex: 1; display: flex; flex-direction: column; padding: 30px; overflow: hidden; }
.msg-list { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 20px; padding-right: 10px; margin-bottom: 20px; }
.msg-bubble { background: rgba(255,255,255,0.5); padding: 16px 20px; border-radius: 20px; max-width: 75%; line-height: 1.6; font-size: 14px; align-self: flex-end; }
.msg-bubble.ai { background: rgba(255, 255, 255, 0.9); align-self: flex-start; box-shadow: 0 4px 15px rgba(0,0,0,0.03); }

.input-row { display: flex; gap: 15px; align-items: center; }
.input-box-wrapper { flex: 1; display: flex; align-items: center; border-radius: 999px; padding: 0 20px; height: 56px; background: rgba(255,255,255,0.7); }
.input-box-wrapper input { flex: 1; background: transparent; border: none; outline: none; font-size: 15px; }
.input-icon { font-size: 20px; cursor: pointer; padding: 5px; opacity: 0.6; }
.input-icon:hover { opacity: 1; }
.send-btn { width: 56px; height: 56px; border-radius: 50%; padding: 0; font-size: 20px; display: flex; align-items: center; justify-content: center; }
</style>