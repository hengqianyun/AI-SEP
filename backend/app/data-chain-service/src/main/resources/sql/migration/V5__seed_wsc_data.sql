-- V5: t_data_product 增列 + 全量种子数据
-- 1) t_data_product 补齐 l1/l2 category code + category_path
-- 2) 种子数据从内存 store 迁移（14 分类 + 27 产品 + 3 链版本 + 3 快照 + 6 流事件）

-- ============================================================
-- 1. t_data_product 增列（H2/MySQL 兼容，无 IF NOT EXISTS / AFTER）
-- ============================================================
ALTER TABLE `t_data_product` ADD COLUMN `l1_category_code` VARCHAR(64)  NULL COMMENT '一级分类 code';
ALTER TABLE `t_data_product` ADD COLUMN `l2_category_code` VARCHAR(64)  NULL COMMENT '二级分类 code';
ALTER TABLE `t_data_product` ADD COLUMN `category_path`     VARCHAR(512) NULL COMMENT '分类路径文本';

-- ============================================================
-- 2. 行业分类种子（11 条：2 L1 + 4 L2 + 5 L3）
-- ============================================================
INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l1-health', '医疗卫生', 'L1', NULL, '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l1-health');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l1-finance', '金融服务', 'L1', NULL, '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l1-finance');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l2-emr', '电子病历', 'L2', 'cat-l1-health', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l2-emr');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l2-imaging', '医学影像', 'L2', 'cat-l1-health', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l2-imaging');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l2-credit', '征信评估', 'L2', 'cat-l1-finance', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l2-credit');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l2-txn', '交易流水', 'L2', 'cat-l1-finance', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l2-txn');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l3-emr-desense', '脱敏病历', 'L3', 'cat-l2-emr', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l3-emr-desense');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l3-emr-struct', '结构化病历', 'L3', 'cat-l2-emr', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l3-emr-struct');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l3-img-ct', 'CT 影像', 'L3', 'cat-l2-imaging', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l3-img-ct');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l3-credit-score', '征信评分', 'L3', 'cat-l2-credit', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l3-credit-score');

INSERT INTO `t_industry_category` (`code`, `name`, `level`, `parent_code`, `create_time`, `update_time`)
SELECT 'cat-l3-txn-retail', '零售支付', 'L3', 'cat-l2-txn', '2026-06-01 08:00:00.000', '2026-06-01 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_industry_category` WHERE `code` = 'cat-l3-txn-retail');

-- ============================================================
-- 3. 数据产品种子（6 样例 + 3 待维护 + 18 分页 = 27 条）
-- ============================================================
-- 样例产品 1: 电子病历脱敏数据集
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `industry_category_code`,
    `l1_category_code`, `l2_category_code`, `category_path`, `maintenance_status`,
    `business_category`, `business_sub_category`, `data_source`, `update_frequency`, `delivery_method`,
    `involves_personal_info`, `involves_public_data`, `billing_method`, `price`,
    `supplier_name`, `supplier_credit_code`, `property_rights_type`,
    `tags_json`, `summary`, `scenario`, `type_dataset_json`, `status`,
    `create_time`, `update_time`)
SELECT 'MED-EMR-0001', '电子病历脱敏数据集', 'DATASET', 'cat-l3-emr-desense',
    'cat-l1-health', 'cat-l2-emr', '医疗卫生 / 电子病历 / 脱敏病历', 'MAINTAINED',
    '卫生', '病历', 'SELF_PRODUCED', 'DAY', 'FILE',
    1, 1, '按次', '面议',
    '华康数据科技', '91310000MA1KXXXX1A', '数据使用权',
    '["病历","脱敏"]', '覆盖多院区结构化病历字段，供科研脱敏分析。', '临床科研、疾病谱分析', '{"dataset":{"recordCount":120000}}', 'LISTED',
    '2026-06-01 09:00:00.000', '2026-06-01 09:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'MED-EMR-0001');

-- 样例产品 2: 结构化门诊病历样本
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `industry_category_code`,
    `l1_category_code`, `l2_category_code`, `category_path`, `maintenance_status`,
    `business_category`, `business_sub_category`, `data_source`, `update_frequency`, `delivery_method`,
    `involves_personal_info`, `involves_public_data`, `billing_method`, `price`,
    `supplier_name`, `supplier_credit_code`, `property_rights_type`,
    `tags_json`, `summary`, `scenario`, `type_dataset_json`, `status`,
    `create_time`, `update_time`)
SELECT 'MED-EMR-0006', '结构化门诊病历样本', 'DATASET', 'cat-l3-emr-struct',
    'cat-l1-health', 'cat-l2-emr', '医疗卫生 / 电子病历 / 结构化病历', 'MAINTAINED',
    '卫生', '病历', 'SELF_PRODUCED', 'WEEK', 'FILE',
    0, 1, '按次', '面议',
    '华康数据科技', '91310000MA1KXXXX1A', '数据使用权',
    '["门诊"]', '门诊结构化病历字段样本。', '门诊质控', '{"dataset":{"recordCount":8000}}', 'LISTED',
    '2026-06-01 10:00:00.000', '2026-06-01 10:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'MED-EMR-0006');

-- 样例产品 3: 胸部 CT 影像报告
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `industry_category_code`,
    `l1_category_code`, `l2_category_code`, `category_path`, `maintenance_status`,
    `business_category`, `business_sub_category`, `data_source`, `update_frequency`, `delivery_method`,
    `involves_personal_info`, `involves_public_data`, `billing_method`, `price`,
    `supplier_name`, `supplier_credit_code`, `property_rights_type`,
    `tags_json`, `summary`, `scenario`, `type_report_json`, `status`,
    `create_time`, `update_time`)
SELECT 'MED-IMG-0002', '胸部 CT 影像报告', 'REPORT', 'cat-l3-img-ct',
    'cat-l1-health', 'cat-l2-imaging', '医疗卫生 / 医学影像 / CT 影像', 'MAINTAINED',
    '卫生', '影像', 'AGREEMENT', 'WEEK', 'API',
    0, 1, '包年', '面议',
    '影像云服务', '91310000MA1KXXXX2B', '数据使用权',
    '["影像","报告"]', '标准化胸部 CT 影像诊断报告摘要。', '辅助诊断质控', '{"report":{"pageCount":12}}', 'LISTED',
    '2026-06-01 11:00:00.000', '2026-06-01 11:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'MED-IMG-0002');

-- 样例产品 4: 企业征信评分接口
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `industry_category_code`,
    `l1_category_code`, `l2_category_code`, `category_path`, `maintenance_status`,
    `business_category`, `business_sub_category`, `data_source`, `update_frequency`, `delivery_method`,
    `involves_personal_info`, `involves_public_data`, `billing_method`, `price`,
    `supplier_name`, `supplier_credit_code`, `property_rights_type`,
    `tags_json`, `summary`, `scenario`, `type_api_json`, `status`,
    `create_time`, `update_time`)
SELECT 'FIN-CRD-0003', '企业征信评分接口', 'API', 'cat-l3-credit-score',
    'cat-l1-finance', 'cat-l2-credit', '金融服务 / 征信评估 / 征信评分', 'MAINTAINED',
    '金融', '征信', 'DERIVED', 'REALTIME', 'API',
    0, 0, '按调用', '面议',
    '信达征信', '91310000MA1KXXXX3C', '数据使用权',
    '["征信","评分"]', '按统一社会信用代码返回企业征信评分。', '贷前风控', '{"api":{"endpoint":"/v1/credit/score"}}', 'LISTED',
    '2026-06-01 12:00:00.000', '2026-06-01 12:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'FIN-CRD-0003');

-- 样例产品 5: 零售支付流水样本
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `industry_category_code`,
    `l1_category_code`, `l2_category_code`, `category_path`, `maintenance_status`,
    `business_category`, `business_sub_category`, `data_source`, `update_frequency`, `delivery_method`,
    `involves_personal_info`, `involves_public_data`, `billing_method`, `price`,
    `supplier_name`, `supplier_credit_code`, `property_rights_type`,
    `tags_json`, `summary`, `scenario`, `type_dataset_json`, `status`,
    `create_time`, `update_time`)
SELECT 'FIN-TXN-0004', '零售支付流水样本', 'DATASET', 'cat-l3-txn-retail',
    'cat-l1-finance', 'cat-l2-txn', '金融服务 / 交易流水 / 零售支付', 'MAINTAINED',
    '金融', '支付', 'PUBLIC_COLLECT', 'DAY', 'SANDBOX',
    1, 0, '免费试用', '0',
    '汇通支付', '91310000MA1KXXXX4D', '数据使用权',
    '["支付","流水"]', '脱敏零售支付流水样本，支持沙箱联调。', '支付行为分析', '{"dataset":{"recordCount":50000}}', 'LISTED',
    '2026-06-01 13:00:00.000', '2026-06-01 13:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'FIN-TXN-0004');

-- 样例产品 6: 行业通用其他数据产品
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `industry_category_code`,
    `l1_category_code`, `l2_category_code`, `category_path`, `maintenance_status`,
    `business_category`, `business_sub_category`, `data_source`, `update_frequency`, `delivery_method`,
    `involves_personal_info`, `involves_public_data`, `billing_method`, `price`,
    `supplier_name`, `supplier_credit_code`, `property_rights_type`,
    `tags_json`, `summary`, `scenario`, `status`,
    `create_time`, `update_time`)
SELECT 'GEN-MISC-0005', '行业通用其他数据产品', 'OTHER', 'cat-l3-emr-desense',
    'cat-l1-health', 'cat-l2-emr', '医疗卫生 / 电子病历 / 脱敏病历', 'MAINTAINED',
    '通用', '其他', 'SELF_PRODUCED', 'MONTH', 'PRIVACY_COMPUTE',
    0, 0, '面议', '面议',
    '示例供应商', '91310000MA1KXXXX5E', '数据使用权',
    '["其他"]', '其他类型示例（无类型专属字段，OQ-004）。', '通用对接演示', 'LISTED',
    '2026-06-01 14:00:00.000', '2026-06-01 14:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'GEN-MISC-0005');

-- 待维护产品（PENDING，共 3 条）
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `maintenance_status`, `data_source`, `delivery_method`, `summary`, `scenario`, `supplier_name`, `supplier_credit_code`, `tags_json`, `property_rights_type`, `involves_personal_info`, `involves_public_data`, `billing_method`, `price`, `status`, `create_time`, `update_time`) SELECT 'PEND-0001', '政务授权核验数据目录', 'OTHER', 'PENDING', 'SELF_PRODUCED', 'FILE', '待关联目录条目（演示）', '目录维护', '待关联供应商', '00000000000000000X', '["待关联"]', '数据使用权', 0, 0, '面议', '面议', 'LISTED', NOW(3), NOW(3) WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'PEND-0001');
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `maintenance_status`, `data_source`, `delivery_method`, `summary`, `scenario`, `supplier_name`, `supplier_credit_code`, `tags_json`, `property_rights_type`, `involves_personal_info`, `involves_public_data`, `billing_method`, `price`, `status`, `create_time`, `update_time`) SELECT 'PEND-0002', '供应链溯源数据目录', 'OTHER', 'PENDING', 'SELF_PRODUCED', 'FILE', '待关联目录条目（演示）', '目录维护', '待关联供应商', '00000000000000000X', '["待关联"]', '数据使用权', 0, 0, '面议', '面议', 'LISTED', NOW(3), NOW(3) WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'PEND-0002');
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `maintenance_status`, `data_source`, `delivery_method`, `summary`, `scenario`, `supplier_name`, `supplier_credit_code`, `tags_json`, `property_rights_type`, `involves_personal_info`, `involves_public_data`, `billing_method`, `price`, `status`, `create_time`, `update_time`) SELECT 'PEND-0003', '跨境物流追踪数据目录', 'OTHER', 'PENDING', 'SELF_PRODUCED', 'FILE', '待关联目录条目（演示）', '目录维护', '待关联供应商', '00000000000000000X', '["待关联"]', '数据使用权', 0, 0, '面议', '面议', 'LISTED', NOW(3), NOW(3) WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'PEND-0003');

-- 分页演示产品（18 条：MED-PG-0001 ~ MED-PG-0018）
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`, `industry_category_code`,
    `l1_category_code`, `l2_category_code`, `category_path`, `maintenance_status`,
    `business_category`, `business_sub_category`, `data_source`, `update_frequency`, `delivery_method`,
    `involves_personal_info`, `involves_public_data`, `billing_method`, `price`,
    `supplier_name`, `supplier_credit_code`, `property_rights_type`,
    `tags_json`, `summary`, `scenario`, `type_dataset_json`, `status`,
    `create_time`, `update_time`)
SELECT * FROM (
    SELECT 'MED-PG-0001' AS pc, '脱敏病历分页样例 01' AS pn, 'DATASET' AS pt, 'cat-l3-emr-desense' AS icc, 'cat-l1-health' AS l1, 'cat-l2-emr' AS l2, '医疗卫生 / 电子病历 / 脱敏病历' AS cp, 'MAINTAINED' AS ms, '卫生' AS bc, '病历' AS bsc, 'SELF_PRODUCED' AS ds, 'DAY' AS uf, 'FILE' AS dm, 1, 0, '按次', '面议', '华康数据科技' AS sn, '91310000MA1KXXXX1A' AS scc, '数据使用权' AS prt, '["分页"]' AS tg, '分页加载演示产品 01' AS s, '目录滚动加载' AS sc, '{"dataset":{"recordCount":100}}' AS tdj, 'LISTED' AS st, '2026-07-26 11:00:00.000' AS ct, '2026-07-26 11:00:00.000' AS ut
    UNION ALL SELECT 'MED-PG-0002','脱敏病历分页样例 02','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 02','目录滚动加载','{"dataset":{"recordCount":200}}','LISTED','2026-06-01 15:00:00.000','2026-06-01 15:00:00.000'
    UNION ALL SELECT 'MED-PG-0003','脱敏病历分页样例 03','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 03','目录滚动加载','{"dataset":{"recordCount":300}}','LISTED','2026-06-01 16:00:00.000','2026-06-01 16:00:00.000'
    UNION ALL SELECT 'MED-PG-0004','脱敏病历分页样例 04','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 04','目录滚动加载','{"dataset":{"recordCount":400}}','LISTED','2026-06-01 17:00:00.000','2026-06-01 17:00:00.000'
    UNION ALL SELECT 'MED-PG-0005','脱敏病历分页样例 05','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 05','目录滚动加载','{"dataset":{"recordCount":500}}','LISTED','2026-06-01 18:00:00.000','2026-06-01 18:00:00.000'
    UNION ALL SELECT 'MED-PG-0006','脱敏病历分页样例 06','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 06','目录滚动加载','{"dataset":{"recordCount":600}}','LISTED','2026-06-01 19:00:00.000','2026-06-01 19:00:00.000'
    UNION ALL SELECT 'MED-PG-0007','脱敏病历分页样例 07','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 07','目录滚动加载','{"dataset":{"recordCount":700}}','LISTED','2026-06-01 20:00:00.000','2026-06-01 20:00:00.000'
    UNION ALL SELECT 'MED-PG-0008','脱敏病历分页样例 08','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 08','目录滚动加载','{"dataset":{"recordCount":800}}','LISTED','2026-06-01 21:00:00.000','2026-06-01 21:00:00.000'
    UNION ALL SELECT 'MED-PG-0009','脱敏病历分页样例 09','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 09','目录滚动加载','{"dataset":{"recordCount":900}}','LISTED','2026-06-01 22:00:00.000','2026-06-01 22:00:00.000'
    UNION ALL SELECT 'MED-PG-0010','脱敏病历分页样例 10','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 10','目录滚动加载','{"dataset":{"recordCount":1000}}','LISTED','2026-06-01 23:00:00.000','2026-06-01 23:00:00.000'
    UNION ALL SELECT 'MED-PG-0011','脱敏病历分页样例 11','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 11','目录滚动加载','{"dataset":{"recordCount":1100}}','LISTED','2026-06-02 00:00:00.000','2026-06-02 00:00:00.000'
    UNION ALL SELECT 'MED-PG-0012','脱敏病历分页样例 12','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 12','目录滚动加载','{"dataset":{"recordCount":1200}}','LISTED','2026-06-02 01:00:00.000','2026-06-02 01:00:00.000'
    UNION ALL SELECT 'MED-PG-0013','脱敏病历分页样例 13','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 13','目录滚动加载','{"dataset":{"recordCount":1300}}','LISTED','2026-06-02 02:00:00.000','2026-06-02 02:00:00.000'
    UNION ALL SELECT 'MED-PG-0014','脱敏病历分页样例 14','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 14','目录滚动加载','{"dataset":{"recordCount":1400}}','LISTED','2026-06-02 03:00:00.000','2026-06-02 03:00:00.000'
    UNION ALL SELECT 'MED-PG-0015','脱敏病历分页样例 15','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 15','目录滚动加载','{"dataset":{"recordCount":1500}}','LISTED','2026-06-02 04:00:00.000','2026-06-02 04:00:00.000'
    UNION ALL SELECT 'MED-PG-0016','脱敏病历分页样例 16','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 16','目录滚动加载','{"dataset":{"recordCount":1600}}','LISTED','2026-06-02 05:00:00.000','2026-06-02 05:00:00.000'
    UNION ALL SELECT 'MED-PG-0017','脱敏病历分页样例 17','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 17','目录滚动加载','{"dataset":{"recordCount":1700}}','LISTED','2026-06-02 06:00:00.000','2026-06-02 06:00:00.000'
    UNION ALL SELECT 'MED-PG-0018','脱敏病历分页样例 18','DATASET','cat-l3-emr-desense','cat-l1-health','cat-l2-emr','医疗卫生 / 电子病历 / 脱敏病历','MAINTAINED','卫生','病历','SELF_PRODUCED','DAY','FILE',1,0,'按次','面议','华康数据科技','91310000MA1KXXXX1A','数据使用权','["分页"]','分页加载演示产品 18','目录滚动加载','{"dataset":{"recordCount":1800}}','LISTED','2026-06-02 07:00:00.000','2026-06-02 07:00:00.000'
) t
WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = t.pc);

-- ============================================================
-- 4. 上链版本 + 快照种子（3 条，产品 prod-demo-chain-001 用 WSC-DEMO-001）
-- ============================================================
-- 先确保 seed 产品存在
INSERT INTO `t_data_product` (`product_code`, `product_name`, `product_type`,
    `industry_category_code`, `l1_category_code`, `l2_category_code`, `category_path`, `maintenance_status`,
    `business_category`, `business_sub_category`, `data_source`, `update_frequency`, `delivery_method`,
    `involves_personal_info`, `involves_public_data`, `billing_method`, `price`,
    `supplier_name`, `supplier_credit_code`, `property_rights_type`,
    `tags_json`, `summary`, `scenario`, `type_dataset_json`, `status`,
    `create_time`, `update_time`)
SELECT 'WSC-DEMO-001', '演示数据产品（上链）', 'DATASET', 'cat-l3-credit-score',
    'cat-l1-finance', 'cat-l2-credit', '金融服务 / 征信评估 / 征信评分', 'MAINTAINED',
    '金融', '征信', '内部系统', '日更', 'API',
    0, 1, '面议', '面议',
    '演示供应商', '91310000MA1KXXXX0X', '数据使用权',
    '["演示","上链"]', 'TASK-WSC-006 内存种子演示产品', '接入端工作台试点验收', '{"dataset":{"recordCount":1000}}', 'LISTED',
    '2026-07-20 08:00:00.000', '2026-07-20 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_data_product` WHERE `product_code` = 'WSC-DEMO-001');

-- 上链版本 v1
INSERT INTO `t_chain_version` (`version_code`, `product_code`, `version_no`, `metadata_hash`, `owner_did`, `cert_owner`, `ts`, `create_time`, `update_time`)
SELECT 'cv-seed-001-v1', 'WSC-DEMO-001', 1, 'sha256:1111111111111111111111111111111111111111111111111111111111111111', 'did:wsc:sim:seed000000000001', '模拟权属方-WSC-DEMO-001', '2026-07-20 08:00:00.000', '2026-07-20 08:00:00.000', '2026-07-20 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_chain_version` WHERE `version_code` = 'cv-seed-001-v1');

-- 上链版本 v2
INSERT INTO `t_chain_version` (`version_code`, `product_code`, `version_no`, `metadata_hash`, `owner_did`, `cert_owner`, `ts`, `create_time`, `update_time`)
SELECT 'cv-seed-001-v2', 'WSC-DEMO-001', 2, 'sha256:2222222222222222222222222222222222222222222222222222222222222222', 'did:wsc:sim:seed000000000001', '模拟权属方-WSC-DEMO-001', '2026-07-22 10:30:00.000', '2026-07-22 10:30:00.000', '2026-07-22 10:30:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_chain_version` WHERE `version_code` = 'cv-seed-001-v2');

-- 上链版本 v3
INSERT INTO `t_chain_version` (`version_code`, `product_code`, `version_no`, `metadata_hash`, `owner_did`, `cert_owner`, `ts`, `create_time`, `update_time`)
SELECT 'cv-seed-001-v3', 'WSC-DEMO-001', 3, 'sha256:3333333333333333333333333333333333333333333333333333333333333333', 'did:wsc:sim:seed000000000001', '模拟权属方-WSC-DEMO-001', '2026-07-25 14:15:00.000', '2026-07-25 14:15:00.000', '2026-07-25 14:15:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_chain_version` WHERE `version_code` = 'cv-seed-001-v3');

-- 快照 v1
INSERT INTO `t_chain_catalog_snapshot` (`version_code`, `snapshot_json`, `create_time`, `update_time`)
SELECT 'cv-seed-001-v1', '{"versionId":"cv-seed-001-v1","productCode":"WSC-DEMO-001","productName":"演示数据产品（上链）","productType":"DATASET","categoryPath":"金融服务 / 征信评估 / 征信评分","categoryPathParts":{"l1":"金融服务","l2":"征信评估","l3":"征信评分"},"basicInfo":{"dataSource":"内部系统","updateFrequency":"日更","deliveryMethod":"API"},"supplierInfo":{"supplierName":"演示供应商","supplierCreditCode":"91310000MA1KXXXX0X"},"propertyRights":{"propertyRightsType":"数据使用权"},"typeSpecific":{"datasetFormat":"CSV","recordCount":1001},"tags":["演示","上链","v1"],"summary":"TASK-WSC-006 内存种子快照 v1","scenario":"接入端工作台试点验收","capturedAt":"2026-07-20T08:00:00Z","attestation":{"metadataHash":"sha256:1111111111111111111111111111111111111111111111111111111111111111","ownerDID":"did:wsc:sim:seed000000000001","timestamp":"2026-07-20T08:00:00Z","certificate":{"owner":"模拟权属方-WSC-DEMO-001"}}}', '2026-07-20 08:00:00.000', '2026-07-20 08:00:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_chain_catalog_snapshot` WHERE `version_code` = 'cv-seed-001-v1');

-- 快照 v2
INSERT INTO `t_chain_catalog_snapshot` (`version_code`, `snapshot_json`, `create_time`, `update_time`)
SELECT 'cv-seed-001-v2', '{"versionId":"cv-seed-001-v2","productCode":"WSC-DEMO-001","productName":"演示数据产品（上链）","productType":"DATASET","categoryPath":"金融服务 / 征信评估 / 征信评分","categoryPathParts":{"l1":"金融服务","l2":"征信评估","l3":"征信评分"},"basicInfo":{"dataSource":"内部系统","updateFrequency":"日更","deliveryMethod":"API"},"supplierInfo":{"supplierName":"演示供应商","supplierCreditCode":"91310000MA1KXXXX0X"},"propertyRights":{"propertyRightsType":"数据使用权"},"typeSpecific":{"datasetFormat":"CSV","recordCount":1002},"tags":["演示","上链","v2"],"summary":"TASK-WSC-006 内存种子快照 v2","scenario":"接入端工作台试点验收","capturedAt":"2026-07-22T10:30:00Z","attestation":{"metadataHash":"sha256:2222222222222222222222222222222222222222222222222222222222222222","ownerDID":"did:wsc:sim:seed000000000001","timestamp":"2026-07-22T10:30:00Z","certificate":{"owner":"模拟权属方-WSC-DEMO-001"}}}', '2026-07-22 10:30:00.000', '2026-07-22 10:30:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_chain_catalog_snapshot` WHERE `version_code` = 'cv-seed-001-v2');

-- 快照 v3
INSERT INTO `t_chain_catalog_snapshot` (`version_code`, `snapshot_json`, `create_time`, `update_time`)
SELECT 'cv-seed-001-v3', '{"versionId":"cv-seed-001-v3","productCode":"WSC-DEMO-001","productName":"演示数据产品（上链）","productType":"DATASET","categoryPath":"金融服务 / 征信评估 / 征信评分","categoryPathParts":{"l1":"金融服务","l2":"征信评估","l3":"征信评分"},"basicInfo":{"dataSource":"内部系统","updateFrequency":"日更","deliveryMethod":"API"},"supplierInfo":{"supplierName":"演示供应商","supplierCreditCode":"91310000MA1KXXXX0X"},"propertyRights":{"propertyRightsType":"数据使用权"},"typeSpecific":{"datasetFormat":"CSV","recordCount":1003},"tags":["演示","上链","v3"],"summary":"TASK-WSC-006 内存种子快照 v3","scenario":"接入端工作台试点验收","capturedAt":"2026-07-25T14:15:00Z","attestation":{"metadataHash":"sha256:3333333333333333333333333333333333333333333333333333333333333333","ownerDID":"did:wsc:sim:seed000000000001","timestamp":"2026-07-25T14:15:00Z","certificate":{"owner":"模拟权属方-WSC-DEMO-001"}}}', '2026-07-25 14:15:00.000', '2026-07-25 14:15:00.000'
WHERE NOT EXISTS (SELECT 1 FROM `t_chain_catalog_snapshot` WHERE `version_code` = 'cv-seed-001-v3');

-- ============================================================
-- 5. 总览流事件种子（6 条）
-- ============================================================
INSERT INTO `t_overview_stream_event` (`event_type`, `subject`, `action_summary`, `occurred_at`, `chain_record_id`, `create_time`, `update_time`)
SELECT * FROM (
    SELECT 'TRADE_ORDER' AS et, '订单 ORD-20260729-001' AS sub, '交易订单上链存证' AS act, '2026-07-29 05:45:00.000' AS oa, '0xstream-trade-001' AS cri, '2026-07-29 05:45:00.000' AS ct, '2026-07-29 05:45:00.000' AS ut
    UNION ALL SELECT 'DATA_REGISTER', '企业「华南数据」', '数据登记上链（演示文案）', '2026-07-29 04:20:00.000', '0xstream-data-001', '2026-07-29 04:20:00.000', '2026-07-29 04:20:00.000'
    UNION ALL SELECT 'CATALOG_REGISTER', '产品「企业信用数据集」', '目录登记上链', '2026-07-29 02:10:00.000', '0xstream-catalog-001', '2026-07-29 02:10:00.000', '2026-07-29 02:10:00.000'
    UNION ALL SELECT 'CATALOG_REGISTER', '产品「供应链发票报告」', '目录登记上链', '2026-07-28 18:00:00.000', '0xstream-catalog-002', '2026-07-28 18:00:00.000', '2026-07-28 18:00:00.000'
    UNION ALL SELECT 'TRADE_ORDER', '订单 ORD-20260728-009', '交易订单上链存证', '2026-07-28 12:30:00.000', '0xstream-trade-002', '2026-07-28 12:30:00.000', '2026-07-28 12:30:00.000'
    UNION ALL SELECT 'DATA_REGISTER', '企业「华北智造」', '数据登记上链（演示文案）', '2026-07-28 09:00:00.000', '0xstream-data-002', '2026-07-28 09:00:00.000', '2026-07-28 09:00:00.000'
) t
WHERE NOT EXISTS (SELECT 1 FROM `t_overview_stream_event` WHERE `chain_record_id` = t.cri);
