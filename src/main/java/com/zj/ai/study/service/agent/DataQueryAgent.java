package com.zj.ai.study.service.agent;

import com.zj.ai.study.domain.dto.AnalysisTaskState;
import com.zj.ai.study.service.TestDataInitializer;
import com.zj.ai.study.utils.JSONUtils;
import org.bsc.langgraph4j.action.NodeAction;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * 数据查询 Agent：根据拆解的条件查询数据库，获取原始数据
 */
@Component
public class DataQueryAgent implements NodeAction<AnalysisTaskState> {

    private final JdbcTemplate jdbcTemplate;
    private final ChatModel chatModel;

    // 注入 Spring AI 的数据库工具（简化 SQL 执行）
    public DataQueryAgent(JdbcTemplate jdbcTemplate, ChatModel chatModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatModel = chatModel;
    }

    public AnalysisTaskState query(AnalysisTaskState state) {
        String timeRange = state.getTimeRange();
        String targetIndicator = state.getTargetIndicator();
        String analysisDimension = state.getAnalysisDimension();

        // 1. 解析时间范围为数据库查询条件（以 Q3 为例，转换为 7-9 月）
        LocalDate startDate = LocalDate.of(2025, 7, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 30);
        if (timeRange.contains("Q3")) {
            startDate = LocalDate.of(2025, 7, 1);
            endDate = LocalDate.of(2025, 9, 30);
        }

        // 2. 构建 SQL（根据指标和维度动态生成）
//        String sql = buildQuerySql(targetIndicator, analysisDimension, startDate, endDate);
//        System.out.println("执行 SQL：" + sql);

        // 3. 调用数据库工具执行查询
//        List<Map<String, Object>> resultList = jdbcTemplate.query(sql, new ColumnMapRowMapper());
        String rawData = JSONUtils.toJSONString(TestDataInitializer.orders);
        System.out.println("查询原始数据：" + rawData);

        // 4. 返回更新后的状态
        return state.withRawData(rawData);
    }

    // 动态构建 SQL
    private String buildQuerySql(String indicator, String dimension, LocalDate start, LocalDate end) {
        String indicatorColumn = "quantity"; // 默认销量
        if (indicator.contains("销售额")) {
            indicatorColumn = "amount";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return String.format("""
                SELECT '%s' AS dimension, SUM(%s) AS total
                FROM orders
                WHERE order_date BETWEEN '%s' AND '%s'
                GROUP BY '%s'
                ORDER BY total DESC
                """, dimension, indicatorColumn, start.format(formatter), end.format(formatter), dimension);
    }

    @Override
    public Map<String, Object> apply(AnalysisTaskState analysisTaskState) throws Exception {
        AnalysisTaskState result = this.query(analysisTaskState);
        return result.toMap();
    }
}
