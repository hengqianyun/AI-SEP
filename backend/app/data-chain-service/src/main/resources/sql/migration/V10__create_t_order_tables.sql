-- V10: 订单表（PLAN-WSC-9.2 §3.2 / TASK-WSC-912）
-- 需求映射：REQ-WSC-ORDER-006, REQ-WSC-ORDER-009, REQ-WSC-ORDER-013,
--           REQ-WSC-ORDER-014, REQ-WSC-ORDER-016, REQ-WSC-ORDER-017, REQ-WSC-ORDER-003
--
-- 规则遵守（CLAUDE.md）：
--   t_ 前缀、单数表名；审计四件套 + del_flag；无跨表物理外键；
--   BIGINT UNSIGNED AUTO_INCREMENT PK；utf8mb4 / utf8mb4_unicode_ci / InnoDB；
--   金额 DECIMAL(12,2) 两列（含税/未税）；订单号 VARCHAR(36) UUID v4。
--
-- 幂等策略：CREATE TABLE IF NOT EXISTS。
-- 产品表 ALTER TABLE ADD COLUMN 需应用层保证不重复执行（重复执行会报列已存在错误，
-- 由 Flyway baseline 或 repair 处理；本脚本发布后不可修改）。
--
-- 本脚本仅允许 TASK-WSC-912 写入。

-- ============================================================
-- 1. t_order_header（订单头表）
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_order_header` (
    `id`                        BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_no`                  VARCHAR(36)     NOT NULL                COMMENT '订单号（UUID v4，不可含业务语义）',
    `status`                    VARCHAR(32)     NOT NULL DEFAULT ''     COMMENT '订单状态（PENDING_CONFIRM / PENDING_UPLOAD / PENDING_CONTRACT_CONFIRM / CONTRACT_REACHED / CANCELLED）',
    `demand_user_id`            VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '需求方用户 ID（create_by 逻辑等价）',
    `provider_enterprise_id`    VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '提供方企业 ID（产品 create_by 对应 enterprise_id）',
    `product_id`                BIGINT          NOT NULL DEFAULT 0      COMMENT '产品 ID',
    `product_code`              VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '产品编码',
    `product_snapshot`          JSON                                    COMMENT '下单时产品快照（冻结，不跟产品漂移）',
    `remark`                    TEXT                                    COMMENT '备注',
    `notice_version`            VARCHAR(32)     NOT NULL DEFAULT ''     COMMENT '平台统一须知版本号（服务端自动填充）',
    `amount_tax_inclusive`      DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '含税金额（人民币两位小数）',
    `amount_tax_exclusive`      DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '未税金额（人民币两位小数，本期与含税同价）',
    `currency`                  VARCHAR(8)      NOT NULL DEFAULT 'CNY'  COMMENT '币种',
    `chain_count`               INT             NOT NULL DEFAULT 0      COMMENT '上链次数缓存（mock 记录条数，与详情时间线一致）',
    `create_time`               DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`                 VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`               DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`                 VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`                  TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_order_header_order_no` (`order_no`),
    KEY `idx_t_order_header_demand_user_id` (`demand_user_id`),
    KEY `idx_t_order_header_provider_enterprise_id` (`provider_enterprise_id`),
    KEY `idx_t_order_header_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单头表';

-- ============================================================
-- 2. t_order_line（订单明细 / 计费行）
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_order_line` (
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_header_id`   BIGINT UNSIGNED NOT NULL                COMMENT '所属订单头 ID（业务列，非物理 FK）',
    `unit`              VARCHAR(32)     NOT NULL DEFAULT ''     COMMENT '计量单位',
    `quantity`          INT             NOT NULL DEFAULT 0      COMMENT '数量',
    `unit_price`        DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '单价（人民币两位小数）',
    `subtotal`          DECIMAL(12,2)   NOT NULL DEFAULT 0.00   COMMENT '小计（人民币两位小数）',
    `create_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`          TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_t_order_line_order_header_id` (`order_header_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细（计费行，仅由 contract 阶段写入）';

-- ============================================================
-- 3. t_order_event（订单时间线事件）
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_order_event` (
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_header_id`   BIGINT UNSIGNED NOT NULL                COMMENT '所属订单头 ID（业务列，非物理 FK）',
    `event_type`        VARCHAR(32)     NOT NULL DEFAULT ''     COMMENT '事件类型（CREATED / CONFIRMED / CONTRACT_UPLOADED / CONTRACT_CONFIRMED / CANCELLED 等）',
    `description`       TEXT                                    COMMENT '事件描述',
    `operator_id`       VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '操作人用户 ID',
    `operator_role`     VARCHAR(16)     NOT NULL DEFAULT ''     COMMENT '操作人角色（ADMIN / PROVIDER / USER）',
    `create_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`          TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_t_order_event_order_header_id` (`order_header_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单时间线事件';

-- ============================================================
-- 4. t_order_chain_log（订单上链日志 — mock 模拟记录）
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_order_chain_log` (
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_header_id`   BIGINT UNSIGNED NOT NULL                COMMENT '所属订单头 ID（业务列，非物理 FK）',
    `event_type`        VARCHAR(32)     NOT NULL DEFAULT ''     COMMENT '事件类型',
    `chain_hash`        VARCHAR(128)    NOT NULL DEFAULT ''     COMMENT '上链哈希（mock 模拟值，禁止写入真实链节点 URL/私钥/证书）',
    `block_height`      VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '区块高度（mock 模拟值）',
    `chain_node`        VARCHAR(128)    NOT NULL DEFAULT ''     COMMENT '链节点标识（mock 模拟值）',
    `chain_timestamp`   DATETIME(3)                             COMMENT '链上时间戳（mock 模拟值）',
    `create_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`          TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_t_order_chain_log_order_header_id` (`order_header_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单上链日志（mock 模拟记录，禁止写入真实链节点配置）';

-- ============================================================
-- 5. t_order_attachment（订单附件）
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_order_attachment` (
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_header_id`   BIGINT UNSIGNED NOT NULL                COMMENT '所属订单头 ID（业务列，非物理 FK）',
    `file_name`         VARCHAR(256)    NOT NULL DEFAULT ''     COMMENT '文件名',
    `file_type`         VARCHAR(32)     NOT NULL DEFAULT ''     COMMENT '文件类型（Word/PDF 白名单）',
    `file_size`         BIGINT          NOT NULL DEFAULT 0      COMMENT '文件大小（字节，上限 20MB）',
    `storage_key`       VARCHAR(512)    NOT NULL DEFAULT ''     COMMENT '存储键（对象存储路径）',
    `scan_result`       VARCHAR(32)     NOT NULL DEFAULT ''     COMMENT '扫描结果（PASSED / FAILED / PENDING）',
    `create_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`          TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_t_order_attachment_order_header_id` (`order_header_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单附件（签署附件为主，Word/PDF ≤20MB）';

-- ============================================================
-- 6. t_order_contract_version（数字合约版本）
-- ============================================================
CREATE TABLE IF NOT EXISTS `t_order_contract_version` (
    `id`                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_header_id`   BIGINT UNSIGNED NOT NULL                COMMENT '所属订单头 ID（业务列，非物理 FK）',
    `version_no`        INT             NOT NULL DEFAULT 0      COMMENT '版本号（从 1 递增）',
    `snapshot_json`     JSON                                    COMMENT '合约快照 JSON（主数据 + 变更历史）',
    `create_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`       DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`         VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`          TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    KEY `idx_t_order_contract_version_order_header_id` (`order_header_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数字合约版本（下单初版 + 状态变更递增）';

-- ============================================================
-- 7. 产品表增量：allow_simple_order（链内简易流程开关）
-- 既有行默认值 1（允许链内订购），便于 Wave A 验收（§1.2 假设）。
-- ============================================================
ALTER TABLE `t_data_product` ADD COLUMN `allow_simple_order` TINYINT NOT NULL DEFAULT 1 COMMENT '允许链内简易流程下单：0=不允许 1=允许（既有行默认允许）';
