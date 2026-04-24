package com.zj.ai.langgraph4j.agent;

import com.zj.ai.langgraph4j.domain.dto.ExecutionResult;
import com.zj.ai.langgraph4j.domain.dto.PlanStep;
import com.zj.ai.langgraph4j.domain.state.PlanExecuteState;
import com.zj.ai.langgraph4j.service.DynamicModelManager;
import com.zj.ai.langgraph4j.service.ToolRegistryService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 计划执行 Agent
 * 按步骤执行计划，调用工具，支持部分失败处理
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
public class ExecuteAgent implements NodeAction<PlanExecuteState> {

    private final ToolRegistryService toolRegistry;
    private final DynamicModelManager modelManager;

    public ExecuteAgent(ToolRegistryService toolRegistry, DynamicModelManager modelManager) {
        this.toolRegistry = toolRegistry;
        this.modelManager = modelManager;
    }

    @Override
    public Map<String, Object> apply(PlanExecuteState state) throws Exception {
        log.info("=== ExecuteAgent: 开始执行计划 ===");

        List<ExecutionResult> results = new ArrayList<>();
        StringBuilder executionLog = new StringBuilder();
        int successCount = 0;
        int failureCount = 0;

        // 遍历执行每个步骤
        for (PlanStep step : state.getPlanSteps()) {
            log.info("执行步骤 {}: {} - 工具: {} - 输入: {}",
                    step.getStepIndex(), step.getDescription(), step.getToolName(), step.getToolInput());

            // 标记步骤为执行中
            step.markExecuting();

            try {
                // 执行工具
                Object result = toolRegistry.executeTool(step.getToolName(), step.getToolInput());

                // 记录成功结果
                ExecutionResult execResult = ExecutionResult.success(
                        step.getStepIndex(),
                        step.getToolName(),
                        result
                );
                results.add(execResult);

                // 标记步骤完成
                step.markCompleted();
                successCount++;

                executionLog.append(String.format("步骤 %d 执行成功: %s\n结果: %s\n\n",
                        step.getStepIndex(), step.getDescription(), result));

                log.info("步骤 {} 执行成功，结果: {}", step.getStepIndex(), result);

            } catch (Exception e) {
                // 记录失败结果
                ExecutionResult execResult = ExecutionResult.failure(
                        step.getStepIndex(),
                        step.getToolName(),
                        e.getMessage()
                );
                results.add(execResult);

                // 标记步骤失败
                step.markFailed();
                failureCount++;

                executionLog.append(String.format("步骤 %d 执行失败: %s\n错误: %s\n\n",
                        step.getStepIndex(), step.getDescription(), e.getMessage()));

                log.error("步骤 {} 执行失败: {}", step.getStepIndex(), e.getMessage());
            }
        }

        // 更新状态
        state.setExecutionResults(results);

        // 根据执行结果决定下一步
        if (failureCount == 0) {
            // 全部成功
            state.resetExecutionFailures();
            String finalAnswer = generateFinalAnswer(state, results, executionLog.toString());
            state.setFinalAnswer(finalAnswer);
            state.setCompleted(true);
            log.info("=== 执行完成 (全部成功) ===");

        } else if (successCount > 0) {
            // 部分成功
            String finalAnswer = generatePartialResultAnswer(state, results, executionLog.toString(), successCount, failureCount);
            state.setFinalAnswer(finalAnswer);
            state.setCompleted(true);
            log.info("=== 执行完成 (部分成功: {}/{}) ===", successCount, successCount + failureCount);

        } else {
            // 全部失败
            state.incrementExecutionFailure();
            state.setNeedReplan(true);
            state.setValidationResult(String.format("所有 %d 个步骤都执行失败", failureCount));
            log.warn("=== 执行失败 (需要重新规划) ===");
        }

        log.info("最终答案: {}", state.getFinalAnswer());

        return state.toMap();
    }

    /**
     * 生成最终答案
     */
    private String generateFinalAnswer(PlanExecuteState state, List<ExecutionResult> results, String executionLog) {
        // 如果只有一个结果，直接返回
        if (results.size() == 1 && results.get(0).isSuccess()) {
            return String.format("问题: %s\n\n答案: %s",
                    state.getUserQuery(), results.get(0).getResult());
        }

        // 使用 LLM 整合多个结果
        String summaryPrompt = buildSummaryPrompt(state, executionLog);
        ChatModel model = modelManager.getDefaultChatModel();

        try {
            return model.chat(summaryPrompt);
        } catch (Exception e) {
            log.warn("LLM 整合失败，使用简单汇总: {}", e.getMessage());
            return buildSimpleSummary(state, results);
        }
    }

    /**
     * 生成部分成功的答案
     */
    private String generatePartialResultAnswer(PlanExecuteState state, List<ExecutionResult> results,
                                                String executionLog, int successCount, int failureCount) {
        String summaryPrompt = """
                请根据以下部分执行结果，回答用户的原始问题。

                用户问题: %s

                执行过程:
                %s

                注意: 共执行了 %d 个步骤，其中 %d 个成功，%d 个失败。
                请根据成功的执行结果给出尽可能有帮助的答案。

                请给出简洁、准确的最终答案（不超过200字）:
                """.formatted(state.getUserQuery(), executionLog,
                successCount + failureCount, successCount, failureCount);

        ChatModel model = modelManager.getDefaultChatModel();

        try {
            return model.chat(summaryPrompt);
        } catch (Exception e) {
            log.warn("LLM 整合失败，使用简单汇总: {}", e.getMessage());
            return buildSimpleSummary(state, results) +
                    String.format("\n\n(部分步骤执行失败: %d/%d)", failureCount, successCount + failureCount);
        }
    }

    /**
     * 构建简单汇总
     */
    private String buildSimpleSummary(PlanExecuteState state, List<ExecutionResult> results) {
        StringBuilder answer = new StringBuilder();
        answer.append("问题: ").append(state.getUserQuery()).append("\n\n");
        answer.append("执行结果:\n");

        for (ExecutionResult result : results) {
            if (result.isSuccess()) {
                answer.append(String.format("- %s\n", result.getResult()));
            } else {
                answer.append(String.format("- [失败] %s\n", result.getErrorMessage()));
            }
        }

        return answer.toString();
    }

    /**
     * 构建汇总 Prompt
     */
    private String buildSummaryPrompt(PlanExecuteState state, String executionLog) {
        return """
                请根据以下执行结果，回答用户的原始问题。

                用户问题: %s

                执行过程:
                %s

                请给出简洁、准确的最终答案（不超过200字）:
                """.formatted(state.getUserQuery(), executionLog);
    }
}
