package com.zj.ai.langgraph4j;

import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import org.bsc.langgraph4j.CompiledGraph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Plan-Execute 工作流测试
 *
 * @author zj
 * @date 2026/04/12
 */
@SpringBootTest
class PlanExecuteWorkflowTest {

    @Autowired
    private CompiledGraph<PlanExecuteState> planExecuteWorkflow;

    @Test
    void testCalculatorQuery() {
        System.out.println("\n=== 测试: 数学计算查询 ===\n");

        // 创建初始状态
        PlanExecuteState state = new PlanExecuteState();
        state.setUserQuery("计算 25 * 4 + 10 的结果");
        state.setMaxReplanAttempts(3);

        // 执行工作流
        PlanExecuteState result = planExecuteWorkflow.invoke(state.toMap())
                .orElseThrow(() -> new AssertionError("工作流执行返回空结果"));

        // 验证结果
        assertNotNull(result.getFinalAnswer(), "最终答案不应为空");
        assertTrue(result.isCompleted(), "应标记为已完成");
        System.out.println("\n最终答案:\n" + result.getFinalAnswer());
    }

    @Test
    void testWeatherQuery() {
        System.out.println("\n=== 测试: 天气查询 ===\n");

        // 创建初始状态
        PlanExecuteState state = new PlanExecuteState();
        state.setUserQuery("北京今天天气怎么样？");
        state.setMaxReplanAttempts(3);

        // 执行工作流
        PlanExecuteState result = planExecuteWorkflow.invoke(state.toMap())
                .orElseThrow(() -> new AssertionError("工作流执行返回空结果"));

        // 验证结果
        assertNotNull(result.getFinalAnswer(), "最终答案不应为空");
        assertTrue(result.isCompleted(), "应标记为已完成");
        System.out.println("\n最终答案:\n" + result.getFinalAnswer());
    }

    @Test
    void testSearchQuery() {
        System.out.println("\n=== 测试: 搜索查询 ===\n");

        // 创建初始状态
        PlanExecuteState state = new PlanExecuteState();
        state.setUserQuery("搜索关于人工智能的最新资讯");
        state.setMaxReplanAttempts(3);

        // 执行工作流
        PlanExecuteState result = planExecuteWorkflow.invoke(state.toMap())
                .orElseThrow(() -> new AssertionError("工作流执行返回空结果"));

        // 验证结果
        assertNotNull(result.getFinalAnswer(), "最终答案不应为空");
        assertTrue(result.isCompleted(), "应标记为已完成");
        System.out.println("\n最终答案:\n" + result.getFinalAnswer());
    }

    @Test
    void testComplexQuery() {
        System.out.println("\n=== 测试: 复合查询 ===\n");

        // 创建初始状态
        PlanExecuteState state = new PlanExecuteState();
        state.setUserQuery("帮我计算 100 / 5 的结果，然后查询上海的天气");
        state.setMaxReplanAttempts(3);

        // 执行工作流
        PlanExecuteState result = planExecuteWorkflow.invoke(state.toMap())
                .orElseThrow(() -> new AssertionError("工作流执行返回空结果"));

        // 验证结果
        assertNotNull(result.getFinalAnswer(), "最终答案不应为空");
        assertTrue(result.isCompleted(), "应标记为已完成");
        System.out.println("\n最终答案:\n" + result.getFinalAnswer());
    }
}
