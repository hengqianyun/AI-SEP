-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: data_chain
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `chain_catalog_snapshot`
--

DROP TABLE IF EXISTS `chain_catalog_snapshot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chain_catalog_snapshot` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `version_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `snapshot_json` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_chain_catalog_snapshot_version` (`version_id`),
  CONSTRAINT `fk_chain_catalog_snapshot_version` FOREIGN KEY (`version_id`) REFERENCES `chain_version` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chain_catalog_snapshot`
--

LOCK TABLES `chain_catalog_snapshot` WRITE;
/*!40000 ALTER TABLE `chain_catalog_snapshot` DISABLE KEYS */;
/*!40000 ALTER TABLE `chain_catalog_snapshot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chain_version`
--

DROP TABLE IF EXISTS `chain_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chain_version` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `version_no` int NOT NULL,
  `metadata_hash` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `owner_did` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `cert_owner` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `ts` timestamp(3) NOT NULL,
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_chain_version_product_no` (`product_id`,`version_no`),
  KEY `idx_chain_version_product` (`product_id`),
  CONSTRAINT `fk_chain_version_product` FOREIGN KEY (`product_id`) REFERENCES `data_product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chain_version`
--

LOCK TABLES `chain_version` WRITE;
/*!40000 ALTER TABLE `chain_version` DISABLE KEYS */;
/*!40000 ALTER TABLE `chain_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `data_product`
--

DROP TABLE IF EXISTS `data_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `data_product` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_code` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_name` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `l3_category_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `maintenance_status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `business_category` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `business_sub_category` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `data_source` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_frequency` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `delivery_method` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `involves_personal_info` tinyint(1) DEFAULT NULL,
  `involves_public_data` tinyint(1) DEFAULT NULL,
  `billing_method` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `supplier_name` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `supplier_credit_code` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `property_rights_type` varchar(128) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags_json` text COLLATE utf8mb4_unicode_ci,
  `summary` text COLLATE utf8mb4_unicode_ci,
  `scenario` text COLLATE utf8mb4_unicode_ci,
  `type_dataset_json` text COLLATE utf8mb4_unicode_ci,
  `type_report_json` text COLLATE utf8mb4_unicode_ci,
  `type_api_json` text COLLATE utf8mb4_unicode_ci,
  `status` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LISTED',
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_data_product_code` (`product_code`),
  KEY `idx_data_product_l3` (`l3_category_id`),
  KEY `idx_data_product_type` (`product_type`),
  KEY `idx_data_product_maint` (`maintenance_status`),
  CONSTRAINT `fk_data_product_l3` FOREIGN KEY (`l3_category_id`) REFERENCES `industry_category` (`id`),
  CONSTRAINT `ck_data_product_maint` CHECK ((`maintenance_status` in (_utf8mb4'PENDING',_utf8mb4'MAINTAINED'))),
  CONSTRAINT `ck_data_product_type` CHECK ((`product_type` in (_utf8mb4'DATASET',_utf8mb4'REPORT',_utf8mb4'API',_utf8mb4'OTHER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `data_product`
--

LOCK TABLES `data_product` WRITE;
/*!40000 ALTER TABLE `data_product` DISABLE KEYS */;
/*!40000 ALTER TABLE `data_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `demo`
--

DROP TABLE IF EXISTS `demo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `demo` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '名称',
  `description` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '描述',
  `created_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '创建时间',
  `updated_at` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_demo_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Demo 示例表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demo`
--

LOCK TABLES `demo` WRITE;
/*!40000 ALTER TABLE `demo` DISABLE KEYS */;
INSERT INTO `demo` VALUES (1,'示例数据','由 Flyway 写入的数据','2026-08-02 12:52:27.091','2026-08-02 12:52:27.091');
/*!40000 ALTER TABLE `demo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flyway_schema_history`
--

DROP TABLE IF EXISTS `flyway_schema_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flyway_schema_history` (
  `installed_rank` int NOT NULL COMMENT 'æ‰§è¡Œåºå·(ä¸»é”®)',
  `version` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'ç‰ˆæœ¬å·,baseline è®°ä¸º 0',
  `description` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æè¿°',
  `type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'ç±»åž‹:SQL / BASELINE / TABLE ç­‰',
  `script` varchar(1000) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'è„šæœ¬åç§°',
  `checksum` int DEFAULT NULL COMMENT 'æ ¡éªŒå’Œ,baseline / å»ºè¡¨æ ‡è®°ä¸º NULL',
  `installed_by` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'æ‰§è¡Œäºº',
  `installed_on` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'æ‰§è¡Œæ—¶é—´',
  `execution_time` int NOT NULL COMMENT 'æ‰§è¡Œè€—æ—¶(ms)',
  `success` tinyint(1) NOT NULL COMMENT 'æ˜¯å¦æ‰§è¡ŒæˆåŠŸ',
  PRIMARY KEY (`installed_rank`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Flyway æ•°æ®åº“ç‰ˆæœ¬è¿ç§»åŽ†å²è¡¨';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flyway_schema_history`
--

LOCK TABLES `flyway_schema_history` WRITE;
/*!40000 ALTER TABLE `flyway_schema_history` DISABLE KEYS */;
INSERT INTO `flyway_schema_history` VALUES (-1,NULL,'<< Flyway Schema History table created >>','TABLE','',NULL,'root','2026-08-02 04:52:23',0,1),(1,'0','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',NULL,'root','2026-08-02 04:52:23',0,1),(2,'1','create demo table','SQL','V1__create_demo_table.sql',1140083655,'root','2026-08-02 04:52:27',70,1),(3,'2','insert demo data','SQL','V2__insert_demo_data.sql',-261372117,'root','2026-08-02 04:52:27',6,1),(4,'202607301600','init wsc v11','SQL','V202607301600__init_wsc_v11.sql',-1955470739,'root','2026-08-02 04:52:27',622,1);
/*!40000 ALTER TABLE `flyway_schema_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `import_error_report`
--

DROP TABLE IF EXISTS `import_error_report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `import_error_report` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `expires_at` timestamp(3) NOT NULL,
  `success_count` int NOT NULL DEFAULT '0',
  `failure_count` int NOT NULL DEFAULT '0',
  `rows_json` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_by` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_import_error_report_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `import_error_report`
--

LOCK TABLES `import_error_report` WRITE;
/*!40000 ALTER TABLE `import_error_report` DISABLE KEYS */;
/*!40000 ALTER TABLE `import_error_report` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `industry_category`
--

DROP TABLE IF EXISTS `industry_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `industry_category` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `name` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `level` varchar(8) COLLATE utf8mb4_unicode_ci NOT NULL,
  `parent_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_industry_category_parent` (`parent_id`),
  KEY `idx_industry_category_level` (`level`),
  CONSTRAINT `fk_industry_category_parent` FOREIGN KEY (`parent_id`) REFERENCES `industry_category` (`id`),
  CONSTRAINT `ck_industry_category_level` CHECK ((`level` in (_utf8mb4'L1',_utf8mb4'L2',_utf8mb4'L3')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `industry_category`
--

LOCK TABLES `industry_category` WRITE;
/*!40000 ALTER TABLE `industry_category` DISABLE KEYS */;
/*!40000 ALTER TABLE `industry_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `overview_stream_event`
--

DROP TABLE IF EXISTS `overview_stream_event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `overview_stream_event` (
  `id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  `event_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL,
  `subject` varchar(512) COLLATE utf8mb4_unicode_ci NOT NULL,
  `action_summary` varchar(1024) COLLATE utf8mb4_unicode_ci NOT NULL,
  `occurred_at` timestamp(3) NOT NULL,
  `chain_record_id` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_overview_stream_occurred` (`occurred_at`),
  CONSTRAINT `ck_overview_stream_event_type` CHECK ((`event_type` in (_utf8mb4'CATALOG_REGISTER',_utf8mb4'DATA_REGISTER',_utf8mb4'TRADE_ORDER')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `overview_stream_event`
--

LOCK TABLES `overview_stream_event` WRITE;
/*!40000 ALTER TABLE `overview_stream_event` DISABLE KEYS */;
/*!40000 ALTER TABLE `overview_stream_event` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'data_chain'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-02 12:52:28
