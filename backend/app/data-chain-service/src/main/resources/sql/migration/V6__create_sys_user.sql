-- V6: 持久化用户表 sys_user（SNAP-WSC-005 / REQ-USER-001）
-- 可重复：CREATE TABLE IF NOT EXISTS；username 唯一；role；password_hash；软删 del_flag；审计字段。
-- 种子账号由 UserAccountService 启动引导写入（BCrypt 单向哈希）；演示口令见代码常量，不在本脚本落明文生产密钥。

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `username`         VARCHAR(64)     NOT NULL                COMMENT '登录名（唯一）',
    `password_hash`    VARCHAR(100)    NOT NULL                COMMENT 'BCrypt 密码哈希',
    `display_name`     VARCHAR(128)    NOT NULL DEFAULT ''     COMMENT '显示名',
    `role`             VARCHAR(32)     NOT NULL                COMMENT '角色：ADMIN/PROVIDER/USER',
    `enterprise_name`  VARCHAR(256)    NOT NULL DEFAULT ''     COMMENT '企业名',
    `create_time`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
    `create_by`        VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '创建人',
    `update_time`      DATETIME(3)     NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
    `update_by`        VARCHAR(64)     NOT NULL DEFAULT ''     COMMENT '更新人',
    `del_flag`         TINYINT         NOT NULL DEFAULT 0      COMMENT '逻辑删除：0=未删除 1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`),
    KEY `idx_sys_user_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统用户';
