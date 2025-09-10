# 服务注册与发现中心

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

服务注册与发现中心是一个基于Spring Boot开发的微服务注册中心，提供服务注册、心跳检测、服务发现、虚拟域名访问、限流控制等功能。

![register-center](/data/p1.jpg)

## 功能特性

- ✅ **服务注册与注销** - 支持通过API注册和注销服务实例
- ✅ **心跳检测** - 自动检测服务状态，维护服务在线状态
- ✅ **服务发现** - 提供服务查询接口，支持按服务名和版本查找
- ✅ **虚拟域名** - 为服务分配虚拟域名，通过域名访问服务
- ✅ **反向代理** - 通过虚拟域名代理请求到实际服务
- ✅ **限流控制** - 支持对服务进行限流控制，保护服务稳定运行
- ✅ **Web管理界面** - 提供友好的Web界面管理注册的服务


## 技术栈

- **后端框架**: Spring Boot 3.3.4
- **编程语言**: Java 21
- **数据库**: H2内存数据库
- **构建工具**: Gradle
- **模板引擎**: Thymeleaf
- **代码简化**: Lombok

## 项目结构

```
register-center/
├── src/main/java/com/example/registercenter/
│   ├── controller/        # 控制器层
│   ├── entity/            # 实体类
│   ├── repository/        # 数据访问层
│   ├── service/           # 业务逻辑层
│   ├── config/            # 配置类
│   └── util/              # 工具类
├── src/main/resources/    # 资源文件
├── build.gradle           # Gradle构建配置
├── heartbeat_sender.sh    # 心跳发送脚本
├── test-server.py         # 测试服务器
├── service-registry-api.md# API文档
└── virtual-domain-api.md  # 虚拟域名功能文档
```

## 快速开始

### 环境要求

- Java 21或更高版本
- Gradle 8.0或更高版本

### 构建项目

```bash
# 克隆项目
git clone git@github.com:vvoooxx/register-center.git
cd register-center

# 构建项目
./gradlew build

# 运行项目
./gradlew bootRun
```

### 访问应用

启动成功后，可以通过以下地址访问：

- Web管理界面: http://localhost:8761
- API接口: http://localhost:8761/api

## API接口说明

### 服务注册

#### 1. 注册服务（JSON方式）

```http
POST /api/services
Content-Type: application/json
```

请求体:
```json
{
  "serviceName": "testService",
  "serviceVersion": "1.0",
  "ip": "127.0.0.1",
  "port": 8081,
  "virtualDomain": "test-service.local"  // 可选
}
```

#### 2. 注册服务（表单方式）

```http
POST /api/services/register
Content-Type: application/x-www-form-urlencoded
```

参数:
- serviceName: 服务名称
- serviceVersion: 服务版本
- ip: 服务IP地址
- port: 服务端口
- virtualDomain: 虚拟域名（可选）

### 服务发现

#### 1. 获取所有服务

```http
GET /api/services
```

#### 2. 根据服务名查找服务

```http
GET /api/services/find?serviceName=testService&serviceVersion=1.0
```

### 心跳检测

#### 1. 发送心跳（通过服务ID）

```http
PUT /api/services/{id}/heartbeat
```

#### 2. 发送心跳（通过服务标识）

```http
PUT /api/services/heartbeat?serviceName=testService&serviceVersion=1.0&ip=127.0.0.1&port=8081
```

### 服务注销

#### 1. 注销服务（通过服务ID）

```http
DELETE /api/services/{id}
```

#### 2. 注销服务（通过服务标识）

```http
DELETE /api/services/deregister?serviceName=testService&serviceVersion=1.0&ip=127.0.0.1&port=8081
```

### 虚拟域名功能

#### 1. 根据虚拟域名查找服务

```http
GET /api/services/domain/{virtualDomain}
```

#### 2. 通过虚拟域名访问服务（代理）

```http
GET /proxy/{virtualDomain}/api/data
```

### 限流控制

#### 1. 获取服务限流配置

```http
GET /api/rate-limit/{serviceId}
```

#### 2. 设置服务限流配置

```http
PUT /api/rate-limit/{serviceId}?enabled=true&maxRequestsPerSecond=100
```

#### 3. 启用服务限流

```http
POST /api/rate-limit/{serviceId}/enable?maxRequestsPerSecond=100
```

#### 4. 禁用服务限流

```http
POST /api/rate-limit/{serviceId}/disable
```

## 虚拟域名功能

服务注册中心支持为服务分配虚拟域名，之后可以通过虚拟域名访问服务，无需记住服务的IP地址和端口号。

### 使用示例

1. 注册一个带虚拟域名的服务:
```bash
curl -X POST http://localhost:8761/api/services/register \
  --data "serviceName=testService" \
  --data "serviceVersion=1.0" \
  --data "ip=127.0.0.1" \
  --data "port=8080" \
  --data "virtualDomain=test-service.local"
```

2. 查询虚拟域名对应的服务:
```bash
curl http://localhost:8761/api/services/domain/test-service.local
```

3. 通过虚拟域名访问服务:
```bash
curl http://localhost:8761/proxy/test-service.local/api/data
```

## 限流功能

服务注册中心支持对服务进行限流控制，防止服务因请求过多而崩溃。

### 使用示例

1. 启用服务限流:
```bash
curl -X POST http://localhost:8761/api/rate-limit/1/enable?maxRequestsPerSecond=10
```

2. 禁用服务限流:
```bash
curl -X POST http://localhost:8761/api/rate-limit/1/disable
```

## 心跳机制

服务需要定期发送心跳以保持在线状态，默认每90秒未收到心跳的服务将被标记为离线。

### 心跳发送脚本

项目提供了`heartbeat_sender.sh`脚本，可以自动为指定服务发送心跳。

```bash
# 修改脚本中的服务ID列表
SERVICE_IDS=('1' '2')

# 运行脚本
./heartbeat_sender.sh
```

## 测试工具

### 测试服务器

项目提供了`test-server.py`作为测试服务器，可以用于测试服务注册和虚拟域名功能。

```bash
# 启动测试服务器
python test-server.py

# 测试服务器运行在8090端口
```

## 部署说明

### 生产环境部署

1. 构建项目:
```bash
./gradlew build
```

2. 运行JAR包:
```bash
java -jar build/libs/register-center-1.0-SNAPSHOT.jar
```

3. 自定义端口:
```bash
java -jar build/libs/register-center-1.0-SNAPSHOT.jar --server.port=8080
```

## 数据库

本项目使用H2内存数据库，默认配置如下:

- 数据库文件位置: `data/register_center.mv.db`
- 数据库连接: 自动配置，无需额外设置

## 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启Pull Request

## 许可证

本项目采用MIT许可证，详情请见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请提交Issue或联系项目维护者。