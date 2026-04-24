package com.zj.ai.langgraph4j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zj.ai.langgraph4j.domain.entity.ToolConfigEntity;
import com.zj.ai.langgraph4j.exception.ToolExecutionException;
import com.zj.ai.langgraph4j.repository.ToolConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册服务
 * 支持动态加载、注册和执行工具
 *
 * @author zj
 * @date 2026/04/12
 */
@Slf4j
@Service
public class ToolRegistryService {

    private final ToolConfigRepository repository;
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    /**
     * 工具实例缓存
     */
    private final Map<String, Object> toolInstances = new ConcurrentHashMap<>();

    /**
     * 工具方法缓存
     */
    private final Map<String, Method> toolMethods = new ConcurrentHashMap<>();

    @Autowired
    public ToolRegistryService(ToolConfigRepository repository, ApplicationContext applicationContext) {
        this.repository = repository;
        this.applicationContext = applicationContext;
        this.objectMapper = new ObjectMapper();
    }

    // ==================== 工具查询 ====================

    /**
     * 获取所有启用的工具
     */
    public List<ToolConfigEntity> getEnabledTools() {
        return repository.findByEnabledTrueOrderByPriorityAsc();
    }

    /**
     * 获取工具配置
     */
    public Optional<ToolConfigEntity> getToolConfig(String toolName) {
        return repository.findByToolName(toolName);
    }

    /**
     * 检查工具是否可用
     */
    public boolean isToolAvailable(String toolName) {
        return repository.findByToolNameAndEnabledTrue(toolName).isPresent();
    }

    /**
     * 获取所有工具名称
     */
    public List<String> getToolNames() {
        return getEnabledTools().stream()
                .map(ToolConfigEntity::getToolName)
                .toList();
    }

    // ==================== 工具执行 ====================

    /**
     * 执行工具
     *
     * @param toolName  工具名称
     * @param inputJson 输入参数 (JSON 字符串或简单字符串)
     * @return 执行结果
     */
    public Object executeTool(String toolName, String inputJson) {
        log.info("Executing tool: {} with input: {}", toolName, inputJson);

        try {
            // 1. 获取工具配置
            ToolConfigEntity config = repository.findByToolName(toolName)
                    .orElseThrow(() -> new ToolExecutionException("Tool not found: " + toolName));

            // 2. 获取工具实例
            Object toolInstance = getToolInstance(config);

            // 3. 获取执行方法
            Method method = getToolMethod(config, toolInstance);

            // 4. 解析参数并执行
            Object[] args = parseArguments(method, inputJson);
            Object result = method.invoke(toolInstance, args);

            log.info("Tool {} executed successfully, result: {}", toolName, result);
            return result;

        } catch (ToolExecutionException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to execute tool: {}", toolName, e);
            throw new ToolExecutionException("Tool execution failed: " + e.getMessage(), e);
        }
    }

    // ==================== 工具实例管理 ====================

    /**
     * 获取工具实例（反射加载）
     */
    private Object getToolInstance(ToolConfigEntity config) {
        return toolInstances.computeIfAbsent(config.getToolName(), name -> {
            try {
                Class<?> clazz = Class.forName(config.getClassName());
                // 尝试从 Spring 容器获取，如果没有则创建新实例
                try {
                    return applicationContext.getBean(clazz);
                } catch (Exception e) {
                    // 如果不在 Spring 容器中，创建新实例
                    return applicationContext.getAutowireCapableBeanFactory()
                            .createBean(clazz);
                }
            } catch (ClassNotFoundException e) {
                throw new ToolExecutionException("Tool class not found: " + config.getClassName(), e);
            }
        });
    }

    /**
     * 获取工具方法
     */
    private Method getToolMethod(ToolConfigEntity config, Object toolInstance) {
        return toolMethods.computeIfAbsent(config.getToolName() + "#" + config.getMethodName(), key -> {
            try {
                if (config.getMethodName() == null || config.getMethodName().isEmpty()) {
                    // 如果没有指定方法名，获取第一个公开方法
                    Method[] methods = toolInstance.getClass().getDeclaredMethods();
                    for (Method method : methods) {
                        if (!method.getName().startsWith("get") &&
                            !method.getName().startsWith("set") &&
                            !method.getName().startsWith("is")) {
                            return method;
                        }
                    }
                    throw new ToolExecutionException("No valid method found for tool: " + config.getToolName());
                }
                return toolInstance.getClass().getMethod(config.getMethodName(), String.class);
            } catch (NoSuchMethodException e) {
                throw new ToolExecutionException("Method not found: " + config.getMethodName(), e);
            }
        });
    }

    /**
     * 解析方法参数
     */
    private Object[] parseArguments(Method method, String inputJson) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        if (parameters.length == 0) {
            return args;
        }

        // 简单处理：如果只有一个 String 参数，直接传入
        if (parameters.length == 1 && parameters[0].getType() == String.class) {
            args[0] = inputJson;
            return args;
        }

        // 尝试解析 JSON
        try {
            if (inputJson != null && inputJson.trim().startsWith("{")) {
                Map<String, Object> params = objectMapper.readValue(inputJson, Map.class);
                for (int i = 0; i < parameters.length; i++) {
                    String paramName = parameters[i].getName();
                    if (params.containsKey(paramName)) {
                        args[i] = params.get(paramName);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse input as JSON, using raw input: {}", inputJson);
        }

        return args;
    }

    // ==================== 工具描述生成 ====================

    /**
     * 生成工具描述（用于 LLM）
     */
    public String generateToolDescriptions() {
        StringBuilder sb = new StringBuilder();
        sb.append("Available tools:\n");

        for (ToolConfigEntity tool : getEnabledTools()) {
            sb.append("- ").append(tool.getToolName()).append(": ");
            sb.append(tool.getDescription() != null ? tool.getDescription() : "No description");
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 生成工具详细信息（JSON 格式）
     */
    public List<Map<String, Object>> getToolInfoList() {
        List<Map<String, Object>> tools = new ArrayList<>();

        for (ToolConfigEntity config : getEnabledTools()) {
            Map<String, Object> toolInfo = new HashMap<>();
            toolInfo.put("name", config.getToolName());
            toolInfo.put("description", config.getDescription());
            toolInfo.put("className", config.getClassName());
            toolInfo.put("methodName", config.getMethodName());
            tools.add(toolInfo);
        }

        return tools;
    }

    // ==================== 缓存管理 ====================

    /**
     * 刷新工具缓存
     */
    public void refreshTool(String toolName) {
        toolInstances.remove(toolName);
        toolMethods.keySet().removeIf(key -> key.startsWith(toolName + "#"));
        log.info("Tool cache refreshed: {}", toolName);
    }

    /**
     * 清空所有缓存
     */
    public void clearCache() {
        toolInstances.clear();
        toolMethods.clear();
        log.info("All tool caches cleared");
    }
}
