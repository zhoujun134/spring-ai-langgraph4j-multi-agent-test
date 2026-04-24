package com.zj.ai.langgraph4j.agent.edge;

import com.zj.ai.langgraph4j.agent.action.NodeActionEnum;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.EdgeAction;
import org.springframework.stereotype.Component;

/**
 * 计划验证条件边
 * 根据验证结果决定下一步操作
 *
 * 决策逻辑:
 * 1. 如果已完成（PlanAgent直接给出答案）→ END
 * 2. 如果计划可行 → EXECUTE
 * 3. 如果连续失败次数过多 → END（避免死循环）
 * 4. 如果可以重规划 → RE_PLAN
 * 5. 否则 → END
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
public class PlanValidationEdge implements EdgeAction<PlanExecuteState> {

    @Override
    public String apply(PlanExecuteState state) throws Exception {
        log.info("=== PlanValidationEdge: 判断下一步操作 ===");
        log.info("计划可行性: {}, 重规划次数: {}/{}, 连续验证失败: {}, 连续执行失败: {}",
                state.isPlanFeasible(),
                state.getRePlanCount(), state.getMaxRePlanAttempts(),
                state.getConsecutiveValidationFailures(),
                state.getConsecutiveExecutionFailures());

        // 1. 如果已完成，直接结束
        if (state.isCompleted()) {
            log.info("任务已完成，进入结束阶段");
            return NodeActionEnum.END.getName();
        }

        // 2. 如果计划可行，执行
        if (state.isPlanFeasible()) {
            log.info("计划验证通过，进入执行阶段");
            return NodeActionEnum.EXECUTE.getName();
        }

        // 3. 检查是否超过最大失败阈值
        if (state.hasExceededMaxFailures()) {
            log.warn("已超过最大失败阈值，终止执行");
            state.setErrorMessage("多次尝试后仍无法生成有效计划，请简化您的问题或稍后重试");
            return NodeActionEnum.END.getName();
        }

        // 4. 检查是否超过最大重规划次数
        if (!state.canRePlan()) {
            log.warn("已达到最大重规划次数 {}，终止执行", state.getMaxRePlanAttempts());
            state.setErrorMessage("计划验证失败，已达到最大重规划次数: " + state.getMaxRePlanAttempts());
            return NodeActionEnum.END.getName();
        }

        // 5. 进入重规划
        log.info("计划验证失败，进入重规划阶段 (剩余次数: {})",
                state.getMaxRePlanAttempts() - state.getRePlanCount());
        return NodeActionEnum.RE_PLAN.getName();
    }
}
