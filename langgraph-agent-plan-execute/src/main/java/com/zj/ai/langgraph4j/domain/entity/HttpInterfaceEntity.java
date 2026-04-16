package com.zj.ai.langgraph4j.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * HTTP 接口定义实体
 * 存储 HTTP 接口的完整配置信息
 *
 * @author zj
 * @date 2026/04/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "http_interface", autoResultMap = true)
public class HttpInterfaceEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接口编码（唯一标识）
     */
    private String interfaceCode;

    /**
     * 接口名称
     */
    private String interfaceName;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 所属分类
     */
    private String category;

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * 请求路径（支持路径变量，如 /users/{userId}/posts/{postId}）
     */
    private String path;

    /**
     * 完整 URL（当 baseUrl 和 path 不适用时使用）
     */
    private String fullUrl;

    /**
     * HTTP 方法：GET, POST, PUT, DELETE, PATCH
     */
    private String method;

    /**
     * Content-Type
     */
    private String contentType;

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 路径参数定义（JSON 格式）
     * 示例：[{"name":"userId","description":"用户ID","required":true,"defaultValue":null}]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ParamDefinition> pathParams;

    /**
     * 查询参数定义（JSON 格式）
     * 示例：[{"name":"page","description":"页码","required":false,"defaultValue":"1"}]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ParamDefinition> queryParams;

    /**
     * 请求头定义（JSON 格式）
     * 示例：[{"name":"Authorization","description":"认证令牌","required":true,"defaultValue":"Bearer ${token}"}]
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<ParamDefinition> headers;

    /**
     * 请求体定义（JSON 格式）
     * 示例：{"type":"object","properties":{"name":{"type":"string","description":"名称"},"age":{"type":"integer","description":"年龄"}}}
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private BodyDefinition bodyDefinition;

    /**
     * 默认请求头（JSON 格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, String> defaultHeaders;

    /**
     * 默认查询参数（JSON 格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> defaultQueryParams;

    /**
     * 默认请求体模板（JSON 字符串或模板）
     */
    private String defaultBodyTemplate;

    /**
     * 认证类型：NONE, BASIC, BEARER, API_KEY, OAUTH2, CUSTOM
     */
    private String authType;

    /**
     * 认证配置（JSON 格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private AuthConfig authConfig;

    /**
     * 响应数据提取规则（JSON 格式）
     * 定义如何从响应中提取数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ResponseExtraction responseExtraction;

    /**
     * 错误处理配置（JSON 格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private ErrorHandling errorHandling;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 标签（JSON 数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 扩展配置（JSON 格式，存储自定义配置）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extension;

    /**
     * 创建人
     */
    private String createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updatedBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 参数定义
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParamDefinition implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 参数名称
         */
        private String name;

        /**
         * 参数描述
         */
        private String description;

        /**
         * 是否必填
         */
        private Boolean required;

        /**
         * 默认值
         */
        private Object defaultValue;

        /**
         * 数据类型：string, integer, number, boolean, array, object
         */
        private String dataType;

        /**
         * 参数位置：path, query, header, body
         */
        private String location;

        /**
         * 示例值
         */
        private Object example;

        /**
         * 校验规则（正则表达式或 JSON Schema）
         */
        private String validation;
    }

    /**
     * 请求体定义
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BodyDefinition implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 数据类型：json, form, xml, raw
         */
        private String type;

        /**
         * JSON Schema 定义
         */
        private Map<String, Object> schema;

        /**
         * 示例请求体
         */
        private String example;
    }

    /**
     * 认证配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthConfig implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * BASIC 认证配置
         */
        private BasicAuth basic;

        /**
         * Bearer Token 配置
         */
        private BearerAuth bearer;

        /**
         * API Key 配置
         */
        private ApiKeyAuth apiKey;

        /**
         * OAuth2 配置
         */
        private OAuth2Auth oauth2;

        /**
         * 自定义认证配置
         */
        private Map<String, String> custom;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicAuth implements Serializable {
        private static final long serialVersionUID = 1L;

        private String username;
        private String password;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BearerAuth implements Serializable {
        private static final long serialVersionUID = 1L;

        private String token;
        private String prefix;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiKeyAuth implements Serializable {
        private static final long serialVersionUID = 1L;

        private String keyName;
        private String keyValue;
        /**
         * 位置：header, query
         */
        private String location;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OAuth2Auth implements Serializable {
        private static final long serialVersionUID = 1L;

        private String clientId;
        private String clientSecret;
        private String tokenUrl;
        private String scope;
    }

    /**
     * 响应数据提取规则
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseExtraction implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 提取方式：JSON_PATH, JMESPATH, REGEX, NONE
         */
        private String extractType;

        /**
         * 提取表达式
         */
        private String extractExpression;

        /**
         * 是否提取为列表
         */
        private Boolean extractAsList;

        /**
         * 字段映射（源字段 -> 目标字段）
         */
        private Map<String, String> fieldMapping;
    }

    /**
     * 错误处理配置
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorHandling implements Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * 重试状态码列表
         */
        private List<Integer> retryStatusCodes;

        /**
         * 错误响应字段定义
         */
        private String errorField;

        /**
         * 错误消息字段
         */
        private String errorMessageField;

        /**
         * 自定义错误处理类
         */
        private String errorHandlerClass;
    }
}
