<template>
  <div class="app-container">
    <!-- 通用顶部导航 -->
    <header class="top-nav-bar">
      <div class="logo-area glass-panel border-none" @click="$router.push('/')">
        <span class="logo-icon">✨</span>
        <span class="logo-text">Hope & Sparks</span>
      </div>
      
      <!-- 主导航链接 -->
      <div class="main-nav-links">
        <div class="nav-link" :class="{ active: $route.path.includes('/dashboard') }" @click="$router.push('/app/dashboard')">工作台</div>
        <div class="nav-link" :class="{ active: $route.path.includes('/resource') }" @click="$router.push('/app/resource')">资源引擎</div>
        <div class="nav-link" :class="{ active: $route.path.includes('/community') }" @click="$router.push('/app/community')">知识社区</div>
      </div>

      <!-- 全局知识探索引擎 -->
      <div class="search-bar-fake glass-panel" @click="openExplore">
        <span style="font-size: 20px; opacity: 0.8;">🔍</span> 
        <input type="text" placeholder="向 Nebula 引擎输入您想探索的知识或痛点..." readonly style="pointer-events: none;">
      </div>

      <div class="personal-area">
        <div class="personal-icon-btn" title="消息与通知" @click="$router.push('/app/notification')">🔔</div>
        <div class="personal-icon-btn" title="系统偏好设置" @click="$router.push('/app/settings')">⚙️</div>
        <div class="personal-icon-btn profile-btn" title="个人账户" @click="$router.push('/app/profile')">👤</div>
      </div>
    </header>

    <!-- 子路由页面渲染区 -->
    <main class="main-workspace">
      <router-view />
    </main>

    <!-- 全局 Nebula 探索模态框 -->
    <div v-if="showSearchModal" class="search-modal-overlay" @click.self="closeSearch">
      <div class="search-modal glass-panel">
        <div class="search-input-area">
          <span style="font-size: 24px;">🌌</span>
          <input 
            type="text" 
            v-model="searchQuery" 
            @keyup.enter="handleSearch"
            placeholder="输入任何技术痛点，Nebula 为您实时生成专属知识库..." 
            autofocus
          >
          <button class="btn btn-primary" @click="handleSearch">探索</button>
        </div>

        <!-- 搜索结果区 -->
        <div class="search-results" v-if="isSearching">
          <div class="loading-state">
            <div class="spinner"></div>
            <p>Nebula 正在调用多智能体协同网络，为您生成多模态资源包...</p>
          </div>
        </div>
        <div class="search-results" v-else-if="hasSearched">
          <h3 style="margin-bottom: 15px;">✨ 探索结果：已为您生成以下专属资源</h3>
          <div class="result-list">
            <div class="result-item" @click="goToResource('/app/video')">
              <div class="icon" style="background: rgba(186,230,253,0.5); color: #0369a1;">▶</div>
              <div class="info">
                <h4>{{ searchQuery }} · 核心概念动画演示</h4>
                <p>Horizon 事实核查通过 · 预计耗时 12 分钟</p>
              </div>
            </div>
            <div class="result-item" @click="goToResource('/app/document')">
              <div class="icon" style="background: rgba(233,213,255,0.5); color: #7e22ce;">📄</div>
              <div class="info">
                <h4>{{ searchQuery }} · 高阶避坑指南与底层源码解析</h4>
                <p>由 Old Money 萃取一线大厂实战经验</p>
              </div>
            </div>
          </div>
        </div>
        <div class="search-hints" v-else>
          <p>尝试探索：</p>
          <div class="hint-tags">
            <span class="tag" @click="searchQuery='如何解决微服务分布式事务？'; handleSearch()">如何解决微服务分布式事务？</span>
            <span class="tag" @click="searchQuery='Rust 语言生命周期详解'; handleSearch()">Rust 语言生命周期详解</span>
            <span class="tag" @click="searchQuery='手写一个简易版 Vue3 响应式系统'; handleSearch()">手写一个简易版 Vue3 响应式系统</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const openExplore = () => {
  router.push('/app/explore')
}
</script>

<style scoped>
.top-nav-bar {
  display: flex; justify-content: space-between; align-items: center; 
  padding: 20px 30px; border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}
.logo-area { display: flex; align-items: center; gap: 12px; cursor: pointer; padding: 5px 15px; flex: 0 0 auto; }
.logo-area:hover { transform: scale(1.02); }
.logo-text { font-size: 22px; font-weight: 800; background: linear-gradient(135deg, var(--accent), var(--c-coach)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }

.main-nav-links { display: flex; gap: 20px; align-items: center; margin: 0 30px; }
.nav-link { font-size: 15px; font-weight: 600; color: var(--text-sub); cursor: pointer; transition: 0.2s; padding: 8px 12px; border-radius: 8px; }
.nav-link:hover { color: var(--text-main); background: rgba(255,255,255,0.4); }
.nav-link.active { color: var(--accent); background: rgba(255,255,255,0.8); box-shadow: 0 4px 10px rgba(0,0,0,0.03); }

.search-bar-fake {
  flex: 1; max-width: 500px; border-radius: 999px; height: 46px; 
  display: flex; align-items: center; padding: 0 20px; cursor: text;
  margin-right: auto;
}
.search-bar-fake input { flex:1; background: transparent; border: none; outline: none; margin-left: 15px; font-size: 15px; color: var(--text-main); }

.personal-area { display: flex; align-items: center; gap: 15px; }
.personal-icon-btn {
  width: 44px; height: 44px; border-radius: 50%; display: flex; align-items: center; justify-content: center;
  font-size: 20px; cursor: pointer; background: rgba(255,255,255,0.4); border: 1px solid rgba(0,0,0,0.03);
  transition: all 0.3s;
}
.personal-icon-btn:hover { background: rgba(255,255,255,0.9); transform: translateY(-3px); box-shadow: 0 8px 20px rgba(0,0,0,0.08); }
.profile-btn { background: rgba(255,255,255,0.8); border: 2px solid var(--accent); box-shadow: 0 4px 15px rgba(10,132,255,0.2); }

.main-workspace { flex: 1; overflow: hidden; position: relative; padding: 0 30px 30px; }

/* 搜索模态框样式 */
.search-modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.3); backdrop-filter: blur(10px); z-index: 1000; display: flex; justify-content: center; padding-top: 100px; }
.search-modal { width: 800px; background: rgba(255,255,255,0.8); padding: 30px; display: flex; flex-direction: column; gap: 20px; align-self: flex-start; animation: slideDown 0.3s ease; }
.search-input-area { display: flex; align-items: center; gap: 15px; background: #fff; padding: 10px 20px; border-radius: 999px; box-shadow: 0 10px 30px rgba(0,0,0,0.05); }
.search-input-area input { flex: 1; border: none; outline: none; font-size: 16px; padding: 10px 0; }
.search-input-area .btn { border-radius: 50px; padding: 10px 30px; }

.search-results { padding: 20px 0; }
.loading-state { display: flex; flex-direction: column; align-items: center; gap: 15px; color: var(--text-sub); padding: 40px 0; }
.spinner { width: 40px; height: 40px; border: 4px solid rgba(10, 132, 255, 0.2); border-top-color: var(--accent); border-radius: 50%; animation: spin 1s linear infinite; }

.result-list { display: flex; flex-direction: column; gap: 15px; }
.result-item { display: flex; align-items: center; gap: 15px; padding: 20px; background: #fff; border-radius: 16px; cursor: pointer; transition: 0.2s; border: 1px solid rgba(0,0,0,0.05); }
.result-item:hover { transform: translateX(5px); box-shadow: 0 10px 20px rgba(0,0,0,0.05); border-color: var(--accent); }
.result-item .icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; }
.result-item h4 { font-size: 16px; margin-bottom: 5px; }
.result-item p { font-size: 13px; color: var(--text-sub); }

.search-hints { padding: 20px 10px; }
.search-hints p { font-size: 13px; color: var(--text-sub); margin-bottom: 15px; }
.hint-tags { display: flex; gap: 10px; flex-wrap: wrap; }
.hint-tags .tag { background: rgba(255,255,255,0.6); border: 1px solid rgba(0,0,0,0.05); padding: 8px 16px; border-radius: 50px; font-size: 13px; cursor: pointer; transition: 0.2s; }
.hint-tags .tag:hover { background: var(--accent); color: #fff; border-color: var(--accent); }

@keyframes slideDown { from { opacity: 0; transform: translateY(-20px); } to { opacity: 1; transform: translateY(0); } }
@keyframes spin { to { transform: rotate(360deg); } }
</style>