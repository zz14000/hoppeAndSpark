<template>
  <div class="publish-container">
    <!-- 顶部操作栏 -->
    <header class="publish-header glass-panel">
      <div class="left">
        <button class="btn icon-btn" @click="$router.push('/app/community')" title="返回">←</button>
        <span class="draft-status">草稿自动保存于 10:24</span>
      </div>
      <div class="right">
        <button class="btn" @click="invokeHorizon">✨ 呼叫 Horizon 润色</button>
        <button class="btn btn-primary publish-btn" @click="handlePublish" :disabled="publishing">
          {{ publishing ? '发布中...' : '发布文章' }}
        </button>
      </div>
    </header>

    <div class="publish-main">
      <!-- 左侧编辑器 -->
      <div class="editor-area glass-panel">
        <input type="text" class="title-input" placeholder="输入文章标题..." v-model="title">
        
        <div class="toolbar">
          <div class="tool-group">
            <span title="粗体">B</span>
            <span title="斜体">I</span>
            <span title="删除线">S</span>
          </div>
          <div class="tool-group">
            <span title="引用">”</span>
            <span title="代码块">&lt;/&gt;</span>
            <span title="链接">🔗</span>
          </div>
          <div class="tool-group">
            <span title="图片">🖼️</span>
            <span title="公式">∑</span>
          </div>
        </div>

        <textarea 
          class="markdown-input" 
          placeholder="在此处开始使用 Markdown 编写您的技术心得... 
支持拖拽图片上传，支持 LaTeX 公式。"
          v-model="content"
        ></textarea>
      </div>

      <!-- 右侧设置与 AI 面板 -->
      <aside class="sidebar-settings">
        <!-- AI 助手面板 -->
        <div class="glass-panel ai-helper-panel" :class="{ 'active': isPolishing }">
          <div class="panel-title">
            <span class="icon">🤖</span> Horizon 创作助手
          </div>
          
          <div v-if="!isPolishing && !polished" class="ai-empty">
            <p>遇到创作瓶颈？点击上方按钮，让 Horizon 帮你：</p>
            <ul>
              <li>检查错别字与语病</li>
              <li>优化技术术语表达</li>
              <li>自动生成 Nebula 摘要</li>
              <li>提取文章核心标签</li>
            </ul>
          </div>
          
          <div v-else-if="isPolishing" class="ai-loading">
            <div class="spinner"></div>
            <p>Horizon 正在阅读并理解您的文章...</p>
          </div>

          <div v-else-if="polished" class="ai-result">
            <div class="result-card">
              <h5>📝 自动生成的摘要</h5>
              <p>本文探讨了前端架构中关于状态管理的最佳实践，结合了实际业务场景，对比了 Vuex 和 Pinia 的性能差异...</p>
              <button class="btn small-btn">应用此摘要</button>
            </div>
            <div class="result-card">
              <h5>🏷️ 推荐标签</h5>
              <div class="tags">
                <span class="tag">前端架构</span>
                <span class="tag">状态管理</span>
                <span class="tag">Pinia</span>
              </div>
              <button class="btn small-btn">一键添加标签</button>
            </div>
          </div>
        </div>

        <!-- 文章设置面板 -->
        <div class="glass-panel setting-panel">
          <div class="panel-title">发布设置</div>
          
          <div class="form-group">
            <label>文章分类</label>
            <select class="custom-select">
              <option>前端开发</option>
              <option>后端架构</option>
              <option>人工智能</option>
              <option>算法与数据结构</option>
              <option>面经与职场</option>
            </select>
          </div>

          <div class="form-group">
            <label>添加标签</label>
            <div class="tag-input-wrap">
              <span class="tag" v-for="(tag, i) in tags" :key="i">
                {{ tag }} <span class="del" @click="tags.splice(i, 1)">×</span>
              </span>
              <input type="text" placeholder="输入标签后回车" @keyup.enter="addTag">
            </div>
          </div>

          <div class="form-group">
            <label>文章封面</label>
            <div class="cover-uploader">
              <span class="icon">📷</span>
              <p>点击或拖拽上传封面图</p>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { publishArticle, polishArticle } from '../../api/article.js'

const router = useRouter()
const title = ref('')
const content = ref('')
const tags = ref(['前端架构'])
const isPolishing = ref(false)
const polished = ref(false)
const polishResult = ref(null)
const publishing = ref(false)

const addTag = (e) => {
  const val = e.target.value.trim()
  if (val && !tags.value.includes(val)) {
    tags.value.push(val)
  }
  e.target.value = ''
}

const invokeHorizon = async () => {
  if (!content.value && !title.value) return alert('请先输入一些内容')
  isPolishing.value = true
  try {
    const res = await polishArticle({ title: title.value, content: content.value })
    if (res.code === 200) {
      polishResult.value = res.data
      polished.value = true
    }
  } catch (e) {
    alert(e.message || '润色请求失败')
  } finally {
    isPolishing.value = false
  }
}

const handlePublish = async () => {
  if (!title.value.trim() || !content.value.trim()) {
    alert('请填写标题和正文')
    return
  }
  publishing.value = true
  try {
    const res = await publishArticle({
      title: title.value,
      content: content.value,
      tags: tags.value,
      summary: polishResult.value?.summary,
      visibility: 'public',
    })
    if (res.code === 200) {
      router.push(`/app/article/${res.data?.id || res.data?.articleId}`)
    } else {
      alert(res.message || '发布失败')
    }
  } catch (e) {
    alert(e.message || '发布请求失败')
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.publish-container { display: flex; flex-direction: column; height: 100%; gap: 20px; }

/* 顶部操作栏 */
.publish-header { padding: 15px 30px; display: flex; justify-content: space-between; align-items: center; border-radius: 16px; }
.left { display: flex; align-items: center; gap: 20px; }
.icon-btn { padding: 8px 12px; background: rgba(255,255,255,0.5); font-size: 16px; }
.draft-status { font-size: 13px; color: var(--text-sub); }
.right { display: flex; gap: 15px; }
.publish-btn { padding: 8px 24px; border-radius: 8px; }

/* 主区域 */
.publish-main { flex: 1; display: flex; gap: 20px; overflow: hidden; }

/* 左侧编辑器 */
.editor-area { flex: 1; display: flex; flex-direction: column; border-radius: 16px; overflow: hidden; background: rgba(255,255,255,0.7); }
.title-input { font-size: 24px; font-weight: 700; padding: 25px 30px; border: none; outline: none; background: transparent; border-bottom: 1px solid rgba(0,0,0,0.05); color: var(--text-main); }
.title-input::placeholder { color: #cbd5e1; }

.toolbar { display: flex; gap: 20px; padding: 10px 30px; border-bottom: 1px solid rgba(0,0,0,0.05); background: rgba(255,255,255,0.4); }
.tool-group { display: flex; gap: 15px; border-right: 1px solid rgba(0,0,0,0.1); padding-right: 20px; }
.tool-group:last-child { border-right: none; }
.tool-group span { cursor: pointer; color: var(--text-sub); font-size: 15px; font-weight: 600; font-family: monospace; transition: 0.2s; }
.tool-group span:hover { color: var(--accent); }

.markdown-input { flex: 1; padding: 30px; border: none; outline: none; background: transparent; resize: none; font-size: 15px; line-height: 1.8; color: #334155; font-family: 'Fira Code', monospace; }

/* 右侧侧边栏 */
.sidebar-settings { flex: 0 0 320px; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; }

.panel-title { font-size: 15px; font-weight: 700; margin-bottom: 20px; color: var(--text-main); display: flex; align-items: center; gap: 8px; }
.ai-helper-panel { padding: 25px; border-radius: 16px; background: linear-gradient(135deg, rgba(255,255,255,0.8), rgba(243,232,255,0.6)); border: 1px solid rgba(216,180,254,0.3); transition: 0.3s; }
.ai-helper-panel.active { border-color: #c084fc; box-shadow: 0 0 20px rgba(192,132,252,0.2); }

.ai-empty p { font-size: 13px; color: var(--text-sub); margin-bottom: 15px; }
.ai-empty ul { padding-left: 20px; font-size: 13px; color: var(--text-sub); line-height: 1.8; }

.ai-loading { display: flex; flex-direction: column; align-items: center; gap: 15px; padding: 20px 0; }
.spinner { width: 30px; height: 30px; border: 3px solid rgba(192,132,252,0.2); border-top-color: #a855f7; border-radius: 50%; animation: spin 1s linear infinite; }
.ai-loading p { font-size: 13px; color: #9333ea; font-weight: 600; }

.result-card { background: rgba(255,255,255,0.6); padding: 15px; border-radius: 12px; margin-bottom: 15px; }
.result-card h5 { font-size: 13px; margin-bottom: 8px; color: #7e22ce; }
.result-card p { font-size: 13px; color: #334155; line-height: 1.5; margin-bottom: 10px; }
.small-btn { font-size: 12px; padding: 4px 10px; border-radius: 6px; background: #fff; border: 1px solid rgba(0,0,0,0.1); }
.small-btn:hover { background: var(--accent); color: #fff; }
.tags { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }

.setting-panel { padding: 25px; border-radius: 16px; flex: 1; }
.form-group { margin-bottom: 20px; }
.form-group label { display: block; font-size: 13px; font-weight: 600; margin-bottom: 8px; color: var(--text-sub); }
.custom-select { width: 100%; padding: 10px 12px; border-radius: 8px; border: 1px solid rgba(0,0,0,0.1); background: rgba(255,255,255,0.5); outline: none; font-size: 14px; }

.tag-input-wrap { display: flex; flex-wrap: wrap; gap: 8px; padding: 8px; border: 1px solid rgba(0,0,0,0.1); border-radius: 8px; background: rgba(255,255,255,0.5); }
.tag-input-wrap .tag { background: var(--accent); color: #fff; padding: 4px 8px; border-radius: 4px; font-size: 12px; display: flex; align-items: center; gap: 6px; }
.tag-input-wrap .del { cursor: pointer; font-weight: 700; }
.tag-input-wrap input { flex: 1; min-width: 100px; border: none; background: transparent; outline: none; font-size: 13px; }

.cover-uploader { height: 120px; border: 2px dashed rgba(0,0,0,0.1); border-radius: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 10px; cursor: pointer; transition: 0.2s; background: rgba(255,255,255,0.3); }
.cover-uploader:hover { border-color: var(--accent); background: rgba(10,132,255,0.05); }
.cover-uploader .icon { font-size: 24px; color: var(--text-sub); }
.cover-uploader p { font-size: 12px; color: var(--text-sub); }

@keyframes spin { to { transform: rotate(360deg); } }
</style>
