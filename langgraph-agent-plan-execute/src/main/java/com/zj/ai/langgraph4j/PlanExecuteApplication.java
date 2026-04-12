package com.zj.ai.langgraph4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Plan-Execute Agent 应用启动类
 *
 * @author zj
 * @date 2026/04/12
 */
@SpringBootApplication
public class PlanExecuteApplication {

    static {
        // 禁用 Ollama 服务器的代理（解决 502 错误）
        System.setProperty("http.nonProxyHosts", "localhost|127.0.0.1|192.168.*|10.*|*.local");
        System.setProperty("https.nonProxyHosts", "localhost|127.0.0.1|192.168.*|10.*|*.local");
    }

    public static void main(String[] args) {
        SpringApplication.run(PlanExecuteApplication.class, args);
    }
}
