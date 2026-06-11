<template>
  <div class="sandbox-container">
    <!-- 左侧题目与 Coach 交互区 -->
    <div class="left-panel glass-panel">
      <div class="panel-header">
        <div class="agent-avatar">🤺</div>
        <div>
          <h3>Coach 沙盘演练</h3>
          <p>实战模拟 · 压力测试</p>
        </div>
        <div class="timer">⏱ {{ formattedTime }}</div>
      </div>

      <div class="challenge-content">
        <div class="tags"><span class="tag">算法</span><span class="tag">二叉树</span><span class="tag diff-hard">Hard</span></div>
        <h2 class="title">验证二叉搜索树</h2>
        <div class="desc">
          <p>给你一个二叉树的根节点 <code>root</code> ，判断其是否是一个有效的二叉搜索树 (BST)。</p>
          <p><strong>有效</strong> 二叉搜索树定义如下：</p>
          <ul>
            <li>节点的左子树只包含<strong> 小于 </strong>当前节点的数。</li>
            <li>节点的右子树只包含<strong> 大于 </strong>当前节点的数。</li>
            <li>所有左子树和右子树自身必须也是二叉搜索树。</li>
          </ul>
        </div>
      </div>

      <div class="coach-chat-area">
        <div class="msg-bubble coach">
          这是大厂面试高频题。注意陷阱：不能只判断左右孩子节点的值，必须保证<strong>整个左子树</strong>的所有节点都小于根节点。给你 15 分钟，写出最优解。
        </div>
        <div class="msg-bubble me" v-if="submitted">
          我已经提交了代码，请评审。
        </div>
        <div class="msg-bubble coach" v-if="showResult">
          代码通过了所有测试用例！时间复杂度 O(N)，空间复杂度 O(N)。<br><br>
          <strong>追问：</strong>如果这棵树非常不平衡，退化成了链表，你的空间复杂度会怎样？如果要求空间复杂度优化到 O(1) 呢？尝试用 Morris 遍历试试看。
        </div>
      </div>

      <div class="action-bar">
        <button class="btn" @click="askHint">求助 Coach</button>
        <button class="btn btn-primary" @click="submitCode">提交评测</button>
      </div>
    </div>

    <!-- 右侧代码编辑器区 (Mock) -->
    <div class="right-panel glass-panel">
      <div class="editor-header">
        <div class="tabs">
          <div class="tab active">solution.js</div>
          <div class="tab">testcase.js</div>
        </div>
        <div class="tools">
          <span class="icon">⚙️</span>
          <span class="icon">🔲</span>
        </div>
      </div>
      
      <div class="editor-body">
        <div class="line-numbers">
          <div v-for="n in 20" :key="n">{{ n }}</div>
        </div>
        <textarea class="code-input" v-model="code" spellcheck="false"></textarea>
      </div>

      <div class="terminal-panel" :class="{ 'is-open': showTerminal }">
        <div class="terminal-header" @click="showTerminal = !showTerminal">
          <span>终端输出</span>
          <span>{{ showTerminal ? '▼' : '▲' }}</span>
        </div>
        <div class="terminal-body" v-if="showTerminal">
          <div v-if="!submitted" style="color: var(--text-muted)">准备就绪，等待提交...</div>
          <div v-else class="run-logs">
            <div style="color: #10b981;">> 运行测试用例...</div>
            <div style="color: #10b981;">✓ Test Case 1: root = [2,1,3] -> Expected: true, Actual: true</div>
            <div style="color: #10b981;">✓ Test Case 2: root = [5,1,4,null,null,3,6] -> Expected: false, Actual: false</div>
            <div style="color: #38bdf8; margin-top: 10px;">执行成功！用时: 64ms, 内存: 42MB</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const code = ref(`/**
 * Definition for a binary tree node.
 * function TreeNode(val, left, right) {
 *     this.val = (val===undefined ? 0 : val)
 *     this.left = (left===undefined ? null : left)
 *     this.right = (right===undefined ? null : right)
 * }
 */
/**
 * @param {TreeNode} root
 * @return {boolean}
 */
var isValidBST = function(root) {
    const helper = (node, lower, upper) => {
        if (node === null) return true;
        if (node.val <= lower || node.val >= upper) return false;
        return helper(node.left, lower, node.val) && helper(node.right, node.val, upper);
    }
    return helper(root, -Infinity, Infinity);
};`)

const timeRemaining = ref(15 * 60)
const formattedTime = ref('15:00')
const submitted = ref(false)
const showResult = ref(false)
const showTerminal = ref(false)

onMounted(() => {
  setInterval(() => {
    if (timeRemaining.value > 0 && !submitted.value) {
      timeRemaining.value--
      const m = Math.floor(timeRemaining.value / 60).toString().padStart(2, '0')
      const s = (timeRemaining.value % 60).toString().padStart(2, '0')
      formattedTime.value = `${m}:${s}`
    }
  }, 1000)
})

const askHint = () => {
  // 模拟提示
}

const submitCode = () => {
  if (submitted.value) return
  submitted.value = true
  showTerminal.value = true
  
  setTimeout(() => {
    showResult.value = true
  }, 1500)
}
</script>

<style scoped>
.sandbox-container { display: flex; height: 100%; gap: 20px; padding-bottom: 10px; }

/* 左侧面板 */
.left-panel { flex: 0 0 400px; display: flex; flex-direction: column; overflow: hidden; background: rgba(255,255,255,0.7); }
.panel-header { display: flex; align-items: center; gap: 15px; padding: 20px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.agent-avatar { width: 44px; height: 44px; border-radius: 12px; background: linear-gradient(135deg, #c084fc, #9333ea); color: #fff; display: flex; align-items: center; justify-content: center; font-size: 24px; box-shadow: 0 4px 15px rgba(147,51,234,0.3); }
.panel-header h3 { font-size: 16px; font-weight: 700; margin-bottom: 2px; }
.panel-header p { font-size: 12px; color: var(--text-sub); }
.timer { margin-left: auto; font-family: monospace; font-size: 18px; font-weight: 700; color: var(--text-main); background: rgba(0,0,0,0.05); padding: 4px 10px; border-radius: 8px; }

.challenge-content { padding: 25px 20px; border-bottom: 1px solid rgba(0,0,0,0.05); }
.tags { display: flex; gap: 8px; margin-bottom: 15px; }
.tag { font-size: 12px; background: rgba(0,0,0,0.05); padding: 4px 10px; border-radius: 4px; color: var(--text-sub); }
.diff-hard { background: rgba(239,68,68,0.1); color: #ef4444; font-weight: 600; }
.title { font-size: 20px; font-weight: 700; margin-bottom: 15px; }
.desc { font-size: 14px; line-height: 1.6; color: var(--text-sub); }
.desc p { margin-bottom: 10px; }
.desc ul { padding-left: 20px; margin-bottom: 10px; }
.desc code { background: rgba(0,0,0,0.05); padding: 2px 6px; border-radius: 4px; font-family: monospace; color: #ef4444; }

.coach-chat-area { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 15px; background: rgba(147,51,234,0.02); }
.msg-bubble { padding: 12px 16px; border-radius: 12px; font-size: 13px; line-height: 1.5; max-width: 90%; }
.msg-bubble.coach { background: #fff; align-self: flex-start; border-top-left-radius: 4px; box-shadow: 0 4px 15px rgba(0,0,0,0.03); border: 1px solid rgba(147,51,234,0.1); }
.msg-bubble.me { background: var(--accent); color: #fff; align-self: flex-end; border-top-right-radius: 4px; }

.action-bar { padding: 20px; display: flex; gap: 15px; border-top: 1px solid rgba(0,0,0,0.05); background: #fff; }
.action-bar .btn { flex: 1; border-radius: 12px; padding: 12px; }

/* 右侧编辑器面板 */
.right-panel { flex: 1; display: flex; flex-direction: column; overflow: hidden; background: #1e293b; border: 1px solid rgba(255,255,255,0.2); }
.editor-header { display: flex; justify-content: space-between; align-items: center; background: #0f172a; border-bottom: 1px solid #334155; }
.tabs { display: flex; }
.tab { padding: 12px 20px; font-size: 13px; color: #94a3b8; cursor: pointer; border-right: 1px solid #334155; border-top: 2px solid transparent; }
.tab.active { background: #1e293b; color: #e2e8f0; border-top-color: #38bdf8; }
.tools { display: flex; gap: 15px; padding: 0 20px; color: #94a3b8; cursor: pointer; }

.editor-body { flex: 1; display: flex; overflow: hidden; position: relative; }
.line-numbers { width: 40px; padding: 15px 0; background: #0f172a; color: #475569; text-align: right; font-family: 'Fira Code', monospace; font-size: 14px; line-height: 24px; user-select: none; border-right: 1px solid #334155; }
.line-numbers div { padding-right: 10px; }
.code-input { flex: 1; background: transparent; border: none; outline: none; color: #e2e8f0; font-family: 'Fira Code', monospace; font-size: 14px; line-height: 24px; padding: 15px; resize: none; white-space: pre; }

.terminal-panel { background: #0f172a; border-top: 1px solid #334155; display: flex; flex-direction: column; }
.terminal-header { padding: 10px 20px; font-size: 12px; color: #94a3b8; display: flex; justify-content: space-between; cursor: pointer; user-select: none; background: #1e293b; }
.terminal-body { height: 150px; padding: 15px 20px; font-family: 'Fira Code', monospace; font-size: 13px; line-height: 1.6; overflow-y: auto; background: #0f172a; }
</style>
