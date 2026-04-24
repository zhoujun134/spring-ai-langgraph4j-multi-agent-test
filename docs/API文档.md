# API 文档

## 1. 概述

本文档描述了 Plan-Execute Agent 工作流的 REST API 接口规范。

**基础URL**: `http://localhost:8081/api/agent`

**数据格式**: JSON

**编码**: UTF-8

---

## 2. 接口列表

### 2.1 执行查询

**接口描述**: 执行用户查询，通过 Plan-Execute 工作流处理并返回结果。

**请求方式**: `POST`

**请求路径**: `/query`

**请求头**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| Content-Type | String | 是 | application/json |

**请求体**:

```json
{
  "query": "今天北京的天气怎么样？",
  "maxReplanAttempts": 3
}
```

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| query | String | 是 | 用户查询内容，最大2000字符 |
| maxReplanAttempts | Integer | 否 | 最大重规划次数，默认3，范围1-10 |

**响应体**:

```json
{
  "success": true,
  "query": "今天北京的天气怎么样？",
  "plan": [
    {
      "stepIndex": 1,
      "description": "查询北京的天气",
      "toolName": "weather",
      "toolInput": "北京",
      "result": null,
      "status": "COMPLETED"
    }
  ],
  "executionResults": [
    {
      "stepIndex": 1,
      "toolName": "weather",
      "result": "【北京天气】晴天，温度 15°C，空气质量良好",
      "success": true,
      "errorMessage": null
    }
  ],
  "finalAnswer": "北京今天天气晴朗，温度15°C，空气质量良好。",
  "errorMessage": ""
}
```

**响应字段说明**:

| 字段名 | 类型 | 说明 |
|--------|------|------|
| success | Boolean | 执行是否成功 |
| query | String | 原始查询内容 |
| plan | Array | 生成的执行计划 |
| executionResults | Array | 工具执行结果列表 |
| finalAnswer | String | 最终答案 |
| errorMessage | String | 错误信息（成功时为空） |

**状态码**:

| 状态码 | 说明 |
|--------|------|
| 200 | 执行成功 |
| 400 | 参数校验失败 |
| 500 | 服务器内部错误 |

---

### 2.2 健康检查

**接口描述**: 检查服务运行状态。

**请求方式**: `GET`

**请求路径**: `/health`

**响应体**:

```json
{
  "status": "UP",
  "service": "plan-execute-agent"
}
```

**状态码**:

| 状态码 | 说明 |
|--------|------|
| 200 | 服务正常 |

---

## 3. 错误响应

当请求失败时，返回以下格式的错误响应：

```json
{
  "success": false,
  "error": "错误类型",
  "message": "错误详情",
  "timestamp": "2026-04-24T10:30:00",
  "status": 400
}
```

**常见错误码**:

| 状态码 | 错误类型 | 说明 |
|--------|----------|------|
| 400 | 参数校验错误 | 请求参数不符合要求 |
| 500 | 模型配置错误 | LLM模型配置问题 |
| 500 | 工具执行错误 | 工具调用失败 |
| 500 | 工作流执行错误 | 工作流执行异常 |

---

## 4. 使用示例

### 4.1 cURL 示例

```bash
# 执行查询
curl -X POST http://localhost:8081/api/agent/query \
  -H "Content-Type: application/json" \
  -d '{
    "query": "计算 25 * 4 + 10",
    "maxReplanAttempts": 3
  }'

# 健康检查
curl http://localhost:8081/api/agent/health
```

### 4.2 Java 示例

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AgentClient {
    
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        String requestBody = """
            {
              "query": "计算 25 * 4 + 10",
              "maxReplanAttempts": 3
            }
            """;
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/agent/query"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        
        HttpResponse<String> response = client.send(request, 
                HttpResponse.BodyHandlers.ofString());
        
        System.out.println(response.body());
    }
}
```

### 4.3 Python 示例

```python
import requests

# 执行查询
response = requests.post(
    "http://localhost:8081/api/agent/query",
    json={
        "query": "计算 25 * 4 + 10",
        "maxReplanAttempts": 3
    }
)

print(response.json())
```

---

## 5. 工具列表

系统内置以下工具：

| 工具名 | 描述 | 输入示例 |
|--------|------|----------|
| calculator | 执行数学计算 | "25 * 4 + 10" |
| weather | 查询城市天气 | "北京" |
| search | 网络搜索 | "人工智能最新进展" |

---

## 6. 工作流说明

### 6.1 执行流程

```
用户查询 → PlanAgent → ValidateAgent → ExecuteAgent → 返回结果
                              ↓
                          验证失败
                              ↓
                          ReplanAgent → ValidateAgent → ...
```

### 6.2 状态转换

| 状态 | 说明 |
|------|------|
| PENDING | 步骤待执行 |
| EXECUTING | 步骤执行中 |
| COMPLETED | 步骤已完成 |
| FAILED | 步骤执行失败 |

---

*文档版本: 1.0.0*
*最后更新: 2026/04/24*
