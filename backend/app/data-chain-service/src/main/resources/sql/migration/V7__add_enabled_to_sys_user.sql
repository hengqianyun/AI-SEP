-- V7: sys_user 增加 enabled，启停与逻辑删除分离（TASK-WSC-HOTFIX-USER-001 / FIND-WSC-707-R1-001）
--
-- Migration choice（权威）:
--   - 新增 enabled TINYINT NOT NULL DEFAULT 1（true=启用，false=停用）
--   - 既有行全部 enabled=1；既有 del_flag 保持不变（已逻辑删除行继续隔离，不出现在默认列表）
--   - 不将历史 del_flag=1 映射为停用（enabled=0），因产品此前用 del_flag 兼做「停用」语义不清；
--     更稳妥是保留已删数据为已删，新启停走独立字段。

ALTER TABLE `sys_user`
    ADD COLUMN `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '启用：1=启用 0=停用' AFTER `enterprise_name`;
