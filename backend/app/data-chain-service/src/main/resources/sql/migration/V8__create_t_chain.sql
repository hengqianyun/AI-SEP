-- V8: 创建溯源链注册表 t_chain（一个数据产品对应一条溯源链）
-- 注：版本号用 V8 避让共享库中已存在的 V7（add enabled to sys user，其脚本不在本仓库源码内）。
-- 溯源链模型下，产品首版上链时向平台建链得到 chain_id，并在此表登记 product_code↔chain_id 映射；
-- 后续编辑复用同一 chain_id 向链上追加节点。
-- 并发兜底：uk_t_chain_product 保证同一产品全局只登记一条链（registerChainId 走 insert-if-absent）。
-- 审计四件套 + del_flag 齐全（对齐 V1 的 t_chain_version）；幂等可重入（CREATE TABLE IF NOT EXISTS）。
CREATE TABLE IF NOT EXISTS `t_chain` (
    `id`           BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `chain_id`     VARCHAR(128)    NOT NULL                COMMENT '溯源链ID（平台返回）',
    `product_code` VARCHAR(128)    NOT NULL                COMMENT '产品编码（关联 t_data_product.product_code，无外键）',
    `status`       VARCHAR(32)     NOT NULL DEFAULT 'active' COMMENT '链登记状态：active',
    `create_time`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`    VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`  DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`    VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`     TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_t_chain_product` (`product_code`),
    UNIQUE KEY `uk_t_chain_id` (`chain_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='溯源链注册表（产品↔溯源链映射）';
