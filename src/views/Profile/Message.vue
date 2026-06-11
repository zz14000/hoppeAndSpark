<template>
  <div class="msg-layout glass-panel">
    <!-- 最左侧功能栏 -->
    <div class="msg-nav">
      <div class="nav-item active" title="所有会话">💬<span class="badge">3</span></div>
      <div class="nav-item" title="联系人">👥</div>
      <div class="nav-item" title="搜索">🔍</div>
      <div style="margin-top: auto;" class="nav-item" title="系统通知">🔔</div>
    </div>

    <!-- 联系人/群组列表区 -->
    <div class="msg-sidebar-list">
      <div class="search-bar">
        <input type="text" placeholder="搜索联系人、群聊或记录...">
      </div>
      <div class="list-scroll">
        <div 
          v-for="chat in chatList" 
          :key="chat.id" 
          class="list-item" 
          :class="{ active: activeChatId === chat.id }"
          @click="activeChatId = chat.id"
        >
          <div class="avatar-wrap">
            <div class="avatar" :class="chat.type">{{ chat.avatar }}</div>
            <div v-if="chat.online" class="dot online"></div>
          </div>
          <div class="info">
            <div class="top">
              <h4>{{ chat.name }}</h4>
              <span>{{ chat.time }}</span>
            </div>
            <p>{{ chat.lastMsg }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧主聊天区 -->
    <div class="msg-main-content">
      <div class="msg-header">
        <div class="header-info">
          <h3>{{ activeChat.name }}</h3>
          <span v-if="activeChat.type === 'group'" class="member-count">({{ activeChat.memberCount }}人)</span>
        </div>
        <div class="header-tools">
          <span>📞</span><span>📹</span><span>⚙️</span>
        </div>
      </div>
      
      <div class="msg-body" ref="msgBody">
        <div 
          v-for="(msg, index) in activeChat.messages" 
          :key="index" 
          class="chat-row" 
          :class="{ me: msg.isMe, other: !msg.isMe }"
        >
          <div class="chat-avatar" :class="msg.isMe ? 'me-bg' : activeChat.type">
            {{ msg.isMe ? '我' : (msg.senderAvatar || activeChat.avatar) }}
          </div>
          <div class="chat-content-wrap">
            <div v-if="!msg.isMe && activeChat.type === 'group'" class="sender-name">{{ msg.senderName }}</div>
            <div class="chat-bubble">{{ msg.content }}</div>
          </div>
        </div>
      </div>
      
      <div class="msg-footer">
        <div class="tools">
          <span title="表情">😀</span>
          <span title="文件">📁</span>
          <span title="代码片段">⌨️</span>
          <span title="聊天记录">📜</span>
        </div>
        <textarea 
          v-model="inputText" 
          placeholder="输入消息... (按 Enter 键发送)"
          @keyup.enter.prevent="sendMessage"
        ></textarea>
        <div class="footer-bottom">
          <span class="hint">按 Enter 发送，Shift + Enter 换行</span>
          <button class="btn btn-primary send-btn" @click="sendMessage">发送(S)</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'

const activeChatId = ref(1)
const inputText = ref('')
const msgBody = ref(null)

const chatList = ref([
  {
    id: 1,
    name: '千星奇域技术讨论群',
    type: 'group',
    avatar: '群',
    time: '17:26',
    lastMsg: '我: 这是最新的原型代码，可以嵌套进去测试。',
    memberCount: 267,
    messages: [
      { isMe: false, senderName: '张导师', senderAvatar: '张', content: '我的基本就这些，有些界面直接调视频和论坛就行' },
      { isMe: true, content: '这是最新的 HTML/CSS 原型图代码，可以直接嵌套进去测试。' }
    ]
  },
  {
    id: 2,
    name: '一粒黑子',
    type: 'user',
    avatar: '黑',
    online: true,
    time: '昨天',
    lastMsg: '大佬能开源一下代码吗？',
    messages: [
      { isMe: false, content: '大佬你好，看了你博客里写的那个基于 AI 的全栈面试平台，太牛了！' },
      { isMe: false, content: '请问能开源一下代码或者指点一下 WebSocket 怎么保持长连接吗？' }
    ]
  },
  {
    id: 3,
    name: '系统通知',
    type: 'system',
    avatar: '🔔',
    time: '周一',
    lastMsg: '你的每周学习周报已生成。',
    messages: [
      { isMe: false, content: 'Spark 你好，你的每周学习周报已生成。上周你总计学习了 14 小时，战胜了 85% 的社区创作者，请继续保持！' }
    ]
  }
])

const activeChat = computed(() => chatList.value.find(c => c.id === activeChatId.value))

const scrollToBottom = async () => {
  await nextTick()
  if (msgBody.value) {
    msgBody.value.scrollTop = msgBody.value.scrollHeight
  }
}

const sendMessage = () => {
  if (!inputText.value.trim()) return
  
  const chat = chatList.value.find(c => c.id === activeChatId.value)
  chat.messages.push({ isMe: true, content: inputText.value.trim() })
  chat.lastMsg = `我: ${inputText.value.trim()}`
  chat.time = '刚刚'
  
  inputText.value = ''
  scrollToBottom()

  // 模拟对方回复
  if (chat.type === 'user') {
    setTimeout(() => {
      chat.messages.push({ isMe: false, content: '感谢大佬回复！我这就去试试！' })
      chat.lastMsg = '感谢大佬回复！我这就去试试！'
      scrollToBottom()
    }, 1500)
  }
}
</script>

<style scoped>
.msg-layout { display: flex; height: 100%; border-radius: 24px; overflow: hidden; padding: 0; background: rgba(255,255,255,0.6); border: 1px solid rgba(255,255,255,0.8); }

/* 最左侧导航 */
.msg-nav { width: 70px; background: rgba(255,255,255,0.4); border-right: 1px solid rgba(0,0,0,0.05); display: flex; flex-direction: column; align-items: center; padding: 20px 0; gap: 20px; }
.nav-item { width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 20px; cursor: pointer; position: relative; transition: 0.2s; color: var(--text-main); opacity: 0.7; }
.nav-item:hover { background: rgba(255,255,255,0.8); opacity: 1; }
.nav-item.active { background: var(--accent); color: #fff; opacity: 1; box-shadow: 0 4px 12px rgba(10,132,255,0.3); }
.badge { position: absolute; top: -4px; right: -4px; background: #ff4757; color: #fff; font-size: 10px; padding: 2px 6px; border-radius: 10px; border: 2px solid #fff; font-weight: 700; }

/* 侧边列表 */
.msg-sidebar-list { width: 300px; background: rgba(255,255,255,0.5); border-right: 1px solid rgba(0,0,0,0.05); display: flex; flex-direction: column; }
.search-bar { padding: 15px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.search-bar input { width: 100%; padding: 10px 16px; border-radius: 12px; border: 1px solid rgba(0,0,0,0.05); background: #fff; outline: none; font-size: 13px; transition: 0.2s; }
.search-bar input:focus { border-color: var(--accent); box-shadow: 0 0 0 3px rgba(10,132,255,0.1); }
.list-scroll { flex: 1; overflow-y: auto; padding: 10px; display: flex; flex-direction: column; gap: 5px; }
.list-item { display: flex; padding: 12px; cursor: pointer; border-radius: 12px; transition: 0.2s; border: 1px solid transparent; }
.list-item:hover { background: rgba(255,255,255,0.8); }
.list-item.active { background: #fff; border-color: rgba(0,0,0,0.05); box-shadow: 0 4px 15px rgba(0,0,0,0.03); }
.avatar-wrap { position: relative; margin-right: 12px; }
.avatar { width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 700; font-size: 16px; }
.avatar.group { background: linear-gradient(135deg, #10b981, #059669); }
.avatar.user { background: linear-gradient(135deg, #f59e0b, #d97706); }
.avatar.system { background: linear-gradient(135deg, #6366f1, #3b82f6); font-size: 20px; }
.dot { position: absolute; bottom: -2px; right: -2px; width: 14px; height: 14px; border-radius: 50%; border: 2px solid #fff; }
.dot.online { background: #10b981; }
.info { flex: 1; overflow: hidden; display: flex; flex-direction: column; justify-content: center; }
.top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.top h4 { font-size: 14px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: var(--text-main); }
.top span { font-size: 11px; color: var(--text-muted); }
.info p { font-size: 12px; color: var(--text-sub); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* 右侧主内容区 */
.msg-main-content { flex: 1; display: flex; flex-direction: column; background: rgba(255,255,255,0.3); }
.msg-header { padding: 20px 25px; border-bottom: 1px solid rgba(0,0,0,0.05); background: rgba(255,255,255,0.6); display: flex; justify-content: space-between; align-items: center; }
.header-info { display: flex; align-items: center; gap: 10px; }
.header-info h3 { font-size: 18px; font-weight: 700; }
.member-count { font-size: 13px; color: var(--text-sub); }
.header-tools { display: flex; gap: 20px; font-size: 18px; cursor: pointer; opacity: 0.6; }
.header-tools span:hover { opacity: 1; }

/* 聊天气泡区 */
.msg-body { flex: 1; overflow-y: auto; padding: 30px; display: flex; flex-direction: column; gap: 24px; }
.chat-row { display: flex; gap: 15px; align-items: flex-start; max-width: 80%; }
.chat-row.me { align-self: flex-end; flex-direction: row-reverse; }
.chat-avatar { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600; font-size: 14px; flex-shrink: 0; }
.chat-avatar.me-bg { background: var(--accent); }
.chat-avatar.group { background: #94a3b8; }
.chat-avatar.user { background: #f59e0b; }
.chat-avatar.system { background: #6366f1; }

.chat-content-wrap { display: flex; flex-direction: column; gap: 6px; }
.chat-row.me .chat-content-wrap { align-items: flex-end; }
.sender-name { font-size: 12px; color: var(--text-muted); margin-left: 4px; }
.chat-bubble { padding: 14px 20px; border-radius: 18px; font-size: 14px; line-height: 1.6; background: #fff; box-shadow: 0 4px 15px rgba(0,0,0,0.03); color: var(--text-main); }
.chat-row.me .chat-bubble { background: var(--accent); color: #fff; border-top-right-radius: 4px; }
.chat-row.other .chat-bubble { border-top-left-radius: 4px; }

/* 底部输入区 */
.msg-footer { padding: 15px 25px; background: #fff; border-top: 1px solid rgba(0,0,0,0.05); display: flex; flex-direction: column; height: 180px; border-bottom-right-radius: 24px; }
.tools { display: flex; gap: 20px; font-size: 20px; margin-bottom: 12px; color: var(--text-sub); }
.tools span { cursor: pointer; transition: 0.2s; }
.tools span:hover { transform: scale(1.1); color: var(--text-main); }
.msg-footer textarea { flex: 1; background: transparent; border: none; outline: none; resize: none; font-size: 14px; font-family: inherit; line-height: 1.5; color: var(--text-main); }
.footer-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: 10px; }
.hint { font-size: 12px; color: var(--text-muted); }
.send-btn { padding: 8px 24px; border-radius: 8px; font-size: 14px; }
</style>
