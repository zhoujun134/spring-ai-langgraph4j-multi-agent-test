package com.zj.ai.common.sdk.dotenv;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntriesFilter;
import io.github.cdimascio.dotenv.DotenvEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DotEnvUtils {

    private static Map<String, String> envMap = new HashMap<>();

    static {
        init();
    }

    private static void init() {
        Dotenv dotenv = loadDotEnv();
        // 将 .env 文件内容存入 Map，实现 .env 优先
        envMap = new HashMap<>();
        for (DotenvEntry entry : dotenv.entries(DotenvEntriesFilter.DECLARED_IN_ENV_FILE)) {
            envMap.put(entry.getKey(), entry.getValue());
        }
    }
    public static Dotenv loadDotEnv() {
        // 获取项目的根目录（向上查找包含 .env 文件的目录）
        String projectRoot = findProjectRoot();
        log.debug("当前项目根目录: {}", projectRoot);

        try {

            return Dotenv.configure()
                    .directory(projectRoot)
                    .filename(".env")
                    // 文件不存在时，不报错
                    .ignoreIfMissing()
                    .systemProperties()
                    .load();
        } catch (Exception e) {
            log.error("加载 .env 文件时发生错误: {}", e.getMessage(), e);
            throw new RuntimeException("加载 .env 文件失败", e);
        }
    }

    /**
     * 查找项目根目录（向上查找包含 .env 文件的目录）
     * 如果找不到 .env 文件，则返回当前工作目录
     *
     * @return 项目根目录路径
     */
    private static String findProjectRoot() {
        String currentDir = System.getProperty("user.dir");
        if (currentDir == null || currentDir.isEmpty()) {
            log.warn("无法获取当前工作目录，使用当前目录");
            return ".";
        }

        File dir = new File(currentDir);
        // 最多向上查找 10 层，防止无限循环
        int maxDepth = 10;
        int depth = 0;

        while (dir != null && depth < maxDepth) {
            File envFile = new File(dir, ".env");
            if (envFile.exists()) {
                return dir.getAbsolutePath();
            }
            dir = dir.getParentFile();
            depth++;
        }

        log.warn("未找到 .env 文件，使用当前工作目录: {}", currentDir);
        return currentDir;
    }
    /**
     * 获取环境变量（.env 文件优先，其次是系统环境变量）
     *
     * @param key 变量名
     * @return 变量值，不存在返回 null
     */
    public static String getDotEnvValue(String key) {
        // 优先使用 .env 文件中的值
        String value = envMap.get(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }
        // 其次使用系统环境变量
        return System.getenv(key);
    }

    /**
     * 获取环境变量（.env 文件优先，其次是系统环境变量，最后使用默认值）
     */
    public static String getDotEnvValueOrDefault(String key, String defaultValue) {
        String value = getDotEnvValue(key);
        return StringUtils.isNotBlank(value) ? value : defaultValue;
    }
}
