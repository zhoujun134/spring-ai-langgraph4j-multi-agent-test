package com.zj.ai.common.sdk;

import org.apache.commons.lang3.StringUtils;

/**
 * @ClassName BooleanUtils
 * @Author zj
 * @Description
 * @Date 2026/4/12 18:54
 * @Version v1.0
 **/
public class BooleanUtils {

    // 将字符串转换为一个布尔值
    public static boolean toBoolean(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        return "true".equalsIgnoreCase(str) || "1".equals(str);
    }
}
