package com.zj.ai.langgraph4j.config;

import com.zj.ai.common.sdk.dotenv.DotEnvUtils;
import com.zj.ai.common.sdk.json.JSONUtils;
import com.zj.ai.langgraph4j.domain.entity.ModelConfigEntity;
import com.zj.ai.langgraph4j.domain.entity.ToolConfigEntity;
import com.zj.ai.langgraph4j.repository.ModelConfigRepository;
import com.zj.ai.langgraph4j.repository.ToolConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 在应用启动时初始化模型配置和工具配置
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ModelConfigRepository modelConfigRepository;
    private final ToolConfigRepository toolConfigRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("=== 开始初始化数据 ===");

        // 初始化模型配置
        initModelConfigs();

        // 初始化工具配置
        initToolConfigs();

        log.info("=== 数据初始化完成 ===");
    }

    /**
     * 初始化模型配置
     */
    private void initModelConfigs() {
        if (modelConfigRepository.count() > 0) {
            log.info("模型配置已存在，跳过初始化");
            return;
        }

        log.info("初始化模型配置...");

        // 从环境变量获取 Ollama 配置
        String ollamaBaseUrl = DotEnvUtils.getDotEnvValue("OLLAMA_BASE_URL");
        String ollamaModel = DotEnvUtils.getDotEnvValue("OLLAMA_CHAT_MODEL");
        log.info("ollamaBaseUrl: {}, ollamaModel:{}", ollamaBaseUrl, ollamaModel);

        // 创建 Ollama 模型配置
        ModelConfigEntity ollamaConfig = new ModelConfigEntity();
        ollamaConfig.setModelName("gemma4");
        ollamaConfig.setProvider("ollama");
        ollamaConfig.setBaseUrl(ollamaBaseUrl != null ? ollamaBaseUrl : "http://localhost:11434");
        ollamaConfig.setModelId(ollamaModel != null ? ollamaModel : "gemma4-26b-q3");
        ollamaConfig.setTemperature(0.7);
        ollamaConfig.setMaxTokens(64000);
        ollamaConfig.setEnabled(true);
        ollamaConfig.setIsDefault(true);

        modelConfigRepository.save(ollamaConfig);
        log.info("已创建 Ollama 模型配置: {}", JSONUtils.toJSONString(ollamaConfig));
    }

    /**
     * 初始化工具配置
     */
    private void initToolConfigs() {
        if (toolConfigRepository.count() > 0) {
            log.info("工具配置已存在，跳过初始化");
            return;
        }

        log.info("初始化工具配置...");

        // 计算器工具
        ToolConfigEntity calculatorTool = new ToolConfigEntity();
        calculatorTool.setToolName("calculator");
        calculatorTool.setDescription("执行数学计算，支持加减乘除运算。输入数学表达式，返回计算结果。");
        calculatorTool.setClassName("com.zj.ai.langgraph4j.tool.CalculatorTool");
        calculatorTool.setMethodName("calculate");
        calculatorTool.setEnabled(true);
        calculatorTool.setPriority(1);
        toolConfigRepository.save(calculatorTool);

        // 天气工具
        ToolConfigEntity weatherTool = new ToolConfigEntity();
        weatherTool.setToolName("weather");
        weatherTool.setDescription("查询指定城市的天气情况，返回天气描述和温度信息。");
        weatherTool.setClassName("com.zj.ai.langgraph4j.tool.WeatherTool");
        weatherTool.setMethodName("queryWeather");
        weatherTool.setEnabled(true);
        weatherTool.setPriority(2);
        toolConfigRepository.save(weatherTool);

        // 搜索工具
        ToolConfigEntity searchTool = new ToolConfigEntity();
        searchTool.setToolName("search");
        searchTool.setDescription("在网络上搜索信息，返回相关结果列表。");
        searchTool.setClassName("com.zj.ai.langgraph4j.tool.SearchTool");
        searchTool.setMethodName("search");
        searchTool.setEnabled(true);
        searchTool.setPriority(3);
        toolConfigRepository.save(searchTool);

        log.info("已创建 {} 个工具配置", 3);
    }
}
