package com.zj.ai.langgraph4j.agent;

import com.zj.ai.langgraph4j.domain.dto.PlanStep;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import com.zj.ai.langgraph4j.service.ToolRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 计划验证 Agent
 * 验证计划的可行性，支持智能检测重复计划
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
public class ValidateAgent implements NodeAction<PlanExecuteState> {

    private final ToolRegistryService toolRegistry;

    public ValidateAgent(ToolRegistryService toolRegistry) {
        this.toolRegistry = toolRegistry;
    }

    @Override
    public Map<String, Object> apply(PlanExecuteState state) throws Exception {
        log.info("=== ValidateAgent: 开始验证计划 ===");

        List<String> issues = new ArrayList<>();

        // 1. 检查是否任务已经结束（PlanAgent 直接给出了答案）
        if (state.hasFinalAnswer()) {
            log.info("任务已有最终答案，跳过验证");
            state.setValidationResult("已有最终答案，无需执行计划");
            state.setPlanFeasible(true);
            state.setCompleted(true);
            return state.toMap();
        }

        // 2. 检查是否有计划
        if (!state.hasPlan()) {
            log.warn("没有可执行的计划");
            state.setValidationResult("计划为空，无法执行");
            state.setPlanFeasible(false);
            state.incrementValidationFailure();
            return state.toMap();
        }

        // 3. 检测重复计划（避免死循环）
        if (state.isSameAsLastPlan()) {
            log.warn("检测到重复计划，可能陷入死循环");
            state.setValidationResult("检测到重复计划，请尝试不同的解决方案");
            state.setPlanFeasible(false);
            state.incrementValidationFailure();
            return state.toMap();
        }

        // 4. 获取可用工具列表
        List<String> availableTools = toolRegistry.getToolNames();
        log.info("可用工具列表: {}", availableTools);

        // 5. 验证每个步骤
        Set<String> usedTools = new HashSet<>();

        for (PlanStep step : state.getPlanSteps()) {
            String toolName = step.getToolName();

            // 检查工具名是否为空
            if (toolName == null || toolName.isEmpty()) {
                issues.add(String.format("步骤 %d: 未指定工具", step.getStepIndex()));
                continue;
            }

            // 检查工具是否存在
            boolean toolExists = availableTools.stream()
                    .anyMatch(t -> t.equalsIgnoreCase(toolName));

            if (!toolExists) {
                // 尝试找到相似的工具名
                String suggestion = findSimilarTool(toolName, availableTools);
                if (suggestion != null) {
                    issues.add(String.format("步骤 %d: 工具 '%s' 不存在，是否想使用 '%s'?",
                            step.getStepIndex(), toolName, suggestion));
                } else {
                    issues.add(String.format("步骤 %d: 工具 '%s' 不可用，可用工具: %s",
                            step.getStepIndex(), toolName, availableTools));
                }
            } else {
                usedTools.add(toolName.toLowerCase());
                log.info("步骤 {} 工具 '{}' 验证通过", step.getStepIndex(), toolName);
            }

            // 检查工具输入
            if (step.getToolInput() == null || step.getToolInput().isEmpty()) {
                issues.add(String.format("步骤 %d: 缺少工具输入参数", step.getStepIndex()));
            }
        }

        // 6. 判断是否可行
        boolean feasible = issues.isEmpty();

        String validationResult;
        if (feasible) {
            validationResult = "计划验证通过，可以执行";
            log.info("计划验证通过，使用了 {} 个不同工具", usedTools.size());
            state.resetValidationFailures();
            state.updatePlanHash();
        } else {
            validationResult = "计划验证失败:\n" + String.join("\n", issues);
            log.warn("计划验证失败: {}", issues);
            state.incrementValidationFailure();
        }

        state.setValidationResult(validationResult);
        state.setPlanFeasible(feasible);

        log.info("验证结果: {}, 可行: {}, 连续失败次数: {}",
                validationResult, feasible, state.getConsecutiveValidationFailures());

        return state.toMap();
    }

    /**
     * 查找相似的工具名
     */
    private String findSimilarTool(String toolName, List<String> availableTools) {
        String lowerToolName = toolName.toLowerCase();
        for (String available : availableTools) {
            if (available.toLowerCase().contains(lowerToolName) ||
                    lowerToolName.contains(available.toLowerCase())) {
                return available;
            }
        }
        // 检查首字母相同的工具
        if (!toolName.isEmpty()) {
            char firstChar = Character.toLowerCase(toolName.charAt(0));
            for (String available : availableTools) {
                if (!available.isEmpty() &&
                        Character.toLowerCase(available.charAt(0)) == firstChar) {
                    return available;
                }
            }
        }
        return null;
    }
}
