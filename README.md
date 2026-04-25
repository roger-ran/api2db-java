# API2DB 中间件

一个基于 Java Spring Boot 的轻量级中间件，作为 AI Agent 与数据库之间的安全桥梁。

## 功能特性

- **API-Key 鉴权**: 安全的 API 访问控制
- **动态连接池**: 支持多种数据库（MySQL, PostgreSQL, Oracle, SQL Server）
- **SQL 安全过滤**: 白名单/黑名单机制防止危险操作
- **统一接口**: 简单易用的 REST API
- **Docker 部署**: 开箱即用的容器化部署

## 技术栈

- **框架**: Spring Boot 3.x
- **数据库连接**: HikariCP (动态连接池)
- **核心库**: JDBC Template, SnakeYAML
- **JDK**: 17 (Eclipse Temurin)

## 项目结构

```
api2db-java/
├── config/                         # 配置文件目录
│   ├── config.yaml                 # 数据库连接配置
│   ├── whitelist.txt               # SQL 白名单
│   └── blacklist.txt               # SQL 黑名单
├── src/main/java/com/example/api2db/
│   ├── Api2dbApplication.java      # 主应用程序
│   ├── config/                     # 配置加载
│   ├── controller/                # REST API 控制器
│   ├── service/                    # 业务服务
│   ├── model/                      # 数据模型
│   └── exception/                  # 异常处理
├── Dockerfile                      # Docker 构建文件
└── pom.xml                         # Maven 配置
```

## 快速开始

### 1. 配置数据库连接

编辑 `config/config.yaml` 文件：

```yaml
auth_config:
  - api_key: "your-api-key"
    connections:
      - id: "mysql-db"
        type: "mysql"
        host: "localhost"
        port: 3306
        username: "root"
        password: "password"
        database: "your_database"
```

### 2. 配置 SQL 安全规则

编辑 `config/whitelist.txt` 和 `config/blacklist.txt` 文件来控制允许和禁止的 SQL 操作。

### 3. Maven 镜像源配置（可选）

项目已配置使用华为云 Maven 镜像源，可加速依赖下载。如果需要在本地构建时使用该镜像源，请确保 `settings.xml` 文件位于项目根目录。

如果您想修改镜像源，请编辑 `settings.xml` 文件：

```xml
<mirrors>
    <mirror>
        <id>huaweicloud</id>
        <mirrorOf>*</mirrorOf>
        <url>https://mirrors.huaweicloud.com/repository/maven/</url>
        <name>华为云 Maven 镜像</name>
    </mirror>
</mirrors>
```

### 4. 构建 Docker 镜像

```bash
docker build -t api2db:latest .
```

### 5. 运行容器

```bash
docker run -d \
  --name api2db \
  -p 8080:8080 \
  -v $(pwd)/config:/config \
  api2db:latest
```

### 6. 测试 API

```bash
  curl --location -X POST 'http://192.168.82.173:18080/api/v1/execute' \
  --header 'Content-Type: application/json' \
  --data '{
      "api-key": "agent-001",
      "connection_id": "mysql-db",
      "sqls": ["SELECT * FROM users LIMIT 10"]
    }'
```

## API 文档

### POST /api/v1/execute

执行 SQL 语句。

**请求体**:

```json
{
  "api-key": "your-api-key",
  "connection_id": "mysql-db",
  "sqls": [
    "SELECT * FROM users LIMIT 10",
    "UPDATE logs SET status=1 WHERE id=5"
  ]
}
```

**响应**:

```json
{
  "success": true,
  "results": [
    {
      "sql": "SELECT * FROM users LIMIT 10",
      "type": "QUERY",
      "data": [
        {"id": 1, "name": "test"}
      ],
      "affected_rows": 0
    },
    {
      "sql": "UPDATE logs SET status=1 WHERE id=5",
      "type": "EXECUTE",
      "data": [],
      "affected_rows": 1
    }
  ],
  "error": null
}
```

### POST /api/v1/health

健康检查接口。

**响应**: "OK"

## 配置说明

### config.yaml

映射 API Key 到数据库连接：

- `api_key`: API 访问密钥
- `connections`: 该 API Key 可以访问的数据库连接列表
- `type`: 数据库类型（mysql, postgresql, oracle, sqlserver）
- `host`, `port`, `username`, `password`, `database`: 数据库连接信息

### whitelist.txt

允许执行的 SQL 关键字（不区分大小写）。

### blacklist.txt

严禁执行的 SQL 关键字（不区分大小写）。

## 安全建议

1. **使用强密码**: 确保 API Key 和数据库密码足够复杂
2. **最小权限原则**: 数据库用户只授予必要的权限
3. **定期更新**: 及时更新依赖库和基础镜像
4. **网络隔离**: 在生产环境中使用网络隔离和防火墙
5. **日志监控**: 启用详细的日志记录并进行监控

## 故障排查

### 连接失败

检查：
1. 数据库连接配置是否正确
2. 网络连接是否正常
3. 数据库是否启动并可访问
4. 防火墙规则是否允许连接

### SQL 安全验证失败

检查：
1. whitelist.txt 和 blacklist.txt 配置
2. SQL 语句是否包含黑名单关键字
3. SQL 语句是否包含白名单关键字

### 容器无法启动

检查：
1. Docker 端口是否被占用
2. 配置文件路径是否正确
3. 日志输出：`docker logs api2db`

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！

## 联系方式

如有问题，请提交 Issue 或联系维护者。
