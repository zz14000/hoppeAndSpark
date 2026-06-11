<template>
  <div class="topology-container glass-panel">
    <header class="topo-header">
      <div class="left">
        <button class="btn icon-btn" @click="$router.push('/app/resource')">←</button>
        <div class="title-info">
          <h2>数据结构与算法 · 学习拓扑</h2>
          <p>基于您的动态画像生成，预计还需 45 小时完成本阶段攻克。</p>
        </div>
      </div>
      <div class="right">
        <div class="legend">
          <span class="dot completed"></span> 已掌握
          <span class="dot in-progress"></span> 攻克中
          <span class="dot locked"></span> 未解锁
        </div>
        <button class="btn btn-primary" @click="resetView">🗺️ 视角居中</button>
      </div>
    </header>

    <!-- 可拖拽缩放的画布区 -->
    <div 
      class="topo-canvas" 
      @mousedown="startDrag" 
      @mousemove="onDrag" 
      @mouseup="endDrag" 
      @mouseleave="endDrag"
      @wheel.prevent="onWheel"
    >
      <div class="canvas-wrapper" :style="{ transform: `translate(${panX}px, ${panY}px) scale(${scale})` }">
        <!-- SVG 高级连线层 -->
        <div class="svg-layer">
          <svg width="2000" height="1500" style="overflow: visible;">
            <defs>
              <linearGradient id="lineGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#10b981" />
                <stop offset="100%" stop-color="var(--accent)" />
              </linearGradient>
              <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                <feGaussianBlur stdDeviation="4" result="blur" />
                <feComposite in="SourceGraphic" in2="blur" operator="over" />
              </filter>
            </defs>

            <!-- 使用贝塞尔曲线使连线更圆滑美观 (D3.js 风格) -->
            <!-- 节点1 -> 节点2 -->
            <path d="M 370 200 C 450 200, 450 150, 530 150" stroke="#10b981" stroke-width="4" fill="none" opacity="0.6" />
            <!-- 节点1 -> 节点3 -->
            <path d="M 370 200 C 450 200, 450 300, 530 300" stroke="url(#lineGrad)" stroke-width="4" fill="none" opacity="0.8" filter="url(#glow)" />
            
            <!-- 节点3 -> 节点4 (动画进行中) -->
            <path d="M 770 300 C 850 300, 850 300, 930 300" stroke="var(--accent)" stroke-width="4" fill="none" stroke-dasharray="10,10" class="line-anim" />
            
            <!-- 节点4 -> 节点5,6 -->
            <path d="M 1170 300 C 1250 300, 1250 200, 1330 200" stroke="rgba(0,0,0,0.1)" stroke-width="3" fill="none" />
            <path d="M 1170 300 C 1250 300, 1250 400, 1330 400" stroke="rgba(0,0,0,0.1)" stroke-width="3" fill="none" />
          </svg>
        </div>

        <!-- DOM 节点层 -->
        <div class="nodes-layer">
          <!-- 节点 1 -->
          <div class="topo-node status-completed" style="top: 200px; left: 250px;">
            <div class="node-content">
              <div class="icon">📚</div>
              <div class="info">
                <span class="name">基础数据结构</span>
                <span class="desc">数组、链表、栈与队列</span>
              </div>
            </div>
            <div class="badge">已掌握</div>
          </div>

          <!-- 节点 2 -->
          <div class="topo-node status-completed" style="top: 150px; left: 650px;">
            <div class="node-content">
              <div class="icon">🕸️</div>
              <div class="info">
                <span class="name">图论基础</span>
                <span class="desc">DFS, BFS, 拓扑排序</span>
              </div>
            </div>
            <div class="badge">已掌握</div>
          </div>

          <!-- 节点 3 (当前进度) -->
          <div class="topo-node status-in-progress" style="top: 300px; left: 650px;">
            <div class="node-content">
              <div class="icon">🌳</div>
              <div class="info">
                <span class="name">树结构进阶</span>
                <span class="desc">二叉树遍历与特性</span>
              </div>
            </div>
            <div class="pulse-ring"></div>
            <div class="progress-bar-mini"><div style="width: 60%"></div></div>
          </div>

          <!-- 节点 4 -->
          <div class="topo-node status-locked" style="top: 300px; left: 1050px;">
            <div class="node-content">
              <div class="icon">🔍</div>
              <div class="info">
                <span class="name">二叉搜索树 (BST)</span>
                <span class="desc">前置条件: 树结构进阶</span>
              </div>
            </div>
            <div class="lock-icon">🔒</div>
          </div>

          <!-- 节点 5 -->
          <div class="topo-node status-locked" style="top: 200px; left: 1450px;">
            <div class="node-content">
              <div class="icon">⚖️</div>
              <div class="info">
                <span class="name">平衡二叉树</span>
                <span class="desc">前置条件: BST</span>
              </div>
            </div>
            <div class="lock-icon">🔒</div>
          </div>

          <!-- 节点 6 -->
          <div class="topo-node status-locked" style="top: 400px; left: 1450px;">
            <div class="node-content">
              <div class="icon">🌲</div>
              <div class="info">
                <span class="name">B树与B+树</span>
                <span class="desc">前置条件: BST</span>
              </div>
            </div>
            <div class="lock-icon">🔒</div>
          </div>
        </div>
      </div>

      <!-- 缩放控制悬浮窗 -->
      <div class="zoom-controls glass-panel">
        <button @click="zoomIn">+</button>
        <span>{{ Math.round(scale * 100) }}%</span>
        <button @click="zoomOut">-</button>
      </div>

      <!-- 右下角浮动提示 -->
      <div class="floating-coach glass-panel">
        <span class="avatar">🤺</span>
        <div class="msg">
          <strong>Coach 提示：</strong> 您在“树结构进阶”停留了较长时间，是否需要安排一次基础测验来巩固？
          <button class="btn small-btn" @click="$router.push('/app/quiz')">接受测验</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const scale = ref(1)
const panX = ref(50)
const panY = ref(50)

let isDragging = false
let startX = 0
let startY = 0

const startDrag = (e) => {
  isDragging = true
  startX = e.clientX - panX.value
  startY = e.clientY - panY.value
}

const onDrag = (e) => {
  if (!isDragging) return
  panX.value = e.clientX - startX
  panY.value = e.clientY - startY
}

const endDrag = () => {
  isDragging = false
}

const onWheel = (e) => {
  const zoomFactor = 0.05
  if (e.deltaY < 0) {
    scale.value = Math.min(scale.value + zoomFactor, 2)
  } else {
    scale.value = Math.max(scale.value - zoomFactor, 0.5)
  }
}

const zoomIn = () => scale.value = Math.min(scale.value + 0.1, 2)
const zoomOut = () => scale.value = Math.max(scale.value - 0.1, 0.5)
const resetView = () => {
  scale.value = 1
  panX.value = 50
  panY.value = 50
}
</script>

<style scoped>
.topology-container { display: flex; flex-direction: column; height: 100%; overflow: hidden; padding: 0; position: relative; background: radial-gradient(circle at center, var(--bg) 0%, rgba(0,0,0,0.05) 100%); }

.topo-header { padding: 20px 30px; display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid rgba(0,0,0,0.05); background: var(--panel-bg); z-index: 10; }
.topo-header .left { display: flex; align-items: center; gap: 20px; }
.icon-btn { padding: 8px 12px; background: rgba(255,255,255,0.5); font-size: 16px; border: 1px solid rgba(0,0,0,0.05); }
.title-info h2 { font-size: 20px; font-weight: 800; margin-bottom: 4px; color: var(--text-main); }
.title-info p { font-size: 12px; color: var(--text-sub); }

.topo-header .right { display: flex; align-items: center; gap: 30px; }
.legend { display: flex; gap: 15px; font-size: 13px; color: var(--text-sub); font-weight: 600; }
.legend .dot { display: inline-block; width: 10px; height: 10px; border-radius: 50%; }
.dot.completed { background: #10b981; }
.dot.in-progress { background: var(--accent); box-shadow: 0 0 10px var(--accent); }
.dot.locked { background: #cbd5e1; }

.topo-canvas { flex: 1; position: relative; overflow: hidden; cursor: grab; background-image: radial-gradient(rgba(0,0,0,0.1) 1px, transparent 1px); background-size: 20px 20px; }
.topo-canvas:active { cursor: grabbing; }

.canvas-wrapper { position: absolute; inset: 0; transform-origin: 0 0; transition: transform 0.1s ease-out; }

.svg-layer { position: absolute; inset: 0; pointer-events: none; }
.line-anim { stroke-dasharray: 10; animation: dash 1s linear infinite; }
@keyframes dash { to { stroke-dashoffset: -20; } }

.nodes-layer { position: absolute; inset: 0; }

.topo-node { position: absolute; padding: 15px 20px; border-radius: 16px; background: var(--panel-bg); border: 2px solid transparent; box-shadow: 0 10px 30px rgba(0,0,0,0.05); cursor: pointer; transition: 0.2s; transform: translate(-50%, -50%); width: 240px; z-index: 2; backdrop-filter: blur(10px); }
.topo-node:hover { z-index: 5; box-shadow: 0 15px 40px rgba(0,0,0,0.1); filter: brightness(1.05); }
.node-content { display: flex; align-items: center; gap: 15px; }
.topo-node .icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; background: rgba(0,0,0,0.05); flex-shrink: 0; }
.topo-node .info { display: flex; flex-direction: column; gap: 4px; flex: 1; }
.topo-node .name { font-size: 15px; font-weight: 700; color: var(--text-main); }
.topo-node .desc { font-size: 11px; color: var(--text-sub); line-height: 1.4; }

/* 状态样式 */
.status-completed { border-color: #10b981; }
.status-completed .icon { background: rgba(16, 185, 129, 0.1); color: #10b981; }
.badge { position: absolute; top: -10px; right: -10px; background: #10b981; color: #fff; font-size: 10px; padding: 2px 8px; border-radius: 10px; font-weight: 700; border: 2px solid var(--bg); }

.status-in-progress { border-color: var(--accent); box-shadow: 0 10px 30px rgba(168, 85, 247, 0.2); }
.status-in-progress .icon { background: var(--accent); color: #fff; }
.progress-bar-mini { position: absolute; bottom: 0; left: 20px; right: 20px; height: 3px; background: rgba(0,0,0,0.05); border-radius: 3px 3px 0 0; overflow: hidden; }
.progress-bar-mini div { height: 100%; background: var(--accent); }

.status-locked { opacity: 0.6; filter: grayscale(100%); border-color: rgba(0,0,0,0.1); }
.status-locked:hover { opacity: 1; filter: none; }
.lock-icon { font-size: 16px; opacity: 0.5; }

/* 呼吸动画 */
.pulse-ring { position: absolute; inset: -6px; border-radius: 22px; border: 2px solid var(--accent); animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite; pointer-events: none; }
@keyframes pulse { 0% { transform: scale(1); opacity: 0.8; } 100% { transform: scale(1.15); opacity: 0; } }

/* 悬浮缩放控制 */
.zoom-controls { position: absolute; bottom: 40px; left: 40px; display: flex; align-items: center; gap: 15px; padding: 10px 20px; border-radius: 50px; border: 1px solid rgba(0,0,0,0.05); font-weight: 600; font-size: 14px; }
.zoom-controls button { background: transparent; border: none; font-size: 18px; font-weight: 700; cursor: pointer; color: var(--text-main); }
.zoom-controls button:hover { color: var(--accent); }

/* 右下角教练 */
.floating-coach { position: absolute; bottom: 40px; right: 40px; padding: 20px; border-radius: 16px; display: flex; gap: 15px; max-width: 350px; border: 1px solid rgba(239, 68, 68, 0.2); background: rgba(255,255,255,0.9); box-shadow: 0 15px 40px rgba(0,0,0,0.1); animation: slideUp 0.5s ease 1s both; opacity: 0; }
.floating-coach .avatar { font-size: 24px; width: 40px; height: 40px; background: rgba(239, 68, 68, 0.1); border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.floating-coach .msg { font-size: 13px; line-height: 1.6; color: var(--text-main); display: flex; flex-direction: column; align-items: flex-start; gap: 10px; }
.floating-coach .msg strong { color: #ef4444; }
.small-btn { padding: 6px 12px; font-size: 12px; border-radius: 6px; background: #ef4444; color: #fff; border: none; cursor: pointer; }

@keyframes slideUp { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }
</style>
