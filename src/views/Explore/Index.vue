<template>
  <div class="explore-container">
    <header class="explore-header glass-panel">
      <button class="btn icon-btn" @click="$router.push('/app/dashboard')">← 返回</button>
      <div class="search-bar">
        <span class="icon">🌌</span>
        <input type="text" v-model="query" @keyup.enter="doSearch" placeholder="继续向 Nebula 引擎提问...">
        <button class="btn btn-primary" @click="doSearch">探索</button>
      </div>
    </header>

    <div class="explore-main">
      <div class="left-results">
        <div v-if="isSearching" class="loading-state glass-panel">
          <div class="spinner"></div>
          <p>Nebula 正在调用多智能体协同网络，为您生成多模态资源包...</p>
        </div>

        <div v-else-if="hasSearched && searchResult" class="result-content">
          <div class="ai-summary-card glass-panel">
            <h3>✨ Nebula 核心解答</h3>
            <p>{{ searchResult.summary }}</p>
            <ul>
              <li><strong>2PC / 3PC (两阶段提交)：</strong>强一致性，但性能差，容易产生单点故障。</li>
              <li><strong>TCC (Try-Confirm-Cancel)：</strong>业务层面两阶段提交，灵活性高，但开发成本大。</li>
              <li><strong>可靠消息最终一致性：</strong>基于 MQ (如 RocketMQ)，适合异步解耦场景。</li>
              <li><strong>Seata 框架：</strong>阿里开源的一站式分布式事务解决方案，内置 AT、TCC、SAGA 等多种模式。</li>
            </ul>
            <div class="ai-actions">
              <button class="btn small-btn">继续追问 Sage</button>
              <button class="btn small-btn">生成思维导图</button>
            </div>
          </div>

          <h3 class="section-title">为你推荐的资源包</h3>
          <div class="resource-grid">
            <div class="res-card glass-panel" v-for="res in searchResult.resources" :key="res.id">
              <div class="card-cover" :class="res.type + '-cover'">
                <span class="icon" v-if="res.type === 'document'">📄</span>
                <span class="icon" v-if="res.type === 'video'">▶</span>
                <span class="icon" v-if="res.type === 'quiz'">⚔️</span>
              </div>
              <div class="info">
                <h4>{{ res.title }}</h4>
                <p>推荐资源 · 自动生成</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <aside class="right-knowledge-graph glass-panel">
        <h4 class="panel-title">🧠 知识关联图谱</h4>
        <div class="graph-placeholder">
          <div class="node main-node">分布式事务</div>
          <div class="node sub-node node-1">CAP定理</div>
          <div class="node sub-node node-2">Seata</div>
          <div class="node sub-node node-3">MQ 最终一致性</div>
          
          <svg class="lines">
            <line x1="50%" y1="50%" x2="20%" y2="20%" stroke="var(--accent)" stroke-width="2" />
            <line x1="50%" y1="50%" x2="80%" y2="20%" stroke="var(--accent)" stroke-width="2" />
            <line x1="50%" y1="50%" x2="50%" y2="80%" stroke="var(--accent)" stroke-width="2" />
          </svg>
        </div>
        
        <div class="related-queries">
          <h5>延伸探索：</h5>
          <span class="tag">Seata AT 模式原理</span>
          <span class="tag">RocketMQ 事务消息</span>
          <span class="tag">BASE 理论详解</span>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { exploreNebula } from '../../api/index.js'

const route = useRoute()
const query = ref('')
const isSearching = ref(true)
const hasSearched = ref(false)
const searchResult = ref(null)

onMounted(() => {
  query.value = route.query.q || '如何解决微服务分布式事务？'
  doSearch()
})

const doSearch = async () => {
  if (!query.value.trim()) return
  isSearching.value = true
  hasSearched.value = false
  
  try {
    const res = await exploreNebula(query.value)
    if (res.code === 200) {
      searchResult.value = res.data
    }
  } catch (error) {
    console.error(error)
  } finally {
    isSearching.value = false
    hasSearched.value = true
  }
}
</script>

<style scoped>
.explore-container { display: flex; flex-direction: column; height: 100%; gap: 20px; }

.explore-header { display: flex; align-items: center; gap: 20px; padding: 15px 30px; border-radius: 16px; }
.icon-btn { padding: 8px 15px; border: 1px solid rgba(0,0,0,0.05); }
.search-bar { flex: 1; display: flex; align-items: center; gap: 15px; background: rgba(255,255,255,0.6); padding: 5px 5px 5px 20px; border-radius: 50px; border: 1px solid rgba(0,0,0,0.05); }
.search-bar input { flex: 1; border: none; background: transparent; outline: none; font-size: 16px; }
.search-bar .btn { border-radius: 50px; padding: 10px 30px; }

.explore-main { flex: 1; display: flex; gap: 20px; overflow: hidden; }

/* 左侧结果区 */
.left-results { flex: 1; overflow-y: auto; display: flex; flex-direction: column; gap: 20px; padding-right: 10px; }

.loading-state { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 300px; gap: 20px; border-radius: 16px; }
.spinner { width: 40px; height: 40px; border: 4px solid rgba(168, 85, 247, 0.2); border-top-color: var(--accent); border-radius: 50%; animation: spin 1s linear infinite; }

.ai-summary-card { padding: 30px; border-radius: 16px; background: linear-gradient(135deg, rgba(255,255,255,0.8), rgba(243,232,255,0.6)); border: 1px solid rgba(192,132,252,0.3); }
.ai-summary-card h3 { font-size: 18px; color: #7e22ce; margin-bottom: 15px; display: flex; align-items: center; gap: 8px; }
.ai-summary-card p { font-size: 15px; line-height: 1.6; color: var(--text-main); margin-bottom: 15px; }
.ai-summary-card ul { padding-left: 20px; font-size: 14px; color: var(--text-sub); line-height: 1.8; margin-bottom: 20px; }
.ai-summary-card strong { color: var(--text-main); }
.ai-actions { display: flex; gap: 15px; }
.small-btn { padding: 6px 15px; font-size: 13px; border-radius: 8px; border: 1px solid rgba(192,132,252,0.4); color: #7e22ce; background: #fff; }
.small-btn:hover { background: #f3e8ff; }

.section-title { font-size: 18px; font-weight: 700; margin: 10px 0; }

.resource-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 20px; }
.res-card { border-radius: 16px; overflow: hidden; cursor: pointer; transition: 0.2s; border: 1px solid transparent; }
.res-card:hover { transform: translateY(-5px); border-color: var(--accent); box-shadow: 0 10px 20px rgba(0,0,0,0.05); }
.card-cover { height: 120px; display: flex; align-items: center; justify-content: center; font-size: 40px; }
.doc-cover { background: linear-gradient(135deg, #f0f9ff, #e0f2fe); }
.video-cover { background: linear-gradient(135deg, #1e293b, #334155); }
.quiz-cover { background: linear-gradient(135deg, #fef2f2, #fee2e2); }
.res-card .info { padding: 15px; background: rgba(255,255,255,0.5); }
.res-card h4 { font-size: 14px; margin-bottom: 5px; line-height: 1.4; }
.res-card p { font-size: 12px; color: var(--text-sub); }

/* 右侧知识图谱 */
.right-knowledge-graph { flex: 0 0 300px; border-radius: 16px; padding: 25px; display: flex; flex-direction: column; gap: 20px; }
.panel-title { font-size: 16px; font-weight: 700; border-bottom: 1px solid rgba(0,0,0,0.05); padding-bottom: 15px; }

.graph-placeholder { flex: 1; position: relative; background: rgba(0,0,0,0.02); border-radius: 12px; overflow: hidden; min-height: 250px; }
.node { position: absolute; padding: 8px 15px; border-radius: 50px; font-size: 12px; font-weight: 600; transform: translate(-50%, -50%); z-index: 2; box-shadow: 0 4px 10px rgba(0,0,0,0.05); cursor: pointer; transition: 0.2s; }
.node:hover { transform: translate(-50%, -50%) scale(1.1); }
.main-node { top: 50%; left: 50%; background: var(--accent); color: #fff; font-size: 14px; padding: 10px 20px; }
.sub-node { background: #fff; color: var(--text-main); border: 1px solid rgba(0,0,0,0.05); }
.node-1 { top: 20%; left: 20%; }
.node-2 { top: 20%; left: 80%; }
.node-3 { top: 80%; left: 50%; }
.lines { position: absolute; inset: 0; width: 100%; height: 100%; pointer-events: none; z-index: 1; }

.related-queries h5 { font-size: 13px; margin-bottom: 10px; color: var(--text-sub); }
.related-queries .tag { display: inline-block; background: rgba(255,255,255,0.6); border: 1px solid rgba(0,0,0,0.05); padding: 6px 12px; border-radius: 50px; font-size: 12px; margin: 0 8px 8px 0; cursor: pointer; transition: 0.2s; }
.related-queries .tag:hover { background: var(--accent); color: #fff; }

@keyframes spin { to { transform: rotate(360deg); } }
</style>
