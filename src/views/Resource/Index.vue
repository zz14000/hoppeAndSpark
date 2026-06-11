<template>
  <div class="resource-container">
    <!-- 左侧分类侧边栏 -->
    <div class="sidebar glass-panel">
      <div class="menu-group">
        <div class="group-title">知识探索</div>
        <div class="menu-item" :class="{ active: currentCategory === 'all' }" @click="currentCategory = 'all'">
          <span class="icon">🌌</span> 专属资源库
        </div>
        <div class="menu-item featured" @click="$router.push('/app/topology')">
          <span class="icon">🗺</span> 学习计划拓扑图
        </div>
      </div>

      <div class="menu-group">
        <div class="group-title">资源形态</div>
        <div class="menu-item" :class="{ active: currentCategory === 'video' }" @click="currentCategory = 'video'">
          <span class="icon">▶</span> 视频动画
        </div>
        <div class="menu-item" :class="{ active: currentCategory === 'doc' }" @click="currentCategory = 'doc'">
          <span class="icon">📄</span> 课程讲解文档
        </div>
        <div class="menu-item" :class="{ active: currentCategory === 'quiz' }" @click="currentCategory = 'quiz'">
          <span class="icon">📝</span> 练习与测验
        </div>
        <div class="menu-item" :class="{ active: currentCategory === 'case' }" @click="currentCategory = 'case'">
          <span class="icon">💻</span> 实操案例与源码
        </div>
      </div>
    </div>

    <!-- 右侧主内容区 -->
    <div class="main-view">
      <!-- 顶部 AI 动态横幅 -->
      <div class="hero glass-panel">
        <div class="hero-content">
          <div class="course-tag">Nebula 引擎 · 动态生成</div>
          <h1>数据结构与算法：进阶深水区</h1>
          <p>基于您的最新画像，Horizon 已为您过滤所有幻觉，生成了 <strong>3</strong> 份高阶资源包。</p>
          <div class="action-row">
            <button class="btn btn-primary" @click="$router.push('/app/topology')">查看学习路径</button>
            <button class="btn" style="background: rgba(255,255,255,0.8); border:none;">呼叫 Strict 调整难度</button>
          </div>
        </div>
        <div class="hero-bg-graphic"></div>
      </div>

      <!-- 资源过滤与展示区 -->
      <div class="filter-bar">
        <h3 class="section-title">为你推荐</h3>
        <div class="search-box glass-panel">
          <span>🔍</span>
          <input type="text" placeholder="在专属资源库中搜索...">
        </div>
      </div>

      <div class="resource-grid">
        <!-- 资源卡片 1：视频 -->
        <div class="res-card glass-panel" v-show="['all', 'video'].includes(currentCategory)" @click="$router.push('/app/video')">
          <div class="card-cover video-cover">
            <div class="play-icon">▶</div>
            <div class="duration">12:30</div>
          </div>
          <div class="card-body">
            <div class="card-top">
              <span class="type-tag video-tag">视频</span>
              <span class="ai-tag">✨ Horizon 认证</span>
            </div>
            <h4>二叉搜索树 BST 概念入门</h4>
            <p>结合动画演示，直观理解查找、插入、删除的 O(log n) 复杂度优势。</p>
            <div class="card-footer">
              <div class="progress"><div style="width: 65%"></div></div>
              <span class="progress-text">65%</span>
            </div>
          </div>
        </div>

        <!-- 资源卡片 2：文档 -->
        <div class="res-card glass-panel" v-show="['all', 'doc'].includes(currentCategory)" @click="$router.push('/app/document')">
          <div class="card-cover doc-cover">
            <div class="doc-icon">📄</div>
          </div>
          <div class="card-body">
            <div class="card-top">
              <span class="type-tag doc-tag">文档</span>
              <span class="ai-tag">✨ Old Money 萃取</span>
            </div>
            <h4>高并发场景下的红黑树应用</h4>
            <p>从底层源码出发，剖析为什么 Linux 内核与 Java 集合类偏爱红黑树。</p>
            <div class="card-footer">
              <div class="progress"><div style="width: 0%"></div></div>
              <span class="progress-text">未开始</span>
            </div>
          </div>
        </div>

        <!-- 资源卡片 3：测验 -->
        <div class="res-card glass-panel" v-show="['all', 'quiz'].includes(currentCategory)" @click="$router.push('/app/quiz')">
          <div class="card-cover quiz-cover">
            <div class="quiz-icon">📝</div>
          </div>
          <div class="card-body">
            <div class="card-top">
              <span class="type-tag quiz-tag">测验</span>
              <span class="ai-tag">✨ Coach 出题</span>
            </div>
            <h4>二叉树高频面试题沙盘</h4>
            <p>包含 10 道真实大厂面试题，支持在线编写代码并进行自动化测试评估。</p>
            <div class="card-footer">
              <button class="btn btn-primary start-btn">进入沙盘</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const currentCategory = ref('all')
</script>

<style scoped>
.resource-container { display: flex; height: 100%; gap: 30px; }

/* 左侧分类栏 */
.sidebar { flex: 0 0 260px; padding: 20px; display: flex; flex-direction: column; gap: 20px; }
.menu-group { display: flex; flex-direction: column; gap: 8px; }
.group-title { font-size: 12px; font-weight: 700; color: var(--text-sub); padding-left: 10px; margin-bottom: 5px; text-transform: uppercase; letter-spacing: 1px; }
.menu-item { padding: 12px 15px; border-radius: 14px; background: transparent; cursor: pointer; font-weight: 600; display: flex; align-items: center; gap: 12px; transition: 0.2s; color: var(--text-main); font-size: 14px; border: 1px solid transparent; }
.menu-item:hover { background: rgba(255,255,255,0.5); }
.menu-item.active { background: #fff; box-shadow: 0 4px 15px rgba(0,0,0,0.05); border-color: rgba(0,0,0,0.03); color: var(--accent); }
.menu-item.active .icon { color: var(--accent); }
.menu-item.featured { background: linear-gradient(135deg, rgba(186,230,253,0.4), rgba(233,213,255,0.4)); border-color: rgba(233,213,255,0.5); }
.menu-item.featured:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(186,230,253,0.3); }

/* 主视图 */
.main-view { flex: 1; display: flex; flex-direction: column; gap: 30px; overflow-y: auto; padding-right: 10px; padding-bottom: 30px; }

/* 顶部 Hero */
.hero { padding: 40px; border-radius: 24px; position: relative; overflow: hidden; display: flex; background: linear-gradient(135deg, rgba(255,255,255,0.9), rgba(255,255,255,0.5)); border: 1px solid rgba(255,255,255,0.8); }
.hero-content { position: relative; z-index: 2; max-width: 600px; }
.course-tag { display: inline-block; padding: 6px 16px; background: rgba(186,230,253,0.5); color: #0369a1; border-radius: 50px; font-size: 12px; margin-bottom: 15px; font-weight: 700; border: 1px solid rgba(186,230,253,0.8); }
.hero h1 { font-size: 32px; margin-bottom: 15px; font-weight: 800; letter-spacing: -0.5px; }
.hero p { color: var(--text-sub); font-size: 15px; line-height: 1.6; margin-bottom: 25px; }
.action-row { display: flex; gap: 15px; }
.hero-bg-graphic { position: absolute; right: -50px; top: -50px; width: 300px; height: 300px; background: radial-gradient(circle, rgba(192,132,252,0.2) 0%, transparent 70%); border-radius: 50%; pointer-events: none; }

/* 过滤栏 */
.filter-bar { display: flex; justify-content: space-between; align-items: center; }
.section-title { font-size: 20px; font-weight: 800; }
.search-box { display: flex; align-items: center; gap: 10px; padding: 10px 20px; border-radius: 50px; background: rgba(255,255,255,0.6); border: 1px solid rgba(0,0,0,0.05); width: 300px; }
.search-box input { flex: 1; border: none; background: transparent; outline: none; font-size: 14px; }

/* 资源网格 */
.resource-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 25px; }
.res-card { border-radius: 20px; overflow: hidden; display: flex; flex-direction: column; cursor: pointer; transition: 0.3s; border: 1px solid rgba(255,255,255,0.6); }
.res-card:hover { transform: translateY(-5px); box-shadow: 0 15px 35px rgba(0,0,0,0.06); border-color: var(--accent); }

/* 卡片封面 */
.card-cover { height: 160px; position: relative; display: flex; align-items: center; justify-content: center; }
.video-cover { background: linear-gradient(135deg, #1e293b, #334155); }
.doc-cover { background: linear-gradient(135deg, #f0f9ff, #e0f2fe); }
.quiz-cover { background: linear-gradient(135deg, #faf5ff, #f3e8ff); }

.play-icon { width: 50px; height: 50px; background: rgba(255,255,255,0.9); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 20px; color: #1e293b; padding-left: 4px; box-shadow: 0 4px 15px rgba(0,0,0,0.2); }
.duration { position: absolute; bottom: 10px; right: 10px; background: rgba(0,0,0,0.7); color: #fff; font-size: 11px; padding: 3px 8px; border-radius: 6px; font-family: monospace; }
.doc-icon { font-size: 50px; filter: drop-shadow(0 4px 10px rgba(0,0,0,0.1)); }
.quiz-icon { font-size: 50px; filter: drop-shadow(0 4px 10px rgba(0,0,0,0.1)); }

/* 卡片内容 */
.card-body { padding: 20px; display: flex; flex-direction: column; flex: 1; background: rgba(255,255,255,0.4); }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.type-tag { font-size: 11px; font-weight: 700; padding: 4px 10px; border-radius: 6px; }
.video-tag { background: #e0f2fe; color: #0369a1; }
.doc-tag { background: #f3e8ff; color: #7e22ce; }
.quiz-tag { background: #ffedd5; color: #c2410c; }
.ai-tag { font-size: 11px; font-weight: 600; color: var(--accent); background: rgba(10,132,255,0.1); padding: 4px 10px; border-radius: 50px; }

.card-body h4 { font-size: 16px; margin-bottom: 8px; line-height: 1.4; }
.card-body p { font-size: 13px; color: var(--text-sub); line-height: 1.5; margin-bottom: 20px; flex: 1; }

.card-footer { display: flex; align-items: center; gap: 10px; margin-top: auto; }
.progress { flex: 1; height: 6px; background: rgba(0,0,0,0.05); border-radius: 3px; overflow: hidden; }
.progress > div { height: 100%; background: linear-gradient(90deg, #38bdf8, #c084fc); border-radius: 3px; }
.progress-text { font-size: 12px; font-weight: 600; color: var(--text-sub); width: 40px; text-align: right; }
.start-btn { width: 100%; padding: 8px; border-radius: 10px; font-size: 13px; }
</style>
