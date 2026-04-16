package com.zj.ai.langgraph4j.edge;

import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.EdgeAction;
import org.springframework.stereotype.Component;

/**
 * 计划验证条件边
 * 根据验证结果决定下一步操作
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
        log.info("计划可行性: {}, 重规划次数: {}/{}",
                state.isPlanFeasible(), state.getRePlanCount(), state.getMaxRePlanAttempts());

        // 1. 如果计划可行，执行
        if (state.isPlanFeasible()) {
            log.info("计划验证通过，进入执行阶段");
            return "execute";
        }

        // 2. 如果超过最大重规划次数，结束并返回错误
        if (!state.canReplan()) {
            log.warn("已达到最大重规划次数，终止执行");
            state.setErrorMessage("计划验证失败，已达到最大重规划次数: " + state.getMaxRePlanAttempts());
            return "end";
        }

        // 3. 否则重新规划
        log.info("计划验证失败，进入重规划阶段");
        return "replan";
    }
}
