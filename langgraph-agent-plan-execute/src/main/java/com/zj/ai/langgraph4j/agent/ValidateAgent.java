package com.zj.ai.langgraph4j.agent;

import com.zj.ai.langgraph4j.domain.dto.PlanStep;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import com.zj.ai.langgraph4j.service.DynamicModelManager;
import com.zj.ai.langgraph4j.service.ToolRegistryService;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 计划验证 Agent
 * 验证计划的可行性
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
public class ValidateAgent implements NodeAction<PlanExecuteState> {

    private final DynamicModelManager modelManager;
    private final ToolRegistryService toolRegistry;

    public ValidateAgent(DynamicModelManager modelManager, ToolRegistryService toolRegistry) {
        this.modelManager = modelManager;
        this.toolRegistry = toolRegistry;
    }

    @Override
    public Map<String, Object> apply(PlanExecuteState state) throws Exception {
        log.info("=== ValidateAgent: 开始验证计划 ===");

        List<String> issues = new ArrayList<>();

        // 1. 检查是否任务已经结束
        if (state.hasFinalAnswer()) {
            issues.add("没有可执行的计划");
            state.setValidationResult("计划为空，无法执行");
            state.setPlanFeasible(false);
            return state.toMap();
        }
        // 2. 检查是否有计划
        if (!state.hasPlan()) {
            issues.add("没有可执行的计划");
            state.setValidationResult("计划为空，无法执行");
            state.setPlanFeasible(false);
            return state.toMap();
        }

        // 3. 获取可用工具列表
        List<String> availableTools = toolRegistry.getToolNames();
        log.info("可用工具列表: {}", availableTools);

        // 3. 检查每个步骤的工具是否可用
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
                issues.add(String.format("步骤 %d: 工具 '%s' 不可用，可用工具: %s",
                        step.getStepIndex(), toolName, availableTools));
            } else {
                log.info("步骤 {} 工具 '{}' 验证通过", step.getStepIndex(), toolName);
            }

            // 检查工具输入
            if (step.getToolInput() == null || step.getToolInput().isEmpty()) {
                issues.add(String.format("步骤 %d: 缺少工具输入参数", step.getStepIndex()));
            }
        }

        // 4. 判断是否可行
        boolean feasible = issues.isEmpty();

        String validationResult;
        if (feasible) {
            validationResult = "计划验证通过，可以执行";
            log.info("计划验证通过");
        } else {
            validationResult = "计划验证失败:\n" + String.join("\n", issues);
            log.warn("计划验证失败: {}", issues);
        }

        state.setValidationResult(validationResult);
        state.setPlanFeasible(feasible);

        log.info("验证结果: {}, 可行: {}", validationResult, feasible);

        return state.toMap();
    }
}
