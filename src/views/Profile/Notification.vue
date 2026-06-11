<template>
  <div class="notification-container glass-panel">
    <header class="notify-header">
      <div class="title-area">
        <h2>🔔 消息与通知</h2>
        <span class="badge">3 条未读</span>
      </div>
      <div class="actions">
        <button class="btn icon-btn" @click="markAllRead">全部标为已读</button>
        <button class="btn icon-btn" @click="$router.push('/app/settings')">通知设置</button>
      </div>
    </header>

    <div class="notify-layout">
      <!-- 左侧分类 Tab -->
      <aside class="notify-sidebar">
        <div class="nav-item active">
          <span class="icon">🤖</span> 智能体提醒
          <span class="dot"></span>
        </div>
        <div class="nav-item">
          <span class="icon">💬</span> 社区互动
          <span class="dot"></span>
        </div>
        <div class="nav-item">
          <span class="icon">📢</span> 系统公告
        </div>
      </aside>

      <!-- 右侧消息列表 -->
      <main class="notify-list">
        <div v-if="isLoading" class="loading-text">正在拉取通知...</div>
        <!-- 消息卡片 -->
        <div v-else v-for="msg in notifications" :key="msg.id" class="msg-card" :class="{ unread: msg.unread }">
          <div class="msg-icon" :class="msg.type">
            <span v-if="msg.type === 'strict'">📋</span>
            <span v-if="msg.type === 'ava'">👩‍🏫</span>
            <span v-if="msg.type === 'system'">🌟</span>
          </div>
          <div class="msg-content">
            <div class="msg-top">
              <h4>{{ msg.title }}</h4>
              <span class="time">{{ msg.time }}</span>
            </div>
            <p>{{ msg.content }}</p>
            <div class="msg-actions" v-if="msg.type === 'strict'">
              <button class="btn small-btn primary">立即前往沙盘</button>
              <button class="btn small-btn">稍后提醒</button>
            </div>
            <div class="msg-actions" v-if="msg.type === 'system'">
              <button class="btn small-btn">查看周报</button>
            </div>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getNotifications, markNotificationRead } from '../../api/index.js'

const notifications = ref([])
const isLoading = ref(true)

onMounted(async () => {
  try {
    const res = await getNotifications()
    if (res.code === 200) {
      notifications.value = res.data
    }
  } catch (e) {
    console.error(e)
  } finally {
    isLoading.value = false
  }
})

const markAllRead = async () => {
  await markNotificationRead('all')
  notifications.value.forEach(n => n.unread = false)
}
</script>

<style scoped>
.notification-container { display: flex; flex-direction: column; height: 100%; border-radius: 20px; overflow: hidden; }

.notify-header { padding: 25px 30px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.05); background: rgba(255,255,255,0.4); }
.title-area { display: flex; align-items: center; gap: 15px; }
.title-area h2 { font-size: 22px; font-weight: 800; color: var(--text-main); margin: 0; }
.badge { background: #ef4444; color: #fff; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 50px; }
.actions { display: flex; gap: 10px; }
.icon-btn { background: rgba(255,255,255,0.6); border: 1px solid rgba(0,0,0,0.05); font-size: 13px; }

.notify-layout { display: flex; flex: 1; overflow: hidden; }

.notify-sidebar { flex: 0 0 220px; padding: 20px; border-right: 1px solid rgba(0,0,0,0.05); display: flex; flex-direction: column; gap: 8px; background: rgba(255,255,255,0.2); }
.nav-item { padding: 12px 15px; border-radius: 12px; cursor: pointer; display: flex; align-items: center; font-size: 14px; font-weight: 600; color: var(--text-sub); transition: 0.2s; position: relative; border: 1px solid transparent; }
.nav-item:hover { background: rgba(255,255,255,0.5); }
.nav-item.active { background: #fff; color: var(--accent); box-shadow: 0 4px 15px rgba(0,0,0,0.03); border-color: rgba(0,0,0,0.05); }
.nav-item .icon { font-size: 18px; margin-right: 10px; }
.nav-item .dot { position: absolute; right: 15px; width: 8px; height: 8px; background: #ef4444; border-radius: 50%; }

.notify-list { flex: 1; overflow-y: auto; padding: 30px; display: flex; flex-direction: column; gap: 20px; }
.msg-card { display: flex; gap: 20px; padding: 20px; border-radius: 16px; background: rgba(255,255,255,0.5); border: 1px solid rgba(0,0,0,0.05); transition: 0.2s; }
.msg-card:hover { transform: translateX(5px); background: rgba(255,255,255,0.8); box-shadow: 0 10px 30px rgba(0,0,0,0.03); }
.msg-card.unread { border-left: 4px solid var(--accent); background: #fff; }

.msg-icon { width: 48px; height: 48px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 24px; flex-shrink: 0; }
.msg-icon.strict { background: rgba(255, 149, 0, 0.1); color: #ff9500; }
.msg-icon.ava { background: rgba(255, 45, 85, 0.1); color: #ff2d55; }
.msg-icon.system { background: rgba(10, 132, 255, 0.1); color: #007aff; }

.msg-content { flex: 1; }
.msg-top { display: flex; justify-content: space-between; margin-bottom: 8px; }
.msg-top h4 { font-size: 16px; font-weight: 700; color: var(--text-main); }
.time { font-size: 12px; color: var(--text-muted); }
.msg-content p { font-size: 14px; color: var(--text-sub); line-height: 1.6; margin-bottom: 15px; }

.msg-actions { display: flex; gap: 10px; }
.small-btn { padding: 6px 16px; font-size: 13px; border-radius: 8px; border: 1px solid rgba(0,0,0,0.1); background: #fff; }
.small-btn.primary { background: var(--accent); color: #fff; border-color: var(--accent); }
.small-btn:not(.primary):hover { background: rgba(0,0,0,0.05); }
</style>
