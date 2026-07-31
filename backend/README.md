# data-chain-backend

Data Chain 后端仓库。业务实现统一落在 **`app/data-chain-service`**（含接入端工作台 WSC）。

| 项 | 说明 |
|---|---|
| 模块 | `app/data-chain-service` |
| 启动类 | `com.shdata.datachain.DataChainApplication` |
| 工作台包 | 全部在 `com.shdata.datachain.*`（catalog / chain / overview / rbac / security 等） |
| 技术栈 | Java 17 · Spring Boot **2.7.18** · JPA · QueryDSL · Flyway · MySQL · Redis · Knife4j |

控制面保留在本地 AI-SEP；前端仓：`data-chain-static`。

## 构建与运行

```bash
mvn -pl app/data-chain-service -am -DskipTests package

# 无外部 MySQL/Redis 冒烟
java -jar app/data-chain-service/target/data-chain-service-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

# 联调（MySQL + Redis，凭环境变量 / application-local.yml）
mvn -pl app/data-chain-service -am spring-boot:run
```

健康检查：`GET /health` · Actuator：`/actuator/health` · 文档：`/doc.html`

## 环境变量

| 变量 | 默认 | 说明 |
|---|---|---|
| `SERVER_PORT` | `8080` | HTTP 端口 |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | localhost / 3306 / data_chain | MySQL |
| `DB_USERNAME` / `DB_PASSWORD` | root / root | 库账号（本地请用 `application-local.yml`，勿提交密钥） |
| `REDIS_HOST` / `REDIS_PORT` | localhost / 6379 | Redis |
| `WSC_CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | 前端 Origin |

前端：`VITE_API_BASE_URL=http://<本服务>/api/v1`。

## Flyway

`sql/migration/` 含骨架 demo（`V1`/`V2`）与 WSC 业务表（`V202607301600__init_wsc_v11.sql`）。空库初始化见 `sql/init/`。
