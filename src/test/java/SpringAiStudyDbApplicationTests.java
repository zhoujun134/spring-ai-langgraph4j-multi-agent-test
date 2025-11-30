import com.zj.ai.study.MutilAgentApplication;
import com.zj.ai.study.domain.dto.AnalysisTaskState;
import jakarta.transaction.Transactional;
import org.bsc.langgraph4j.CompiledGraph;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MutilAgentApplication.class)
@Transactional // 测试结束后自动回滚事务，避免污染数据库
public class SpringAiStudyDbApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private CompiledGraph<AnalysisTaskState> analysisWorkflow;

    @Test
    void testAnalysisWorkflow() {
        // 1. 构造用户需求（初始状态）
        String userQuery = "分析2025年Q3电商订单数据，生成销量Top3品类及增长原因，给出业务建议";
        AnalysisTaskState initialState = new AnalysisTaskState();
        initialState.setUserQuery(userQuery);

        // 2. 执行多智能体流程（LangGraph4j 自动按图流转）
        AnalysisTaskState finalState = analysisWorkflow.invoke(initialState.toMap())
                .orElse(new AnalysisTaskState());

        // 3. 输出最终报告
        System.out.println("\n==================== 最终数据分析报告 ====================");
        System.out.println(finalState.getFinalReport());
    }

}
