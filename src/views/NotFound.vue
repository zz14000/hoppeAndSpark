<template>
  <div class="not-found-container glass-panel">
    <div class="content">
      <h1 class="error-code">404</h1>
      <h2>页面飘向了星辰大海</h2>
      <p>您访问的区域可能已被 Nebula 引擎重新规划，或您正在尝试探索一片未知的代码领域。</p>
      
      <div class="actions">
        <button class="btn btn-primary" @click="$router.push('/app/dashboard')">返回工作台</button>
        <button class="btn" @click="$router.back()">返回上一页</button>
      </div>

      <div class="agent-tip">
        <span class="icon">🤖</span>
        <span><strong>Sage:</strong> "错误也是一种学习。我们如何确保下次不再迷失？"</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.not-found-container { display: flex; align-items: center; justify-content: center; height: 100vh; width: 100vw; background: var(--bg); }
.content { display: flex; flex-direction: column; align-items: center; text-align: center; max-width: 600px; padding: 60px; border-radius: 24px; background: var(--panel-bg); border: 1px solid rgba(0,0,0,0.05); box-shadow: 0 20px 40px rgba(0,0,0,0.05); }

.error-code { font-size: 120px; font-weight: 900; line-height: 1; margin-bottom: 20px; background: linear-gradient(135deg, var(--accent), #ff2d55); -webkit-background-clip: text; -webkit-text-fill-color: transparent; text-shadow: 0 10px 30px rgba(168, 85, 247, 0.2); }
h2 { font-size: 28px; font-weight: 800; color: var(--text-main); margin-bottom: 15px; }
p { font-size: 16px; color: var(--text-sub); line-height: 1.6; margin-bottom: 30px; }

.actions { display: flex; gap: 15px; margin-bottom: 40px; }
.actions .btn { padding: 12px 30px; border-radius: 50px; font-size: 16px; font-weight: 600; }

.agent-tip { display: flex; align-items: center; gap: 10px; background: rgba(0, 122, 255, 0.05); border: 1px solid rgba(0, 122, 255, 0.1); padding: 12px 20px; border-radius: 12px; font-size: 14px; color: var(--text-sub); }
.agent-tip .icon { font-size: 20px; }
.agent-tip strong { color: var(--c-sage); }
</style>
