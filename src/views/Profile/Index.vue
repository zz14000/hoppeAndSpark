<template>
  <div class="profile-container">
    <!-- 左侧菜单栏 -->
    <aside class="sidebar glass-panel">
      <ul class="profile-menu">
        <li :class="{ active: currentTab === 'info' }" @click="currentTab = 'info'">个人资料</li>
        <li :class="{ active: currentTab === 'security' }" @click="currentTab = 'security'">账号设置</li>
        <li :class="{ active: currentTab === 'stats' }" @click="currentTab = 'stats'">学习数据统计</li>
        <div class="divider"></div>
        <li :class="{ active: currentTab === 'blog' }" @click="currentTab = 'blog'">我的博客与代码</li>
        <li :class="{ active: currentTab === 'collection' }" @click="currentTab = 'collection'">我的收藏</li>
      </ul>
    </aside>

    <!-- 右侧内容渲染区 -->
    <main class="main-content">
      
      <!-- 统一的顶部横幅 -->
      <div class="profile-banner glass-panel" v-if="userProfile">
        <img src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix" alt="avatar" class="avatar-img">
        <div class="profile-info">
          <h1>{{ userProfile.nickname }} 🥇</h1>
          <p>IP 属地: {{ userProfile.location }} | 社区星火值: 1500</p>
          <div class="profile-stats">
            <div><span>6,047</span>访问量</div>
            <div><span>{{ userProfile.stats?.articles ?? 0 }}</span>原创</div>
            <div><span>{{ userProfile.stats?.followers ?? 0 }}</span>粉丝</div>
          </div>
        </div>
        <button class="btn" style="margin-left: auto; align-self: flex-start;">编辑主页</button>
      </div>

      <!-- 面板 1：学习数据统计 -->
      <div v-show="currentTab === 'stats'" class="panel-section glass-panel">
        <h2>📈 学习数据洞察</h2>
        <div class="stats-grid">
          <div class="stat-card">
            <span class="label">累计学习总时长</span>
            <span class="value">1,248<span class="unit">h</span></span>
            <span class="sub-label">超越了 85% 的社区创作者</span>
          </div>
          <div class="stat-card">
            <span class="label">连续打卡天数</span>
            <span class="value text-red">42<span class="unit">天</span></span>
            <span class="sub-label">保持你的学习热情！🔥</span>
          </div>
          <div class="stat-card">
            <span class="label">资源生成采纳率</span>
            <span class="value text-green">78%</span>
            <span class="sub-label">Nebula 引擎共生成 152 次</span>
          </div>
        </div>

        <h3 style="margin-top: 30px; margin-bottom: 15px;">🎯 学习计划进度</h3>
        <div class="plan-item">
          <div>
            <h4>100天算法刷题冲刺计划</h4>
            <p>当前阶段：动态规划 (Day 45) | 已刷题数：120/300</p>
            <div class="progress-bar-bg"><div class="progress-bar-fill" style="width: 45%;"></div></div>
          </div>
          <button class="btn">继续学习</button>
        </div>
      </div>

      <!-- 面板 2：我的博客 -->
      <div v-show="currentTab === 'blog'" class="panel-section glass-panel">
        <div style="display:flex; justify-content:space-between; margin-bottom:20px;">
          <h2>📚 我的创作库</h2>
          <button class="btn btn-primary">+ 发布新内容</button>
        </div>
        <div class="blog-item" v-for="i in [1,2]" :key="i">
          <div class="blog-info">
            <h4>【开源项目】基于 AI 的全栈式模拟面试与简历优化平台</h4>
            <p>集成视频模拟面试、AI简历诊断与在线编辑、内容驱动的博客社区以及WebSocket...</p>
            <div class="blog-meta">
              <span class="tag original">原创</span> 2025.11.22 · 1426 阅读 · 35 点赞
            </div>
          </div>
        </div>
      </div>

      <!-- 其他面板可用 v-show 类似扩展... -->
      <div v-show="currentTab === 'info'" class="panel-section glass-panel" v-if="userProfile">
        <h2>👤 个人资料</h2>
        <div class="form-group">
          <label>昵称</label>
          <input type="text" class="input-field" :value="userProfile.nickname">
        </div>
        <div class="form-group">
          <label>个人简介</label>
          <textarea class="input-field" rows="3">{{ userProfile.bio }}</textarea>
        </div>
        <div class="form-group">
          <label>学习阶段</label>
          <select class="input-field">
            <option>高中生</option>
            <option selected>大学生</option>
            <option>职场新人</option>
            <option>资深开发者</option>
          </select>
        </div>
        <button class="btn btn-primary" style="margin-top: 10px;">保存修改</button>
      </div>

      <div v-show="currentTab === 'security'" class="panel-section glass-panel">
        <h2>🛡️ 账号设置</h2>
        <div class="security-list">
          <div class="security-item">
            <div>
              <h4>登录密码</h4>
              <p>定期修改密码有助于保护账号安全</p>
            </div>
            <button class="btn">修改密码</button>
          </div>
          <div class="security-item">
            <div>
              <h4>绑定邮箱</h4>
              <p>已绑定：user***@example.com</p>
            </div>
            <button class="btn">更换邮箱</button>
          </div>
          <div class="security-item">
            <div>
              <h4>设备管理</h4>
              <p>管理登录过该账号的设备</p>
            </div>
            <button class="btn">查看设备</button>
          </div>
        </div>
      </div>

      <div v-show="currentTab === 'collection'" class="panel-section glass-panel">
        <h2>⭐ 我的收藏</h2>
        <div class="collection-list">
          <div class="collection-item">
            <div class="icon-wrap" style="background: rgba(186,230,253,0.5); color: #0369a1;">▶</div>
            <div class="info">
              <h4>红黑树与 AVL 树的本质区别 (视频)</h4>
              <p>收藏于 2025.10.12</p>
            </div>
            <button class="btn">取消收藏</button>
          </div>
          <div class="collection-item">
            <div class="icon-wrap" style="background: rgba(233,213,255,0.5); color: #7e22ce;">📄</div>
            <div class="info">
              <h4>高并发架构设计核心指南</h4>
              <p>收藏于 2025.09.28</p>
            </div>
            <button class="btn">取消收藏</button>
          </div>
        </div>
      </div>

    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCurrentUser } from '../../api/index.js'

const currentTab = ref('stats') // 默认选中数据统计
const userProfile = ref(null)

onMounted(async () => {
  const res = await getCurrentUser()
  if (res.code === 200) {
    userProfile.value = res.data
  }
})
</script>

<style scoped>
.profile-container { display: grid; grid-template-columns: 240px 1fr; gap: 20px; height: 100%; }

/* 侧边栏 */
.sidebar { padding: 15px 0; }
.profile-menu { list-style: none; }
.profile-menu li { padding: 15px 25px; cursor: pointer; transition: 0.3s; color: var(--text-main); font-size: 14px; position: relative; }
.profile-menu li:hover { background: rgba(255,255,255,0.4); }
.profile-menu li.active { color: var(--accent); background: rgba(255,255,255,0.65); font-weight: 700; }
.profile-menu li.active::before { content: ''; position: absolute; left: 0; top: 0; bottom: 0; width: 4px; background: var(--accent); border-radius: 0 4px 4px 0; }
.divider { height: 1px; background: rgba(0,0,0,0.05); margin: 10px 0; }

/* 主区域 */
.main-content { display: flex; flex-direction: column; gap: 20px; overflow-y: auto; padding-right: 10px; }

/* 顶部 Banner */
.profile-banner { padding: 30px; display: flex; align-items: center; gap: 30px; }
.avatar-img { width: 90px; height: 90px; border-radius: 50%; border: 4px solid #fff; box-shadow: 0 8px 20px rgba(0,0,0,0.1); }
.profile-info h1 { font-size: 24px; margin-bottom: 5px; }
.profile-info p { color: var(--text-sub); font-size: 13px; margin-bottom: 15px; }
.profile-stats { display: flex; gap: 25px; font-size: 13px; color: var(--text-sub); }
.profile-stats span { font-weight: bold; color: var(--text-main); font-size: 18px; margin-right: 5px; }

/* 通用面板内容 */
.panel-section { padding: 30px; }
.panel-section h2 { font-size: 20px; margin-bottom: 20px; }
.flex-center { display: flex; flex-direction: column; align-items: center; justify-content: center; min-height: 300px; }

/* 数据统计 Grid */
.stats-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
.stat-card { background: rgba(255,255,255,0.3); padding: 25px; border-radius: 16px; border: 1px solid rgba(255,255,255,0.6); display: flex; flex-direction: column; align-items: center; text-align: center; }
.stat-card .label { font-size: 14px; font-weight: 600; color: var(--text-main); }
.stat-card .value { font-size: 32px; font-weight: 700; color: var(--accent); margin: 10px 0; font-family: monospace; }
.stat-card .unit { font-size: 16px; color: var(--text-sub); margin-left: 4px; }
.stat-card .sub-label { font-size: 12px; color: var(--text-sub); }
.text-red { color: #ff4757 !important; }
.text-green { color: #10b981 !important; }

/* 计划条目 */
.plan-item { display: flex; align-items: center; justify-content: space-between; padding: 20px; background: rgba(255,255,255,0.3); border: 1px solid rgba(255,255,255,0.5); border-radius: 16px; }
.plan-item h4 { margin-bottom: 8px; font-size: 16px; }
.plan-item p { font-size: 13px; color: var(--text-sub); margin-bottom: 12px; }
.progress-bar-bg { width: 300px; height: 8px; background: rgba(0,0,0,0.05); border-radius: 10px; overflow: hidden; }
.progress-bar-fill { height: 100%; background: linear-gradient(90deg, #38bdf8, #0071e3); border-radius: 10px; }

/* 博客条目 */
.blog-item { padding: 20px 0; border-bottom: 1px solid rgba(0,0,0,0.05); }
.blog-info h4 { font-size: 16px; margin-bottom: 10px; cursor: pointer; transition: 0.2s; }
.blog-info h4:hover { color: var(--accent); }
.blog-info p { font-size: 13px; color: var(--text-sub); margin-bottom: 12px; line-height: 1.5; }
.blog-meta { font-size: 12px; color: var(--text-muted); display: flex; align-items: center; gap: 10px; }
.tag.original { background: rgba(225,112,85,0.1); color: #e17055; padding: 2px 8px; border-radius: 4px; font-weight: 600; }

/* 个人资料表单 */
.form-group { margin-bottom: 20px; display: flex; flex-direction: column; gap: 8px; }
.form-group label { font-size: 14px; font-weight: 600; color: var(--text-main); }
.input-field { padding: 12px 15px; border-radius: 12px; border: 1px solid rgba(0,0,0,0.1); background: rgba(255,255,255,0.6); outline: none; font-size: 14px; font-family: inherit; transition: 0.2s; }
.input-field:focus { border-color: var(--accent); background: #fff; }

/* 账号设置列表 */
.security-list { display: flex; flex-direction: column; gap: 15px; }
.security-item { display: flex; justify-content: space-between; align-items: center; padding: 20px; background: rgba(255,255,255,0.4); border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); }
.security-item h4 { font-size: 16px; margin-bottom: 5px; }
.security-item p { font-size: 13px; color: var(--text-sub); }

/* 收藏列表 */
.collection-list { display: flex; flex-direction: column; gap: 15px; }
.collection-item { display: flex; align-items: center; gap: 15px; padding: 15px 20px; background: rgba(255,255,255,0.4); border-radius: 16px; border: 1px solid rgba(0,0,0,0.05); cursor: pointer; transition: 0.2s; }
.collection-item:hover { background: rgba(255,255,255,0.7); transform: translateX(5px); }
.icon-wrap { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 18px; }
.collection-item .info { flex: 1; }
.collection-item h4 { font-size: 15px; margin-bottom: 5px; }
.collection-item p { font-size: 12px; color: var(--text-sub); }
</style>