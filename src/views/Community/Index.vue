<template>
  <div class="community-container">
    <!-- 左侧快捷导航与标签 -->
    <aside class="sidebar glass-panel">
      <div class="nav-menu">
        <div class="menu-item active">🔥 热门沸点</div>
        <div class="menu-item">⭐ 我的关注</div>
        <div class="menu-item">🏆 创作者榜单</div>
        <div class="menu-item">📅 技术活动</div>
      </div>

      <div class="tags-section">
        <h4 class="section-title">热门领域</h4>
        <div class="tag-cloud">
          <span class="tag"># 前端架构</span>
          <span class="tag"># Rust 实践</span>
          <span class="tag"># AI 智能体</span>
          <span class="tag"># 算法刷题</span>
          <span class="tag"># 面试面经</span>
        </div>
      </div>
    </aside>

    <!-- 中间文章流 -->
    <main class="feed-main">
      <div class="feed-header glass-panel">
        <div class="tab-group">
          <div class="tab active">推荐</div>
          <div class="tab">最新</div>
          <div class="tab">问答</div>
        </div>
        <button class="btn btn-primary publish-btn" @click="$router.push('/app/publish')">✍️ 发布文章</button>
      </div>

      <div class="article-list">
        <div v-if="isLoading" class="loading-text">正在加载社区文章...</div>
        <div v-else v-for="article in articles" :key="article.id" class="article-card glass-panel" @click="$router.push('/app/article/' + article.id)">
          <div class="article-author" @click.stop="$router.push('/app/user/1')">
            <div class="avatar">{{ article.author[0] }}</div>
            <div class="author-info">
              <span class="name">{{ article.author }}</span>
              <span class="meta">{{ article.time }} · {{ article.readCount }} 阅读</span>
            </div>
            <div class="ai-badge" v-if="article.aiSummary">✨ AI 总结</div>
          </div>
          
          <div class="article-content">
            <h2 class="title">{{ article.title }}</h2>
            <p class="excerpt">{{ article.excerpt }}</p>
            <div v-if="article.aiSummary" class="ai-summary-box">
              <strong>Nebula 摘要：</strong> {{ article.aiSummary }}
            </div>
          </div>

          <div class="article-footer">
            <div class="tags">
              <span v-for="tag in article.tags" :key="tag" class="small-tag">{{ tag }}</span>
            </div>
            <div class="actions">
              <span class="action-btn">👍 {{ article.likes }}</span>
              <span class="action-btn">💬 {{ article.comments }}</span>
              <span class="action-btn">⭐ {{ article.collects }}</span>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- 右侧推荐榜单 -->
    <aside class="right-sidebar">
      <div class="leaderboard glass-panel">
        <h4 class="section-title">🏆 活跃创作者</h4>
        <div class="user-list">
          <div class="user-item" v-for="(user, i) in topUsers" :key="i">
            <div class="rank" :class="'rank-' + (i+1)">{{ i + 1 }}</div>
            <div class="avatar">{{ user.name[0] }}</div>
            <div class="user-info">
              <div class="name">{{ user.name }}</div>
              <div class="desc">{{ user.desc }}</div>
            </div>
            <button class="follow-btn">关注</button>
          </div>
        </div>
      </div>

      <div class="ad-banner glass-panel">
        <div class="ad-content">
          <h4>💡 想要提升面试通过率？</h4>
          <p>尝试进入深水区实战模拟，让 Coach 智能体为你进行压力测试！</p>
          <button class="btn btn-primary" @click="$router.push('/app/quiz')">去挑战</button>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getArticleList } from '../../api/index.js'

const articles = ref([])
const isLoading = ref(true)

onMounted(async () => {
  try {
    const res = await getArticleList()
    if (res.code === 200) {
      articles.value = res.data.list
    }
  } catch (error) {
    console.error('Failed to fetch articles:', error)
  } finally {
    isLoading.value = false
  }
})

const topUsers = ref([
  { name: '架构师老李', desc: '深耕后端架构 10 年' },
  { name: '前端小王', desc: '开源爱好者，Vue Contributor' },
  { name: '数据挖掘机', desc: 'Kaggle Master' },
  { name: '一粒黑子', desc: '全栈开发，热衷分享' }
])
</script>

<style scoped>
.community-container { display: flex; gap: 20px; height: 100%; padding-bottom: 20px; }

/* 侧边栏公用 */
.sidebar, .right-sidebar { flex: 0 0 260px; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; }

/* 左侧导航 */
.sidebar { padding: 20px; }
.nav-menu { display: flex; flex-direction: column; gap: 8px; }
.menu-item { padding: 12px 16px; border-radius: 12px; cursor: pointer; font-size: 15px; font-weight: 600; color: var(--text-main); transition: 0.2s; border: 1px solid transparent; }
.menu-item:hover { background: rgba(255,255,255,0.6); }
.menu-item.active { background: #fff; color: var(--accent); box-shadow: 0 4px 15px rgba(0,0,0,0.05); border-color: rgba(0,0,0,0.03); }

.tags-section { margin-top: 10px; }
.section-title { font-size: 14px; font-weight: 700; color: var(--text-sub); margin-bottom: 15px; }
.tag-cloud { display: flex; flex-wrap: wrap; gap: 10px; }
.tag { background: rgba(255,255,255,0.5); border: 1px solid rgba(0,0,0,0.05); padding: 6px 12px; border-radius: 50px; font-size: 12px; cursor: pointer; transition: 0.2s; }
.tag:hover { background: var(--accent); color: #fff; border-color: var(--accent); }

/* 中间主内容 */
.feed-main { flex: 1; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; padding-right: 10px; }
.feed-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 25px; border-radius: 16px; }
.tab-group { display: flex; gap: 20px; }
.tab { font-size: 16px; font-weight: 600; color: var(--text-sub); cursor: pointer; position: relative; padding-bottom: 5px; transition: 0.2s; }
.tab:hover { color: var(--text-main); }
.tab.active { color: var(--accent); }
.tab.active::after { content: ''; position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 20px; height: 3px; background: var(--accent); border-radius: 3px; }
.publish-btn { border-radius: 50px; padding: 8px 20px; font-size: 14px; }

/* 文章列表 */
.article-list { display: flex; flex-direction: column; gap: 20px; }
.article-card { padding: 25px; border-radius: 20px; transition: 0.2s; cursor: pointer; border: 1px solid rgba(255,255,255,0.6); }
.article-card:hover { box-shadow: 0 10px 30px rgba(0,0,0,0.06); transform: translateY(-2px); border-color: var(--accent); }

.article-author { display: flex; align-items: center; gap: 12px; margin-bottom: 15px; }
.avatar { width: 40px; height: 40px; border-radius: 50%; background: linear-gradient(135deg, #38bdf8, #c084fc); color: #fff; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 16px; }
.author-info { display: flex; flex-direction: column; }
.author-info .name { font-size: 14px; font-weight: 600; color: var(--text-main); }
.author-info .meta { font-size: 12px; color: var(--text-sub); }
.ai-badge { margin-left: auto; background: linear-gradient(135deg, rgba(186,230,253,0.5), rgba(233,213,255,0.5)); color: #7e22ce; font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 50px; }

.article-content .title { font-size: 18px; font-weight: 700; margin-bottom: 10px; line-height: 1.4; }
.article-content .excerpt { font-size: 14px; color: var(--text-sub); line-height: 1.6; margin-bottom: 15px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.ai-summary-box { background: rgba(255,255,255,0.5); border-left: 4px solid #c084fc; padding: 12px 15px; border-radius: 0 8px 8px 0; font-size: 13px; color: var(--text-main); margin-bottom: 15px; line-height: 1.5; }

.article-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 20px; }
.tags { display: flex; gap: 8px; }
.small-tag { background: rgba(0,0,0,0.04); padding: 4px 10px; border-radius: 6px; font-size: 12px; color: var(--text-sub); }
.actions { display: flex; gap: 15px; }
.action-btn { font-size: 13px; color: var(--text-sub); cursor: pointer; transition: 0.2s; display: flex; align-items: center; gap: 4px; }
.action-btn:hover { color: var(--accent); }

/* 右侧榜单 */
.right-sidebar { padding-right: 10px; }
.leaderboard { padding: 20px; border-radius: 20px; }
.user-list { display: flex; flex-direction: column; gap: 15px; margin-top: 15px; }
.user-item { display: flex; align-items: center; gap: 10px; }
.rank { width: 24px; font-size: 16px; font-weight: 800; color: var(--text-muted); text-align: center; font-style: italic; }
.rank-1 { color: #f59e0b; }
.rank-2 { color: #94a3b8; }
.rank-3 { color: #b45309; }
.user-item .avatar { width: 36px; height: 36px; font-size: 14px; }
.user-info { flex: 1; overflow: hidden; }
.user-info .name { font-size: 14px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.user-info .desc { font-size: 11px; color: var(--text-sub); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.follow-btn { background: rgba(10,132,255,0.1); color: var(--accent); border: none; padding: 4px 12px; border-radius: 50px; font-size: 12px; font-weight: 600; cursor: pointer; transition: 0.2s; }
.follow-btn:hover { background: var(--accent); color: #fff; }

.ad-banner { padding: 25px; border-radius: 20px; background: linear-gradient(135deg, rgba(255,255,255,0.8), rgba(255,255,255,0.4)); position: relative; overflow: hidden; }
.ad-banner::before { content:''; position: absolute; right: -30px; top: -30px; width: 100px; height: 100px; background: radial-gradient(circle, rgba(186,230,253,0.6), transparent 70%); border-radius: 50%; }
.ad-content h4 { font-size: 16px; margin-bottom: 10px; }
.ad-content p { font-size: 13px; color: var(--text-sub); line-height: 1.5; margin-bottom: 15px; }
.ad-content .btn { width: 100%; border-radius: 12px; padding: 10px; }
</style>
