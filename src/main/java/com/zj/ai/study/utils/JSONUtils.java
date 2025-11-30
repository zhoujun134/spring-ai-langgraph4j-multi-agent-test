package com.zj.ai.study.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @ClassName JSONUtils
 * @Author zj
 * @Description
 * @Date 2025/11/30 14:14
 * @Version v1.0
 **/
@Slf4j
public class JSONUtils {

    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 注册 JavaTimeModule
        objectMapper.registerModule(new JavaTimeModule());

        // 可选：自定义 LocalDate 格式（如 "yyyy-MM-dd"）
        // JavaTimeModule 默认使用 ISO_LOCAL_DATE (即 "2025-04-05")，通常已满足需求
        // 如果需要自定义，可以这样做：
        /*
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        objectMapper.registerModule(javaTimeModule);
        */

        // 禁用将日期写为时间戳（可选）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
    public static String toJSONString(Object data) {
        if (Objects.isNull(data)) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(data);
//            return gson.toJson(data);
        } catch (Exception e) {
            log.error("JsonUtils######toString： error data={}", data, e);
            return "";
        }
    }

    public static <T> T parseObject(String text, Class<T> tClass) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return objectMapper.readValue(text, tClass);
        } catch (Exception e) {
            log.error("JSONUtils######parseObject： error data={}", text, e);
            return null;
        }
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        List<T> result = Collections.emptyList();
        try {
            if (StringUtils.isBlank(text)) {
                text = "[]";
            }
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
            result = objectMapper.readValue(text, listType);
        } catch (Exception e) {
            String message =
                    String.format("JSONUtils######parseArray 对象转换 json 异常! text=%s, clazz=%s", text, clazz);
            log.error(message, e);
            throw new RuntimeException(message);
        }
        return result;
    }

    public static <K, V> Map<K, V> parseMap(String text, Class<K> keyClass, Class<V> valueClass) {
        try {
            if (StringUtils.isBlank(text)) {
                return Collections.emptyMap();
            }
            JavaType mapType = objectMapper.getTypeFactory()
                    .constructMapType(Map.class, keyClass, valueClass);
            return objectMapper.readValue(text, mapType);
        } catch (Exception e) {
            String message = String.format(
                    "JSONUtils######parseMap 对象转换 map 异常! text=%s, keyClass=%s, valueClass=%s",
                    text, keyClass.getName(), valueClass.getName()
            );
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public static Map<String, Object> parseMap(String text) {
        try {
            if (StringUtils.isBlank(text)) {
                return Collections.emptyMap();
            }
            return objectMapper.readValue(text, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            String message = String.format("JSONUtils######parseMap 异常! text=%s", text);
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }

    public static Map<String, Object> convertToMap(Object data) {
        if (data == null) {
            return Collections.emptyMap();
        }
        try {
            // 使用 convertValue 直接将对象转换为 Map<String, Object>
            return objectMapper.convertValue(data, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
            });
        } catch (Exception e) {
            String message = String.format(
                    "JSONUtils######convertToMap 对象转换为 Map 异常! data=%s",
                    data.getClass().getSimpleName()
            );
            log.error(message, e);
            throw new RuntimeException(message, e);
        }
    }
}
