<template>
  <div class="settings-container">
    <div class="settings-sidebar glass-panel">
      <h2 class="sidebar-title">偏好设置</h2>
      <ul class="settings-menu">
        <li :class="{ active: currentTab === 'general' }" @click="currentTab = 'general'">
          <span class="icon">⚙️</span> 通用设置
        </li>
        <li :class="{ active: currentTab === 'agent' }" @click="currentTab = 'agent'">
          <span class="icon">🤖</span> 智能体偏好
        </li>
        <li :class="{ active: currentTab === 'theme' }" @click="currentTab = 'theme'">
          <span class="icon">🎨</span> 外观与主题
        </li>
        <li :class="{ active: currentTab === 'notification' }" @click="currentTab = 'notification'">
          <span class="icon">🔔</span> 消息与通知
        </li>
        <li :class="{ active: currentTab === 'privacy' }" @click="currentTab = 'privacy'">
          <span class="icon">🛡️</span> 隐私与安全
        </li>
      </ul>
    </div>

    <div class="settings-main glass-panel">
      <!-- 通用设置 -->
      <div v-show="currentTab === 'general'" class="settings-section">
        <h3>通用设置</h3>
        <p class="section-desc">管理您的基本使用习惯和系统默认行为。</p>
        
        <div class="setting-item">
          <div class="info">
            <h4>语言与地区</h4>
            <p>选择界面显示的默认语言</p>
          </div>
          <select class="custom-select">
            <option>简体中文 (zh-CN)</option>
            <option>English (US)</option>
            <option>繁體中文 (zh-TW)</option>
          </select>
        </div>

        <div class="setting-item">
          <div class="info">
            <h4>开机自动启动</h4>
            <p>在系统登录时自动运行 Hope & Sparks 客户端</p>
          </div>
          <label class="switch">
            <input type="checkbox" checked>
            <span class="slider"></span>
          </label>
        </div>

        <div class="setting-item">
          <div class="info">
            <h4>缓存清理</h4>
            <p>清理本地生成的资源缓存，释放磁盘空间。当前占用：1.2GB</p>
          </div>
          <button class="btn">一键清理</button>
        </div>
      </div>

      <!-- 智能体偏好 -->
      <div v-show="currentTab === 'agent'" class="settings-section">
        <h3>🤖 智能体行为偏好</h3>
        <p class="section-desc">自定义集群中各智能体的介入频率和性格严厉程度。</p>
        
        <div class="setting-item">
          <div class="info">
            <h4>Sage 启发模式强度</h4>
            <p>决定 Sage 给出直接答案的概率。强度越高，反问与引导越深。</p>
          </div>
          <div class="range-slider-wrap">
            <span class="range-label">低</span>
            <input type="range" min="1" max="10" value="8" class="custom-range">
            <span class="range-label">高</span>
          </div>
        </div>

        <div class="setting-item">
          <div class="info">
            <h4>Coach 压力测试环境</h4>
            <p>开启后，Coach 会在练习中随机抛出突发系统故障，要求您处理异常。</p>
          </div>
          <label class="switch">
            <input type="checkbox" checked>
            <span class="slider"></span>
          </label>
        </div>

        <div class="setting-item">
          <div class="info">
            <h4>Ava 情绪干预频率</h4>
            <p>当系统检测到您长时间卡顿或情绪低落时，Ava 的主动问候频率。</p>
          </div>
          <select class="custom-select">
            <option>高频 (推荐)</option>
            <option>适中</option>
            <option>仅在严重卡顿时</option>
            <option>关闭主动干预</option>
          </select>
        </div>
        
        <div class="setting-item">
          <div class="info">
            <h4>重新构建 Spark 画像</h4>
            <p>如果觉得当前推荐的内容不准，可以重置画像并重新进行对话。</p>
          </div>
          <button class="btn btn-primary" @click="$router.push('/setup')">重新评估画像</button>
        </div>
      </div>

      <!-- 外观与主题 -->
      <div v-show="currentTab === 'theme'" class="settings-section">
        <h3>外观与主题</h3>
        <p class="section-desc">定制适合您的视觉体验。</p>
        
        <div class="setting-item theme-selection">
          <div class="theme-card" :class="{ active: appTheme === 'light' }" @click="setTheme('light')">
            <div class="theme-preview light-preview"></div>
            <span>毛玻璃 (默认)</span>
          </div>
          <div class="theme-card" :class="{ active: appTheme === 'dark' }" @click="setTheme('dark')">
            <div class="theme-preview dark-preview"></div>
            <span>深空极客 (暗色)</span>
          </div>
        </div>
        
        <div class="setting-item" style="margin-top: 20px;">
          <div class="info">
            <h4>界面动画效果</h4>
            <p>关闭后可减少眩晕感并提升系统性能。</p>
          </div>
          <label class="switch">
            <input type="checkbox" checked>
            <span class="slider"></span>
          </label>
        </div>
      </div>

      <!-- 通知 / 隐私 等其他省略展示 -->
      <div v-show="['notification', 'privacy'].includes(currentTab)" class="settings-section flex-center">
        <h2>🛠️ 模块正在打磨中...</h2>
        <p style="color:var(--text-sub)">点击 "通用设置" 或 "智能体偏好" 查看效果</p>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const currentTab = ref('agent')
const appTheme = ref('light')

onMounted(() => {
  const savedTheme = localStorage.getItem('spark_theme')
  if (savedTheme) {
    appTheme.value = savedTheme
  }
})

const setTheme = (theme) => {
  appTheme.value = theme
  window.dispatchEvent(new CustomEvent('theme-changed', { detail: theme }))
}
</script>

<style scoped>
.settings-container { display: flex; gap: 30px; height: 100%; }

/* 左侧边栏 */
.settings-sidebar { flex: 0 0 240px; padding: 25px 0; }
.sidebar-title { padding: 0 25px; font-size: 18px; font-weight: 700; margin-bottom: 20px; color: var(--text-main); }
.settings-menu { list-style: none; display: flex; flex-direction: column; gap: 5px; padding: 0 15px; }
.settings-menu li { padding: 12px 20px; border-radius: 12px; cursor: pointer; display: flex; align-items: center; gap: 12px; font-size: 14px; font-weight: 500; color: var(--text-sub); transition: 0.2s; }
.settings-menu li:hover { background: rgba(255,255,255,0.5); color: var(--text-main); }
.settings-menu li.active { background: #fff; color: var(--accent); box-shadow: 0 4px 15px rgba(0,0,0,0.03); font-weight: 600; }
.settings-menu li .icon { font-size: 16px; }

/* 右侧主内容 */
.settings-main { flex: 1; padding: 40px 60px; overflow-y: auto; }
.settings-section h3 { font-size: 24px; font-weight: 800; margin-bottom: 10px; color: var(--text-main); }
.section-desc { font-size: 14px; color: var(--text-sub); margin-bottom: 40px; line-height: 1.5; }

.setting-item { display: flex; justify-content: space-between; align-items: center; padding: 20px 0; border-bottom: 1px solid rgba(0,0,0,0.05); }
.setting-item:last-child { border-bottom: none; }
.setting-item .info { flex: 1; padding-right: 30px; }
.setting-item .info h4 { font-size: 15px; font-weight: 600; margin-bottom: 6px; color: var(--text-main); }
.setting-item .info p { font-size: 13px; color: var(--text-sub); line-height: 1.5; }

/* 控件样式 */
.custom-select { padding: 10px 15px; border-radius: 10px; border: 1px solid rgba(0,0,0,0.1); background: rgba(255,255,255,0.8); outline: none; font-size: 14px; min-width: 180px; font-family: inherit; cursor: pointer; }

/* Switch Toggle */
.switch { position: relative; display: inline-block; width: 48px; height: 26px; }
.switch input { opacity: 0; width: 0; height: 0; }
.slider { position: absolute; cursor: pointer; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0,0,0,0.1); transition: .4s; border-radius: 34px; }
.slider:before { position: absolute; content: ""; height: 20px; width: 20px; left: 3px; bottom: 3px; background-color: white; transition: .4s; border-radius: 50%; box-shadow: 0 2px 5px rgba(0,0,0,0.2); }
input:checked + .slider { background-color: var(--accent); }
input:checked + .slider:before { transform: translateX(22px); }

/* Range Slider */
.range-slider-wrap { display: flex; align-items: center; gap: 15px; width: 250px; }
.range-label { font-size: 12px; color: var(--text-sub); font-weight: 600; }
.custom-range { flex: 1; -webkit-appearance: none; width: 100%; height: 6px; border-radius: 3px; background: rgba(0,0,0,0.1); outline: none; }
.custom-range::-webkit-slider-thumb { -webkit-appearance: none; appearance: none; width: 18px; height: 18px; border-radius: 50%; background: var(--accent); cursor: pointer; box-shadow: 0 2px 5px rgba(0,0,0,0.2); }

/* 主题卡片 */
.theme-selection { display: flex; justify-content: flex-start; gap: 30px; border-bottom: none; padding-top: 10px; }
.theme-card { display: flex; flex-direction: column; align-items: center; gap: 12px; cursor: pointer; }
.theme-preview { width: 140px; height: 90px; border-radius: 12px; border: 2px solid transparent; box-shadow: 0 4px 15px rgba(0,0,0,0.05); transition: 0.2s; }
.theme-card.active .theme-preview { border-color: var(--accent); box-shadow: 0 8px 20px rgba(10,132,255,0.2); }
.theme-card span { font-size: 13px; font-weight: 600; color: var(--text-sub); }
.theme-card.active span { color: var(--accent); }

.light-preview { background: linear-gradient(135deg, #f8fafc, #e2e8f0); position: relative; overflow: hidden; }
.light-preview::after { content: ''; position: absolute; left: 10px; top: 10px; right: 10px; bottom: 10px; background: rgba(255,255,255,0.6); border-radius: 8px; }
.dark-preview { background: linear-gradient(135deg, #0f172a, #1e293b); position: relative; overflow: hidden; }
.dark-preview::after { content: ''; position: absolute; left: 10px; top: 10px; right: 10px; bottom: 10px; background: rgba(255,255,255,0.1); border-radius: 8px; }
.auto-preview { background: linear-gradient(135deg, #e2e8f0 50%, #1e293b 50%); position: relative; overflow: hidden; }

.flex-center { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 60%; }
</style>
