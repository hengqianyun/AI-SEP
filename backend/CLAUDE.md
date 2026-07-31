# CLAUDE.md

本文件是本项目的 AI 工作指令（由 Claude Code 自动加载）兼团队约定。项目概况、构建与启动方式见 `README.md`。

技术栈：Java 17 / Spring Boot 2.7.18 / Spring Data JPA / QueryDSL / **Flyway** / MySQL / Redis / Knife4j。

---

## 编码规则

> 以下为通用编码约定，适用于所有 Java/Spring Boot 代码；数据库 schema 相关变更另见下方"数据库变更规则"。

### 1. 复用与抽象

- **复用优先**：尽量复用现有方法，避免重新造轮子。
- **独立能力抽取**：能抽成独立能力的方法不要包含在大段代码块中，单独成方法，便于复用与测试。

### 2. 分层职责

- **Controller 只做编排**：Controller 只负责调用方法的顺序、判断和组合；业务逻辑一律下沉到 Service。

### 3. 命名

- **见名知意**：命名清晰达意，避免缩写；通用缩写（如 URL、DTO、ID、VO）可保留。

### 4. 文档与注释

- **Javadoc 必写**：每个方法都要有 Javadoc，描述清楚功能、入参、出参。
- **逻辑注释**：多写清楚逻辑注释，方便排查问题。
- **功能清单（Tips）**：每个 Service 的根目录下编写功能清单；以 `Tips` 开头、固定格式、固定搜索，便于快速查找定位。

### 5. 数据库

- **慎用数据库**：尽量避免使用数据库，除非确有必要；确需建表/改表时严格遵守下方 Flyway 规则。

### 6. 大段代码编写流程（顺序铁律，不得跳步）

1. 先写伪代码 `//TODO`，保证整体逻辑清晰；
2. 再补注释；
3. 最后填充所有伪代码实现。

### 7. 排错顺序

- 出错时依次排查：**自己的代码 → 依赖的代码 → 环境**。

### 8. 兜底与不可妥协（硬约束）

- **不得以"简化代码"或"性能更好"为由违反上述任何规则**；有疑问先让用户判断。
- **输出受限兜底**：若输出长度受限，优先保证 Javadoc、逻辑注释和伪代码框架的完整，严禁省略注释直接输出大段代码。

---

## 数据库变更规则（Flyway 是唯一入口）

### 1. 核心原则（硬约束）

1. **所有数据库变更只通过 Flyway 执行。** DDL（建表、改表、加/删字段、索引、约束）和必要的初始化 DML，只能写在 `app/data-chain-service/src/main/resources/sql/migration/` 下的版本脚本里，由应用启动时 Flyway 自动执行。
   - 禁止直连数据库手工改表。
   - 禁止依赖 Hibernate 建表：`spring.jpa.hibernate.ddl-auto` 固定为 `validate`，只校验、不生成。

2. **代码引起的数据库变更必须同步 Flyway。** 凡改动会影响 schema 的代码——新增/修改 JPA 实体、加字段、改类型或长度、加索引、改表名、删字段等——**必须同时新增一个版本号递增的迁移脚本 `V{n}__xxx.sql`**。绝不允许只改 Java 代码而漏掉迁移脚本。schema 与实体不一致时，`validate` 会导致应用启动失败。

3. **版本脚本一旦发布即不可变。** 已存在/已执行过的 `V{n}__` 脚本禁止任何修改（Flyway 会因 checksum 不一致在启动时校验失败）。需要修正或回滚时，只能追加一个更高版本的新脚本（如 `V5__drop_xxx`、`V6__rename_xxx`）。

### 2. 脚本命名

- 路径：`app/data-chain-service/src/main/resources/sql/migration/`
- 格式：`V{版本号}__{动词}_{对象}.sql`，版本号为递增整数（必要时点分如 `V3_1`）；版本号与描述之间是**两个下划线** `__`。
- 描述：全小写、下划线分词、动宾结构，清楚表达变更内容。
  - 正例：`V3__create_sys_user_table.sql`、`V4__add_status_to_t_order_header.sql`、`V5__create_idx_t_order_status.sql`
  - 反例：`V3.sql`、`V3__update.sql`（描述含糊）、`v3__xxx.sql`（V 须大写）

### 3. 表名规则

一律小写、下划线分词、单数。

| 类型 | 前缀 | 格式 | 示例 |
| --- | --- | --- | --- |
| 系统表 | `sys_` | `sys_{名称}` | `sys_user`、`sys_role`、`sys_dict`、`sys_log`、`sys_config` |
| 业务表 | `t_` | `t_{业务模块}_{实例(可选)}` | `t_order`、`t_order_header`、`t_order_detail`、`t_trade_account` |

- `sys_`：平台级、跨业务通用、基础设施类表（用户、权限、字典、日志、配置）。
- `t_`：领域业务表。`{业务模块}` 必填；`{实例}` 可选，用于一个模块内多张表区分主从/头尾（如订单头表 `t_order_header` 与明细 `t_order_detail`）。
- 现有 `demo` 表为早期示例，保留；**新建表一律遵守本规则**。

### 4. 列与约束命名

- 主键：`id`（`BIGINT UNSIGNED AUTO_INCREMENT`，与 demo 一致）。
- 外键：不允许使用跨表外键；唯一键：`uk_{表}_{字段}`；普通索引：`idx_{表}_{字段}`。
- **审计字段四件套（业务/系统表必备）**：每张业务表与系统表必须同时包含以下四列，缺一不可：
  - `create_time` `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)` —— 创建时间；
  - `create_by` `VARCHAR(64) NOT NULL DEFAULT ''` —— 创建人（存用户登录名或用户 ID 字符串，不建外键）；
  - `update_time` `DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3)` —— 更新时间；
  - `update_by` `VARCHAR(64) NOT NULL DEFAULT ''` —— 更新人。
  - 插入写满 `create_*`；任何更新都必须刷新 `update_time` 与 `update_by`（详见第 6 节）。
- **逻辑删除标记（必备）**：每张表必须含 `del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除'`，默认 0，删除即置 1；详见第 6 节。
- **关系表例外**：仅记录关联关系、且仅靠唯一键（`uk_`）保证幂等的"短表"（表内只有关联外键列、无独立业务属性），可省略 `create_by` / `update_by` / `del_flag`，建议至少保留 `create_time`。
- 旧 `demo` 表沿用 `created_at` / `updated_at`（无 `create_by` / `update_by` / `del_flag`），属早期遗留示例，已发布脚本不可改；**新建表一律按上述四件套 + `del_flag` 执行**。
- 字符集 `utf8mb4`、排序 `utf8mb4_unicode_ci`；存储引擎 `InnoDB`。

### 5. 编写约定

- 脚本要幂等可重入：建表用 `CREATE TABLE IF NOT EXISTS`；初始化数据用 `INSERT ... SELECT ... WHERE NOT EXISTS` 或 `ON DUPLICATE KEY UPDATE`。
- 一个版本脚本聚焦一个变更主题（一张新表，或一组紧密相关的字段变更），便于审阅和回滚定位。
- `sql/init/` 仅用于全新空库的冷启动（建库 + 打 baseline），**不在其中维护业务 schema**；业务表与数据统一由 `sql/migration/` 驱动。
- 生成迁移脚本后，条件允许时启动应用验证 Flyway 能顺利执行，或至少确认 JPA 实体与 schema 对齐、`validate` 通过。

### 6. 审计自动填充与逻辑删除（硬约束）

- **自动填充审计字段**：JPA 实体层面的插入/更新必须自动写入四个审计字段——插入写 `create_time` / `create_by`，更新写 `update_time` / `update_by`。推荐用 `@PrePersist` / `@PreUpdate` 监听器或统一审计切面填充，`create_by` / `update_by` 从安全上下文取当前用户。**任何 `UPDATE` 都必须刷新 `update_time` 与 `update_by`**，不得手写 SQL/JPQL 绕过；如确需批量更新，须在同一语句里显式带上 `update_time` 与 `update_by`。
- **逻辑删除优先**：删除一律做逻辑删除——`UPDATE 表 SET del_flag = 1, update_time = ?, update_by = ? WHERE ...`，**禁止 `DELETE` 物理删除**（关系表等明确无需软删的短表除外）。默认查询应自带 `del_flag = 0` 条件，仅在需要检索已删除数据时显式去掉。
- **实体与 schema 对齐**：启用逻辑删除的实体应配合 `@Where(clause = "del_flag = 0")`（Hibernate）等机制，避免查出脏数据；新增 `del_flag`、`create_by`、`update_by` 列时，实体与 Flyway 脚本必须同步，否则 `validate` 启动失败。
