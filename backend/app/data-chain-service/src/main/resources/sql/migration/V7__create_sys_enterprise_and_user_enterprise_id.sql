-- V7: 最小企业实体 + sys_user.enterprise_id 逻辑归属（REQ-USER-002 / TASK-WSC-904）
-- H2/MySQL 兼容：无 IF NOT EXISTS 的 ALTER、无 AFTER、无跨表物理外键（CLAUDE.md）。
-- Flyway 版本号保证脚本只执行一次；数据写入用 INSERT ... WHERE NOT EXISTS。
-- 本脚本操作者约定：create_by / update_by = 'system'。
--
-- enterprise_name 兼容 / 回填 / deprecate 策略（单一真源 = enterprise_id → sys_enterprise）：
-- 1) 兼容：保留 sys_user.enterprise_name（NOT NULL），供旧客户端与列表展示；写入时用企业表名称回填该列，禁止与 enterprise_id 双源冲突。
-- 2) 回填：按 enterprise_name 匹配 sys_enterprise.name（优先非未归属企业）；匹配失败或空名 → 未归属默认企业（code=UNASSIGNED，OQ-V16-002）。
-- 3) deprecate：V1.6 起会话/授权只信 enterprise_id；enterprise_name 为反规范化缓存。后续版本可删除该列，本版不删以免旧库/报表中断。
-- 4) 安全：UNASSIGNED 仅为可审计占位，不得与演示企业 DEMO 共用 id；orphan 用户因此不会获得其他企业产品的 mine/myCatalog 写权限（写拦截由 905 消费本字段）。
-- 5) 未匹配非空原名：第三段 UPDATE 用企业表规范名覆盖缓存列（消除双源）。覆盖前写入可查询备份表
--    sys_user_v7_enterprise_name_backup（至少 id, username, enterprise_name, enterprise_id）。
--    警告：未匹配原名覆盖后，仅靠 DROP INDEX/COLUMN/TABLE 不可恢复；须从备份表还原。备份表亦删除后不可恢复。
--
-- ============================================================
-- 前置备份与回滚 / 前进修复手册（FIND-MIG-WSC-904-001）
-- ============================================================
-- 前置备份：在覆盖未匹配非空 enterprise_name 之前，同脚本 CREATE TABLE IF NOT EXISTS
--   + INSERT ... SELECT 写入 sys_user_v7_enterprise_name_backup
--   （列：id, username, enterprise_name, enterprise_id + 审计四件套 + del_flag）。
--
-- 回滚触发条件（满足任一即应停止前进、按下列步骤处理）：
--   A) 第三段覆盖后业务无法接受未匹配原名丢失（例如须保留「未知公司」展示）
--   B) 应用启动 Hibernate ddl-auto=validate 失败，或回填结果与预期不符
--   C) 需整段撤回 V7（去掉企业表与 sys_user.enterprise_id）
--
-- 恢复未匹配原名（须在 DROP 备份表之前执行；仅还原备份行的 enterprise_name）：
--   UPDATE `sys_user` u
--   INNER JOIN `sys_user_v7_enterprise_name_backup` b ON u.`id` = b.`id`
--   SET u.`enterprise_name` = b.`enterprise_name`,
--       u.`update_time` = CURRENT_TIMESTAMP(3),
--       u.`update_by` = 'system'
--   WHERE b.`del_flag` = 0;
--
-- 撤回扩展 DDL（前进修复 / 完全回滚；不能恢复未匹配原名）：
--   DROP INDEX `idx_sys_user_enterprise_id` ON `sys_user`;
--   ALTER TABLE `sys_user` DROP COLUMN `enterprise_id`;
--   DROP TABLE IF EXISTS `sys_user_v7_enterprise_name_backup`;
--   DROP TABLE IF EXISTS `sys_enterprise`;
-- 警告：未匹配原名覆盖后不可恢复（除非先从备份表还原，且备份表尚未删除）。
--
-- Flyway：本脚本成功一次后不会重跑。若 MySQL DDL 隐式提交后后续 DML 失败，
--   列/索引可能已存在，须 flyway repair 后再手工补跑补偿 DML 或依赖启动 backfillEnterpriseIds。

-- ============================================================
-- 1. 企业表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_enterprise` (
    `id`                    BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `code`                  VARCHAR(64)     NOT NULL                COMMENT '企业编码（业务唯一）',
    `name`                  VARCHAR(256)    NOT NULL                COMMENT '企业名称',
    `unassigned_default`    TINYINT         NOT NULL DEFAULT 0      COMMENT 'OQ-V16-002：1=未归属默认企业（orphan 占位）',
    `create_time`           DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`             VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`           DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`             VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`              TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_enterprise_code` (`code`),
    KEY `idx_sys_enterprise_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统企业（最小实体）';

INSERT INTO `sys_enterprise` (`code`, `name`, `unassigned_default`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`)
SELECT 'DEMO', '演示企业', 0, CURRENT_TIMESTAMP(3), 'system', CURRENT_TIMESTAMP(3), 'system', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_enterprise` WHERE `code` = 'DEMO');

INSERT INTO `sys_enterprise` (`code`, `name`, `unassigned_default`, `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`)
SELECT 'UNASSIGNED', '未归属默认企业', 1, CURRENT_TIMESTAMP(3), 'system', CURRENT_TIMESTAMP(3), 'system', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_enterprise` WHERE `code` = 'UNASSIGNED');

-- ============================================================
-- 2. sys_user 增加逻辑归属列（0=迁移占位，随后回填）
-- ============================================================
ALTER TABLE `sys_user` ADD COLUMN `enterprise_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '归属企业 ID（逻辑 FK，无物理外键）';

CREATE INDEX `idx_sys_user_enterprise_id` ON `sys_user` (`enterprise_id`);

-- ============================================================
-- 3. 回填：按名称匹配 → 剩余进 UNASSIGNED → 备份未匹配非空原名 → 同步缓存列
-- ============================================================
UPDATE `sys_user`
SET `enterprise_id` = (
    SELECT MIN(e.`id`) FROM `sys_enterprise` e
    WHERE e.`name` = `sys_user`.`enterprise_name`
      AND e.`del_flag` = 0
      AND e.`unassigned_default` = 0
),
    `update_time` = CURRENT_TIMESTAMP(3),
    `update_by` = 'system'
WHERE `enterprise_id` = 0
  AND EXISTS (
    SELECT 1 FROM `sys_enterprise` e
    WHERE e.`name` = `sys_user`.`enterprise_name`
      AND e.`del_flag` = 0
      AND e.`unassigned_default` = 0
);

UPDATE `sys_user`
SET `enterprise_id` = (
    SELECT MIN(e.`id`) FROM `sys_enterprise` e
    WHERE e.`code` = 'UNASSIGNED' AND e.`del_flag` = 0
),
    `update_time` = CURRENT_TIMESTAMP(3),
    `update_by` = 'system'
WHERE `enterprise_id` = 0;

-- 覆盖未匹配非空名称之前：可查询备份（FIND-MIG-WSC-904-001 closeWhen ②）
CREATE TABLE IF NOT EXISTS `sys_user_v7_enterprise_name_backup` (
    `id`               BIGINT UNSIGNED NOT NULL                COMMENT 'sys_user.id（覆盖前快照）',
    `username`         VARCHAR(64)     NOT NULL                COMMENT '登录名',
    `enterprise_name`  VARCHAR(256)    NOT NULL                COMMENT '覆盖前原企业名',
    `enterprise_id`    BIGINT UNSIGNED NOT NULL                COMMENT '回填后的归属企业 ID',
    `create_time`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '备份时间',
    `create_by`        VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '备份操作者',
    `update_time`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`        VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`         TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='V7 覆盖未匹配企业名之前的可查询备份';

INSERT INTO `sys_user_v7_enterprise_name_backup` (
    `id`, `username`, `enterprise_name`, `enterprise_id`,
    `create_time`, `create_by`, `update_time`, `update_by`, `del_flag`
)
SELECT u.`id`, u.`username`, u.`enterprise_name`, u.`enterprise_id`,
       CURRENT_TIMESTAMP(3), 'system', CURRENT_TIMESTAMP(3), 'system', 0
FROM `sys_user` u
WHERE u.`enterprise_id` <> 0
  AND u.`enterprise_name` <> ''
  AND u.`enterprise_name` <> (
      SELECT e.`name` FROM `sys_enterprise` e
      WHERE e.`id` = u.`enterprise_id` AND e.`del_flag` = 0
  )
  AND NOT EXISTS (
      SELECT 1 FROM `sys_user_v7_enterprise_name_backup` b WHERE b.`id` = u.`id`
  );

UPDATE `sys_user`
SET `enterprise_name` = (
    SELECT e.`name` FROM `sys_enterprise` e WHERE e.`id` = `sys_user`.`enterprise_id`
),
    `update_time` = CURRENT_TIMESTAMP(3),
    `update_by` = 'system'
WHERE `enterprise_id` <> 0
  AND EXISTS (SELECT 1 FROM `sys_enterprise` e WHERE e.`id` = `sys_user`.`enterprise_id`);
