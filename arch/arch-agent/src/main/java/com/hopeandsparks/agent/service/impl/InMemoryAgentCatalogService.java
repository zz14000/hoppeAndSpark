package com.hopeandsparks.agent.service.impl;

import com.hopeandsparks.agent.service.AgentCatalogService;
import com.hopeandsparks.agent.vo.AgentInfoVO;
import com.hopeandsparks.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 智能体目录的 W3 mock 实现，后续可替换为 sys_agent_config + sys_agent_prompt 查询。
 */
@Service
public class InMemoryAgentCatalogService implements AgentCatalogService {

    private static final Set<String> WORKFLOW_AGENTS = Set.of("nebula", "horizon", "strict");

    private final List<AgentInfoVO> agents = List.of(
            new AgentInfoVO("ava", "Ava", "潜意识唤醒与学习激励", List.of("情绪干预", "学习动力"),
                    "ava.png", "Spark 你好，我们先把今天最小的一步完成。", true, false),
            new AgentInfoVO("sage", "Sage", "苏格拉底式启发答疑", List.of("分层反问", "伴读引导"),
                    "sage.png", "把问题发给我，我们一起从关键边界开始拆。", true, false),
            new AgentInfoVO("coach", "Coach", "练习诊断与提示", List.of("错因诊断", "分级提示"),
                    "coach.png", "先把你的答案贴上来，我帮你看卡在哪一步。", true, false),
            new AgentInfoVO("strict", "Strict", "学习计划与自律推进", List.of("计划生成", "节奏校准"),
                    "strict.png", "告诉我目标和时间，我会给你排一个能执行的计划。", false, true),
            new AgentInfoVO("nebula", "Nebula", "探索学习方向与资源生成", List.of("知识探索", "资源草案", "思维导图"),
                    "nebula.png", "输入主题，我先帮你生成探索路径和资源草案。", false, true),
            new AgentInfoVO("horizon", "Horizon", "事实核查与质量审查", List.of("质量检查", "回修建议"),
                    "horizon.png", "我会检查内容结构、事实风险和可修复问题。", false, true),
            new AgentInfoVO("oldmoney", "Old Money", "行业经验与反方挑战", List.of("方案挑战", "经验复盘"),
                    "oldmoney.png", "把方案讲给我听，我会从现实约束里挑重点。", true, false)
    );

    private final Map<String, AgentInfoVO> index = agents.stream()
            .collect(Collectors.toUnmodifiableMap(AgentInfoVO::id, Function.identity()));

    @Override
    public List<AgentInfoVO> listAgents() {
        return agents;
    }

    @Override
    public AgentInfoVO getAgent(String agentId) {
        String normalized = normalizeAgentId(agentId);
        AgentInfoVO agent = index.get(normalized);
        if (agent == null) {
            throw new BusinessException(400, "智能体不存在: " + agentId);
        }
        return agent;
    }

    @Override
    public boolean isWorkflowAgent(String agentId) {
        return WORKFLOW_AGENTS.contains(normalizeAgentId(agentId));
    }

    private String normalizeAgentId(String agentId) {
        if (agentId == null || agentId.isBlank()) {
            throw new BusinessException(400, "agentId 不能为空");
        }
        return agentId.trim().replace("_", "").toLowerCase(Locale.ROOT);
    }
}
