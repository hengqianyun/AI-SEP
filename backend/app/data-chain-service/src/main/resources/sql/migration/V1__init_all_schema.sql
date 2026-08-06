-- ============================================================
-- V1: 项目初始化基线 — demo 示例表 + demo 种子数据 + WSC 业务表
-- 创建时间：2026-08-04
-- ============================================================

-- ---------------------------------------------------------
-- 1. Demo 示例表（早期遗留，沿用 created_at / updated_at）
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS `demo`
(
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `name`        VARCHAR(100)    NOT NULL COMMENT '名称',
    `description` VARCHAR(500)    NULL COMMENT '描述',
    `created_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `updated_at`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_demo_name` (`name`)
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Demo 示例表';

-- ---------------------------------------------------------
-- 2. Demo 种子数据
-- ---------------------------------------------------------
INSERT INTO `demo` (`name`, `description`)
SELECT '示例数据', '由 Flyway 写入的数据'
WHERE NOT EXISTS (
    SELECT 1
    FROM `demo`
    WHERE `name` = '示例数据'
);

-- ============================================================
-- 3. WSC 业务表（按 CLAUDE.md 规范：t_ 前缀、审计四件套 + del_flag、无外键）
-- ============================================================

-- ---------------------------------------------------------
-- 3.1 行业分类（三级：L1 空间 / L2 行业 / L3 子类）
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_industry_category` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `code`          VARCHAR(64)     NOT NULL                COMMENT '分类编码（业务唯一标识）',
    `name`          VARCHAR(256)    NOT NULL                COMMENT '分类名称',
    `level`         VARCHAR(8)      NOT NULL                COMMENT '层级：L1/L2/L3（应用层校验）',
    `parent_code`   VARCHAR(64)     NULL                    COMMENT '上级分类 code（无外键）',
    `create_time`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3)           COMMENT '创建时间',
    `create_by`     VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '创建人',
    `update_time`   DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`     VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '更新人',
    `del_flag`      TINYINT         NOT NULL DEFAULT 0                              COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_industry_category_code` (`code`),
    KEY             `idx_t_industry_category_parent_code` (`parent_code`),
    KEY             `idx_t_industry_category_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='行业分类（三级树）';

-- ---------------------------------------------------------
-- 3.2 数据产品
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_data_product` (
    `id`                      BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `product_code`            VARCHAR(128)    NOT NULL                COMMENT '产品编码（唯一业务标识）',
    `product_name`            VARCHAR(512)    NOT NULL                COMMENT '产品名称',
    `product_type`            VARCHAR(32)     NOT NULL                COMMENT '产品类型：DATASET/REPORT/API/OTHER',
    `industry_category_code`  VARCHAR(64)     NULL                    COMMENT '三级分类 code（无外键）',
    `maintenance_status`      VARCHAR(32)     NOT NULL DEFAULT 'PENDING' COMMENT '维护状态：PENDING/MAINTAINED',
    `business_category`       VARCHAR(256)    NULL                    COMMENT '行业门类（GB/T 4754）',
    `business_sub_category`   VARCHAR(256)    NULL                    COMMENT '行业大类',
    `data_source`             VARCHAR(64)     NULL                    COMMENT '数据来源',
    `update_frequency`        VARCHAR(64)     NULL                    COMMENT '更新频率',
    `delivery_method`         VARCHAR(64)     NULL                    COMMENT '交付方式',
    `involves_personal_info`  TINYINT(1)      NULL                    COMMENT '是否涉及个人信息',
    `involves_public_data`    TINYINT(1)      NULL                    COMMENT '是否涉及公共数据',
    `billing_method`          VARCHAR(128)    NULL                    COMMENT '计费方式',
    `price`                   VARCHAR(128)    NULL                    COMMENT '价格',
    `supplier_name`           VARCHAR(256)    NULL                    COMMENT '供应商名称',
    `supplier_credit_code`    VARCHAR(128)    NULL                    COMMENT '统一社会信用代码',
    `property_rights_type`    VARCHAR(128)    NULL                    COMMENT '产权类型',
    `tags_json`               TEXT            NULL                    COMMENT '标签 JSON',
    `summary`                 TEXT            NULL                    COMMENT '产品简介',
    `scenario`                TEXT            NULL                    COMMENT '应用场景',
    `type_dataset_json`       TEXT            NULL                    COMMENT '数据集类型专属字段 JSON',
    `type_report_json`        TEXT            NULL                    COMMENT '报告类型专属字段 JSON',
    `type_api_json`           TEXT            NULL                    COMMENT 'API 类型专属字段 JSON',
    `status`                  VARCHAR(32)     NOT NULL DEFAULT 'LISTED' COMMENT '产品状态',
    `create_time`             DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3)           COMMENT '创建时间',
    `create_by`               VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '创建人',
    `update_time`             DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`               VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '更新人',
    `del_flag`                TINYINT         NOT NULL DEFAULT 0                              COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_data_product_code` (`product_code`),
    KEY             `idx_t_data_product_industry_category` (`industry_category_code`),
    KEY             `idx_t_data_product_type` (`product_type`),
    KEY             `idx_t_data_product_maint` (`maintenance_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据产品';

-- ---------------------------------------------------------
-- 3.3 上链版本
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_chain_version` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `version_code`    VARCHAR(64)     NOT NULL                COMMENT '版本编码（唯一业务标识）',
    `product_code`    VARCHAR(128)    NOT NULL                COMMENT '产品编码（关联 t_data_product.product_code）',
    `version_no`      INT             NOT NULL                COMMENT '版本号（产品内递增）',
    `metadata_hash`   VARCHAR(256)    NOT NULL                COMMENT '元数据哈希',
    `owner_did`       VARCHAR(512)    NOT NULL                COMMENT '权属方 DID',
    `cert_owner`      VARCHAR(512)    NOT NULL                COMMENT '证书权属方',
    `ts`              TIMESTAMP(3)    NOT NULL                COMMENT '存证时间戳',
    `create_time`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3)           COMMENT '创建时间',
    `create_by`       VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '创建人',
    `update_time`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`       VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '更新人',
    `del_flag`        TINYINT         NOT NULL DEFAULT 0                              COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_chain_version_code` (`version_code`),
    UNIQUE KEY `uk_t_chain_version_product_no` (`product_code`, `version_no`),
    KEY               `idx_t_chain_version_product` (`product_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上链版本记录';

-- ---------------------------------------------------------
-- 3.4 上链目录快照
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_chain_catalog_snapshot` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `version_code`    VARCHAR(64)     NOT NULL                COMMENT '版本编码（关联 t_chain_version.version_code）',
    `snapshot_json`   TEXT            NOT NULL                COMMENT '目录快照 JSON',
    `create_time`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3)           COMMENT '创建时间',
    `create_by`       VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '创建人',
    `update_time`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`       VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '更新人',
    `del_flag`        TINYINT         NOT NULL DEFAULT 0                              COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_chain_catalog_snapshot_version` (`version_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='上链目录快照';

-- ---------------------------------------------------------
-- 3.5 总览流事件
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_overview_stream_event` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `event_type`      VARCHAR(32)     NOT NULL                COMMENT '事件类型：CATALOG_REGISTER/DATA_REGISTER/TRADE_ORDER',
    `subject`         VARCHAR(512)    NOT NULL                COMMENT '事件主题',
    `action_summary`  VARCHAR(1024)   NOT NULL                COMMENT '事件摘要',
    `occurred_at`     TIMESTAMP(3)    NOT NULL                COMMENT '事件发生时间',
    `chain_record_id` VARCHAR(256)    NOT NULL                COMMENT '链上记录 ID',
    `create_time`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3)           COMMENT '创建时间',
    `create_by`       VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '创建人',
    `update_time`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`       VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '更新人',
    `del_flag`        TINYINT         NOT NULL DEFAULT 0                              COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY               `idx_t_overview_stream_occurred` (`occurred_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='总览流事件';

-- ---------------------------------------------------------
-- 3.6 导入错误报告（TTL 24h）
-- ---------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_import_error_report` (
    `id`              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `expires_at`      TIMESTAMP(3)    NOT NULL                COMMENT '过期时间（创建时间 +24h）',
    `success_count`   INT             NOT NULL DEFAULT 0      COMMENT '导入成功数',
    `failure_count`   INT             NOT NULL DEFAULT 0      COMMENT '导入失败数',
    `rows_json`       TEXT            NOT NULL                COMMENT '错误行 JSON',
    `create_time`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3)           COMMENT '创建时间',
    `create_by`       VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '创建人',
    `update_time`     DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`       VARCHAR(64)     NOT NULL DEFAULT ''                             COMMENT '更新人',
    `del_flag`        TINYINT         NOT NULL DEFAULT 0                              COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY               `idx_t_import_error_report_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导入错误报告（TTL 24h）';

