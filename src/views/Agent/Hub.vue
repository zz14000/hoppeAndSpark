<template>
  <div class="agent-hub-container">
    <button class="btn icon-btn back-btn" @click="$router.back()">← 返回上一级</button>

    <div class="hub-content">
      <h1 class="hub-title">连接您的专属智能体</h1>
      
      <div class="agent-grid">
        <!-- Ava -->
        <div class="agent-card glass-panel" @click="navToAgent('ava')">
          <div class="icon-wrapper">
            <span class="emoji">👩‍🏫</span>
          </div>
          <span class="name">Ava (激励)</span>
        </div>

        <!-- Sage -->
        <div class="agent-card glass-panel" @click="navToAgent('sage')">
          <div class="icon-wrapper">
            <span class="emoji">🧙‍♂️</span>
          </div>
          <span class="name">Sage (启发)</span>
        </div>

        <!-- Coach -->
        <div class="agent-card glass-panel" @click="navToAgent('coach')">
          <div class="icon-wrapper">
            <span class="emoji">🤺</span>
          </div>
          <span class="name">Coach (实战)</span>
        </div>

        <!-- Strict -->
        <div class="agent-card glass-panel" @click="navToAgent('strict')">
          <div class="icon-wrapper">
            <span class="emoji">📋</span>
          </div>
          <span class="name">Strict (计划)</span>
        </div>

        <!-- Old Money -->
        <div class="agent-card glass-panel" @click="navToAgent('oldmoney')">
          <div class="icon-wrapper">
            <span class="emoji">💼</span>
          </div>
          <span class="name">Old Money</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()

const navToAgent = (id) => {
  router.push({ path: '/app/agent-chat', query: { agent: id } })
}
</script>

<style scoped>
.agent-hub-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 20px;
  position: relative;
  /* 匹配设计图中的柔和渐变背景 */
  background: linear-gradient(135deg, rgba(230, 230, 250, 0.4) 0%, rgba(240, 255, 240, 0.4) 100%);
  border-radius: 20px;
}

.back-btn {
  align-self: flex-start;
  background: transparent;
  border: none;
  font-weight: 600;
  color: var(--text-sub);
  margin-bottom: 20px;
  cursor: pointer;
  transition: color 0.2s;
}
.back-btn:hover {
  color: var(--accent);
}

.hub-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-bottom: 10vh; /* 稍微向上偏移以视觉居中 */
}

.hub-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-main);
  margin-bottom: 60px;
  letter-spacing: 1px;
}

.agent-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  max-width: 900px;
  width: 100%;
}

.agent-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.02);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.agent-card:hover {
  transform: translateY(-5px);
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.05);
}

.icon-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.emoji {
  font-size: 40px;
}

.name {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-main);
}
</style>
