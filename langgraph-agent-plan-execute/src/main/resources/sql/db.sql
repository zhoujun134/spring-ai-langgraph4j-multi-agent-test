-- ===========================================
-- MySQL 建表脚本
-- HTTP 接口定义表
-- ===========================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS plan_execute_db DEFAULT CHARACTER SET utf8mb4 DEFAULT COLLATE utf8mb4_unicode_ci;

USE plan_execute_db;

-- 删除已存在的表
DROP TABLE IF EXISTS `http_interface`;

-- 创建表
CREATE TABLE `http_interface` (
                                  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                  `interface_code` VARCHAR(100) NOT NULL COMMENT '接口编码（唯一标识）',
                                  `interface_name` VARCHAR(200) DEFAULT NULL COMMENT '接口名称',
                                  `description` VARCHAR(1000) DEFAULT NULL COMMENT '接口描述',
                                  `category` VARCHAR(50) DEFAULT NULL COMMENT '所属分类',
                                  `base_url` VARCHAR(500) DEFAULT NULL COMMENT '基础URL',
                                  `path` VARCHAR(500) DEFAULT NULL COMMENT '请求路径',
                                  `full_url` VARCHAR(1000) DEFAULT NULL COMMENT '完整URL',
                                  `method` VARCHAR(10) DEFAULT 'GET' COMMENT 'HTTP方法',
                                  `content_type` VARCHAR(100) DEFAULT 'application/json' COMMENT 'Content-Type',
                                  `timeout` INT DEFAULT 30000 COMMENT '请求超时时间（毫秒）',
                                  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
                                  `path_params` JSON DEFAULT NULL COMMENT '路径参数定义',
                                  `query_params` JSON DEFAULT NULL COMMENT '查询参数定义',
                                  `headers` JSON DEFAULT NULL COMMENT '请求头定义',
                                  `body_definition` JSON DEFAULT NULL COMMENT '请求体定义',
                                  `default_headers` JSON DEFAULT NULL COMMENT '默认请求头',
                                  `default_query_params` JSON DEFAULT NULL COMMENT '默认查询参数',
                                  `default_body_template` TEXT DEFAULT NULL COMMENT '默认请求体模板',
                                  `auth_type` VARCHAR(20) DEFAULT 'NONE' COMMENT '认证类型',
                                  `auth_config` JSON DEFAULT NULL COMMENT '认证配置',
                                  `response_extraction` JSON DEFAULT NULL COMMENT '响应数据提取规则',
                                  `error_handling` JSON DEFAULT NULL COMMENT '错误处理配置',
                                  `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
                                  `sort_order` INT DEFAULT 0 COMMENT '排序号',
                                  `tags` JSON DEFAULT NULL COMMENT '标签',
                                  `extension` JSON DEFAULT NULL COMMENT '扩展配置',
                                  `created_by` VARCHAR(50) DEFAULT NULL COMMENT '创建人',
                                  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `updated_by` VARCHAR(50) DEFAULT NULL COMMENT '更新人',
                                  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `uk_interface_code` (`interface_code`),
                                  KEY `idx_category` (`category`),
                                  KEY `idx_method` (`method`),
                                  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='HTTP接口定义表';

-- ===========================================
-- 初始化示例数据
-- ===========================================

-- 用户查询接口
INSERT INTO http_interface (
    interface_code, interface_name, description, category,
    base_url, path, method, content_type,
    path_params, enabled, sort_order
) VALUES (
             'getUserById',
             '根据ID获取用户信息',
             '根据用户ID获取用户的详细信息',
             '用户管理',
             'https://jsonplaceholder.typicode.com',
             '/users/{userId}',
             'GET',
             'application/json',
             '[{"name":"userId","description":"用户ID","required":true,"dataType":"integer"}]',
             1,
             1
         );

-- 帖子列表查询接口
INSERT INTO http_interface (
    interface_code, interface_name, description, category,
    base_url, path, method, content_type,
    query_params, default_query_params, enabled, sort_order
) VALUES (
             'getPosts',
             '获取帖子列表',
             '获取帖子列表，支持分页',
             '帖子管理',
             'https://jsonplaceholder.typicode.com',
             '/posts',
             'GET',
             'application/json',
             '[{"name":"_page","description":"页码","required":false,"dataType":"integer","defaultValue":1},{"name":"_limit","description":"每页数量","required":false,"dataType":"integer","defaultValue":10}]',
             '{"_limit": 10}',
             1,
             2
         );

-- 创建帖子接口
INSERT INTO http_interface (
    interface_code, interface_name, description, category,
    base_url, path, method, content_type,
    body_definition, default_body_template, enabled, sort_order
) VALUES (
             'createPost',
             '创建帖子',
             '创建一篇新帖子',
             '帖子管理',
             'https://jsonplaceholder.typicode.com',
             '/posts',
             'POST',
             'application/json',
             '{"type":"json","schema":{"type":"object","properties":{"title":{"type":"string"},"body":{"type":"string"},"userId":{"type":"integer"}}}}',
             '{"userId": 1}',
             1,
             3
         );

-- 更新帖子接口
INSERT INTO http_interface (
    interface_code, interface_name, description, category,
    base_url, path, method, content_type,
    path_params, body_definition, enabled, sort_order
) VALUES (
             'updatePost',
             '更新帖子',
             '更新指定ID的帖子',
             '帖子管理',
             'https://jsonplaceholder.typicode.com',
             '/posts/{postId}',
             'PUT',
             'application/json',
             '[{"name":"postId","description":"帖子ID","required":true,"dataType":"integer"}]',
             '{"type":"json","schema":{"type":"object","properties":{"title":{"type":"string"},"body":{"type":"string"},"userId":{"type":"integer"}}}}',
             1,
             4
         );

-- 删除帖子接口
INSERT INTO http_interface (
    interface_code, interface_name, description, category,
    base_url, path, method, content_type,
    path_params, enabled, sort_order
) VALUES (
             'deletePost',
             '删除帖子',
             '删除指定ID的帖子',
             '帖子管理',
             'https://jsonplaceholder.typicode.com',
             '/posts/{postId}',
             'DELETE',
             'application/json',
             '[{"name":"postId","description":"帖子ID","required":true,"dataType":"integer"}]',
             1,
             5
         );
