-- =====================================================================
-- 初始化 Flyway 版本历史表并预置 baseline(version = 0)
-- ----------------------------------------------------------------------
-- 用途:与 00_create_database.sql 配合,人工搭建一个全新的数据库。
-- 本脚本只负责建立 Flyway 自身的版本历史表 flyway_schema_history,并预置
-- 一条 baseline 记录(version = 0),与 application.yml 中的配置一致:
--     spring.flyway.baseline-on-migrate: true
--     spring.flyway.baseline-version:   0
--
-- 业务表结构与初始数据不再由 init/ 维护,统一交由 migration/ 下的 Flyway
-- 版本脚本驱动。应用首次启动时,Flyway 识别到已存在的 baseline(v0),
-- 会自动从 V1 开始依次执行后续迁移脚本,完成业务表与数据的创建。
--
-- 说明:本表列定义及 baseline 记录格式,与 Flyway 8.5.13 自行建表并打基线
-- 后的产物完全一致(经隔离环境实际运行探测),可被 Flyway 无感识别。
-- =====================================================================

USE `data_chain`;

-- 1. 创建 Flyway 版本历史表(列定义与 Flyway 8.5.13 自动建表一致)
CREATE TABLE IF NOT EXISTS `flyway_schema_history`
(
    `installed_rank` INT           NOT NULL COMMENT '执行序号(主键)',
    `version`        VARCHAR(50)   NULL COMMENT '版本号,baseline 记为 0',
    `description`    VARCHAR(200)  NOT NULL COMMENT '描述',
    `type`           VARCHAR(20)   NOT NULL COMMENT '类型:SQL / BASELINE / TABLE 等',
    `script`         VARCHAR(1000) NOT NULL COMMENT '脚本名称',
    `checksum`       INT           NULL COMMENT '校验和,baseline / 建表标记为 NULL',
    `installed_by`   VARCHAR(100)  NOT NULL COMMENT '执行人',
    `installed_on`   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '执行时间',
    `execution_time` INT           NOT NULL COMMENT '执行耗时(ms)',
    `success`        BOOL          NOT NULL COMMENT '是否执行成功',
    PRIMARY KEY (`installed_rank`)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Flyway 数据库版本迁移历史表';

-- 2. 复刻 Flyway 建表时写入的元记录(installed_rank = -1)
INSERT INTO `flyway_schema_history`
    (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`,
     `installed_by`, `installed_on`, `execution_time`, `success`)
SELECT -1,
       NULL,
       '<< Flyway Schema History table created >>',
       'TABLE',
       '',
       NULL,
       SUBSTRING_INDEX(CURRENT_USER(), '@', 1),
       CURRENT_TIMESTAMP,
       0,
       TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM `flyway_schema_history`
    WHERE `installed_rank` = -1
);

-- 3. 预置 baseline 记录(version = 0,与 baseline-version 配置一致)
INSERT INTO `flyway_schema_history`
    (`installed_rank`, `version`, `description`, `type`, `script`, `checksum`,
     `installed_by`, `installed_on`, `execution_time`, `success`)
SELECT 1,
       '0',
       '<< Flyway Baseline >>',
       'BASELINE',
       '<< Flyway Baseline >>',
       NULL,
       SUBSTRING_INDEX(CURRENT_USER(), '@', 1),
       CURRENT_TIMESTAMP,
       0,
       TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM `flyway_schema_history`
    WHERE `installed_rank` = 1
      AND `type` = 'BASELINE'
);
