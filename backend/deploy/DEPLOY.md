# Data Chain 演示环境部署

本目录提供单机 Docker Compose 演示部署，仅包含：

- Vue 前端静态站点和 Nginx 反向代理
- Spring Boot 后端

MySQL 和 Redis 不由 Compose 创建，后端沿用项目当前配置连接远程开发环境。

演示环境通过同一个地址提供页面和 API：

- 页面：`http://服务器地址:HTTP_PORT/`
- API：`http://服务器地址:HTTP_PORT/api/v1`
- 健康检查：`http://服务器地址:HTTP_PORT/health`

> 当前目录、产品、上链和总览数据主要保存在后端进程内存中。重启后端会恢复为演示种子数据。本部署不适合作为正式生产环境。

## 1. 服务器目录

前后端仓库必须处于同一父目录：

```text
/opt/data-chain/
├── data-chain-backend/
└── data-chain-static/
```

进入后端仓库：

```bash
cd /opt/data-chain/data-chain
```

## 2. 网络前提

在启动容器前，确认服务器可以访问项目配置中的远程开发 MySQL 和 Redis：

```bash
nc -vz <开发环境数据库地址> <数据库端口>
nc -vz <开发环境Redis地址> <Redis端口>
```

远程服务的防火墙或白名单需要放行部署服务器的出口 IP。

不要设置 `SPRING_PROFILES_ACTIVE=dev`。项目中的 `dev` Profile 会改用 H2 内存数据库，而不是连接远程开发环境。

## 3. 配置

复制环境变量示例：

```bash
cp deploy/.env.example deploy/.env
chmod 600 deploy/.env
```

编辑 `deploy/.env`：

- `HTTP_PORT`：演示服务对外端口。
- `WSC_CORS_ALLOWED_ORIGINS`：实际浏览器访问地址。
- `IMAGE_TAG`：镜像标签，建议发布时改成日期或 Git 提交号。

如果服务器的 80 端口没有被占用，可以设置：

```dotenv
HTTP_PORT=80
WSC_CORS_ALLOWED_ORIGINS=http://服务器IP
```

演示版仅提供 HTTP。需要 HTTPS 时，应在服务器已有的 Nginx、负载均衡或网关上终止 TLS，再转发到 `HTTP_PORT`。

## 4. 构建与启动

检查最终配置：

```bash
docker compose --env-file deploy/.env -f deploy/compose.yml config --quiet
```

构建并启动：

```bash
docker compose --env-file deploy/.env -f deploy/compose.yml up -d --build
```

查看状态：

```bash
docker compose --env-file deploy/.env -f deploy/compose.yml ps
```

后端启动时会连接远程开发数据库并执行 Flyway 校验或迁移。查看后端日志：

```bash
docker compose --env-file deploy/.env -f deploy/compose.yml logs -f --tail=200 backend
```

## 5. 验收

假设端口为 `18080`：

```bash
curl --fail http://127.0.0.1:18080/
curl --fail http://127.0.0.1:18080/health
```

演示账号：

| 用户名 | 密码 | 默认角色 |
|---|---|---|
| `admin` | `demo` | 管理员 |
| `provider` | `demo` | 提供方 |
| `user` | `demo` | 普通用户 |

浏览器访问 `http://服务器地址:18080/`，完成以下检查：

1. 页面可以正常加载，刷新子路由不会出现 404。
2. 使用演示账号登录。
3. 目录、总览和上链页面可以正常请求 API。
4. 管理员或提供方可以完成一次演示新增、修改操作。

## 6. 日常操作

查看全部日志：

```bash
docker compose --env-file deploy/.env -f deploy/compose.yml logs -f --tail=200
```

重新构建并更新：

```bash
docker compose --env-file deploy/.env -f deploy/compose.yml up -d --build
```

停止服务：

```bash
docker compose --env-file deploy/.env -f deploy/compose.yml down
```
