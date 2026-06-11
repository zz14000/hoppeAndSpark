<template>
  <div class="video-container">
    <div class="main-video-area">
      <!-- 播放器模拟区 -->
      <div class="player">
        <div class="visual">
          <div class="play-btn"></div>
        </div>
        <div class="player-controls">
          <span class="time-text">04:21</span>
          <div class="progress-bar"><div></div></div>
          <span class="time-text">12:30</span>
          <div class="ctrl-btns">
            <button class="ctrl-btn">📺 选集</button>
            <button class="ctrl-btn">⏩ 1.0x</button>
            <button class="ctrl-btn">⛶ 全屏</button>
          </div>
        </div>
      </div>

      <!-- 视频信息条 -->
      <div class="video-info-bar glass-panel">
        <div class="left">
          <h2>BST 二叉搜索树 · 概念入门</h2>
          <div class="meta">
            <span class="pill">▶ 12:30</span>
            <span class="pill">👁 8.2k 观看</span>
            <span class="pill accent-pill">第 6 章 · 第 1 节</span>
          </div>
        </div>
        <div class="action-btns">
          <button class="btn">👍 1.2k</button>
          <button class="btn btn-primary" @click="$router.push('/app/topology')">🕸️ 返回资源网络</button>
        </div>
      </div>

      <!-- 评论区简版 -->
      <div class="comment-section glass-panel">
        <h3 style="margin-bottom: 15px;">💭 评论区</h3>
        <div class="input-row">
          <div class="avatar">我</div>
          <input type="text" class="comment-input" placeholder="💬 写下你的看法或学习笔记...">
          <button class="btn btn-primary">发送</button>
        </div>
      </div>
    </div>

    <!-- 右侧选集与笔记 -->
    <aside class="side-panel glass-panel">
      <div class="episode-list">
        <div class="panel-title">📺 课程章节 <span>第 6 章</span></div>
        <div class="episode-item playing">
          <div class="episode-num">1</div>
          <div class="info">
            <div class="title">BST 概念入门</div>
            <div class="meta">12:30 · 进行中</div>
          </div>
        </div>
        <div class="episode-item">
          <div class="episode-num">2</div>
          <div class="info">
            <div class="title">插入与删除可视化</div>
            <div class="meta">15:08 · 未观看</div>
          </div>
        </div>
      </div>

      <div class="ai-tip glass-panel">
        <h4>✨ AI 学习提示</h4>
        <p>本节核心：理解 BST 的 <b>有序性</b> 与 <b>递归结构</b>。看完后建议做 1 道练习题验证。</p>
      </div>
    </aside>
  </div>
</template>

<style scoped>
.video-container { display: flex; height: 100%; gap: 30px; }
.main-video-area { flex: 1; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; padding-right: 10px; }

.player { background: linear-gradient(135deg, #1e293b, #334155); border-radius: 24px; position: relative; width: 100%; padding-bottom: 56.25%; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.1); }
.visual { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; background: radial-gradient(circle at 30% 30%, rgba(192,132,252,0.3), transparent 50%); }
.play-btn { width: 80px; height: 80px; border-radius: 50%; background: rgba(255,255,255,0.9); display: flex; align-items: center; justify-content: center; cursor: pointer; }
.play-btn::after { content: ''; border-style: solid; border-width: 14px 0 14px 22px; border-color: transparent transparent transparent #1d1d1f; margin-left: 6px; }

.player-controls { position: absolute; bottom: 0; left: 0; right: 0; padding: 20px; background: linear-gradient(0deg, rgba(0,0,0,0.6), transparent); display: flex; align-items: center; gap: 15px; }
.progress-bar { flex: 1; height: 4px; background: rgba(255,255,255,0.3); border-radius: 2px; }
.progress-bar > div { width: 35%; height: 100%; background: var(--accent); border-radius: 2px; position: relative; }
.progress-bar > div::after { content: ''; position: absolute; right: -6px; top: -4px; width: 12px; height: 12px; background: #fff; border-radius: 50%; }
.time-text { color: #fff; font-size: 13px; font-family: monospace; }
.ctrl-btns { display: flex; gap: 10px; }
.ctrl-btn { background: rgba(255,255,255,0.2); color: #fff; border: none; padding: 6px 12px; border-radius: 50px; cursor: pointer; font-size: 13px; }

.video-info-bar { padding: 20px 30px; display: flex; justify-content: space-between; align-items: center; }
.video-info-bar h2 { font-size: 20px; margin-bottom: 8px; }
.meta { display: flex; gap: 10px; font-size: 12px; color: var(--text-sub); }
.pill { background: rgba(0,0,0,0.05); padding: 4px 12px; border-radius: 50px; }
.accent-pill { background: rgba(186,230,253,0.5); color: #0369a1; }
.action-btns { display: flex; gap: 15px; }

.comment-section { padding: 30px; }
.input-row { display: flex; gap: 15px; align-items: center; }
.avatar { width: 40px; height: 40px; border-radius: 50%; background: var(--accent); color: #fff; display: flex; align-items: center; justify-content: center; }
.comment-input { flex: 1; background: rgba(255,255,255,0.5); border: 1px solid rgba(0,0,0,0.05); border-radius: 50px; padding: 12px 20px; outline: none; font-size: 14px; }

/* 右侧侧边栏 */
.side-panel { flex: 0 0 350px; display: flex; flex-direction: column; overflow: hidden; padding: 0; }
.tabs { display: flex; border-bottom: 1px solid rgba(0,0,0,0.05); background: rgba(255,255,255,0.4); }
.tab { flex: 1; text-align: center; padding: 15px 0; font-size: 14px; font-weight: 600; color: var(--text-sub); cursor: pointer; transition: 0.2s; border-bottom: 2px solid transparent; }
.tab:hover { background: rgba(255,255,255,0.6); }
.tab.active { color: var(--accent); border-bottom-color: var(--accent); background: #fff; }

.panel-content { flex: 1; overflow-y: auto; padding: 15px; }

/* 智能字幕 */
.transcript-list { display: flex; flex-direction: column; gap: 10px; }
.transcript-item { display: flex; gap: 12px; padding: 12px; border-radius: 12px; cursor: pointer; transition: 0.2s; border: 1px solid transparent; }
.transcript-item:hover { background: rgba(255,255,255,0.6); }
.transcript-item.passed { opacity: 0.6; }
.transcript-item.active { background: #fff; border-color: rgba(10,132,255,0.2); box-shadow: 0 4px 15px rgba(0,0,0,0.03); opacity: 1; }
.transcript-item .time { font-family: monospace; font-size: 12px; color: var(--accent); font-weight: 600; padding-top: 2px; }
.transcript-item p { font-size: 13px; line-height: 1.6; color: var(--text-main); margin: 0; }
.ai-insight { margin-top: 10px; font-size: 12px; background: rgba(147,51,234,0.05); color: #7e22ce; padding: 8px 12px; border-radius: 8px; border-left: 3px solid #a855f7; line-height: 1.5; }
.ai-insight .icon { font-weight: 700; margin-right: 4px; }

/* 剧集列表 */
.episodes-list { display: flex; flex-direction: column; gap: 5px; }
.ep-item { display: flex; align-items: center; gap: 15px; padding: 15px; border-radius: 12px; cursor: pointer; transition: 0.2s; border: 1px solid transparent; }
.ep-item:hover { background: rgba(255,255,255,0.6); }
.ep-item.active { background: rgba(10,132,255,0.05); border-color: rgba(10,132,255,0.1); }
.ep-item .num { font-size: 16px; font-weight: 800; color: var(--text-muted); width: 20px; text-align: center; }
.ep-item.active .num { color: var(--accent); }
.ep-item .info { flex: 1; }
.ep-item h4 { font-size: 14px; margin-bottom: 4px; }
.ep-item .duration { font-size: 12px; color: var(--text-sub); }
.status { font-size: 12px; color: var(--accent); font-weight: 600; }

/* 讨论区 */
.comments-list { display: flex; flex-direction: column; gap: 20px; }
.comment-input { display: flex; gap: 10px; margin-bottom: 10px; }
.comment-input input { flex: 1; padding: 10px 15px; border-radius: 50px; border: 1px solid rgba(0,0,0,0.1); outline: none; font-size: 13px; }
.comment-input .btn { border-radius: 50px; padding: 0 15px; }
.comment-item { display: flex; gap: 12px; }
.comment-item .avatar { width: 32px; height: 32px; border-radius: 50%; background: var(--accent); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: 700; flex-shrink: 0; }
.comment-item .info { flex: 1; }
.comment-item .top { display: flex; justify-content: space-between; margin-bottom: 4px; }
.comment-item .name { font-size: 13px; font-weight: 600; }
.comment-item .time { font-size: 11px; color: var(--text-muted); }
.comment-item p { font-size: 13px; line-height: 1.5; color: var(--text-sub); }
</style>