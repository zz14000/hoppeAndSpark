import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const root = path.join(__dirname, '..');

const OPENAPI_FILES = [
  { file: 'Hope and Sparks API.openapi.json', source: 'main' },
  { file: 'apifox-kb-agent-openapi.json', source: 'kb-agent' },
];

function extractEndpoints(spec, source) {
  const list = [];
  for (const [p, methods] of Object.entries(spec.paths || {})) {
    for (const [method, def] of Object.entries(methods)) {
      if (!['get', 'post', 'put', 'delete', 'patch'].includes(method)) continue;
      list.push({
        method: method.toUpperCase(),
        path: p,
        summary: def.summary || '',
        tag: (def.tags && def.tags[0]) || '',
        source,
      });
    }
  }
  return list;
}

const endpointMap = new Map();
for (const { file, source } of OPENAPI_FILES) {
  const filePath = path.join(root, file);
  if (!fs.existsSync(filePath)) {
    console.warn(`跳过不存在的 OpenAPI 文件: ${file}`);
    continue;
  }
  const spec = JSON.parse(fs.readFileSync(filePath, 'utf8'));
  for (const ep of extractEndpoints(spec, source)) {
    const key = `${ep.method} ${ep.path}`;
    const existing = endpointMap.get(key);
    endpointMap.set(key, existing ? { ...existing, ...ep, source: `${existing.source}+${source}` } : ep);
  }
}

const openapiEndpoints = Array.from(endpointMap.values()).sort(
  (a, b) => a.path.localeCompare(b.path) || a.method.localeCompare(b.method)
);

function normalizePath(p) {
  return p
    .replace(/\$\{[^}]+\}/g, '{param}')
    .replace(/'[^']*'/g, "'{param}'")
    .replace(/`[^`]*\$\{[^}]+\}[^`]*`/g, '{param}');
}

function scanApiDir(dir) {
  const implementations = [];
  for (const file of fs.readdirSync(dir)) {
    if (!file.endsWith('.js') || file === 'request.js' || file === 'index.js') continue;
    const content = fs.readFileSync(path.join(dir, file), 'utf8');
    const module = file.replace('.js', '');
    const regex = /http\.(get|post|put|delete)\(\s*(`[^`]+`|'[^']+'|"[^"]+")/g;
    let match;
    while ((match = regex.exec(content)) !== null) {
      const method = match[1].toUpperCase();
      let route = match[2].slice(1, -1);
      route = route.replace(/\$\{[^}]+\}/g, '{param}');
      implementations.push({ method, path: route, module, file: `src/api/${file}` });
    }
  }
  // request.js refresh token
  const requestContent = fs.readFileSync(path.join(dir, 'request.js'), 'utf8');
  if (requestContent.includes("'/api/v1/auth/refresh'")) {
    implementations.push({ method: 'POST', path: '/api/v1/auth/refresh', module: 'request', file: 'src/api/request.js' });
  }
  // agent.js SSE stream（使用 fetch，非 http.get）
  const agentContent = fs.readFileSync(path.join(dir, 'agent.js'), 'utf8');
  if (agentContent.includes('agent-sessions/${sessionId}/stream')) {
    implementations.push({
      method: 'GET',
      path: '/api/v1/agent-sessions/{sessionId}/stream',
      module: 'agent',
      file: 'src/api/agent.js',
    });
  }
  return implementations;
}

function pathMatches(specPath, implPath) {
  const specParts = specPath.split('/');
  const implParts = implPath.split('/');
  if (specParts.length !== implParts.length) return false;
  return specParts.every((part, i) => {
    if (part.startsWith('{')) return true;
    return part === implParts[i];
  });
}

const implementations = scanApiDir(path.join(root, 'src/api'));

const rows = openapiEndpoints.map((ep) => {
  const impl = implementations.find(
    (i) => i.method === ep.method && pathMatches(ep.path, i.path)
  );
  return {
    ...ep,
    status: impl ? '✅ 已实现' : '❌ 未实现',
    module: impl?.module || '-',
    file: impl?.file || '-',
  };
});

const implemented = rows.filter((r) => r.status.includes('已实现')).length;
const missing = rows.filter((r) => r.status.includes('未实现'));

const output = {
  generatedAt: new Date().toISOString(),
  total: openapiEndpoints.length,
  implemented,
  missing: missing.length,
  coverage: `${((implemented / openapiEndpoints.length) * 100).toFixed(1)}%`,
  endpoints: rows,
};

fs.writeFileSync(path.join(root, 'docs/API_COVERAGE.json'), JSON.stringify(output, null, 2));

function generateMarkdown(data) {
  const lines = [];
  const date = data.generatedAt.slice(0, 10);

  lines.push('# Hope & Sparks 前端 API 接口实现汇总');
  lines.push('');
  lines.push(`> 依据主规范 + KB/Agent 补充规范自动生成，最后更新：${date}`);
  lines.push('>');
  lines.push('> - `Hope and Sparks API.openapi.json`');
  lines.push('> - `apifox-kb-agent-openapi.json`');
  lines.push('');
  lines.push('## 覆盖率概览');
  lines.push('');
  lines.push('| 指标 | 数值 |');
  lines.push('|------|------|');
  lines.push(`| OpenAPI 接口总数 | ${data.total} |`);
  lines.push(`| 已实现 | ${data.implemented} |`);
  lines.push(`| 未实现 | ${data.missing} |`);
  lines.push(`| 覆盖率 | **${data.coverage}** |`);
  lines.push('');
  lines.push('重新生成：');
  lines.push('');
  lines.push('```bash');
  lines.push('npm run api:coverage');
  lines.push('```');
  lines.push('');
  lines.push('## 前端模块索引');
  lines.push('');
  lines.push('| 模块文件 | 接口数 | 说明 |');
  lines.push('|----------|--------|------|');

  const byModule = {};
  data.endpoints.forEach((ep) => {
    if (!byModule[ep.module]) byModule[ep.module] = { count: 0, file: ep.file };
    byModule[ep.module].count += 1;
    if (ep.file !== '-') byModule[ep.module].file = ep.file;
  });

  const moduleDesc = {
    auth: '用户注册、登录、Token、密码找回',
    onboarding: 'Spark 画像引导与重建',
    user: '用户资料、收藏、关注、设备与安全',
    agent: '智能体会话与争议举报',
    system: 'Nebula 探索、日历、通知',
    resource: '学习资源、视频、文档',
    learning: '学习计划、拓扑、技能树',
    exercise: '练习与测试',
    article: '社区文章与评论',
    chat: '私信、群聊、好友申请',
    settings: '用户设置与缓存',
    upload: '文件上传',
    manage: '管理端后台（用户、审核、资源等）',
    kb: '知识库文档、入库治理、评估与 Agent Ops',
    request: 'HTTP 请求层（Token 自动刷新）',
  };

  Object.keys(byModule)
    .sort()
    .forEach((mod) => {
      const { count, file } = byModule[mod];
      lines.push(`| \`${file}\` | ${count} | ${moduleDesc[mod] || ''} |`);
    });

  lines.push('');
  lines.push('## 调用方式');
  lines.push('');
  lines.push('```js');
  lines.push("import { login, getArticleList, manageLogin } from '@/api'");
  lines.push('// 或');
  lines.push("import { forgotPassword } from '@/api/auth.js'");
  lines.push('```');
  lines.push('');
  lines.push('统一从 `src/api/index.js` 导出。请求层见 `src/api/request.js`，开发代理见 `vite.config.js`。');
  lines.push('');

  if (data.missing > 0) {
    lines.push('## ❌ 未实现接口');
    lines.push('');
    lines.push('| 方法 | 接口路径 | 说明 | OpenAPI 标签 |');
    lines.push('|------|----------|------|--------------|');
    data.endpoints
      .filter((ep) => ep.status.includes('未实现'))
      .forEach((ep) => {
        lines.push(`| ${ep.method} | \`${ep.path}\` | ${ep.summary} | ${ep.tag} |`);
      });
    lines.push('');
  }

  lines.push('## 完整接口清单');
  lines.push('');

  const byTag = {};
  data.endpoints.forEach((ep) => {
    const tag = ep.tag || '其他';
    if (!byTag[tag]) byTag[tag] = [];
    byTag[tag].push(ep);
  });

  Object.keys(byTag).forEach((tag) => {
    lines.push(`### ${tag}`);
    lines.push('');
    lines.push('| 状态 | 方法 | 接口路径 | 说明 | 前端模块 |');
    lines.push('|------|------|----------|------|----------|');
    byTag[tag].forEach((ep) => {
      lines.push(
        `| ${ep.status} | ${ep.method} | \`${ep.path}\` | ${ep.summary} | \`${ep.file}\` |`
      );
    });
    lines.push('');
  });

  lines.push('---');
  lines.push('');
  lines.push('相关文件：');
  lines.push('');
  lines.push('- 主 OpenAPI：`Hope and Sparks API.openapi.json`');
  lines.push('- KB/Agent OpenAPI：`apifox-kb-agent-openapi.json`');
  lines.push('- JSON 机器可读：`docs/API_COVERAGE.json`');
  lines.push('');

  return lines.join('\n');
}

fs.writeFileSync(path.join(root, 'docs/API_COVERAGE.md'), generateMarkdown(output));

console.log(`\nAPI 覆盖率: ${implemented}/${openapiEndpoints.length} (${output.coverage})\n`);
console.log('已更新 docs/API_COVERAGE.json 与 docs/API_COVERAGE.md\n');
if (missing.length) {
  console.log('未实现接口:');
  missing.forEach((m) => console.log(`  ${m.method} ${m.path} - ${m.summary}`));
} else {
  console.log('全部 OpenAPI 接口均已实现！');
}

process.exit(missing.length > 0 ? 1 : 0);
