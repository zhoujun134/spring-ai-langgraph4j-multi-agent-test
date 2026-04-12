package com.zj.ai.langgraph4j.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 搜索工具
 * 模拟网络搜索功能
 *
 * @author zj
 * @date 2026/04/12
 */
@Component
public class SearchTool {

    /**
     * 执行搜索
     *
     * @param query 搜索关键词
     * @return 搜索结果
     */
    @Tool("在网络上搜索信息，返回相关结果列表")
    public String search(@P("搜索关键词") String query) {
        // 模拟搜索结果
        List<String> results = generateMockSearchResults(query);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("搜索 \"%s\" 找到 %d 条结果:\n\n", query, results.size()));

        for (int i = 0; i < results.size(); i++) {
            sb.append(String.format("%d. %s\n\n", i + 1, results.get(i)));
        }

        return sb.toString();
    }

    /**
     * 搜索新闻
     *
     * @param keyword 新闻关键词
     * @return 新闻搜索结果
     */
    @Tool("搜索最新新闻资讯")
    public String searchNews(@P("新闻关键词") String keyword) {
        String[] mockNews = {
                "【科技新闻】AI 技术取得重大突破，智能助手功能更加完善",
                "【财经新闻】全球市场波动，投资者关注最新动态",
                "【体育新闻】最新比赛结果出炉，精彩赛事回顾",
                "【娱乐新闻】明星动态，影视作品推荐"
        };

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("搜索新闻 \"%s\" 结果:\n\n", keyword));

        Random random = new Random();
        for (int i = 0; i < 3; i++) {
            sb.append(String.format("%d. %s\n\n", i + 1, mockNews[random.nextInt(mockNews.length)]));
        }

        return sb.toString();
    }

    /**
     * 生成模拟搜索结果
     */
    private List<String> generateMockSearchResults(String query) {
        List<String> results = new ArrayList<>();

        // 根据查询生成相关结果
        results.add(String.format("【百度百科】%s 的定义和详细介绍...", query));
        results.add(String.format("【知乎问答】关于 %s 的热门讨论和回答...", query));
        results.add(String.format("【新闻资讯】%s 相关的最新新闻报道...", query));
        results.add(String.format("【学术论文】%s 领域的研究进展和成果...", query));
        results.add(String.format("【技术文档】%s 的技术实现和应用场景...", query));

        return results;
    }
}
