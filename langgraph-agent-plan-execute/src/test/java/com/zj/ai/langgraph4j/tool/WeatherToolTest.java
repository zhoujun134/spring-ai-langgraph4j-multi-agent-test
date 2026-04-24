package com.zj.ai.langgraph4j.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 天气工具测试
 *
 * @author zj
 * @date 2026/04/24
 */
class WeatherToolTest {

    private WeatherTool weatherTool;

    @BeforeEach
    void setUp() {
        weatherTool = new WeatherTool();
    }

    @Test
    void testQueryWeatherBeijing() {
        String result = weatherTool.queryWeather("北京");
        assertNotNull(result);
        assertTrue(result.contains("北京"));
        assertTrue(result.contains("天气"));
    }

    @Test
    void testQueryWeatherShanghai() {
        String result = weatherTool.queryWeather("上海");
        assertNotNull(result);
        assertTrue(result.contains("上海"));
        assertTrue(result.contains("天气"));
    }

    @Test
    void testQueryWeatherUnknownCity() {
        String result = weatherTool.queryWeather("未知城市");
        assertNotNull(result);
        assertTrue(result.contains("天气"));
    }

    @Test
    void testQueryMultipleCities() {
        String result = weatherTool.queryMultipleCities("北京,上海,广州");
        assertNotNull(result);
        assertTrue(result.contains("北京"));
        assertTrue(result.contains("上海"));
        assertTrue(result.contains("广州"));
    }

    @Test
    void testQueryMultipleCitiesWithChineseComma() {
        String result = weatherTool.queryMultipleCities("北京，上海，广州");
        assertNotNull(result);
        assertTrue(result.contains("北京"));
        assertTrue(result.contains("上海"));
        assertTrue(result.contains("广州"));
    }
}
