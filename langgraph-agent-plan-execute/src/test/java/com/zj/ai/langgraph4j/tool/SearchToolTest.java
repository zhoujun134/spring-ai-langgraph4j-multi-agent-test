package com.zj.ai.langgraph4j.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 搜索工具测试
 *
 * @author zj
 * @date 2026/04/24
 */
class SearchToolTest {

    private SearchTool searchTool;

    @BeforeEach
    void setUp() {
        searchTool = new SearchTool();
    }

    @Test
    void testSearch() {
        String result = searchTool.search("人工智能");
        assertNotNull(result);
        assertTrue(result.contains("人工智能"));
        assertTrue(result.contains("结果"));
    }

    @Test
    void testSearchWithEmptyQuery() {
        String result = searchTool.search("");
        assertNotNull(result);
    }

    @Test
    void testSearchNews() {
        String result = searchTool.searchNews("科技");
        assertNotNull(result);
        assertTrue(result.contains("新闻"));
    }

    @Test
    void testSearchResultFormat() {
        String result = searchTool.search("Java");
        assertNotNull(result);
        assertTrue(result.contains("1."));
        assertTrue(result.contains("2."));
    }
}
