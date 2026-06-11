<template>
  <div class="home-container">
    <!-- 左侧智能体快速呼叫栏 -->
    <div class="sidebar glass-panel">
      <div class="agent-icon" @click="navToAgent('ava')" title="Ava - 激励">👩‍🏫</div>
      <div class="agent-icon" @click="navToAgent('sage')" title="Sage - 启发">🧙‍♂️</div>
      <div class="agent-icon" @click="navToAgent('coach')" title="Coach - 实战">🤺</div>
      <div class="agent-icon" @click="navToAgent('strict')" title="Strict - 计划">📋</div>
      <div class="agent-icon" @click="navToAgent('oldmoney')" title="Old Money - 经验">💼</div>
      <div style="margin-top: auto;" class="agent-icon" title="所有智能体" @click="$router.push('/app/agent-hub')">⊞</div>
    </div>

    <!-- 右侧便当盒数据网格 -->
    <div class="main-content">
      <div class="bento-grid">
        
        <!-- 学习资源 -->
        <div class="bento-card glass-panel col-span-2" @click="$router.push('/app/resource')">
          <h3><span class="status-dot"></span> 专属学习资源</h3>
          <p>基于您的最新画像，Nebula 结合 Horizon 事实核查，为您动态生成了 3 份无幻觉的多模态资源包，请查阅。</p>
          <div class="tag-group">
            <span class="tag">图文+视频</span>
            <span class="tag accent-tag">Horizon 认证</span>
          </div>
        </div>
        
        <!-- 日历与计划 -->
        <div class="bento-card glass-panel col-span-2" @click="$router.push('/app/calendar')">
          <div class="card-header">
            <h3>📅 日历与计划 <span>由 Strict 调度</span></h3>
            <span class="strict-text">今日有任务</span>
          </div>
          
          <div class="mini-week">
            <div class="day"><div>一</div><div class="num">18</div></div>
            <div class="day"><div>二</div><div class="num">19</div></div>
            <div class="day active"><div>三</div><div class="num">20</div></div>
            <div class="day"><div>四</div><div class="num">21</div></div>
            <div class="day"><div>五</div><div class="num">22</div></div>
          </div>
          
          <div class="task-strip" v-if="upcomingEvent">
            <div class="stripe-bar"></div>
            <span class="time">{{ upcomingEvent.time }}</span>
            <span class="task">{{ upcomingEvent.title }} - {{ upcomingEvent.desc }}</span>
          </div>
        </div>

        <!-- 动态学习路径 -->
        <div class="bento-card glass-panel col-span-2" @click="$router.push('/app/topology')">
          <h3>🛤️ 动态学习路径</h3>
          <div class="path-steps">
            <div class="step done">✓ 基础通信</div>
            <div class="step current">▶ Agent构建</div>
            <div class="step locked">🔒 评估集成</div>
          </div>
          <div class="progress-bg">
            <div class="progress-fill"></div>
          </div>
        </div>

        <!-- 技能树 -->
        <div class="bento-card glass-panel col-span-1 flex-center" @click="$router.push('/app/skill-tree')">
          <h3 class="text-center">🌳 技能树点亮</h3>
          <div class="skill-graph">
            <div class="circle-chart" :style="{ background: `conic-gradient(var(--accent) 0% ${skillProgress}%, rgba(0,0,0,0.05) ${skillProgress}% 100%)` }">
              <span class="perc">{{ skillProgress }}%</span>
            </div>
          </div>
          <span class="ava-text">Ava 提醒：2 节点待复习</span>
        </div>

        <!-- 模拟考核 -->
        <div class="bento-card glass-panel col-span-1 coach-bg">
          <h3 class="coach-text">⚔️ 实战沙盘</h3>
          <div class="coach-badge">本周待挑战: 2</div>
          <p class="small-p">进入深水区，Coach 已为您准备好了大厂算法高频题模拟面试。</p>
          <button class="btn coach-btn" @click="$router.push('/app/quiz')">进入沙盘</button>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCalendarEvents, getCurrentUser } from '../../api/index.js'

const router = useRouter()

const skillProgress = ref(0)
const upcomingEvent = ref(null)

onMounted(async () => {
  // 获取用户进度
  const userRes = await getCurrentUser()
  if (userRes.code === 200) {
    skillProgress.value = userRes.data?.progress ?? 0
  }
  
  // 获取日历事件
  const eventRes = await getCalendarEvents()
  if (eventRes.code === 200 && Array.isArray(eventRes.data) && eventRes.data.length > 0) {
    upcomingEvent.value = eventRes.data[1] || eventRes.data[0]
  }
})

const navToAgent = (id) => {
  router.push({ path: '/app/agent-chat', query: { agent: id } })
}
</script>

<style scoped>
.home-container { display: flex; width: 100%; height: 100%; gap: 30px; }

/* 侧边智能体栏 */
.sidebar { flex: 0 0 80px; padding: 30px 0; display: flex; flex-direction: column; align-items: center; gap: 20px; }
.agent-icon {
  width: 48px; height: 48px; border-radius: 16px; display: flex; justify-content: center; align-items: center;
  font-size: 24px; cursor: pointer; background: rgba(255,255,255,0.4); border: 1px solid rgba(0,0,0,0.03);
  transition: all 0.3s var(--ease-apple);
}
.agent-icon:hover { transform: scale(1.15); background: rgba(255,255,255,0.8); box-shadow: 0 8px 20px rgba(0,0,0,0.08); }

/* 主内容区 */
.main-content { flex: 1; display: flex; flex-direction: column; }
.bento-grid { display: grid; grid-template-columns: repeat(4, 1fr); grid-template-rows: 1fr 1fr; gap: 24px; flex: 1; min-height: 0; }
.col-span-2 { grid-column: span 2; }
.col-span-1 { grid-column: span 1; }

.bento-card { padding: 30px; display: flex; flex-direction: column; cursor: pointer; position: relative; overflow: hidden; border: 1px solid rgba(255,255,255,0.4); transition: 0.3s cubic-bezier(0.16, 1, 0.3, 1); }
.bento-card:hover { transform: translateY(-5px) scale(1.02); box-shadow: 0 20px 40px rgba(0,0,0,0.08); border-color: rgba(255,255,255,0.8); z-index: 10; }
.bento-card h3 { font-size: 20px; font-weight: 700; margin-bottom: 12px; display: flex; align-items: center; gap: 10px; }
.bento-card p { color: var(--text-sub); line-height: 1.6; font-size: 14px; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; box-shadow: 0 0 10px currentColor; color: var(--accent); background: currentColor; }

.tag-group { margin-top: auto; display: flex; gap: 10px; }
.tag { background: rgba(0,0,0,0.04); padding: 4px 12px; border-radius: 999px; font-size: 12px; font-weight: 600; border: 1px solid rgba(0,0,0,0.05); }
.accent-tag { color: var(--accent); border-color: rgba(10, 132, 255, 0.2); }

/* 日历微件 */
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.card-header span { font-size: 13px; font-weight: 500; color: var(--text-muted); }
.strict-text { font-size: 13px; font-weight: 600; color: var(--c-strict); }
.mini-week { display: flex; justify-content: space-between; padding: 0 10px; margin-bottom: 20px; }
.day { text-align: center; color: var(--text-muted); font-size: 12px; font-weight: 600; }
.day .num { font-size: 16px; margin-top: 5px; }
.day.active { color: #fff; background: var(--text-main); border-radius: 14px; padding: 6px 14px; box-shadow: 0 6px 15px rgba(0,0,0,0.15); }
.task-strip { margin-top: auto; background: rgba(0,0,0,0.03); padding: 14px 18px; border-radius: 16px; display: flex; align-items: center; gap: 15px; }
.stripe-bar { width: 4px; height: 16px; background: var(--c-strict); border-radius: 4px; }
.time { font-size: 15px; font-weight: 600; }
.task { font-size: 14px; color: var(--text-sub); font-weight: 500; }

/* 路径微件 */
.path-steps { display: flex; justify-content: space-between; margin-top: 15px; font-size: 14px; }
.step.done { color: var(--text-muted); }
.step.current { font-weight: 700; color: var(--text-main); }
.step.locked { color: var(--text-muted); }
.progress-bg { width: 100%; height: 8px; background: rgba(0,0,0,0.05); border-radius: 999px; margin-top: 18px; }
.progress-fill { width: 50%; height: 100%; background: var(--text-main); border-radius: 999px; }

/* 其它卡片 */
.flex-center { align-items: center; justify-content: center; }
.text-center { justify-content: center; width: 100%; margin-bottom: 20px; }
.skill-graph { flex: 1; display: flex; align-items: center; justify-content: center; }
.circle-chart { width: 100px; height: 100px; border-radius: 50%; background: conic-gradient(var(--accent) 0% 68%, rgba(0,0,0,0.05) 68% 100%); display: flex; align-items: center; justify-content: center; margin-bottom: 15px; position: relative; }
.circle-chart::before { content: ''; position: absolute; width: 75px; height: 75px; background: var(--panel-bg); border-radius: 50%; backdrop-filter: blur(5px); }
.circle-chart .perc { position: relative; z-index: 2; font-size: 20px; font-weight: 800; color: var(--text-main); font-family: monospace; }

/* 点亮粒子动画 */
.particle-spark { position: absolute; inset: -10px; border-radius: 50%; border: 2px solid var(--accent); animation: sparkOut 0.8s ease-out forwards; pointer-events: none; z-index: 3; }
@keyframes sparkOut { 0% { transform: scale(1); opacity: 1; } 100% { transform: scale(1.5); opacity: 0; } }

.ava-text { font-size: 13px; color: var(--c-ava); font-weight: 600; margin-top: auto; background: rgba(255,45,85,0.05); padding: 6px 12px; border-radius: 50px; }
.ava-text.success { color: #10b981; background: rgba(16, 185, 129, 0.1); }

.coach-bg { background: linear-gradient(135deg, rgba(191, 90, 242, 0.05), rgba(147, 51, 234, 0.1)); border-color: rgba(191, 90, 242, 0.2); }
.coach-text { color: var(--c-coach); }
.coach-badge { display: inline-block; background: var(--c-coach); color: #fff; font-size: 11px; font-weight: 600; padding: 4px 10px; border-radius: 50px; margin-bottom: 15px; align-self: flex-start; }
.small-p { font-size: 13px !important; margin-bottom: 20px; flex: 1; }
.coach-btn { width: 100%; color: var(--c-coach); background: #fff; margin-top: auto; border: 1px solid rgba(191, 90, 242, 0.2); transition: 0.2s; }
.coach-btn:hover { background: var(--c-coach); color: #fff; }
</style>