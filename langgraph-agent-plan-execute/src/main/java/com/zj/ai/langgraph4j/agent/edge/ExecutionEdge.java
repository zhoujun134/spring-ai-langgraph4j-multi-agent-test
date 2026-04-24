package com.zj.ai.langgraph4j.agent.edge;

import com.zj.ai.langgraph4j.agent.action.NodeActionEnum;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.EdgeAction;
import org.springframework.stereotype.Component;

/**
 * 执行条件边
 * 根据执行结果决定下一步操作
 *
 * 决策逻辑:
 * 1. 如果已完成（成功或部分成功）→ END
 * 2. 如果需要重新规划且可以重规划 → RE_PLAN
 * 3. 否则 → END
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
public class ExecutionEdge implements EdgeAction<PlanExecuteState> {

    @Override
    public String apply(PlanExecuteState state) throws Exception {
        log.info("=== ExecutionEdge: 判断执行后操作 ===");
        log.info("已完成: {}, 需要重规划: {}, 连续执行失败: {}, 重规划次数: {}/{}",
                state.isCompleted(),
                state.isNeedReplan(),
                state.getConsecutiveExecutionFailures(),
                state.getRePlanCount(), state.getMaxRePlanAttempts());

        // 1. 如果已完成，结束
        if (state.isCompleted()) {
            log.info("执行完成，进入结束阶段");
            return NodeActionEnum.END.getName();
        }

        // 2. 如果需要重新规划
        if (state.isNeedReplan()) {
            // 检查是否超过最大失败阈值
            if (state.hasExceededMaxFailures()) {
                log.warn("已超过最大失败阈值，终止执行");
                state.setErrorMessage("多次执行失败，请检查工具配置或简化您的问题");
                return NodeActionEnum.END.getName();
            }

            // 检查是否可以重规划
            if (state.canRePlan()) {
                log.info("执行失败，进入重规划阶段");
                return NodeActionEnum.RE_PLAN.getName();
            }

            // 不能重规划，结束
            log.warn("已达到最大重规划次数，终止执行");
            state.setErrorMessage("执行失败，已达到最大重规划次数");
            return NodeActionEnum.END.getName();
        }

        // 3. 默认结束
        return NodeActionEnum.END.getName();
    }
}
