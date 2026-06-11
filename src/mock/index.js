// src/mock/index.js
// 统一的前端 Mock 数据源

export const MOCK_USER = {
  id: 'u_1001',
  nickname: '一粒黑子',
  avatar: '一',
  bio: '永远保持对技术的好奇心。',
  location: '上海',
  company: 'Spark 开源社区',
  github: 'github.com/heizi',
  stats: {
    followers: '2.4k',
    likes: '1.2w',
    articles: 15
  },
  skills: [
    { name: '前端工程化', level: 4 },
    { name: '微服务架构', level: 5 },
    { name: '数据库调优', level: 3 }
  ],
  progress: 68
};

export const MOCK_ARTICLES = [
  {
    id: 1,
    title: '深入浅出 Vue3 响应式原理与手写实现',
    excerpt: 'Proxy 到底比 Object.defineProperty 好在哪里？本文将带你从零手写一个简易版的响应式系统，彻底搞懂底层的依赖收集与派发更新机制。',
    author: '前端架构师老李',
    time: '2 小时前',
    readCount: '2.4k',
    aiSummary: true,
    tags: ['Vue3', '源码解析', '前端']
  },
  {
    id: 2,
    title: '高并发场景下的分布式锁解决方案：从 Redis 到 Redisson',
    excerpt: '在微服务架构中，分布式锁是保证数据一致性的重要组件。我们团队在处理双十一高并发秒杀场景时，踩过了不少 Redis 分布式锁的坑...',
    author: '一粒黑子',
    time: '昨天',
    readCount: '5.1k',
    aiSummary: false,
    tags: ['分布式', 'Redis', '架构设计']
  },
  {
    id: 3,
    title: 'Rust 学习笔记：所有权与生命周期的心智模型',
    excerpt: '很多初学者在 Rust 的借用检查器面前败下阵来。其实只要转变一下心智模型，把变量当成有生命周期的实体，一切就豁然开朗了。',
    author: 'Rustacean',
    time: '3 天前',
    readCount: '1.8k',
    aiSummary: true,
    tags: ['Rust', '底层语言']
  }
];

export const MOCK_NOTIFICATIONS = [
  {
    id: 'n_1',
    type: 'strict',
    title: 'Strict 调度提醒：学习计划偏离',
    time: '10 分钟前',
    content: '您本周在“分布式系统”模块的进度落后于预期 15%。建议您今晚抽出 45 分钟完成遗留的沙盘测验。',
    unread: true
  },
  {
    id: 'n_2',
    type: 'ava',
    title: 'Ava 的鼓励',
    time: '2 小时前',
    content: '太棒了！您刚刚连续打卡 7 天，并成功攻克了【红黑树】节点。去 Dashboard 点亮你的技能树吧！',
    unread: true
  },
  {
    id: 'n_3',
    type: 'system',
    title: 'Nebula 引擎周报已生成',
    time: '昨天',
    content: '上周您累计学习 14 小时，击败了 85% 的社区学习者。点击查看详细的学习图谱分析。',
    unread: false
  }
];

export const MOCK_CALENDAR_EVENTS = [
  {
    id: 'e_1',
    date: '2026-05-18',
    time: '09:00',
    title: '算法挑战',
    desc: '排序与搜索专题突破',
    type: 'blue',
    status: '待完成'
  },
  {
    id: 'e_2',
    date: '2026-05-20',
    time: '14:00',
    title: '系统架构复习',
    desc: '分布式高并发方案设计',
    type: 'purple',
    status: '进行中'
  },
  {
    id: 'e_3',
    date: '2026-05-22',
    time: '16:30',
    title: '社区交流',
    desc: '参与开源项目 PR Review',
    type: 'green',
    status: '未开始'
  }
];

// 模拟延迟返回函数
export const delay = (ms = 500) => new Promise(resolve => setTimeout(resolve, ms));
