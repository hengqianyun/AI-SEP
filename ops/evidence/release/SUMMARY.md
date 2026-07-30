# WSC RELEASE 证据摘要 — Flyway 空库 + 回滚演练 A

| 字段 | 值 |
|---|---|
| evidenceId | TESTRUN-WSC-RELEASE-GATES |
| actorInstance | tester-wsc-release-gates |
| testedAt | 2026-07-30T11:30:48+08:00 |
| gitSha | 4805318cb07d5b753ca13b8b09b25ab87b48e20e |
| contracts | wsc-contracts@1.1.0（迁移注释对齐） |
| result | **PASSED**（两项证据缺口均关闭；不含人类 MAINTAINER 批准） |

## 环境

| 项 | 值 |
|---|---|
| Docker Client/Server | 29.0.1 |
| PostgreSQL 镜像 | `postgres:16` @ `sha256:33f923b05f64ca54ac4401c01126a6b92afe839a0aa0a52bc5aeb5cc958e5f20` |
| Flyway 镜像 | `flyway/flyway:10-alpine`（Flyway OSS 10.22.0）@ `sha256:94a81ca7db9a9f24fd8acd7463fa4560cb8f66aae2aa64485c27eb296f5851cf` |
| 容器名 | `wsc-pg-release`（演练后已 `docker rm -f`） |
| 库端口映射 | 宿主 `5432→5432`（宿主另有进程亦监听 5432；Flyway **未**经 host 端口，改用 docker network） |
| Docker network | `wsc-release-net`（演练后已删除） |
| 凭证 | 一次性容器 env `POSTGRES_USER/PASSWORD/DB=wsc`；**未**写入 git 跟踪的 `application-local.yml` |

## 1. Flyway 空库干净应用 — PASSED

### 命令

```text
docker rm -f wsc-pg-release 2>$null
docker run -d --name wsc-pg-release `
  -e POSTGRES_USER=wsc -e POSTGRES_PASSWORD=wsc -e POSTGRES_DB=wsc `
  -p 5432:5432 postgres:16
# exitCode=0；pg_isready exitCode=0

docker network create wsc-release-net
docker network connect wsc-release-net wsc-pg-release

docker run --rm --network wsc-release-net `
  -v "C:\WorkSpace\AI-SEP\backend\src\main\resources\db\migration:/flyway/sql:ro" `
  flyway/flyway:10-alpine `
  -url=jdbc:postgresql://wsc-pg-release:5432/wsc `
  -user=wsc -password=wsc -connectRetries=5 migrate
# exitCode=0
```

### 成功判据（已满足）

- Flyway 输出：`Successfully applied 1 migration ... now at version v202607281600`
- `flyway_schema_history`：`version=202607281600`，`script=V202607281600__init_wsc.sql`，`success=t`（见 `flyway-schema-history.txt`）
- `\dt` 含 `industry_category`、`data_product`、`chain_version`、`chain_catalog_snapshot` 等（见 `psql-dt-after-migrate.txt`）

### 备注

首次尝试经 `host.docker.internal:5432` 失败（Password 认证失败），因宿主 `5432` 存在双监听；改用容器网络直连 `wsc-pg-release:5432` 后成功。不记为 BLOCKED（可执行路径已验证）。

## 2. 回滚演练（策略 A：备份恢复）— PASSED

**禁止项遵守**：未使用 `flyway undo` / down 脚本。

### 命令（有效演练；首次失败后重试）

```text
# 备份（容器内 UTF-8，避免 PowerShell 重定向 UTF-16 BOM）
docker exec wsc-pg-release pg_dump -U wsc -d wsc -F p --no-owner --no-acl -f /tmp/pg_dump-pre.sql
# exitCode=0
docker cp wsc-pg-release:/tmp/pg_dump-pre.sql ops/evidence/release/pg_dump-pre.sql
# exitCode=0；SHA256=499C77C051D2E57A77A68E25393EE7FCADDEDD096967ED719AD9BD30D1F10052

# 探针
docker exec ... INSERT INTO industry_category ... 'probe-release-001' ...
# exitCode=0

# 破坏后恢复
docker exec ... psql -d postgres -c "DROP DATABASE wsc;"   # exitCode=0
docker exec ... psql -d postgres -c "CREATE DATABASE wsc OWNER wsc;"  # exitCode=0
docker exec ... psql -d wsc -v ON_ERROR_STOP=1 -f /tmp/pg_dump-pre.sql  # exitCode=0
```

### 校验（`post-restore-verify.txt`）

| 检查 | 结果 |
|---|---|
| `flyway_schema_history` 恢复且 success=t | 是 |
| `industry_category` / `data_product` 存在 | 是 |
| 探针 `probe-release-001` 行数 | **0**（备份在插入前，恢复后探针消失 = 回滚生效） |

### 首次失败（已记录，不记 PASS）

- 多语句 `DROP DATABASE` 同 `-c` → transaction block 错误（`recreate_exit=1`）
- PowerShell `>` 重定向 `pg_dump` → UTF-16 BOM → restore `invalid byte sequence`
- 见 `commands.log` 11:27 段；**以 11:28 RETRY 段为准**

## 清理

- `docker rm -f wsc-pg-release` exitCode=0
- `docker network rm wsc-release-net` exitCode=0
- **未删除**本目录证据文件

## 证据文件清单

| 文件 | 说明 |
|---|---|
| `SUMMARY.md` | 本摘要 |
| `commands.log` | 时间戳命令/exitCode |
| `flyway-schema-history.txt` | migrate 后 history |
| `psql-dt-after-migrate.txt` | migrate 后表列表 |
| `key-tables-check.txt` | 关键表 to_regclass |
| `pg_dump-pre.sql` | 策略 A 逻辑备份 |
| `pg_dump-pre.sha256.txt` | 备份哈希 |
| `probe-insert.txt` | 探针插入 |
| `restore-output.txt` | 恢复输出 |
| `psql-dt-after-restore.txt` | 恢复后表列表 |
| `post-restore-verify.txt` | 探针=0 + history |
| `drop-recreate-db.txt` | 首次失败 DROP 尝试残留 |

## 非本证据范围

- **未**伪造 / 写入 MAINTAINER 人类发布批准
- **未**修改 `ai/runs/**`、业务源码、契约
