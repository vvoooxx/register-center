# 服务注册中心虚拟域名功能文档

## 1. 功能概述

虚拟域名功能允许用户为注册的服务分配一个唯一的虚拟域名，之后可以通过该虚拟域名访问服务，无需记忆服务的IP地址和端口号。

## 2. 实体结构修改

我们对`RegisteredService`实体进行了扩展，添加了`virtualDomain`字段：

```java
// 新增虚拟域名字段，用于通过虚拟域名访问服务
@Column(nullable = true, length = 100)
private String virtualDomain;
```

## 3. API接口说明

### 3.1 注册服务（支持虚拟域名）

#### 3.1.1 POST /api/services（JSON方式）

**请求体：**
```json
{
  "serviceName": "服务名称",
  "serviceVersion": "服务版本",
  "ip": "服务IP地址",
  "port": 服务端口,
  "virtualDomain": "虚拟域名（可选）"
}
```

**响应：**
```json
{
  "id": 服务ID,
  "serviceName": "服务名称",
  "serviceVersion": "服务版本",
  "ip": "服务IP地址",
  "port": 服务端口,
  "status": "服务状态",
  "registerTime": "注册时间",
  "lastHeartbeat": "最后心跳时间",
  "virtualDomain": "分配的虚拟域名"
}
```

#### 3.1.2 POST /api/services/register（表单方式）

**请求参数：**
- serviceName: 服务名称
- serviceVersion: 服务版本
- ip: 服务IP地址
- port: 服务端口
- virtualDomain: 虚拟域名（可选）

**响应：**
```json
{
  "success": true/false,
  "message": "消息",
  "data": {
    "id": 服务ID,
    "serviceName": "服务名称",
    "serviceVersion": "服务版本",
    "ip": "服务IP地址",
    "port": 服务端口,
    "status": "服务状态",
    "registerTime": "注册时间",
    "lastHeartbeat": "最后心跳时间",
    "virtualDomain": "分配的虚拟域名"
  }
}
```

### 3.2 根据虚拟域名查询服务

#### 3.2.1 GET /api/services/domain/{virtualDomain}

**请求参数：**
- virtualDomain: 虚拟域名（路径参数）

**响应：**
```json
{
  "success": true/false,
  "message": "消息",
  "data": {
    "id": 服务ID,
    "serviceName": "服务名称",
    "serviceVersion": "服务版本",
    "ip": "服务IP地址",
    "port": 服务端口,
    "status": "服务状态",
    "registerTime": "注册时间",
    "lastHeartbeat": "最后心跳时间",
    "virtualDomain": "虚拟域名"
  }
}
```

### 3.3 通过虚拟域名访问服务（代理转发）

#### 3.3.1 所有HTTP方法 /proxy/{virtualDomain}/**

**请求格式：** `/proxy/{virtualDomain}/[path]`

- virtualDomain: 虚拟域名（路径参数）
- path: 目标服务的路径（可选）

**功能：** 将请求转发到虚拟域名对应的服务，并将服务的响应返回给客户端。支持GET、POST、PUT、DELETE等HTTP方法。

## 4. 使用示例

### 4.1 注册一个带虚拟域名的服务

```bash
curl -X POST http://localhost:8761/api/services/register \
  --data "serviceName=testService" \
  --data "serviceVersion=1.0" \
  --data "ip=127.0.0.1" \
  --data "port=8080" \
  --data "virtualDomain=test-service.local"
```

### 4.2 查询虚拟域名对应的服务

```bash
curl http://localhost:8761/api/services/domain/test-service.local
```

### 4.3 通过虚拟域名访问服务

假设服务提供了一个`/api/data`的端点：

```bash
curl http://localhost:8761/proxy/test-service.local/api/data
```

## 5. 虚拟域名规则

1. 虚拟域名必须唯一，不能重复
2. 虚拟域名可以包含字母、数字、连字符(-)和点(.)
3. 建议使用有意义的名称，例如`service-name.app`或`service-name.local`
4. 虚拟域名最大长度为100个字符
5. 虚拟域名为可选字段，如果不提供则无法通过虚拟域名访问服务

## 6. 错误处理

1. 如果虚拟域名已被使用，服务注册将失败
2. 如果请求的虚拟域名不存在，将返回404错误
3. 如果虚拟域名对应的服务已离线，将返回503错误
4. 代理转发过程中的错误将返回500错误，并包含错误信息

## 7. 测试端点

注册中心提供了以下测试端点，可用于测试虚拟域名功能：

1. `/virtual-domain-test/info` - 返回基本服务信息
2. `/virtual-domain-test/time` - 返回当前时间
3. `/proxy-test/echo` - 用于代理测试的端点

这些端点可以通过注册中心的IP和端口直接访问，也可以在将注册中心自身注册为服务并分配虚拟域名后，通过虚拟域名访问。