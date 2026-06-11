<template>
  <div class="article-page">
    <!-- 左侧悬浮操作栏 -->
    <aside class="action-sidebar">
      <div class="action-btn" title="点赞">
        <span class="icon">👍</span>
        <span class="count">342</span>
      </div>
      <div class="action-btn" title="评论">
        <span class="icon">💬</span>
        <span class="count">56</span>
      </div>
      <div class="action-btn" title="收藏">
        <span class="icon">⭐</span>
        <span class="count">128</span>
      </div>
      <div class="action-btn" title="分享">
        <span class="icon">🔗</span>
      </div>
    </aside>

    <!-- 中间文章主体 -->
    <main class="article-main glass-panel">
      <button class="back-btn" @click="$router.push('/app/community')">← 返回社区</button>
      
      <header class="article-header" v-if="article">
        <h1>{{ article.title }}</h1>
        <div class="meta-info">
          <div class="author-basic">
            <div class="avatar">{{ (article.author || '?')[0] }}</div>
            <span class="name">{{ article.author }}</span>
          </div>
          <span class="time">{{ article.time || article.publishedAt }}</span>
          <span class="views">阅读 {{ article.readCount }}</span>
          <div class="tags">
            <span class="tag" v-for="tag in article.tags" :key="tag">{{ tag }}</span>
          </div>
        </div>
      </header>
      <div v-else-if="loading" class="loading-hint">加载文章中...</div>

      <!-- AI 摘要卡片 -->
      <div class="ai-summary glass-panel">
        <div class="summary-header">
          <span class="icon">✨</span> <strong>Nebula 引擎智能摘要</strong>
        </div>
        <p>本文通过 300 行代码还原了 Vue3 的 Proxy 响应式原理，重点讲解了依赖收集(track)和派发更新(trigger)的底层逻辑。相比 Vue2 的 Object.defineProperty，Proxy 提供了更全面的拦截能力。文章最后还附带了简单的 effect 函数实现，适合前端进阶阅读。</p>
      </div>

      <article class="article-body">
        <h2>1. 为什么要重写响应式系统？</h2>
        <p>在 Vue2 中，响应式系统核心是 <code>Object.defineProperty</code>，但它有很多局限性：无法检测对象属性的添加或删除，也无法直接监听数组的索引变化和长度变化。</p>
        <p>Vue3 引入了 ES6 的 <code>Proxy</code> 对象，它可以直接代理整个对象，拦截对对象的各种操作（如 get, set, deleteProperty 等），从而完美解决了 Vue2 的痛点。</p>

        <h2>2. 核心 API：reactive 与 effect</h2>
        <p>响应式系统的本质是：当数据读取时收集依赖，当数据修改时触发更新。我们用 <code>track</code> 函数收集依赖，用 <code>trigger</code> 函数派发更新。</p>
        
        <div class="code-block">
          <div class="code-header">
            <span>javascript</span>
            <button class="copy-btn">复制</button>
          </div>
          <pre><code><span class="keyword">const</span> targetMap = <span class="keyword">new</span> <span class="function">WeakMap</span>();

<span class="keyword">function</span> <span class="function">track</span>(target, key) {
  <span class="keyword">if</span> (!activeEffect) <span class="keyword">return</span>;
  <span class="keyword">let</span> depsMap = targetMap.<span class="function">get</span>(target);
  <span class="keyword">if</span> (!depsMap) {
    targetMap.<span class="function">set</span>(target, (depsMap = <span class="keyword">new</span> <span class="function">Map</span>()));
  }
  <span class="keyword">let</span> dep = depsMap.<span class="function">get</span>(key);
  <span class="keyword">if</span> (!dep) {
    depsMap.<span class="function">set</span>(key, (dep = <span class="keyword">new</span> <span class="function">Set</span>()));
  }
  dep.<span class="function">add</span>(activeEffect);
}</code></pre>
        </div>

        <h2>3. 总结</h2>
        <p>通过 Proxy，我们能够优雅地拦截对象的行为。但这只是 Vue3 响应式的冰山一角，完整的系统还包含 ref、computed、readonly 等复杂的边界处理。</p>
      </article>

      <hr class="divider">

      <section class="comments-section">
        <h3>全部评论 (56)</h3>
        <div class="comment-input-area">
          <div class="avatar me">我</div>
          <div class="input-wrapper">
            <textarea placeholder="输入你的评论... 支持 Markdown"></textarea>
            <div class="input-footer">
              <button class="btn btn-primary">发表评论</button>
            </div>
          </div>
        </div>
        
        <div class="comment-list">
          <div class="comment-item">
            <div class="avatar">李</div>
            <div class="comment-content">
              <div class="c-header">
                <span class="name">架构师老李</span>
                <span class="time">2小时前</span>
              </div>
              <p>写得很通透！特别是 WeakMap 那里，如果不强调垃圾回收机制，很多人容易忽视内存泄漏的问题。</p>
              <div class="c-actions">
                <span>👍 12</span>
                <span>回复</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 右侧作者与目录 -->
    <aside class="right-sidebar">
      <div class="author-card glass-panel">
        <div class="author-header">
          <div class="avatar large">一</div>
          <div class="info">
            <div class="name">一粒黑子</div>
            <div class="desc">全栈开发，热衷分享。目前在死磕底层源码。</div>
          </div>
        </div>
        <div class="author-stats">
          <div class="stat"><span class="val">42</span><span class="lbl">文章</span></div>
          <div class="stat"><span class="val">12.5k</span><span class="lbl">阅读</span></div>
          <div class="stat"><span class="val">3.2k</span><span class="lbl">粉丝</span></div>
        </div>
        <button class="btn btn-primary follow-btn">关注作者</button>
      </div>

      <div class="toc-card glass-panel">
        <h4 class="toc-title">目录</h4>
        <ul class="toc-list">
          <li class="active">1. 为什么要重写响应式系统？</li>
          <li>2. 核心 API：reactive 与 effect</li>
          <li>3. 总结</li>
        </ul>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getArticleDetail } from '../../api/index.js'

const route = useRoute()
const article = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getArticleDetail(route.params.id)
    if (res.code === 200) article.value = res.data
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.article-page { display: flex; gap: 25px; height: 100%; position: relative; }

/* 左侧操作栏 */
.action-sidebar { flex: 0 0 60px; display: flex; flex-direction: column; gap: 15px; align-items: center; padding-top: 60px; }
.action-btn { width: 48px; height: 48px; background: rgba(255,255,255,0.6); border-radius: 50%; display: flex; flex-direction: column; align-items: center; justify-content: center; cursor: pointer; transition: 0.2s; box-shadow: 0 4px 10px rgba(0,0,0,0.03); color: var(--text-sub); border: 1px solid rgba(255,255,255,0.8); }
.action-btn:hover { background: #fff; transform: translateY(-3px); color: var(--accent); box-shadow: 0 8px 20px rgba(0,0,0,0.06); }
.action-btn .icon { font-size: 18px; margin-bottom: 2px; }
.action-btn .count { font-size: 11px; font-weight: 600; }

/* 中间主内容 */
.article-main { flex: 1; overflow-y: auto; padding: 40px 60px; }
.back-btn { background: transparent; border: none; color: var(--text-sub); font-size: 14px; cursor: pointer; margin-bottom: 30px; font-weight: 600; padding: 0; }
.back-btn:hover { color: var(--accent); }

.article-header { margin-bottom: 30px; }
.article-header h1 { font-size: 32px; font-weight: 800; line-height: 1.4; margin-bottom: 20px; color: var(--text-main); }
.meta-info { display: flex; align-items: center; gap: 20px; font-size: 13px; color: var(--text-sub); flex-wrap: wrap; }
.author-basic { display: flex; align-items: center; gap: 8px; font-weight: 600; color: var(--text-main); }
.avatar { width: 28px; height: 28px; border-radius: 50%; background: linear-gradient(135deg, #38bdf8, #c084fc); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 12px; flex-shrink: 0; }
.tags { display: flex; gap: 8px; }
.tag { background: rgba(0,0,0,0.04); padding: 4px 10px; border-radius: 6px; }

/* AI 摘要 */
.ai-summary { padding: 20px; border-radius: 16px; background: linear-gradient(135deg, rgba(233,213,255,0.4), rgba(186,230,253,0.3)); border: 1px solid rgba(233,213,255,0.6); margin-bottom: 40px; }
.summary-header { margin-bottom: 10px; font-size: 15px; color: #7e22ce; display: flex; align-items: center; gap: 8px; }
.ai-summary p { font-size: 14px; line-height: 1.6; color: #334155; margin: 0; }

/* 正文 */
.article-body { font-size: 16px; line-height: 1.8; color: #334155; }
.article-body p { margin-bottom: 20px; }
.article-body h2 { font-size: 22px; font-weight: 700; margin: 40px 0 20px; color: var(--text-main); }
.article-body code { background: rgba(0,0,0,0.05); padding: 2px 6px; border-radius: 4px; font-family: monospace; color: #ef4444; font-size: 14px; }

/* 代码块 */
.code-block { background: #1e293b; border-radius: 12px; overflow: hidden; margin: 25px 0; }
.code-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 15px; background: rgba(255,255,255,0.05); color: #94a3b8; font-size: 12px; font-family: monospace; }
.copy-btn { background: transparent; border: none; color: #94a3b8; cursor: pointer; }
.copy-btn:hover { color: #fff; }
.code-block pre { padding: 15px; margin: 0; overflow-x: auto; }
.code-block pre code { background: transparent; padding: 0; color: #e2e8f0; font-size: 14px; }
.keyword { color: #c678dd; }
.function { color: #61afef; }

/* 评论区 */
.divider { border: none; border-top: 1px solid rgba(0,0,0,0.05); margin: 50px 0 30px; }
.comments-section h3 { font-size: 20px; font-weight: 700; margin-bottom: 25px; }
.comment-input-area { display: flex; gap: 15px; margin-bottom: 40px; }
.avatar.me { background: var(--accent); width: 40px; height: 40px; font-size: 14px; }
.input-wrapper { flex: 1; background: rgba(255,255,255,0.6); border-radius: 12px; border: 1px solid rgba(0,0,0,0.05); overflow: hidden; }
.input-wrapper textarea { width: 100%; height: 100px; padding: 15px; border: none; outline: none; background: transparent; resize: none; font-size: 14px; font-family: inherit; }
.input-footer { padding: 10px 15px; border-top: 1px solid rgba(0,0,0,0.05); display: flex; justify-content: flex-end; background: rgba(255,255,255,0.8); }
.input-footer .btn { padding: 6px 16px; border-radius: 8px; font-size: 13px; }

.comment-list { display: flex; flex-direction: column; gap: 25px; }
.comment-item { display: flex; gap: 15px; }
.comment-item .avatar { width: 36px; height: 36px; }
.comment-content { flex: 1; }
.c-header { display: flex; align-items: center; gap: 10px; margin-bottom: 5px; }
.c-header .name { font-weight: 600; font-size: 14px; color: var(--text-main); }
.c-header .time { font-size: 12px; color: var(--text-muted); }
.comment-content p { font-size: 14px; line-height: 1.6; color: #334155; margin-bottom: 10px; }
.c-actions { display: flex; gap: 15px; font-size: 12px; color: var(--text-sub); }
.c-actions span { cursor: pointer; transition: 0.2s; }
.c-actions span:hover { color: var(--accent); }

/* 右侧边栏 */
.right-sidebar { flex: 0 0 280px; display: flex; flex-direction: column; gap: 20px; }
.author-card { padding: 25px; border-radius: 16px; display: flex; flex-direction: column; align-items: center; text-align: center; }
.author-header { display: flex; flex-direction: column; align-items: center; gap: 15px; margin-bottom: 20px; }
.avatar.large { width: 64px; height: 64px; font-size: 24px; }
.author-header .name { font-size: 18px; font-weight: 700; margin-bottom: 5px; }
.author-header .desc { font-size: 13px; color: var(--text-sub); line-height: 1.5; }
.author-stats { display: flex; justify-content: space-around; width: 100%; margin-bottom: 20px; }
.stat { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat .val { font-size: 16px; font-weight: 700; color: var(--text-main); }
.stat .lbl { font-size: 12px; color: var(--text-sub); }
.follow-btn { width: 100%; border-radius: 50px; }

.toc-card { padding: 20px; border-radius: 16px; position: sticky; top: 20px; }
.toc-title { font-size: 16px; font-weight: 700; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.toc-list { list-style: none; display: flex; flex-direction: column; gap: 12px; }
.toc-list li { font-size: 13px; color: var(--text-sub); cursor: pointer; transition: 0.2s; line-height: 1.4; }
.toc-list li:hover { color: var(--text-main); }
.toc-list li.active { color: var(--accent); font-weight: 600; }
</style>
