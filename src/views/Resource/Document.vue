<template>
  <div class="document-reader">
    <!-- 左侧文档大纲 -->
    <aside class="doc-sidebar glass-panel">
      <div class="sidebar-header">
        <h3>📑 目录</h3>
        <span class="progress-text">已读 35%</span>
      </div>
      <ul class="outline-list">
        <li class="active">
          <span class="dot"></span>1. 什么是多智能体通信
        </li>
        <li>
          <span class="dot"></span>2. 异步回调防死锁
        </li>
        <li>
          <span class="dot"></span>3. 核心代码演练
        </li>
        <li>
          <span class="dot"></span>4. 边界条件测试
        </li>
      </ul>
      <div class="ai-helper-btn" @click="showAiPanel = !showAiPanel">
        <span class="icon">🧙‍♂️</span> 呼叫 Sage 伴读
      </div>
    </aside>

    <!-- 中间沉浸式阅读区 -->
    <main class="doc-main glass-panel">
      <div class="doc-header">
        <div class="tags"><span class="tag">系统架构</span><span class="tag">AI</span></div>
        <h1>多智能体 (Multi-Agent) 通信与死锁防范指南</h1>
        <div class="meta">
          <span>阅读预计：15分钟</span>
          <span>Old Money 审校</span>
        </div>
      </div>

      <div class="doc-content">
        <p>单体的局限性使得集群架构成为必然。在 Hope 体系中，智能体间的情报交互决定了最终产出的无损率。</p>
        
        <h2>1. 什么是多智能体通信机制</h2>
        <p>
          多智能体系统（MAS）的通信不同于传统的微服务 RPC 调用，它更像是一种<strong>基于黑板模式 (Blackboard Pattern) 或发布-订阅 (Pub/Sub) 模式</strong>的异步事件流。
          <span class="highlight-text" @click="askSage('为什么必须用异步事件流？')">由于每个智能体的思考 (推理) 时间具有极大的不确定性，同步阻塞调用将导致整个系统的吞吐量呈指数级下降。</span>
        </p>

        <h2>2. 异步回调防死锁</h2>
        <p>在构建集群时，最常见的致命错误是：Agent A 等待 Agent B 的总结，而 Agent B 内部又触发了对 Agent A 的事实核查请求。这就是典型的逻辑死锁。</p>
        
        <div class="code-block">
          <div class="code-header">
            <span>typescript</span>
            <button class="copy-btn">复制</button>
          </div>
          <pre><code><span class="keyword">async function</span> <span class="function">dispatch</span>(event: Event) {
  <span class="comment">// 使用超时机制与非阻塞 Broker 避免死锁</span>
  <span class="keyword">await</span> Broker.<span class="function">publish</span>(<span class="string">'ai-cluster'</span>, event, { timeout: <span class="number">5000</span> });
}</code></pre>
        </div>
      </div>
    </main>

    <!-- 右侧 AI 伴读侧边栏 -->
    <aside class="ai-panel glass-panel" :class="{ 'is-open': showAiPanel }">
      <div class="panel-header">
        <div class="agent-info">
          <span class="avatar">🧙‍♂️</span>
          <div>
            <h4>Sage 伴读</h4>
            <p>苏格拉底式启发</p>
          </div>
        </div>
        <button class="close-btn" @click="showAiPanel = false">✕</button>
      </div>
      
      <div class="chat-area">
        <div class="msg-bubble ai">
          你好！我是 Sage。在阅读过程中遇到不懂的概念，可以直接点击高亮文本，或者在这里向我提问。
        </div>
        <div v-for="(msg, i) in aiMessages" :key="i" class="msg-bubble" :class="msg.role">
          {{ msg.content }}
        </div>
      </div>

      <div class="input-area">
        <input type="text" v-model="aiInput" @keyup.enter="sendToAi" placeholder="向 Sage 提问...">
        <button class="send-btn" @click="sendToAi">↑</button>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showAiPanel = ref(false)
const aiInput = ref('')
const aiMessages = ref([])

const askSage = (question) => {
  showAiPanel.value = true
  aiMessages.value.push({ role: 'user', content: question })
  setTimeout(() => {
    aiMessages.value.push({ 
      role: 'ai', 
      content: '好问题。思考一下，如果使用同步阻塞，当模型 API 响应延迟达到 30 秒时，系统会发生什么？试着结合我们在上一章学过的线程池耗尽概念来分析。' 
    })
  }, 800)
}

const sendToAi = () => {
  if (!aiInput.value.trim()) return
  askSage(aiInput.value)
  aiInput.value = ''
}
</script>

<style scoped>
.document-reader { display: flex; height: 100%; gap: 20px; position: relative; overflow: hidden; }

/* 左侧大纲 */
.doc-sidebar { flex: 0 0 260px; display: flex; flex-direction: column; padding: 25px 20px; }
.sidebar-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.sidebar-header h3 { font-size: 16px; font-weight: 700; }
.progress-text { font-size: 12px; color: var(--accent); background: rgba(10,132,255,0.1); padding: 4px 10px; border-radius: 50px; font-weight: 600; }
.outline-list { list-style: none; display: flex; flex-direction: column; gap: 15px; flex: 1; }
.outline-list li { font-size: 14px; color: var(--text-sub); cursor: pointer; display: flex; align-items: center; gap: 10px; transition: 0.2s; }
.outline-list li:hover { color: var(--text-main); }
.outline-list li.active { color: var(--text-main); font-weight: 600; }
.dot { width: 6px; height: 6px; border-radius: 50%; background: transparent; border: 1px solid var(--text-muted); }
.active .dot { background: var(--accent); border-color: var(--accent); }
.ai-helper-btn { background: linear-gradient(135deg, #f3e8ff, #e0e7ff); border: 1px solid rgba(192,132,252,0.3); border-radius: 16px; padding: 15px; text-align: center; cursor: pointer; font-weight: 600; color: #7e22ce; transition: 0.2s; }
.ai-helper-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(126,34,206,0.15); }

/* 主阅读区 */
.doc-main { flex: 1; overflow-y: auto; padding: 50px 80px; scroll-behavior: smooth; }
.doc-header { margin-bottom: 40px; padding-bottom: 20px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.tags { display: flex; gap: 10px; margin-bottom: 15px; }
.tag { font-size: 12px; background: rgba(0,0,0,0.04); padding: 4px 12px; border-radius: 6px; color: var(--text-sub); }
.doc-header h1 { font-size: 36px; font-weight: 800; line-height: 1.4; margin-bottom: 15px; color: var(--text-main); }
.meta { display: flex; gap: 20px; font-size: 13px; color: var(--text-muted); }

.doc-content { font-size: 16px; line-height: 1.8; color: #334155; }
.doc-content p { margin-bottom: 20px; }
.doc-content h2 { font-size: 24px; font-weight: 700; margin: 40px 0 20px; color: var(--text-main); }
.highlight-text { background: rgba(253,224,71,0.4); border-bottom: 2px dashed #eab308; cursor: pointer; transition: 0.2s; padding: 2px 4px; border-radius: 4px; }
.highlight-text:hover { background: rgba(253,224,71,0.8); }

.code-block { background: #1e293b; border-radius: 16px; overflow: hidden; margin: 30px 0; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
.code-header { display: flex; justify-content: space-between; align-items: center; padding: 10px 20px; background: rgba(255,255,255,0.05); color: #94a3b8; font-size: 12px; font-family: monospace; }
.copy-btn { background: transparent; border: none; color: #94a3b8; cursor: pointer; }
.copy-btn:hover { color: #fff; }
.code-block pre { padding: 20px; margin: 0; overflow-x: auto; }
.code-block code { font-family: 'Fira Code', monospace; font-size: 14px; line-height: 1.6; color: #e2e8f0; }
.keyword { color: #c678dd; }
.function { color: #61afef; }
.string { color: #98c379; }
.number { color: #d19a66; }
.comment { color: #5c6370; font-style: italic; }

/* 右侧 AI 面板 */
.ai-panel { position: absolute; right: -350px; top: 0; bottom: 0; width: 350px; display: flex; flex-direction: column; transition: right 0.4s cubic-bezier(0.16, 1, 0.3, 1); z-index: 100; border-left: 1px solid rgba(255,255,255,0.5); }
.ai-panel.is-open { right: 0; box-shadow: -10px 0 30px rgba(0,0,0,0.05); }
.panel-header { padding: 20px; border-bottom: 1px solid rgba(0,0,0,0.05); display: flex; justify-content: space-between; align-items: center; background: rgba(255,255,255,0.4); }
.agent-info { display: flex; align-items: center; gap: 12px; }
.agent-info .avatar { font-size: 24px; background: #fff; width: 40px; height: 40px; display: flex; align-items: center; justify-content: center; border-radius: 12px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
.agent-info h4 { font-size: 15px; font-weight: 700; }
.agent-info p { font-size: 11px; color: #7e22ce; }
.close-btn { background: transparent; border: none; font-size: 18px; cursor: pointer; color: var(--text-sub); }

.chat-area { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 15px; }
.msg-bubble { padding: 12px 16px; border-radius: 16px; font-size: 13px; line-height: 1.5; max-width: 85%; }
.msg-bubble.ai { background: #fff; align-self: flex-start; border-top-left-radius: 4px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); }
.msg-bubble.user { background: var(--accent); color: #fff; align-self: flex-end; border-top-right-radius: 4px; }

.input-area { padding: 15px; background: #fff; display: flex; gap: 10px; border-top: 1px solid rgba(0,0,0,0.05); }
.input-area input { flex: 1; background: rgba(0,0,0,0.03); border: none; outline: none; border-radius: 50px; padding: 0 15px; font-size: 13px; }
.send-btn { width: 36px; height: 36px; border-radius: 50%; background: var(--accent); color: #fff; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; }
</style>
