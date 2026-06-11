<template>
  <div class="auth-page">
    <div class="auth-card glass-panel">
      <h1>找回密码</h1>
      <p class="hint">输入注册邮箱，我们将发送重置链接</p>

      <form class="form" @submit.prevent="handleSubmit">
        <div class="field">
          <label>邮箱</label>
          <input v-model="email" type="email" placeholder="spark@example.com" required />
        </div>
        <p v-if="message" :class="success ? 'success' : 'error'">{{ message }}</p>
        <button class="btn btn-primary" type="submit" :disabled="loading">
          {{ loading ? '发送中...' : '发送重置邮件' }}
        </button>
      </form>

      <button class="link-btn" @click="$router.push('/login')">← 返回登录</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { requestPasswordReset } from '../../api/auth.js'

const email = ref('')
const loading = ref(false)
const message = ref('')
const success = ref(false)

const handleSubmit = async () => {
  message.value = ''
  loading.value = true
  try {
    const res = await requestPasswordReset(email.value)
    if (res.code === 200) {
      success.value = true
      message.value = res.message || '重置邮件已发送，请查收邮箱'
    } else {
      message.value = res.message || '发送失败'
    }
  } catch (e) {
    message.value = e.message || '网络错误'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 24px; }
.auth-card { width: 100%; max-width: 420px; padding: 40px 36px; border-radius: 24px; }
.auth-card h1 { font-size: 24px; margin-bottom: 8px; }
.hint { color: var(--text-sub); font-size: 14px; margin-bottom: 24px; }
.form { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: var(--text-sub); }
.field input { padding: 12px 14px; border-radius: 12px; border: 1px solid rgba(0,0,0,0.08); background: rgba(255,255,255,0.7); font-size: 15px; outline: none; }
.error { color: #ef4444; font-size: 13px; }
.success { color: #10b981; font-size: 13px; }
.btn { width: 100%; padding: 14px; border-radius: 12px; }
.link-btn { width: 100%; margin-top: 16px; background: none; border: none; color: var(--text-sub); cursor: pointer; font-size: 13px; }
</style>
