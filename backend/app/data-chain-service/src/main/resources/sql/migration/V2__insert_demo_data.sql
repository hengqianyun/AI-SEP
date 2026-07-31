INSERT INTO `demo` (`name`, `description`)
SELECT '示例数据', '由 Flyway 写入的数据'
WHERE NOT EXISTS (
    SELECT 1
    FROM `demo`
    WHERE `name` = '示例数据'
);

