<template>
  <div class="intro-page">
    <div class="content-wrapper">
      <div class="title-section">
        <div class="spark-icon">✨</div>
        <h1>Hope & Sparks</h1>
        <p>从思考到实践的全栈式 AI 伴学集群</p>
      </div>

      <div class="feature-grid">
        <div class="card-panel glass-panel">
          <div class="icon">🧠</div>
          <h3>智能路径规划</h3>
          <p>Strict 严莉莉为您动态裁剪冗余路径，基于遗忘曲线定制专属学习链表，效率倍增。</p>
        </div>
        <div class="card-panel glass-panel">
          <div class="icon">🌌</div>
          <h3>零幻觉资源引擎</h3>
          <p>Nebula 结合 Horizon 多模态事实核查系统，为您实时生成最真实可靠的高阶专属知识库。</p>
        </div>
        <div class="card-panel glass-panel">
          <div class="icon">🤺</div>
          <h3>深水区实战模拟</h3>
          <p>Coach 构建真实业务场景进行压力面试，Old Money 挑剔辩论传授一线职场避坑指南。</p>
        </div>
      </div>

      <button class="btn btn-primary start-btn" @click="handleStart">
        快速开始研习 →
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { isLoggedIn } from '../../utils/auth.js'

const router = useRouter()

const handleStart = () => {
  if (!isLoggedIn()) {
    router.push('/login')
    return
  }
  if (localStorage.getItem('spark_onboarded')) {
    router.push('/app/dashboard')
  } else {
    router.push('/setup')
  }
}
</script>

<style scoped>
.intro-page {
  width: 100%; height: 100vh; display: flex;
  align-items: center; justify-content: center;
}
.content-wrapper { display: flex; flex-direction: column; align-items: center; gap: 60px; }
.title-section { text-align: center; animation: slideUp 1s var(--ease-apple); }
.spark-icon { font-size: 64px; margin-bottom: 20px; filter: drop-shadow(0 4px 10px rgba(0,0,0,0.1)); }
h1 { font-size: 64px; font-weight: 800; margin-bottom: 20px; letter-spacing: -1px; background: linear-gradient(135deg, var(--accent), var(--c-coach)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }
.title-section p { font-size: 22px; color: var(--text-sub); }

.feature-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 40px; max-width: 1200px; width: 100%; }
.card-panel { padding: 50px 30px; text-align: center; display: flex; flex-direction: column; align-items: center; }
.card-panel .icon { font-size: 55px; margin-bottom: 20px; }
.card-panel h3 { font-size: 22px; margin-bottom: 15px; color: var(--text-main); }
.card-panel p { font-size: 15px; color: var(--text-sub); line-height: 1.6; }

.start-btn { padding: 18px 50px; font-size: 18px; margin-top: 20px; }

@keyframes slideUp {
  from { transform: translateY(40px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>