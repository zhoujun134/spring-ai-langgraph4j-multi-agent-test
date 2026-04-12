package com.zj.ai.langgraph4j.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 天气查询工具
 * 模拟天气查询功能
 *
 * @author zj
 * @date 2026/04/12
 */
@Component
public class WeatherTool {

    /**
     * 模拟的天气数据
     */
    private final Map<String, String> weatherData = new HashMap<>();

    public WeatherTool() {
        // 初始化模拟数据
        weatherData.put("北京", "晴天，温度 15°C，空气质量良好");
        weatherData.put("上海", "多云，温度 18°C，有轻微雾霾");
        weatherData.put("广州", "小雨，温度 22°C，湿度较高");
        weatherData.put("深圳", "晴天，温度 24°C，适合外出");
        weatherData.put("杭州", "阴天，温度 16°C，可能有雨");
    }

    /**
     * 查询天气
     *
     * @param city 城市名称
     * @return 天气信息
     */
    @Tool("查询指定城市的天气情况，返回天气描述和温度信息")
    public String queryWeather(@P("城市名称，如 '北京'、'上海'") String city) {
        // 先从模拟数据中查找
        if (weatherData.containsKey(city)) {
            return String.format("【%s天气】%s", city, weatherData.get(city));
        }

        // 如果没有找到，生成随机天气
        String[] weathers = {"晴天", "多云", "阴天", "小雨", "大雨"};
        String[] airs = {"优", "良", "轻度污染"};

        Random random = new Random();
        String weather = weathers[random.nextInt(weathers.length)];
        int temp = 10 + random.nextInt(20);
        String air = airs[random.nextInt(airs.length)];

        return String.format("【%s天气】%s，温度 %d°C，空气质量%s", city, weather, temp, air);
    }

    /**
     * 查询多城市天气
     *
     * @param cities 城市列表，用逗号分隔
     * @return 多城市天气信息
     */
    @Tool("批量查询多个城市的天气情况")
    public String queryMultipleCities(@P("城市列表，用逗号分隔，如 '北京,上海,广州'") String cities) {
        StringBuilder result = new StringBuilder("多城市天气查询结果:\n");

        for (String city : cities.split("[,，]")) {
            String trimmedCity = city.trim();
            result.append(queryWeather(trimmedCity)).append("\n");
        }

        return result.toString();
    }
}
