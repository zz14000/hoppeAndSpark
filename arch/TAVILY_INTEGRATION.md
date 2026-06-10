# Tavily 搜索引擎集成指南

## 📋 概述

项目已集成 **Tavily Search API**，这是一个专为AI应用设计的搜索引擎，提供高质量的搜索结果和内容提取。

## 🚀 快速开始

### 1. 获取 Tavily API Key

1. 访问 [Tavily官网](https://tavily.com/)
2. 注册账号
3. 在 Dashboard 中创建 API Key
4. 免费额度：每月1000次搜索（适合开发和测试）

### 2. 配置环境变量

复制 `arch/.env.example` 为 `arch/.env`，填写本地密钥。`arch/.env` 已加入 `.gitignore`，不要提交。

```properties
SEARCH_PROVIDER=tavily
TAVILY_API_KEY=your-tavily-api-key-here
TAVILY_BASE_URL=https://api.tavily.com
TAVILY_SEARCH_DEPTH=basic
TAVILY_MAX_RESULTS=5
```

### 3. 验证配置

配置文件位置：
- `arch-agent-harness/src/main/resources/application.yml`
- `arch-boot/src/main/resources/application.yml`

配置内容（已通过环境变量覆盖）：
```yaml
hope:
  ai:
    search:
      provider: ${SEARCH_PROVIDER:mock}
      api-key: ${TAVILY_API_KEY:}
      base-url: ${TAVILY_BASE_URL:https://api.tavily.com}
      search-depth: ${TAVILY_SEARCH_DEPTH:basic}
      max-results: ${TAVILY_MAX_RESULTS:5}
```

## ✅ 使用方式

### 方式1：通过 Harness 测试

```bash
mvn spring-boot:run -pl arch-agent-harness \
  -Dspring-boot.run.arguments="--mode=rag --query=二分查找的时间复杂度"
```

### 方式2：通过 Web API

```bash
mvn spring-boot:run -pl arch-boot

# 调用Agent API
curl -X POST http://localhost:8080/api/v1/agent/run \
  -H "Content-Type: application/json" \
  -d '{
    "userQuery": "最新的AI技术发展",
    "mode": "rag"
  }'
```

## 🔧 工作原理

### 架构流程

```
用户查询
  ↓
ToolRegistry.call("web_search", ...)
  ↓
WebSearchGateway (根据配置选择实现)
  ├─ MockWebSearchGateway (Mock模式)
  └─ TavilyWebSearchGateway (真实搜索)
  ↓
返回 WebSearchResponse
  - 标题
  - URL
  - 摘要内容
  - 可信度评分
```

### Tavily API 调用细节

```java
POST https://api.tavily.com/search
Headers:
  Authorization: Bearer {API_KEY}
  Content-Type: application/json

Body:
{
  "query": "用户查询",
  "search_depth": "basic",     // basic 或 advanced
  "max_results": 5,            // 返回结果数量
  "include_answer": true,      // 包含AI生成的摘要
  "include_raw_content": false,
  "include_images": false
}

Response:
{
  "results": [
    {
      "title": "网页标题",
      "url": "https://...",
      "content": "提取的内容摘要",
      "score": 0.95            // 相关性评分
    }
  ]
}
```

## 📊 Mock vs Tavily 对比

| 特性 | Mock模式 | Tavily模式 |
|------|---------|-----------|
| **数据源** | 硬编码模拟数据 | 真实互联网搜索 |
| **内容质量** | 低（仅占位） | 高（实时、准确） |
| **速度** | 快（无网络延迟） | 中等（1-3秒） |
| **成本** | 免费 | 免费额度1000次/月 |
| **适用场景** | 开发调试 | 生产环境、真实RAG |
| **依赖** | 无 | 需要API Key和网络 |

## 💡 使用建议

### 开发阶段
```bash
SEARCH_PROVIDER=mock
```

### 测试/生产阶段
```bash
SEARCH_PROVIDER=tavily
TAVILY_API_KEY=tvly-xxx
```

### 混合使用
你可以在不同环境使用不同配置：
- **本地开发**: Mock模式
- **测试环境**: Tavily（控制用量）
- **生产环境**: Tavily（考虑升级套餐）

## 🔍 Tavily 特性

### 优势
✅ **为AI优化**: 专门为RAG和LLM应用设计  
✅ **高质量内容**: 自动提取正文，过滤噪音  
✅ **可信度评分**: 每个结果都有相关性分数  
✅ **实时性**: 搜索最新的互联网内容  
✅ **简单易用**: REST API，无需复杂配置  

### 限制
⚠️ **免费额度**: 1000次/月（超出需付费）  
⚠️ **网络依赖**: 需要稳定的网络连接  
⚠️ **英文为主**: 对中文支持良好但不如英文  

## 🛠️ 故障排查

### 问题1: 启动报错 "no API key configured"

**原因**: 配置了 `provider: tavily` 但没有设置 API Key。

**解决**:
当前实现会自动降级到 Mock，保证本地服务可启动。要启用真实搜索，在 `arch/.env` 设置 `TAVILY_API_KEY`。

### 问题2: 搜索结果为空

**可能原因**:
1. API Key 无效或过期
2. 网络连接问题
3. 查询内容为空

**检查**:
```bash
# 测试API连接
curl -X POST https://api.tavily.com/search \
  -H "Authorization: Bearer $TAVILY_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"query": "test", "max_results": 1}'
```

### 问题3: 响应速度慢

**原因**: Tavily API 通常需要1-3秒

**优化**:
- 减少 `TAVILY_MAX_RESULTS` 值（默认5，可改为3）
- 使用 `search_depth: basic`（默认）而非 `advanced`

## 📝 代码示例

### 在Service中使用

```java
@Service
public class MyService {
    
    private final ToolRegistry toolRegistry;
    
    public MyService(ToolRegistry toolRegistry) {
        this.toolRegistry = toolRegistry;
    }
    
    public List<String> searchAndSummarize(String query) {
        // 调用web_search工具
        WebSearchResponse response = (WebSearchResponse) toolRegistry.call(
            "web_search", 
            Map.of(
                "query", query,
                "topK", 5
            )
        );
        
        // 提取结果
        return response.results().stream()
            .map(result -> result.title() + ": " + result.summary())
            .toList();
    }
}
```

### 在Agent中使用

Sage Agent 在执行 RAG 任务时会自动调用 web_search（如果配置启用）：

```java
// ContextNormalizer.java 中的 ragContext 方法
VectorSearchResponse response = (VectorSearchResponse) toolRegistry.call("kb_search", ...);
// 可以扩展为同时调用 web_search 获取最新信息
WebSearchResponse webResults = (WebSearchResponse) toolRegistry.call("web_search", ...);
```

## 🎯 下一步优化建议

1. **结果缓存**: 对相同查询的结果进行缓存，减少API调用
2. **混合检索**: 同时使用 Chroma + Tavily，结合知识库和实时搜索
3. **结果过滤**: 根据域名、可信度等过滤搜索结果
4. **并发优化**: 并行执行多个搜索查询
5. **结果重排序**: 使用 Rerank 模型对搜索结果再次排序

## 📚 参考资料

- [Tavily 官方文档](https://docs.tavily.com/)
- [Tavily API Reference](https://docs.tavily.com/api-reference/endpoint/search)
- [Tavily Pricing](https://tavily.com/pricing)

---

**总结**: Tavily 集成已完成，只需配置 API Key 即可启用真实网络搜索功能，大幅提升 RAG 系统的实时性和准确性！🎉
