package com.zj.ai.common.sdk.dotenv;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntriesFilter;
import io.github.cdimascio.dotenv.DotenvEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
        String projectRoot = System.getProperty("user.dir");
        log.info("当前项目根目录: {}", projectRoot);
        return Dotenv.configure()
                .directory(projectRoot)
                .filename(".env")
                // 文件不存在时，不报错
                .ignoreIfMissing()
                .systemProperties()
                .load();
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
