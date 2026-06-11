<template>
  <div class="skill-tree-container glass-panel">
    <header class="st-header">
      <div class="left-group">
        <button class="btn icon-btn back-btn" @click="$router.push('/app/dashboard')">← 返回</button>
        <div class="st-title">
          <h2><span class="icon">🧠</span> 全栈技能图谱</h2>
          <p>核心能力觉醒度：<span class="highlight">{{ totalProgress }}%</span></p>
        </div>
      </div>
      <div class="actions">
        <button class="btn btn-primary glow-btn" @click="lightUpSkill" :disabled="isLighting">
          ✨ 点亮核心矩阵
        </button>
      </div>
    </header>

    <div class="st-main">
      <!-- 极客风格的技能宇宙网格背景 -->
      <div class="grid-bg"></div>

      <div class="universe-canvas">
        <!-- SVG 连接线 -->
        <svg class="connection-lines" width="100%" height="100%">
          <defs>
            <filter id="neon-glow" x="-20%" y="-20%" width="140%" height="140%">
              <feGaussianBlur stdDeviation="5" result="blur" />
              <feComposite in="SourceGraphic" in2="blur" operator="over" />
            </filter>
            <linearGradient id="active-line" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#10b981" />
              <stop offset="100%" stop-color="#a855f7" />
            </linearGradient>
          </defs>

          <!-- 从中心向外的连线 -->
          <line x1="50%" y1="50%" x2="25%" y2="30%" class="svg-line completed" />
          <line x1="50%" y1="50%" x2="20%" y2="70%" class="svg-line completed" />
          <line x1="50%" y1="50%" x2="75%" y2="25%" class="svg-line completed" />
          <line x1="50%" y1="50%" x2="80%" y2="75%" class="svg-line" :class="{ 'animating-line': isLighting, 'completed': hasNewLit }" />
        </svg>

        <!-- 中心大脑/核心 -->
        <div class="core-node">
          <div class="core-inner" :class="{ 'pulsing': isLighting }">🧠</div>
          <div class="core-ring r1"></div>
          <div class="core-ring r2"></div>
        </div>

        <!-- 轨道节点 -->
        <!-- 左上: 前端 -->
        <div class="orbit-node completed" style="top: 30%; left: 25%;">
          <div class="node-icon">🎨</div>
          <div class="node-info">
            <span class="name">前端工程化</span>
            <span class="level">Lv. 4</span>
          </div>
        </div>

        <!-- 左下: 数据库 -->
        <div class="orbit-node completed" style="top: 70%; left: 20%;">
          <div class="node-icon">🗄️</div>
          <div class="node-info">
            <span class="name">数据库调优</span>
            <span class="level">Lv. 3</span>
          </div>
        </div>

        <!-- 右上: 后端 -->
        <div class="orbit-node completed" style="top: 25%; left: 75%;">
          <div class="node-icon">⚙️</div>
          <div class="node-info">
            <span class="name">微服务架构</span>
            <span class="level">Lv. 5</span>
          </div>
        </div>

        <!-- 右下: 云原生 (待点亮) -->
        <div class="orbit-node" :class="{ 'completed': hasNewLit }" style="top: 75%; left: 80%;">
          <div class="node-icon">☁️</div>
          <div class="node-info">
            <span class="name">云原生部署</span>
            <span class="level" v-if="hasNewLit">Lv. 1</span>
            <span class="level" v-else>未解锁</span>
          </div>
          <!-- 粒子爆炸特效 -->
          <div class="explosion" v-if="isLighting">
            <div class="particle p1"></div>
            <div class="particle p2"></div>
            <div class="particle p3"></div>
            <div class="particle p4"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const totalProgress = ref(68)
const isLighting = ref(false)
const hasNewLit = ref(false)

const lightUpSkill = () => {
  if (isLighting.value || hasNewLit.value) return
  isLighting.value = true
  
  setTimeout(() => {
    hasNewLit.value = true
    totalProgress.value += 5
    isLighting.value = false
  }, 1500)
}
</script>

<style scoped>
.skill-tree-container { display: flex; flex-direction: column; height: 100%; border-radius: 20px; overflow: hidden; background: var(--bg); position: relative; }

.st-header { padding: 25px 40px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.05); z-index: 10; position: relative; background: rgba(255,255,255,0.4); backdrop-filter: blur(10px); }
.left-group { display: flex; align-items: center; gap: 20px; }
.back-btn { background: rgba(0,0,0,0.05); border: none; font-size: 14px; font-weight: 600; }
.st-title h2 { font-size: 22px; font-weight: 800; color: var(--text-main); margin-bottom: 5px; display: flex; align-items: center; gap: 10px; }
.st-title p { font-size: 13px; color: var(--text-sub); }
.highlight { color: var(--accent); font-weight: 800; font-size: 18px; }

.glow-btn { box-shadow: 0 0 20px rgba(168, 85, 247, 0.4); transition: 0.3s; }
.glow-btn:hover:not(:disabled) { box-shadow: 0 0 30px rgba(168, 85, 247, 0.6); transform: scale(1.05); }
.glow-btn:disabled { opacity: 0.7; cursor: not-allowed; }

.st-main { flex: 1; position: relative; overflow: hidden; }

/* 科技感网格背景 */
.grid-bg { position: absolute; inset: 0; background-image: linear-gradient(rgba(168, 85, 247, 0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(168, 85, 247, 0.05) 1px, transparent 1px); background-size: 40px 40px; transform: perspective(500px) rotateX(60deg) translateY(-100px) translateZ(-200px); opacity: 0.5; pointer-events: none; }

.universe-canvas { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; }

.connection-lines { position: absolute; inset: 0; pointer-events: none; }
.svg-line { stroke: rgba(0,0,0,0.1); stroke-width: 2; transition: 0.5s; }
.svg-line.completed { stroke: #10b981; filter: url(#neon-glow); stroke-width: 3; }
.animating-line { stroke: url(#active-line); stroke-width: 4; stroke-dasharray: 10; animation: dashAnim 0.5s linear infinite; filter: url(#neon-glow); }
@keyframes dashAnim { to { stroke-dashoffset: -20; } }

/* 中心大脑 */
.core-node { position: relative; z-index: 10; display: flex; align-items: center; justify-content: center; }
.core-inner { width: 100px; height: 100px; background: linear-gradient(135deg, var(--accent), #c084fc); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 40px; box-shadow: 0 0 40px rgba(168, 85, 247, 0.5); z-index: 2; position: relative; }
.core-inner.pulsing { animation: corePulse 0.5s ease-in-out infinite alternate; }
@keyframes corePulse { from { transform: scale(1); box-shadow: 0 0 40px rgba(168, 85, 247, 0.5); } to { transform: scale(1.1); box-shadow: 0 0 80px rgba(168, 85, 247, 0.8); } }

.core-ring { position: absolute; border-radius: 50%; border: 1px solid rgba(168, 85, 247, 0.3); }
.r1 { width: 160px; height: 160px; animation: spin 10s linear infinite; border-top-color: var(--accent); }
.r2 { width: 220px; height: 220px; animation: spinReverse 15s linear infinite; border-bottom-color: var(--accent); }
@keyframes spin { to { transform: rotate(360deg); } }
@keyframes spinReverse { to { transform: rotate(-360deg); } }

/* 轨道节点 */
.orbit-node { position: absolute; transform: translate(-50%, -50%); display: flex; flex-direction: column; align-items: center; gap: 10px; cursor: pointer; transition: 0.3s; z-index: 5; }
.orbit-node:hover { transform: translate(-50%, -50%) scale(1.1); z-index: 20; }

.node-icon { width: 60px; height: 60px; border-radius: 16px; background: rgba(255,255,255,0.8); border: 2px solid rgba(0,0,0,0.1); display: flex; align-items: center; justify-content: center; font-size: 24px; box-shadow: 0 10px 20px rgba(0,0,0,0.05); filter: grayscale(1); opacity: 0.6; transition: 0.5s; backdrop-filter: blur(5px); }
.orbit-node.completed .node-icon { filter: grayscale(0); opacity: 1; border-color: #10b981; box-shadow: 0 0 20px rgba(16, 185, 129, 0.3); background: #fff; }

.node-info { text-align: center; background: rgba(255,255,255,0.8); padding: 4px 12px; border-radius: 50px; border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 4px 10px rgba(0,0,0,0.05); backdrop-filter: blur(5px); }
.node-info .name { font-size: 13px; font-weight: 700; color: var(--text-sub); display: block; }
.node-info .level { font-size: 11px; font-weight: 800; color: var(--text-muted); }

.orbit-node.completed .node-info .name { color: var(--text-main); }
.orbit-node.completed .node-info .level { color: #10b981; }

/* 爆炸粒子特效 */
.explosion { position: absolute; inset: 0; pointer-events: none; }
.particle { position: absolute; top: 50%; left: 50%; width: 8px; height: 8px; background: #10b981; border-radius: 50%; box-shadow: 0 0 10px #10b981; }
.p1 { animation: shoot 1s ease-out forwards; --tx: -50px; --ty: -50px; }
.p2 { animation: shoot 1s ease-out forwards; --tx: 50px; --ty: -30px; }
.p3 { animation: shoot 1s ease-out forwards; --tx: -40px; --ty: 60px; }
.p4 { animation: shoot 1s ease-out forwards; --tx: 60px; --ty: 50px; }
@keyframes shoot { 0% { transform: translate(0,0) scale(1); opacity: 1; } 100% { transform: translate(var(--tx), var(--ty)) scale(0); opacity: 0; } }
</style>
