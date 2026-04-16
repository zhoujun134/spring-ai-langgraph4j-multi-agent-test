package com.zj.ai.langgraph4j.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis Plus 配置类
 *
 * @author zj
 * @date 2026/04/15
 */
@Configuration
@MapperScan("com.zj.ai.langgraph4j.mapper")
public class MyBatisPlusConfig {

    // 分页插件等其他配置按需添加
}
