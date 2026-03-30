# LangChain4j Agent 开发中文手册

## 一、核心概念

### 1. Agent 定义
Agent 是能够**自主决策**的 AI 系统，通过以下组件协同工作：
- **LLM（大语言模型）**：核心推理引擎
- **Tools（工具）**：扩展 Agent 能力（搜索、计算、API调用等）
- **Memory（记忆）**：保存对话历史
- **Planning（规划）**：拆解复杂任务

### 2. 两种开发模式

#### 模式一：简单 Agent（使用 `AiServices`）
适合单一任务场景，无需复杂流程编排。

```java
// 1. 定义工具接口
interface Calculator {
    @Tool("计算数学表达式")
    double calculate(@P("表达式") String expression);
}

// 2. 创建 Agent
Calculator agent = AiServices.builder(Calculator.class)
    .chatModel(ChatModel.from(OpenAiChatModel.withApiKey("key")))
    .build();

// 3. 调用
String result = agent.calculate("123 * 456");
```

#### 模式二：复杂 Agent 工作流（使用 LangGraph4j）
适合多步骤、需要状态管理和条件分支的场景。

---

## 二、LangGraph4j 多 Agent 工作流实战

### 1. 核心组件

| 组件 | 职责 | 示例 |
|------|------|------|
| **AgentState** | 全局状态载体，所有 Agent 共享数据 | `AnalysisTaskState` |
| **NodeAction** | 节点执行逻辑，每个 Agent 实现此接口 | `TaskDecompositionAgent` |
| **EdgeAction** | 边上的条件判断，决定流程走向 | `RetryEdgeAction` |
| **StateGraph** | 定义节点和边的流程图 | `AgentWorkflowConfig` |
| **CompiledGraph** | 编译后的可执行工作流 | `analysisWorkflow` Bean |

### 2. 完整示例：智能数据分析报告系统

#### 步骤 1：定义全局状态

```java
@Getter @Setter
public class AnalysisTaskState extends AgentState {
    private String userQuery;          // 用户原始需求
    private String timeRange;          // 时间范围（任务拆解结果）
    private String targetIndicator;    // 目标指标
    private String analysisDimension;  // 分析维度
    private String rawData;            // 查询的原始数据
    private String analysisResult;     // 分析结论
    private String reportDraft;        // 报告草稿
    private String finalReport;        // 最终报告
    private boolean needRetry;         // 是否需要重试

    // 状态更新方法（返回新实例）
    public AnalysisTaskState withTimeRange(String timeRange) { ... }
    public AnalysisTaskState withRawData(String rawData) { ... }
    // ... 其他 with 方法
}
```

#### 步骤 2：实现各个 Agent

**任务拆解 Agent**
```java
@Component
public class TaskDecompositionAgent implements NodeAction<AnalysisTaskState> {
    private final ChatModel chatModel;

    @Override
    public Map<String, Object> apply(AnalysisTaskState state) {
        String prompt = String.format("""
            请解析用户需求，提取以下信息（JSON格式）：
            1. timeRange：时间范围
            2. targetIndicator：目标指标
            3. analysisDimension：分析维度

            用户需求：%s
            """, state.getUserQuery());

        String jsonResult = chatModel.call(prompt);
        return parseJsonToState(state, jsonResult).toMap();
    }
}
```

**数据查询 Agent**
```java
@Component
public class DataQueryAgent implements NodeAction<AnalysisTaskState> {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> apply(AnalysisTaskState state) {
        // 根据拆解结果构建 SQL 查询
        String sql = buildQuerySql(state.getTargetIndicator(),
                                   state.getAnalysisDimension(),
                                   state.getTimeRange());
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
        return state.withRawData(JSONUtils.toJSONString(data)).toMap();
    }
}
```

**数据分析 Agent**
```java
@Component
public class DataAnalysisAgent implements NodeAction<AnalysisTaskState> {
    @Override
    public Map<String, Object> apply(AnalysisTaskState state) {
        String prompt = String.format("""
            分析以下数据（%s按%s统计）：
            %s

            请：
            1. 找出前3名
            2. 计算占比
            3. 分析原因
            """, state.getTargetIndicator(),
               state.getAnalysisDimension(),
               state.getRawData());

        String analysis = chatModel.call(prompt);
        return state.withAnalysisResult(analysis).toMap();
    }
}
```

**报告生成 Agent**
```java
@Component
public class ReportGenerationAgent implements NodeAction<AnalysisTaskState> {
    @Override
    public Map<String, Object> apply(AnalysisTaskState state) {
        String prompt = String.format("""
            根据分析结论生成正式报告：
            - 标题包含时间范围和分析主题
            - 核心数据（前3名）
            - 原因分析
            - 业务建议（2-3条）

            分析结论：%s
            """, state.getAnalysisResult());

        String report = chatModel.call(prompt);
        return state.withReportDraft(report).toMap();
    }
}
```

**结果校验 Agent**
```java
@Component
public class ResultValidationAgent implements NodeAction<AnalysisTaskState> {
    @Override
    public Map<String, Object> apply(AnalysisTaskState state) {
        String prompt = String.format("""
            判断报告是否满足用户需求，仅返回「符合」或「不符合」：

            用户需求：%s
            报告内容：%s
            """, state.getUserQuery(), state.getReportDraft());

        String result = chatModel.call(prompt);
        boolean needRetry = "不符合".equals(result.trim());

        if (needRetry) {
            return state.withNeedRetry(true).toMap();
        } else {
            return state.withFinalReport(state.getReportDraft())
                       .withNeedRetry(false).toMap();
        }
    }
}
```

#### 步骤 3：配置工作流

```java
@Configuration
public class AgentWorkflowConfig {

    @Bean
    public CompiledGraph<AnalysisTaskState> analysisWorkflow(
            TaskDecompositionAgent decompositionAgent,
            DataQueryAgent dataQueryAgent,
            DataAnalysisAgent dataAnalysisAgent,
            ReportGenerationAgent reportGenerationAgent,
            ResultValidationAgent validationAgent,
            RetryEdgeAction retryEdgeAction,
            AnalysisTaskStateFactory stateFactory) throws GraphStateException {

        // 1. 创建状态图
        StateGraph<AnalysisTaskState> graph = new StateGraph<>(stateFactory);

        // 2. 添加节点
        graph.addNode("decompose", node_async(decompositionAgent));
        graph.addNode("query", node_async(dataQueryAgent));
        graph.addNode("analyze", node_async(dataAnalysisAgent));
        graph.addNode("generateReport", node_async(reportGenerationAgent));
        graph.addNode("validate", node_async(validationAgent));

        // 3. 定义边（流程）
        graph.addEdge(START, "decompose");
        graph.addEdge("decompose", "query");
        graph.addEdge("query", "analyze");
        graph.addEdge("analyze", "generateReport");
        graph.addEdge("generateReport", "validate");

        // 4. 条件分支（校验不通过则重试）
        graph.addConditionalEdges(
            "validate",
            AsyncEdgeAction.edge_async(retryEdgeAction),
            Map.of(
                "retry", "decompose",  // 需要重试 → 回到任务拆解
                "end", END             // 通过 → 结束
            )
        );

        return graph.compile();
    }
}
```

#### 步骤 4：执行工作流

```java
@SpringBootTest
public class WorkflowTest {

    @Autowired
    private CompiledGraph<AnalysisTaskState> analysisWorkflow;

    @Test
    void testWorkflow() {
        // 1. 创建初始状态
        AnalysisTaskState initialState = new AnalysisTaskState();
        initialState.setUserQuery("分析2025年Q3电商订单数据，生成销量Top3品类及增长原因");

        // 2. 执行工作流
        AnalysisTaskState finalState = analysisWorkflow.invoke(initialState.toMap())
            .orElse(new AnalysisTaskState());

        // 3. 输出结果
        System.out.println(finalState.getFinalReport());
    }
}
```

---

## 三、最佳实践

### 1. 提示词设计原则
- **明确指令**：告诉 AI 具体要做什么
- **提供上下文**：包含相关背景信息
- **指定格式**：要求返回特定格式（JSON、列表等）
- **限制长度**：避免生成过长内容

### 2. 状态管理
- 使用不可变对象（每次更新返回新实例）
- 通过 `withXxx()` 方法链式更新
- 确保状态可序列化（便于持久化）

### 3. 错误处理
- 添加重试机制（如示例中的 `needRetry`）
- 设置超时和最大重试次数
- 记录详细日志便于调试

### 4. 性能优化
- 使用异步节点（`node_async()`）
- 考虑缓存常用结果
- 批量处理数据减少 API 调用

---

## 四、依赖配置

```xml
<!-- LangGraph4j 核心 -->
<dependency>
    <groupId>org.bsc.langgraph4j</groupId>
    <artifactId>langgraph4j-core</artifactId>
    <version>1.7.4</version>
</dependency>

<!-- Spring AI 适配器 -->
<dependency>
    <groupId>org.bsc.langgraph4j</groupId>
    <artifactId>langgraph4j-spring-ai</artifactId>
    <version>1.7.4</version>
</dependency>

<!-- Spring AI 模型（根据需求选择） -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-ollama</artifactId>
</dependency>
```

---

## 五、总结

LangChain4j 提供了两种 Agent 开发方式：
1. **简单场景**：使用 `AiServices` 快速构建
2. **复杂工作流**：使用 LangGraph4j 进行多 Agent 编排

核心思想是**将复杂任务拆解为多个专门的 Agent**，通过状态图管理执行流程，每个 Agent 专注于自己的职责，最终协同完成复杂任务。
