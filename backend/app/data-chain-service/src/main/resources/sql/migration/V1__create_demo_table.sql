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

