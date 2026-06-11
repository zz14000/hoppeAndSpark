<template>
  <div class="login-page">
    <div class="login-card glass-panel">
      <div class="brand">
        <span class="spark">✨</span>
        <h1>Hope & Sparks</h1>
        <p>登录后开始你的 AI 伴学之旅</p>
      </div>

      <div class="tab-group">
        <button :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>

      <form class="form" @submit.prevent="handleSubmit">
        <div v-if="mode === 'register'" class="field">
          <label>昵称</label>
          <input v-model="form.nickname" type="text" placeholder="一粒黑子" required />
        </div>

        <div class="field">
          <label>{{ mode === 'login' ? '邮箱 / 用户名' : '邮箱' }}</label>
          <input
            v-model="form.account"
            :type="mode === 'register' ? 'email' : 'text'"
            placeholder="spark@example.com"
            required
          />
        </div>

        <div class="field">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="至少 8 位" required minlength="8" />
        </div>

        <div class="forgot-row" v-if="mode === 'login'">
          <button type="button" class="link-btn inline" @click="$router.push('/forgot-password')">忘记密码？</button>
        </div>

        <p v-if="errorMsg" class="error">{{ errorMsg }}</p>

        <button class="btn btn-primary submit-btn" type="submit" :disabled="loading">
          {{ loading ? '处理中...' : mode === 'login' ? '登录' : '注册并登录' }}
        </button>
      </form>

      <button class="link-btn" @click="$router.push('/')">← 返回首页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { login, register } from '../../api/auth.js'

const route = useRoute()
const router = useRouter()

const mode = ref('login')
const loading = ref(false)
const errorMsg = ref('')
const form = reactive({
  account: '',
  password: '',
  nickname: '',
})

const handleSubmit = async () => {
  errorMsg.value = ''
  loading.value = true
  try {
    if (mode.value === 'register') {
      const regRes = await register({
        email: form.account,
        password: form.password,
        nickname: form.nickname,
      })
      if (regRes.code !== 200) {
        errorMsg.value = regRes.message || '注册失败'
        return
      }
    }

    const res = await login({
      account: form.account,
      password: form.password,
    })

    if (res.code === 200) {
      const redirect = route.query.redirect || '/app/dashboard'
      router.replace(typeof redirect === 'string' ? redirect : '/app/dashboard')
    } else {
      errorMsg.value = res.message || '登录失败'
    }
  } catch (e) {
    errorMsg.value = e.message || '网络错误，请检查后端是否已启动'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 40px 36px;
  border-radius: 24px;
}

.brand { text-align: center; margin-bottom: 28px; }
.spark { font-size: 40px; }
.brand h1 {
  font-size: 28px;
  margin: 12px 0 8px;
  background: linear-gradient(135deg, var(--accent), var(--c-coach));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}
.brand p { color: var(--text-sub); font-size: 14px; }

.tab-group {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: rgba(0,0,0,0.04);
  padding: 4px;
  border-radius: 12px;
}
.tab-group button {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  font-weight: 600;
  color: var(--text-sub);
}
.tab-group button.active {
  background: #fff;
  color: var(--accent);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.form { display: flex; flex-direction: column; gap: 16px; }
.field { display: flex; flex-direction: column; gap: 6px; }
.field label { font-size: 13px; font-weight: 600; color: var(--text-sub); }
.field input {
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(0,0,0,0.08);
  background: rgba(255,255,255,0.7);
  font-size: 15px;
  outline: none;
}
.field input:focus { border-color: var(--accent); }

.error { color: #ef4444; font-size: 13px; margin: 0; }
.submit-btn { width: 100%; padding: 14px; margin-top: 8px; border-radius: 12px; }
.link-btn {
  width: 100%;
  margin-top: 16px;
  background: none;
  border: none;
  color: var(--text-sub);
  cursor: pointer;
  font-size: 13px;
}
.link-btn:hover { color: var(--accent); }
.forgot-row { display: flex; justify-content: flex-end; margin-top: -8px; }
.link-btn.inline { width: auto; margin: 0; font-size: 13px; }
</style>
