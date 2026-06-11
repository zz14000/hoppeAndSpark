<template>
  <div class="user-profile-container">
    <button class="btn icon-btn back-btn" @click="$router.back()">← 返回</button>

    <header class="profile-header glass-panel">
      <div class="user-info-main">
        <div class="avatar-large">李</div>
        <div class="details">
          <h1>架构师老李 <span class="badge pro">PRO</span></h1>
          <p class="bio">深耕后端架构 10 年，开源狂热分子。目前主要研究 Rust 与分布式共识算法。</p>
          <div class="meta-tags">
            <span class="tag">📍 杭州</span>
            <span class="tag">🏢 某大厂技术专家</span>
            <span class="tag">🔗 github.com/laoli</span>
          </div>
        </div>
      </div>
      <div class="action-area">
        <div class="stats-row">
          <div class="stat-item">
            <span class="num">12.5k</span>
            <span class="lbl">关注者</span>
          </div>
          <div class="stat-item">
            <span class="num">850</span>
            <span class="lbl">获赞</span>
          </div>
          <div class="stat-item">
            <span class="num">42</span>
            <span class="lbl">文章</span>
          </div>
        </div>
        <div class="btn-group">
          <button class="btn btn-primary">关注</button>
          <button class="btn" @click="$router.push('/app/message')">私信</button>
        </div>
      </div>
    </header>

    <div class="profile-content">
      <!-- 左侧内容流 -->
      <div class="left-feed">
        <div class="tabs glass-panel">
          <div class="tab active">文章专栏</div>
          <div class="tab">开源项目</div>
          <div class="tab">学习图谱</div>
        </div>

        <div class="feed-list">
          <div class="article-card glass-panel" @click="$router.push('/app/article/2')">
            <h3>高并发场景下的分布式锁解决方案：从 Redis 到 Redisson</h3>
            <p>在微服务架构中，分布式锁是保证数据一致性的重要组件。我们团队在处理双十一高并发秒杀场景时，踩过了不少 Redis 分布式锁的坑...</p>
            <div class="card-footer">
              <span class="time">发布于 2025.10.12</span>
              <div class="actions">
                <span>👍 342</span>
                <span>💬 56</span>
              </div>
            </div>
          </div>

          <div class="article-card glass-panel">
            <h3>深入理解 Raft 共识算法的选举机制</h3>
            <p>分布式系统中如何保证多个节点的数据一致性？本文将结合动画演示，带你手写一个简易版的 Raft 选举流程...</p>
            <div class="card-footer">
              <span class="time">发布于 2025.09.28</span>
              <div class="actions">
                <span>👍 512</span>
                <span>💬 89</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧雷达图与徽章 -->
      <aside class="right-widgets">
        <div class="widget-card glass-panel">
          <h4 class="widget-title">⚔️ 技能雷达</h4>
          <div class="radar-placeholder">
            <!-- 纯 CSS 模拟的六边形雷达底图 -->
            <div class="hexagon-grid">
              <div class="hex-layer l1"></div>
              <div class="hex-layer l2"></div>
              <div class="hex-layer l3"></div>
              <div class="radar-data"></div>
            </div>
            <div class="labels">
              <span style="top: 0; left: 50%;">架构设计</span>
              <span style="top: 25%; right: 0;">分布式</span>
              <span style="bottom: 25%; right: 0;">Rust</span>
              <span style="bottom: 0; left: 50%;">算法</span>
              <span style="bottom: 25%; left: 0;">数据库</span>
              <span style="top: 25%; left: 0;">云原生</span>
            </div>
          </div>
        </div>

        <div class="widget-card glass-panel">
          <h4 class="widget-title">🏆 荣誉徽章</h4>
          <div class="badge-grid">
            <div class="badge-item" title="连续打卡 365 天">🔥</div>
            <div class="badge-item" title="解答了 1000 个问题">💡</div>
            <div class="badge-item" title="开源贡献者">💻</div>
            <div class="badge-item locked">🔒</div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.user-profile-container { display: flex; flex-direction: column; gap: 20px; height: 100%; overflow-y: auto; padding-right: 10px; }

.back-btn { margin-bottom: 5px; align-self: flex-start; background: transparent; border: none; font-weight: 600; color: var(--text-sub); }
.back-btn:hover { color: var(--accent); }

/* 头部名片 */
.profile-header { padding: 40px; border-radius: 20px; display: flex; justify-content: space-between; align-items: center; background: linear-gradient(135deg, var(--panel-bg), rgba(255,255,255,0.3)); }
.user-info-main { display: flex; align-items: center; gap: 30px; }
.avatar-large { width: 100px; height: 100px; border-radius: 24px; background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 40px; font-weight: 800; box-shadow: 0 10px 30px rgba(245, 158, 11, 0.3); }
.details h1 { font-size: 28px; font-weight: 800; margin-bottom: 10px; display: flex; align-items: center; gap: 10px; color: var(--text-main); }
.badge.pro { font-size: 12px; background: #1e293b; color: #fbbf24; padding: 2px 8px; border-radius: 6px; border: 1px solid #fbbf24; }
.bio { font-size: 15px; color: var(--text-sub); margin-bottom: 15px; max-width: 500px; line-height: 1.6; }
.meta-tags { display: flex; gap: 12px; }
.meta-tags .tag { font-size: 12px; color: var(--text-muted); background: rgba(0,0,0,0.04); padding: 4px 10px; border-radius: 6px; }

.action-area { display: flex; flex-direction: column; align-items: flex-end; gap: 20px; }
.stats-row { display: flex; gap: 30px; }
.stat-item { display: flex; flex-direction: column; align-items: center; gap: 4px; }
.stat-item .num { font-size: 20px; font-weight: 800; color: var(--text-main); }
.stat-item .lbl { font-size: 13px; color: var(--text-muted); }
.btn-group { display: flex; gap: 15px; }
.btn-group .btn { padding: 8px 30px; border-radius: 50px; font-weight: 600; }

/* 内容区 */
.profile-content { display: flex; gap: 20px; }
.left-feed { flex: 1; display: flex; flex-direction: column; gap: 20px; }
.tabs { display: flex; border-radius: 16px; overflow: hidden; }
.tab { flex: 1; text-align: center; padding: 15px 0; font-weight: 600; font-size: 15px; color: var(--text-sub); cursor: pointer; transition: 0.2s; border-bottom: 2px solid transparent; }
.tab:hover { background: rgba(255,255,255,0.5); }
.tab.active { color: var(--accent); border-bottom-color: var(--accent); background: #fff; }

.feed-list { display: flex; flex-direction: column; gap: 15px; }
.article-card { padding: 25px; border-radius: 16px; cursor: pointer; transition: 0.2s; border: 1px solid transparent; }
.article-card:hover { transform: translateY(-3px); border-color: var(--accent); box-shadow: 0 10px 30px rgba(0,0,0,0.05); }
.article-card h3 { font-size: 18px; font-weight: 700; margin-bottom: 10px; color: var(--text-main); }
.article-card p { font-size: 14px; color: var(--text-sub); line-height: 1.6; margin-bottom: 20px; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.card-footer { display: flex; justify-content: space-between; align-items: center; font-size: 13px; color: var(--text-muted); }
.card-footer .actions { display: flex; gap: 15px; }

/* 右侧小部件 */
.right-widgets { flex: 0 0 320px; display: flex; flex-direction: column; gap: 20px; }
.widget-card { padding: 25px; border-radius: 16px; }
.widget-title { font-size: 16px; font-weight: 700; margin-bottom: 20px; border-bottom: 1px solid rgba(0,0,0,0.05); padding-bottom: 10px; }

/* 纯 CSS 雷达图模拟 */
.radar-placeholder { position: relative; height: 250px; display: flex; align-items: center; justify-content: center; }
.hexagon-grid { position: relative; width: 160px; height: 160px; }
.hex-layer { position: absolute; inset: 0; clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%); border: 1px solid rgba(0,0,0,0.1); background: rgba(0,0,0,0.02); }
.l2 { transform: scale(0.66); }
.l3 { transform: scale(0.33); }
.radar-data { position: absolute; inset: 0; clip-path: polygon(50% 10%, 80% 30%, 90% 70%, 50% 80%, 20% 60%, 30% 20%); background: rgba(168, 85, 247, 0.3); border: 2px solid var(--accent); }
.labels span { position: absolute; font-size: 11px; font-weight: 600; color: var(--text-sub); transform: translate(-50%, -50%); white-space: nowrap; }

.badge-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; }
.badge-item { aspect-ratio: 1; background: rgba(255,255,255,0.6); border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 24px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); border: 1px solid rgba(0,0,0,0.05); cursor: help; transition: 0.2s; }
.badge-item:hover { transform: scale(1.1) rotate(5deg); }
.badge-item.locked { opacity: 0.3; filter: grayscale(1); background: rgba(0,0,0,0.05); }
</style>
