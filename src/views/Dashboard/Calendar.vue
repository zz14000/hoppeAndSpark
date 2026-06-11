<template>
  <div class="calendar-page glass-panel">
    <div class="page-header">
      <div class="view-switcher glass-panel">
        <div class="view-pill" :class="{ active: currentView === 'week' }" @click="currentView = 'week'">周视图</div>
        <div class="view-pill" :class="{ active: currentView === 'month' }" @click="currentView = 'month'">月视图</div>
        <div class="view-pill" :class="{ active: currentView === 'list' }" @click="currentView = 'list'">列表</div>
      </div>
      <div style="display: flex; gap: 15px; align-items: center;">
        <div class="ai-assistant-pill" @click="$router.push('/app/agent-chat?agent=strict')">✨ 呼叫 Strict 排期</div>
        <button class="btn btn-primary" @click="currentView = 'add'">+ 新建日程</button>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧主视图区 -->
      <section class="view-area glass-panel">
        <!-- 周视图 -->
        <div v-show="currentView === 'week'" class="week-grid">
          <div class="time-axis">
            <div style="height: 50px;"></div>
            <div class="week-time-label">09:00</div>
            <div class="week-time-label">11:00</div>
            <div class="week-time-label">13:00</div>
            <div class="week-time-label">15:00</div>
            <div class="week-time-label">17:00</div>
            <div class="week-time-label">19:00</div>
          </div>
          <div class="week-col" v-for="(day, i) in ['一','二','三','四','五','六','日']" :key="i">
            <div class="week-day-title" :class="{ today: i === 2 }">周{{ day }}<span class="num">{{ 18 + i }}</span></div>
            <!-- 模拟事件块 -->
            <div v-if="i === 0" class="week-event tag-blue" style="top: 70px; height: 120px;"><strong>算法挑战</strong><p>排序与搜索专题突破</p></div>
            <div v-if="i === 2" class="week-event tag-purple" style="top: 130px; height: 160px;"><strong>系统架构复习</strong><p>分布式高并发方案设计</p></div>
            <div v-if="i === 4" class="week-event tag-green" style="top: 250px; height: 90px;"><strong>社区交流</strong><p>参与开源项目 PR Review</p></div>
          </div>
        </div>

        <!-- 月视图简版 (示意) -->
        <div v-show="currentView === 'month'" class="month-view">
          <h2 style="margin-bottom: 20px;">2024 年 5 月</h2>
          <div class="month-grid">
             <div class="month-header" v-for="day in ['一','二','三','四','五','六','日']" :key="day">周{{ day }}</div>
             <div v-for="d in 31" :key="d" class="month-cell" :class="{ today: d === 20 }">
               <span class="d">{{ d }}</span>
               <div v-if="d === 18" class="event-dot blue"></div>
               <div v-if="d === 20" class="event-dot purple"></div>
               <div v-if="d === 22" class="event-dot green"></div>
             </div>
          </div>
        </div>

        <div v-show="currentView === 'list'" class="list-view">
          <div class="list-item">
            <div class="time-block"><span>18日</span>09:00</div>
            <div class="info-block"><h4>算法挑战：排序与搜索</h4><p>Coach 安排的模拟测验</p></div>
            <div class="tag blue">待完成</div>
          </div>
          <div class="list-item active">
            <div class="time-block"><span>20日</span>14:00</div>
            <div class="info-block"><h4>系统架构复习</h4><p>研习 Nebula 架构资源包</p></div>
            <div class="tag purple">进行中</div>
          </div>
          <div class="list-item">
            <div class="time-block"><span>22日</span>16:30</div>
            <div class="info-block"><h4>社区交流</h4><p>参与开源项目 PR Review</p></div>
            <div class="tag green">未开始</div>
          </div>
        </div>
      </section>

      <!-- 右侧边栏 -->
      <aside class="side-panel glass-panel">
        <div class="cat-list">
          <div class="section-title">专注分类</div>
          <div class="cat-pill"><div class="dot" style="background:#38bdf8;"></div>算法挑战</div>
          <div class="cat-pill"><div class="dot" style="background:#c084fc;"></div>系统架构</div>
          <div class="cat-pill"><div class="dot" style="background:#10b981;"></div>代码评审</div>
        </div>
        
        <div class="online-tutors" style="margin-top: 40px;">
          <div class="section-title">在线导师推荐</div>
          <div class="user-item">
            <div class="avatar">李</div>
            <div class="user-info">
              <div class="name">老李 <span class="pro">PRO</span></div>
              <div class="role">系统架构 · 在线</div>
            </div>
            <div class="status-dot"></div>
          </div>
          <div class="user-item">
            <div class="avatar">王</div>
            <div class="user-info">
              <div class="name">王工</div>
              <div class="role">算法竞赛 · 忙碌</div>
            </div>
            <div class="status-dot busy"></div>
          </div>
        </div>

        <div class="strict-card">
          <span class="icon">📋</span>
          <div>
            <h5>Strict 的建议</h5>
            <p>您周四的安排比较空闲，建议将周五的代码评审提前，以免周末加班。</p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const currentView = ref('week')
</script>

<style scoped>
.calendar-page { height: 100%; display: flex; flex-direction: column; gap: 20px; padding: 25px; border-radius: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.view-switcher { display: flex; padding: 4px; border-radius: 14px; gap: 4px; }
.view-pill { padding: 6px 20px; border-radius: 10px; font-size: 13px; cursor: pointer; color: var(--text-sub); transition: 0.2s; }
.view-pill:hover { background: rgba(0,0,0,0.02); }
.view-pill.active { background: white; color: var(--text-main); font-weight: 600; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }

.ai-assistant-pill { background: linear-gradient(135deg, rgba(186,230,253,0.7), rgba(233,213,255,0.7)); border-radius: 50px; padding: 8px 20px; font-size: 13px; cursor: pointer; font-weight: 600; color: #7e22ce; border: 1px solid rgba(192, 132, 252, 0.3); transition: 0.2s; }
.ai-assistant-pill:hover { background: linear-gradient(135deg, rgba(186,230,253,0.9), rgba(233,213,255,0.9)); transform: scale(1.05); }

.main-content { flex: 1; display: flex; gap: 20px; min-height: 0; }
.view-area { flex: 1; overflow: auto; padding: 20px; border-radius: 16px; background: rgba(255,255,255,0.4); }
.side-panel { flex: 0 0 280px; padding: 30px 20px; border-radius: 16px; display: flex; flex-direction: column; background: rgba(255,255,255,0.4); }

/* 周视图样式 */
.week-grid { display: grid; grid-template-columns: 60px repeat(7, 1fr); gap: 10px; min-width: 800px; }
.week-time-label { height: 100px; border-top: 1px dashed rgba(0,0,0,0.05); font-size: 12px; color: var(--text-sub); padding: 5px; text-align: right; padding-right: 10px; }
.week-col { border-left: 1px dashed rgba(0,0,0,0.05); position: relative; min-height: 600px; }
.week-day-title { text-align: center; margin-bottom: 20px; font-size: 12px; color: var(--text-sub); }
.week-day-title .num { display: block; font-size: 24px; font-weight: 700; color: var(--text-main); margin-top: 5px; }
.week-day-title.today .num { color: #fff; background: var(--accent); width: 36px; height: 36px; line-height: 36px; border-radius: 50%; margin: 5px auto 0; box-shadow: 0 4px 10px rgba(168, 85, 247, 0.3); }

.week-event { position: absolute; width: calc(100% - 10px); left: 5px; border-radius: 12px; padding: 10px; font-size: 12px; border: 1px solid rgba(255,255,255,0.4); box-shadow: 0 4px 10px rgba(0,0,0,0.05); transition: 0.2s; cursor: pointer; overflow: hidden; backdrop-filter: blur(5px); }
.week-event:hover { transform: translateY(-2px); z-index: 10; box-shadow: 0 10px 20px rgba(0,0,0,0.1); filter: brightness(1.05); }
.week-event strong { display: block; margin-bottom: 4px; font-size: 13px; }
.week-event p { font-size: 11px; opacity: 0.8; line-height: 1.4; }

.tag-blue { background: rgba(186, 230, 253, 0.7); color: #0369a1; border-left: 4px solid #0ea5e9; }
.tag-purple { background: rgba(233, 213, 255, 0.7); color: #7e22ce; border-left: 4px solid #a855f7; }
.tag-green { background: rgba(167, 243, 208, 0.7); color: #047857; border-left: 4px solid #10b981; }

/* 月视图 */
.month-view { padding: 10px; }
.month-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; }
.month-header { text-align: center; font-size: 13px; font-weight: 600; color: var(--text-sub); padding-bottom: 10px; }
.month-cell { aspect-ratio: 1; border-radius: 12px; background: rgba(255,255,255,0.5); padding: 10px; position: relative; border: 1px solid rgba(0,0,0,0.02); transition: 0.2s; cursor: pointer; }
.month-cell:hover { background: #fff; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
.month-cell.today { border-color: var(--accent); background: rgba(168, 85, 247, 0.05); }
.month-cell.today .d { color: var(--accent); font-weight: 700; }
.month-cell .d { font-size: 14px; color: var(--text-main); }
.event-dot { width: 6px; height: 6px; border-radius: 50%; position: absolute; bottom: 10px; left: 50%; transform: translateX(-50%); }
.event-dot.blue { background: #0ea5e9; }
.event-dot.purple { background: #a855f7; }
.event-dot.green { background: #10b981; }

/* 列表视图 */
.list-view { display: flex; flex-direction: column; gap: 15px; padding: 10px; }
.list-item { display: flex; align-items: center; gap: 20px; padding: 20px; background: rgba(255,255,255,0.5); border-radius: 16px; border: 1px solid transparent; transition: 0.2s; }
.list-item:hover { background: #fff; transform: translateX(5px); box-shadow: 0 10px 20px rgba(0,0,0,0.03); }
.list-item.active { border-color: var(--accent); background: rgba(255,255,255,0.8); }
.time-block { display: flex; flex-direction: column; align-items: center; font-size: 16px; font-weight: 700; color: var(--text-main); width: 60px; }
.time-block span { font-size: 12px; color: var(--text-sub); font-weight: 500; }
.info-block { flex: 1; }
.info-block h4 { font-size: 16px; margin-bottom: 4px; }
.info-block p { font-size: 13px; color: var(--text-sub); }
.tag { padding: 4px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; }

/* 侧边栏 */
.section-title { font-size: 13px; font-weight: 700; color: var(--text-sub); margin-bottom: 15px; text-transform: uppercase; letter-spacing: 1px; }
.cat-pill { display: flex; align-items: center; gap: 10px; padding: 8px 12px; border-radius: 8px; font-size: 14px; font-weight: 500; cursor: pointer; transition: 0.2s; }
.cat-pill:hover { background: rgba(255,255,255,0.5); }
.cat-pill .dot { width: 10px; height: 10px; border-radius: 50%; }

.user-item { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; padding: 8px; border-radius: 12px; transition: 0.2s; cursor: pointer; }
.user-item:hover { background: rgba(255,255,255,0.5); }
.avatar { width: 36px; height: 36px; border-radius: 10px; background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.user-info .name { font-size: 14px; font-weight: 600; }
.pro { font-size: 10px; background: #1e293b; color: #fbbf24; padding: 2px 6px; border-radius: 4px; margin-left: 4px; }
.user-info .role { font-size: 11px; color: var(--text-sub); margin-top: 2px; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; background: #10b981; margin-left: auto; }
.status-dot.busy { background: #ef4444; }

.strict-card { margin-top: auto; background: rgba(255, 149, 0, 0.05); border: 1px solid rgba(255, 149, 0, 0.2); padding: 15px; border-radius: 16px; display: flex; gap: 12px; align-items: flex-start; }
.strict-card .icon { font-size: 20px; }
.strict-card h5 { font-size: 13px; color: #ff9500; margin-bottom: 5px; }
.strict-card p { font-size: 12px; color: var(--text-main); line-height: 1.5; }
</style>
