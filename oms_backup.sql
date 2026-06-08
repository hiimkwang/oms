-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: oms
-- ------------------------------------------------------
-- Server version	8.0.46-0ubuntu0.22.04.2

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
-- Table structure for table `branches`
--

DROP TABLE IF EXISTS `branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `branches` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `branches`
--

LOCK TABLES `branches` WRITE;
/*!40000 ALTER TABLE `branches` DISABLE KEYS */;
INSERT INTO `branches` VALUES (1,_binary '','số 5, ngõ 55 Nguyễn Ngọc Nại, Phương Liệt, Hà Nội','Cửa hàng chính','0971130397');
/*!40000 ALTER TABLE `branches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `brands`
--

DROP TABLE IF EXISTS `brands`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brands` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `logo_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKoce3937d2f4mpfqrycbr0l93m` (`name`),
  UNIQUE KEY `UKjwckdguv8xkq16jq8tq5k3pn9` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brands`
--

LOCK TABLES `brands` WRITE;
/*!40000 ALTER TABLE `brands` DISABLE KEYS */;
/*!40000 ALTER TABLE `brands` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cash_transactions`
--

DROP TABLE IF EXISTS `cash_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cash_transactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(38,2) DEFAULT NULL,
  `attachments` text COLLATE utf8mb4_unicode_ci,
  `branch_id` bigint DEFAULT NULL,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `creator_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_method` enum('BANK','CASH') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reference_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_group` enum('CUSTOMER','EMPLOYEE','OTHER','SUPPLIER') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `target_id` bigint DEFAULT NULL,
  `target_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `transaction_date` datetime(6) DEFAULT NULL,
  `type` enum('PAYMENT','RECEIPT') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlxv0ri7v7yjpurqakis6ch781` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cash_transactions`
--

LOCK TABLES `cash_transactions` WRITE;
/*!40000 ALTER TABLE `cash_transactions` DISABLE KEYS */;
INSERT INTO `cash_transactions` VALUES (1,150000.00,NULL,1,'PT1775145819045','2026-04-02 23:03:39.066457','admin','Tiền ăn chênh lệch đơn ảo của Sơn','CASH','Thu nhập khác','','OTHER',NULL,'Khách vãng lai','2026-03-23 23:03:39.054983','RECEIPT'),(2,70000.00,NULL,1,'PT1775145956998','2026-04-02 23:05:57.000366','admin','Thu tiền Mạnh mập đặt đơn ảo ăn chênh lệch','BANK','Thu nhập khác','','CUSTOMER',NULL,'Khách vãng lai','2026-03-22 23:05:56.998594','RECEIPT'),(3,250000.00,NULL,1,'PT1775146015044','2026-04-02 23:06:55.046113','admin','Thu tiền An đặt đơn ảo ăn chênh lệch','CASH','Thu nhập khác','','CUSTOMER',NULL,'Khách vãng lai','2026-03-25 23:06:55.045056','RECEIPT'),(4,450000.00,NULL,1,'PC1775146105971','2026-04-02 23:08:25.972911','admin','Đăng ký mới Tên miền mechkey.vn','CASH','Chi khác','','SUPPLIER',NULL,'Khách vãng lai','2025-03-15 23:08:25.971578','PAYMENT'),(5,774300.00,NULL,1,'PC1775146186883','2026-04-02 23:09:46.884588','admin','Đăng ký mới Hosting xookayik.cloudhost-wphn112403.000web.xyz','CASH','Trả nợ nhà cung cấp','','SUPPLIER',NULL,'Khách vãng lai','2025-03-24 23:09:46.883517','PAYMENT'),(7,178993.00,NULL,1,'PC1775146247990','2026-04-02 23:10:47.992608','admin','Tùy biến gói Cloud Server CS-Linux-20250822170415288','CASH','Trả nợ nhà cung cấp','','SUPPLIER',NULL,'Khách vãng lai','2025-08-26 23:10:47.991085','PAYMENT'),(8,194423.00,NULL,1,'PC1775146297638','2026-04-02 23:11:37.639480','admin','Đăng ký mới Cloud Server Ubuntu-22.04-LTS','CASH','Trả nợ nhà cung cấp','','SUPPLIER',NULL,'Khách vãng lai','2025-08-22 23:11:37.638286','PAYMENT'),(9,458000.00,NULL,1,'PC1775146327690','2026-04-02 23:12:07.691662','admin','Duy trì Tên miền mechkey.vn','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-02-23 23:12:07.690908','PAYMENT'),(10,869358.00,NULL,1,'PC1775146351896','2026-04-02 23:12:31.897420','admin','Duy trì Cloud Server cs-linux-20250822170415288','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-02-27 23:12:31.896999','PAYMENT'),(11,150000.00,NULL,1,'PC1775146380746','2026-04-02 23:13:00.747877','admin','Theme FlatSome','CASH','Trả nợ nhà cung cấp','','SUPPLIER',NULL,'Khách vãng lai','2025-03-15 23:13:00.746710','PAYMENT'),(12,150000.00,NULL,1,'PC1775146398940','2026-04-02 23:13:18.941594','admin','Plugin WP-Rocket','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-01 23:13:18.940707','PAYMENT'),(13,150000.00,NULL,1,'PC1775146419813','2026-04-02 23:13:39.814521','admin','Plugin Yoast-SEO','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-01 23:13:39.813412','PAYMENT'),(14,1119000.00,NULL,1,'PC1775146441212','2026-04-02 23:14:01.212852','admin','Mua tài khoản Capcut 1 năm','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-03 23:14:01.212139','PAYMENT'),(15,419000.00,NULL,1,'PC1775146464935','2026-04-02 23:14:24.936695','admin','Vbee','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-01-06 23:14:24.935955','PAYMENT'),(16,250000.00,NULL,1,'PC1775146493797','2026-04-02 23:14:53.798167','admin','Mua nick Tiktok ','CASH','Trả nợ nhà cung cấp','','SUPPLIER',NULL,'Khách vãng lai','2025-03-05 23:14:53.797357','PAYMENT'),(17,600000.00,NULL,1,'PC1775146509520','2026-04-02 23:15:09.521054','admin','Nhóm facebook','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-07 23:15:09.520238','PAYMENT'),(18,1359560.00,NULL,1,'PC1775146533961','2026-04-02 23:15:33.963151','admin','Máy in đơn và giấy in nhiệt','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-10 23:15:33.961920','PAYMENT'),(19,254000.00,NULL,1,'PC1775146558762','2026-04-02 23:15:58.763272','admin','Đồ gói đơn hàng','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-14 23:15:58.762409','PAYMENT'),(20,140000.00,NULL,1,'PC1775146577783','2026-04-02 23:16:17.784661','admin','Mua tool follow','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-17 23:16:17.783769','PAYMENT'),(21,255000.00,NULL,1,'PC1775146597711','2026-04-02 23:16:37.712609','admin','Mua tem dán cảm ơn/logo','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-19 23:16:37.712096','PAYMENT'),(22,760900.00,NULL,1,'PC1775146690415','2026-04-02 23:18:10.417204','admin','Quảng cáo Shopee','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-03-20 23:18:10.416030','PAYMENT'),(23,340000.00,NULL,1,'PC1775578736256','2026-04-07 23:18:56.266075','admin','Chi quảng cáo Shopee','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-04-07 23:18:56.261652','PAYMENT'),(24,19992151.00,NULL,1,'PT1775612829874','2026-04-08 08:47:09.878646','admin','Rút tiền từ ShopeePay','BANK','Rút tiền từ Ngân hàng','','OTHER',NULL,'Khách vãng lai','2026-04-08 08:47:09.875725','RECEIPT'),(25,950000.00,NULL,1,'PC1775613055165','2026-04-08 08:50:55.186849','admin','Thanh toán đơn nhập hàng REI1775143438896','BANK','Thanh toán đơn nhập hàng','REI1775143438896','SUPPLIER',7,'Công Nghĩa','2026-03-26 08:50:55.186099','PAYMENT'),(26,7040000.00,NULL,1,'PC1775613113448','2026-04-08 08:51:53.453956','admin','Thanh toán đơn nhập hàng REI1775142649635','BANK','Thanh toán đơn nhập hàng','REI1775142649635','SUPPLIER',6,'Trương Minh Trung','2026-03-23 08:51:53.453214','PAYMENT'),(27,1700000.00,NULL,1,'PC1775613147636','2026-04-08 08:52:27.640024','admin','Thanh toán đơn nhập hàng REI1775142578896','BANK','Thanh toán đơn nhập hàng','REI1775142578896','SUPPLIER',5,'Duy Long','2026-03-21 08:52:27.639471','PAYMENT'),(28,860000.00,NULL,1,'PC1775613188139','2026-04-08 08:53:08.143866','admin','Thanh toán đơn nhập hàng REI1775142501301','CASH','Thanh toán đơn nhập hàng','REI1775142501301','SUPPLIER',4,'Lê Thành','2026-03-21 08:53:08.143169','PAYMENT'),(29,1550000.00,NULL,1,'PC1775613229417','2026-04-08 08:53:49.421917','admin','Thanh toán đơn nhập hàng REI1775142417804','BANK','Thanh toán đơn nhập hàng','REI1775142417804','SUPPLIER',3,'Nguyễn Hiếu','2026-03-21 08:53:49.421330','PAYMENT'),(30,630000.00,NULL,1,'PC1775613273326','2026-04-08 08:54:33.333026','admin','Thanh toán đơn nhập hàng REI1775142293658','BANK','Thanh toán đơn nhập hàng','REI1775142293658','SUPPLIER',2,'Quốc Tú','2026-03-17 08:54:33.330436','PAYMENT'),(31,15650431.00,NULL,1,'PC1775613312114','2026-04-08 08:55:12.117917','admin','Thanh toán đơn nhập hàng REI1775142224304','BANK','Thanh toán đơn nhập hàng','REI1775142224304','SUPPLIER',1,'深圳市博诚电脑科技有限公司','2026-03-05 08:55:12.117429','PAYMENT'),(32,297000.00,NULL,1,'PC1775643492735','2026-04-08 17:18:12.759948','admin','Mua tài khoản Canva Pro 1 năm','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-04-08 17:18:12.738357','PAYMENT'),(33,196000.00,NULL,1,'PC1775715587134','2026-04-09 13:19:47.146477','admin','Thẻ cảm ơn','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-04-09 13:19:47.139212','PAYMENT'),(34,400000.00,NULL,1,'PC1775727071466','2026-04-09 16:31:11.468644','admin','Buff 5k follow Shopee','BANK','Chi phí vận hành','0982520658','SUPPLIER',NULL,'Khách vãng lai','2026-04-09 16:31:11.467151','PAYMENT'),(35,400000.00,NULL,1,'PC1775749979857','2026-04-09 22:52:59.860525','admin','Buff 5k follow shopee','BANK','Chi phí vận hành','0982520658','SUPPLIER',NULL,'Khách vãng lai','2026-04-09 22:52:59.858033','PAYMENT'),(36,1139915.00,NULL,1,'PC1775983474668','2026-04-12 15:44:34.671385','admin','Mua cam logitech phục vụ đóng hàng','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-04-12 15:44:34.668675','PAYMENT'),(37,476100.00,NULL,1,'PC1776155075924','2026-04-14 15:24:35.931733','admin','Mua cam HIKVISION phục vụ đóng hàng','CASH','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-04-14 15:24:35.924704','PAYMENT'),(38,54000.00,NULL,1,'PC1776174707797','2026-04-14 20:51:47.798408','admin','Quảng cáo shopee','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-04-14 20:51:47.797584','PAYMENT'),(39,237600.00,NULL,1,'PC1776656951060','2026-04-20 10:49:11.089404','admin','Quảng cáo shopee ','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-04-20 10:49:11.075564','PAYMENT'),(40,38580483.00,NULL,1,'PC1776935444499','2026-04-23 16:10:44.511771','admin','Chi tiền trả nhà cung cấp cho phiếu nhập REI1776935444183','BANK','Trả nợ nhà cung cấp','REI1776935444183','SUPPLIER',1,'深圳市博诚电脑科技有限公司','2026-04-23 16:10:44.498814','PAYMENT'),(41,216000.00,NULL,1,'PC1778082349173','2026-05-06 22:45:49.192357','admin','Quảng cáo shopee','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-05-06 22:45:49.186335','PAYMENT'),(42,6095720.00,NULL,1,'PC1778339995368','2026-05-09 22:19:55.405369','admin','Chi tiền trả nhà cung cấp cho phiếu nhập REI1776935444183','BANK','Trả nợ nhà cung cấp','REI1776935444183','SUPPLIER',1,'深圳市博诚电脑科技有限公司','2026-05-09 22:19:55.363741','PAYMENT'),(43,108000.00,NULL,1,'PC1778491359225','2026-05-11 16:22:39.231664','admin','Quảng cáo','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-05-11 16:22:39.229979','PAYMENT'),(44,270000.00,NULL,1,'PC1778920268480','2026-05-15 15:31:08.487944','admin','Chạy Quảng cáo shopee','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-05-15 15:31:08.483282','PAYMENT'),(45,108000.00,NULL,1,'PC1778920370099','2026-05-16 15:32:50.101383','admin','Quảng cáo','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-05-14 15:32:50.099949','PAYMENT'),(46,292220.00,NULL,1,'PC1779678878113','2026-05-25 10:14:38.123139','admin','Mua bao bì đóng gói','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-05-17 10:14:38.116088','PAYMENT'),(47,108000.00,NULL,1,'PC1780626435336','2026-06-05 09:27:15.343204','admin','Quảng cáo','BANK','Chi phí vận hành','','SUPPLIER',NULL,'Khách vãng lai','2026-06-05 09:27:15.339303','PAYMENT');
/*!40000 ALTER TABLE `cash_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKt8o6pivur7nn124jehx7cygw5` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'2026-04-02 17:21:31.720083','','https://oms.mechkey.vn/media/02042026/3202/img_1775125291713.webp','Bàn phím cơ','2026-04-02 17:21:31.720275'),(2,'2026-04-02 17:21:47.409766','','https://oms.mechkey.vn/media/02042026/8CA1/img_1775125307408.webp','Switch','2026-04-02 17:21:47.409803'),(3,'2026-04-02 17:21:56.622141','','https://oms.mechkey.vn/media/02042026/7399/img_1775125316618.webp','Keycap','2026-04-02 17:21:56.622199'),(4,'2026-04-02 17:22:09.712336','','https://oms.mechkey.vn/media/02042026/BAD8/img_1775125329711.webp','Phụ kiện','2026-04-02 17:22:09.712379'),(5,'2026-04-02 17:22:21.639261','','https://oms.mechkey.vn/media/02042026/3BDB/img_1775125341636.webp','Dịch vụ','2026-04-02 17:22:21.639322'),(6,'2026-04-02 22:13:01.035835','','https://oms.mechkey.vn/media/02042026/FB36/img_1775142915046.png','Chuột','2026-04-02 22:15:15.050600');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customer_groups`
--

DROP TABLE IF EXISTS `customer_groups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customer_groups` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `auto_update` bit(1) DEFAULT NULL,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `color_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `conditions` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime(6) DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKnorqvudff4gtgf863i6fkr0a7` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customer_groups`
--

LOCK TABLES `customer_groups` WRITE;
/*!40000 ALTER TABLE `customer_groups` DISABLE KEYS */;
INSERT INTO `customer_groups` VALUES (1,_binary '','N1775626348497','#0ea5e9','{\"matchType\":\"ALL\",\"rules\":[{\"field\":\"TOTAL_SPENT\",\"operator\":\">\",\"value\":\"3000000\"}]}','2026-04-08 12:32:28.572467','VIP 1','Khách mua hàng trên 3tr');
/*!40000 ALTER TABLE `customer_groups` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `customers`
--

DROP TABLE IF EXISTS `customers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `customers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `company_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `company_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `customer_group` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `dob` date DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `first_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gender` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `has_invoice` bit(1) DEFAULT NULL,
  `last_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ship_address_detail` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ship_city` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ship_company` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ship_district` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tags` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tax_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKmkwx1x9mthieapj92cpxq5msc` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `customers`
--

LOCK TABLES `customers` WRITE;
/*!40000 ALTER TABLE `customers` DISABLE KEYS */;
INSERT INTO `customers` VALUES (1,'KH1775143632926',NULL,NULL,'2026-04-02 22:27:12.943824','Khách lẻ',NULL,NULL,'Vũ Tuấn','Vũ Tuấn','MALE',_binary '\0','',NULL,'0123','',NULL,NULL,NULL,NULL,NULL),(2,'KH1775144176069',NULL,NULL,'2026-04-02 22:36:16.070889','Khách lẻ',NULL,NULL,'Duy Duy','Duy Duy','MALE',_binary '\0','',NULL,'eilionor','',NULL,NULL,NULL,NULL,NULL),(3,'KH1775144398788',NULL,NULL,'2026-04-02 22:39:58.789640','Khách lẻ',NULL,NULL,'Quốc Đạt','Trần Quốc Đạt','MALE',_binary '\0','Trần',NULL,'win123456hy','',NULL,NULL,NULL,NULL,NULL),(4,'KH1775144614396',NULL,NULL,'2026-04-02 22:43:34.398516','Khách lẻ',NULL,NULL,'Minh Anh','Minh Anh','MALE',_binary '\0','',NULL,' demon00286','',NULL,NULL,NULL,NULL,NULL),(5,'KH1775144795332',NULL,NULL,'2026-04-02 22:46:35.333879','Khách lẻ',NULL,NULL,'Đỗ','Nguyễn Văn Đỗ','MALE',_binary '\0','Nguyễn Văn',NULL,'bilonlon','Tháp mẫn xá, Xã Văn Môn, Huyện Yên Phong, Bắc Ninh',NULL,NULL,NULL,NULL,NULL),(6,'KH1775145033744',NULL,NULL,'2026-04-02 22:50:33.746108','Khách lẻ',NULL,NULL,'Toàn Trần','Toàn Trần','MALE',_binary '\0','',NULL,'justoanff','Toà Nhà Big Solution, 46 Đ: Số 23, Khu Đô Thị Thành Phố Giao Lưu, Bắc Từ Liêm, Phường Cổ Nhuế 2, Quận Bắc Từ Liêm, Hà Nội',NULL,NULL,NULL,NULL,NULL),(7,'KH1775145186902',NULL,NULL,'2026-04-02 22:53:06.904958','Khách lẻ',NULL,NULL,'Hưng','Nguyễn Thành Hưng','MALE',_binary '\0','Nguyễn Thành',NULL,' eq2fzzsgdg','Số 3, Ngõ 115 Phố Nguyễn Văn Trỗi, Phường Phương Liệt, Quận Thanh Xuân, Hà Nội',NULL,NULL,NULL,NULL,NULL),(8,'KH1775145390188',NULL,NULL,'2026-04-02 22:56:30.190484','Khách lẻ',NULL,NULL,'Tú','Ngọc Anh Tú','MALE',_binary '\0','Ngọc Anh',NULL,'sngocatu','Số 318, Đê La Thanh, SN3, Phường Ô Chợ Dừa, Quận Đống Đa, Hà Nội',NULL,NULL,NULL,NULL,NULL),(9,'KH1775145515585',NULL,NULL,'2026-04-02 22:58:35.588652','Khách lẻ',NULL,NULL,'Cương Nguyễn','Cương Nguyễn','MALE',_binary '\0','','Khách này có đơn DH1775145565197-E1D0 chưa có số serial',' cuongnguyen_811',' Phường Quảng Yên, Thị Xã Quảng Yên, Quảng Ninh',NULL,NULL,NULL,NULL,NULL),(10,'KH1775211557243','','','2026-04-03 17:19:17.273664','Khách lẻ',NULL,'','BAEK MINSEO','BAEK MINSEO','NAM',_binary '\0','','','minseobaek','Starlake, Khu đô thị Tây Hồ Tây, Phường Cổ Nhuế 1, Quận Bắc Từ Liêm, Hà Nội',NULL,NULL,NULL,'',''),(11,'KH1775211663044','','','2026-04-03 17:21:03.044927','Khách lẻ',NULL,'','Nguyễn Hoàng Long','Nguyễn Hoàng Long',NULL,_binary '\0','','','longlyly1705','số 6 ngõ 135 Kim Lan, Gia Lâm, Hà Nội',NULL,NULL,NULL,'',''),(12,'KH1775397455173',NULL,NULL,'2026-04-05 20:57:35.295025','Khách lẻ',NULL,NULL,'Bui Huy','Bui Huy','MALE',_binary '\0','',NULL,' gtw518x32m','67 Vùi Thị Xuân, Phường An Hải, Thành phố Đà Nẵng',NULL,NULL,NULL,NULL,NULL),(13,'KH1775397633439',NULL,NULL,'2026-04-05 21:00:33.441519','Khách lẻ',NULL,NULL,'Vũ Đức Huy','Vũ Đức Huy','MALE',_binary '\0','',NULL,' vumagnes','Số 8, ngõ 20 Văn Cao, Phường Gia Viên, Thành phố Hải Phòng',NULL,NULL,NULL,NULL,NULL),(14,'KH1775439937390','','','2026-04-06 08:45:37.399005','Khách lẻ',NULL,'','Phạm Tuấn Minh','Phạm Tuấn Minh','NAM',_binary '\0','','','phamminh591','36A Dịch Vọng Hậu, Phường Dịch Vọng Hậu, Quận Cầu Giấy, Hà Nội',NULL,NULL,NULL,'',''),(15,'KH1775577998428',NULL,NULL,'2026-04-07 23:06:38.430931','Khách lẻ',NULL,NULL,'Hải Linh','Hải Linh','MALE',_binary '\0','',NULL,'lubday_','72 Thiên Đức, Phường Vệ An, Thành Phố Bắc Ninh, Bắc Ninh',NULL,NULL,NULL,NULL,NULL),(16,'KH1775719468857',NULL,NULL,'2026-04-09 14:24:28.882306','Khách lẻ',NULL,NULL,'Nguyễn Mạnh Thắng','Nguyễn Mạnh Thắng','MALE',_binary '\0','',NULL,'manhthang712','Cty May Sông Hồng, 105 Nguyễn Đức\nThuận, Phường Thống Nhất, Thành\nPhố Nam Định, Nam Định',NULL,NULL,NULL,NULL,NULL),(17,'KH1776057845031',NULL,NULL,'2026-04-13 12:24:05.046688','Khách lẻ',NULL,NULL,'Nguyen Phuong NC','Nguyen Phuong NC','MALE',_binary '\0','',NULL,'nguyenchidung270816','90 Giảng Võ, Phường Cát Linh, Quận Đống Đa, Hà Nội',NULL,NULL,NULL,NULL,NULL),(18,'KH1776612783379',NULL,NULL,'2026-04-19 22:33:03.447988','Khách lẻ',NULL,NULL,'Hậu','Đặng Hữu Hậu','MALE',_binary '\0','Đặng Hữu',NULL,' huuhau1011','D8/16D khu phố 4, Thị Trấn Tân Túc, Huyện Bình Chánh, TP. Hồ Chí Minh',NULL,NULL,NULL,NULL,NULL),(19,'KH1778081886870',NULL,NULL,'2026-05-06 22:38:06.972451','Khách lẻ',NULL,NULL,'Trương Duy Hùng','Trương Duy Hùng','MALE',_binary '\0','',NULL,'5ue804rtx5','dương đông- tp phú quốc - kien giang, Đặc khu Phú Quốc, Tỉnh An Giang',NULL,NULL,NULL,NULL,NULL),(20,'KH1778082121951',NULL,NULL,'2026-05-06 22:42:01.954332','Khách lẻ',NULL,NULL,'Nguyễn Việt Khôi','Nguyễn Việt Khôi','MALE',_binary '\0','',NULL,'peqtkjh881','Star Tower Số 68, Duong Dương Đình Nghệ, Phường Cầu Giấy, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(21,'KH1778491228871',NULL,NULL,'2026-05-11 16:20:28.903858','Khách lẻ',NULL,NULL,'Ms. Nga','Ms. Nga','FEMALE',_binary '\0','',NULL,' katsu2810','Chung cư Lucky Bắc Hà, số 30, Phạm Văn Đồng, Phường Nghĩa Đô, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(22,'KH1778662780874',NULL,NULL,'2026-05-13 15:59:40.952326','Khách lẻ',NULL,NULL,'Minh Anh','Minh Anh','MALE',_binary '\0','',NULL,'ngoctran_46','Nhà số 2, ngõ 56 phố Hương Viên, quận Hai Bà Trưng, Phường Hai Bà Trưng, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(23,'KH1778741522078',NULL,NULL,'2026-05-14 13:52:02.131014','Khách lẻ',NULL,NULL,'Xuân Hùng','Xuân Hùng','MALE',_binary '\0','',NULL,'xuanhung1811','The Dewey Schools Starlake Tây Hồ Tây , Cổng 1, Lô H3, Khu Đô Thị Starlake Tây Hồ Tây, , Phường Xuân Đỉnh, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(24,'KH1778919116955',NULL,NULL,'2026-05-16 15:11:56.969329','Khách lẻ',NULL,NULL,'Phượng','Phượng','FEMALE',_binary '\0','',NULL,' mguyenthiphuong1928','Nguyệt Quế 21-48 Vinhomes Riverside, Phường Phúc Lợi, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(25,'KH1778919261245',NULL,NULL,'2026-05-16 15:14:21.246883','Khách lẻ',NULL,NULL,'Hà Văn Hoà','Hà Văn Hoà','MALE',_binary '\0','',NULL,'havanhoa0105','Ngã tư Tân Trà, Trương Đăng Quế, Phường Ngũ Hành Sơn, Thành phố Đà Nẵng',NULL,NULL,NULL,NULL,NULL),(26,'KH1778919411662',NULL,NULL,'2026-05-16 15:16:51.664375','Khách lẻ',NULL,NULL,'Nguyễn Quang','Nguyễn Quang','MALE',_binary '\0','',NULL,' 0catows57r','Đền Bùng, Số Nhà 24, Xã Tây Phương, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(27,'KH1778919604020',NULL,NULL,'2026-05-16 15:20:04.022186','Khách lẻ',NULL,NULL,'Hoàng Tuấn','Hoàng Tuấn','MALE',_binary '\0','',NULL,' muadoshoppe123','16/23 đường số 18, Phường Thông Tây Hội, Thành phố Hồ Chí Minh',NULL,NULL,NULL,NULL,NULL),(28,'KH1778919707095',NULL,NULL,'2026-05-16 15:21:47.098671','Khách lẻ',NULL,NULL,'Nguyễn Quốc Nam','Nguyễn Quốc Nam','MALE',_binary '\0','',NULL,'0914194972','49/4 Khúc Thừa Dụ, Phường Long Xuyên, Tỉnh An Giang',NULL,NULL,NULL,NULL,NULL),(29,'KH1778919898867',NULL,NULL,'2026-05-16 15:24:58.869285','Khách lẻ',NULL,NULL,'Hưng Nguyên','Hưng Nguyên','MALE',_binary '\0','',NULL,'hungnguyen5225','106 Trần Phú, Phường Hà Đông, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(30,'KH1778920005912',NULL,NULL,'2026-05-16 15:26:45.915475','Khách lẻ',NULL,NULL,'Nguyễn Viết Hoàng Đăng Diễn','Nguyễn Viết Hoàng Đăng Diễn','MALE',_binary '\0','',NULL,'ngdinnguynvithong218','QL26, Thôn 1, Xã Cư Prao, Tỉnh Đắk Lắk',NULL,NULL,NULL,NULL,NULL),(31,'KH1778920104220',NULL,NULL,'2026-05-16 15:28:24.222024','Khách lẻ',NULL,NULL,'Nguyễn Vũ Hoàng Anh','Nguyễn Vũ Hoàng Anh','MALE',_binary '\0','',NULL,'nvuhoanganh','Số 220, Trần Não An Khánh, Phường An Khánh, Thành phố Hồ Chí Minh',NULL,NULL,NULL,NULL,NULL),(32,'KH1779070924812',NULL,NULL,'2026-05-18 09:22:04.916312','Khách lẻ',NULL,NULL,'Nguyễn Đắc Hải','Nguyễn Đắc Hải','MALE',_binary '\0','',NULL,'ff61fsh_45','Ktx Đại Học Dược, Số 1a, Thọ Lão, Phường Hai Bà Trưng, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(33,'KH1779090935386',NULL,NULL,'2026-05-18 14:55:35.395369','Khách lẻ',NULL,NULL,'Đức Thịnh','Đức Thịnh','MALE',_binary '\0','',NULL,'trongkiler','Tòa Nhà Vinaphone, Số 811, Đường\nGiải Phóng, , Phường Hoàng Mai,\nThành phố Hà Nội\n',NULL,NULL,NULL,NULL,NULL),(34,'KH1779160013579',NULL,NULL,'2026-05-19 10:06:53.590792','Khách lẻ',NULL,NULL,'Huy Nguyen','Huy Nguyen','MALE',_binary '\0','',NULL,'nhathatnha','Số 68, Đ . Lê Văn Lương, Phường Thanh Xuân, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(35,'KH1779160142028',NULL,NULL,'2026-05-19 10:09:02.030462','Khách lẻ',NULL,NULL,'Đình Diện','Đình Diện','MALE',_binary '\0','',NULL,'dien_tram_huong_123','Lankmark 72, phường Yên Hòa, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(36,'KH1779160320738',NULL,NULL,'2026-05-19 10:12:00.742745','Khách lẻ',NULL,NULL,'Minh Tài','Minh Tài','MALE',_binary '\0','',NULL,'trungtinl771','Số 322, Đường 23/8, Phường Bạc Liêu, Tỉnh Cà Mau',NULL,NULL,NULL,NULL,NULL),(37,'KH1779356611802',NULL,NULL,'2026-05-21 16:43:31.894613','Khách lẻ',NULL,NULL,'Nguyễn Quang Minh','Nguyễn Quang Minh','MALE',_binary '\0','',NULL,'minhshopee123','77 Lê Hồng Phong, Phường Chũ, Tỉnh Bắc Ninh',NULL,NULL,NULL,NULL,NULL),(38,'KH1779379588394',NULL,NULL,'2026-05-21 23:06:28.398385','Khách lẻ',NULL,NULL,'Quang Vinh','Quang Vinh','MALE',_binary '\0','',NULL,'tranquangvinh_2268','Toà nhà CT11, Kim Văn,  Phường Định Công, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(39,'KH1779678316659',NULL,NULL,'2026-05-25 10:05:16.793638','Khách lẻ',NULL,NULL,'Quỳnh Anh','Quỳnh Anh','MALE',_binary '\0','',NULL,'_quynhanh205','Toà nhà A, Toà Nhà số 82, Nguyễn Tuân, Phường Thanh Xuân, Thành phố Hà Nội',NULL,NULL,NULL,NULL,NULL),(40,'KH1779678429433',NULL,NULL,'2026-05-25 10:07:09.436441','Khách lẻ',NULL,NULL,'Nguyễn Hữu Thịnh','Nguyễn Hữu Thịnh','MALE',_binary '\0','',NULL,' gachduoithinh','102 Quang Trung, Xã Đại Lộc, Thành phố Đà Nẵng',NULL,NULL,NULL,NULL,NULL),(41,'KH1779678594821',NULL,NULL,'2026-05-25 10:09:54.826556','Khách lẻ',NULL,NULL,'Lưu Minh Hiếu','Lưu Minh Hiếu','MALE',_binary '\0','',NULL,'ntnzz09','Số 531b, Trần Nhân Tông, Tổ 24, Phường Nam Định, Tỉnh Ninh Bình',NULL,NULL,NULL,NULL,NULL),(42,'KH1779678682290',NULL,NULL,'2026-05-25 10:11:22.290530','Khách lẻ',NULL,NULL,'Tâm Đạt','Tâm Đạt','MALE',_binary '\0','',NULL,' souta_ikiru','Đường 1, Khu Dân Cư Hai Lai, Phòng Trọ Số 4, Xã Châu Thành, Tỉnh An Giang',NULL,NULL,NULL,NULL,NULL),(43,'KH1779688779782',NULL,NULL,'2026-05-25 12:59:39.789392','Khách lẻ',NULL,NULL,'Hoàng Mạnh','Hoàng Mạnh','MALE',_binary '\0','',NULL,'0362806817','11 DUy Tân, Cầu Giấy, Hà Nội',NULL,NULL,NULL,NULL,NULL),(44,'KH1780626381270',NULL,NULL,'2026-06-05 09:26:21.283200','Khách lẻ',NULL,NULL,'Hạnh Mai','Hạnh Mai','MALE',_binary '\0','',NULL,'huyhaoho732','Ấp Ô Tung A, Xã Cầu Kè, Tỉnh Vĩnh Long',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `customers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `inventory`
--

DROP TABLE IF EXISTS `inventory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inventory` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `available_stock` int NOT NULL,
  `branch_id` bigint NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `inbound_stock` int DEFAULT NULL,
  `stock` int NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `variant_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKsyu5lcgrrv64sr2mqoshgb4rw` (`variant_id`,`branch_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `inventory`
--

LOCK TABLES `inventory` WRITE;
/*!40000 ALTER TABLE `inventory` DISABLE KEYS */;
INSERT INTO `inventory` VALUES (1,5,1,'2026-04-02 22:03:44.390222',0,5,'2026-05-16 15:09:17.527961',1),(2,2,1,'2026-04-02 22:03:44.405788',0,2,'2026-04-02 22:03:44.405815',5),(3,7,1,'2026-04-02 22:03:44.429165',0,7,'2026-05-16 15:25:28.292273',3),(4,3,1,'2026-04-02 22:03:44.450732',0,3,'2026-05-16 15:12:51.758429',6),(5,3,1,'2026-04-02 22:03:44.471341',0,3,'2026-05-09 22:20:06.012606',7),(6,2,1,'2026-04-02 22:06:57.834396',0,2,'2026-05-09 22:20:06.034263',8),(7,0,1,'2026-04-02 22:06:57.853078',0,0,'2026-04-13 13:37:37.333923',11),(8,1,1,'2026-04-02 22:08:21.333678',0,1,'2026-06-05 09:23:36.503894',13),(9,2,1,'2026-04-02 22:09:38.926142',0,2,'2026-05-09 22:20:05.904881',16),(10,0,1,'2026-04-02 22:23:58.954822',0,1,'2026-06-05 09:26:51.170638',17),(11,0,1,'2026-04-02 22:23:58.984242',0,0,'2026-06-05 09:23:41.855881',14),(12,0,1,'2026-04-23 16:10:44.565148',0,0,'2026-05-13 16:01:29.237973',104),(13,0,1,'2026-04-23 16:10:44.626235',0,0,'2026-05-21 23:42:11.335730',21),(14,0,1,'2026-04-23 16:10:44.653220',0,0,'2026-05-25 10:10:28.321811',22),(15,3,1,'2026-04-23 16:10:44.699731',0,3,'2026-05-09 22:20:05.824115',18),(16,8,1,'2026-04-23 16:10:44.727615',0,8,'2026-05-22 14:48:05.285726',20),(17,3,1,'2026-04-23 16:10:44.773590',0,3,'2026-05-09 22:20:05.942013',28),(18,2,1,'2026-04-23 16:10:44.791326',0,2,'2026-05-09 22:20:05.962553',48),(19,4,1,'2026-04-23 16:10:44.880365',0,4,'2026-05-18 15:51:46.012053',91),(20,5,1,'2026-04-23 16:10:44.897795',0,5,'2026-05-09 22:20:06.080412',103),(21,1,1,'2026-04-23 16:10:44.919542',0,1,'2026-05-19 15:10:35.478483',112);
/*!40000 ALTER TABLE `inventory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `master_data`
--

DROP TABLE IF EXISTS `master_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `master_data` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `data_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `data_value` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `sort_order` int DEFAULT NULL,
  `data_label` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `master_data`
--

LOCK TABLES `master_data` WRITE;
/*!40000 ALTER TABLE `master_data` DISABLE KEYS */;
INSERT INTO `master_data` VALUES (1,'CATEGORY','Bàn phím cơ',1,NULL),(2,'CATEGORY','Switch',2,NULL),(3,'CATEGORY','Keycap',3,NULL),(4,'CATEGORY','Phụ kiện',4,NULL),(5,'CATEGORY','Linh kiện Custom',5,NULL),(6,'CATEGORY','Dụng cụ lube',6,NULL),(7,'BRAND','Aula',1,NULL),(8,'BRAND','Leobog',2,NULL),(9,'BRAND','Akko',3,NULL),(10,'BRAND','Xinmeng',4,NULL),(11,'BRAND','Cherry',5,NULL),(12,'BRAND','Kailh',6,NULL),(13,'BRAND','Gateron',7,NULL),(14,'BRAND','FL Esports',8,NULL),(15,'BRAND','Khác',9,NULL),(16,'UNIT','Chiếc',1,NULL),(17,'UNIT','Bộ',2,NULL),(18,'UNIT','Pack',3,NULL),(19,'UNIT','Lọ',4,NULL),(20,'UNIT','Tuýp',5,NULL),(21,'UNIT','Gram',6,NULL),(22,'UNIT','Sợi',7,NULL),(23,'UNIT','Tấm',8,NULL),(24,'UNIT','Cái',9,NULL),(25,'STORE_NAME','MECHKEY',1,NULL),(26,'STORE_PHONE','0971130397',1,NULL),(27,'STORE_ADDRESS','số 5 ngõ 55 Nguyễn Ngọc Nại, Phương Liệt, Hà Nội',1,NULL),(28,'STORE_LOGO','https://oms.mechkey.vn/media/08042026/10C8/img_1775622890521.png',1,NULL),(29,'STORE_EMAIL','',1,NULL),(30,'ORDER_STATUS','CREATED',1,'Khởi tạo'),(31,'ORDER_STATUS','CONFIRMED',2,'Đã xác nhận'),(32,'ORDER_STATUS','PROCESSING',3,'Đang xử lý'),(33,'ORDER_STATUS','SHIPPING',4,'Đang giao hàng'),(34,'ORDER_STATUS','COMPLETED',5,'Hoàn thành'),(35,'ORDER_STATUS','CANCELLED',6,'Đã hủy'),(36,'ORDER_STATUS','RETURNED',7,'Hoàn trả'),(37,'PAYMENT_STATUS','UNPAID',1,'Chưa thanh toán'),(38,'PAYMENT_STATUS','PARTIAL',2,'Thanh toán một phần'),(39,'PAYMENT_STATUS','PAID',3,'Đã thanh toán'),(40,'PAYMENT_STATUS','REFUNDED',4,'Đã hoàn tiền'),(41,'GENERAL_STATUS','ACTIVE',1,'Đang hoạt động'),(42,'GENERAL_STATUS','INACTIVE',2,'Ngừng hoạt động'),(43,'GENERAL_STATUS','BANNED',3,'Đình chỉ'),(44,'RETURN_STATUS','PENDING',1,'Chờ xử lý'),(45,'RETURN_STATUS','APPROVED',2,'Đã chấp nhận yêu cầu'),(46,'RETURN_STATUS','REJECTED',3,'Đã từ chối'),(47,'RETURN_STATUS','COMPLETED',4,'Hoàn tất trả hàng'),(48,'RECEIPT_STATUS','TRADING',1,'Đang giao dịch'),(49,'RECEIPT_STATUS','COMPLETED',2,'Hoàn thành'),(50,'RECEIPT_STATUS','CANCELLED',3,'Đã hủy');
/*!40000 ALTER TABLE `master_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `link` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `message` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_read` bit(1) DEFAULT NULL,
  `title` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` enum('ORDER','PAYMENT','SYSTEM','WARNING','WARRANTY','IMPORT','RETURN','INFO','ERROR') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,'2026-04-07 23:03:04.442053','/ui/orders/detail/DH1775577784201-D832','Đơn hàng DH1775577784201-D832 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(2,'2026-04-07 23:03:14.589125','/ui/orders/detail/DH1775577784201-D832','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1775577784201-D832','ORDER'),(3,'2026-04-07 23:08:39.415410','/ui/orders/detail/DH1775578119335-125B','Đơn hàng DH1775578119335-125B vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(4,'2026-04-07 23:18:56.288845','/ui/cashbook/payments/23','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 340,000 đ',_binary '','Phiếu chi mới: PC1775578736256','PAYMENT'),(5,'2026-04-08 08:47:09.901991','/ui/cashbook/receipts/24','Đã ghi nhận phiếu thu với Khách vãng lai. Số tiền: 19,992,151 đ',_binary '','Phiếu thu mới: PT1775612829874','PAYMENT'),(6,'2026-04-08 08:50:55.196003','/ui/cashbook/payments/25','Đã ghi nhận phiếu chi với Công Nghĩa. Số tiền: 950,000 đ',_binary '','Phiếu chi mới: PC1775613055165','PAYMENT'),(7,'2026-04-08 08:51:53.462105','/ui/cashbook/payments/26','Đã ghi nhận phiếu chi với Trương Minh Trung. Số tiền: 7,040,000 đ',_binary '','Phiếu chi mới: PC1775613113448','PAYMENT'),(8,'2026-04-08 08:52:27.644233','/ui/cashbook/payments/27','Đã ghi nhận phiếu chi với Duy Long. Số tiền: 1,700,000 đ',_binary '','Phiếu chi mới: PC1775613147636','PAYMENT'),(9,'2026-04-08 08:53:08.152875','/ui/cashbook/payments/28','Đã ghi nhận phiếu chi với Lê Thành. Số tiền: 860,000 đ',_binary '','Phiếu chi mới: PC1775613188139','PAYMENT'),(10,'2026-04-08 08:53:49.427544','/ui/cashbook/payments/29','Đã ghi nhận phiếu chi với Nguyễn Hiếu. Số tiền: 1,550,000 đ',_binary '','Phiếu chi mới: PC1775613229417','PAYMENT'),(11,'2026-04-08 08:54:33.340672','/ui/cashbook/payments/30','Đã ghi nhận phiếu chi với Quốc Tú. Số tiền: 630,000 đ',_binary '','Phiếu chi mới: PC1775613273326','PAYMENT'),(12,'2026-04-08 08:55:12.121509','/ui/cashbook/payments/31','Đã ghi nhận phiếu chi với 深圳市博诚电脑科技有限公司. Số tiền: 15,650,431 đ',_binary '','Phiếu chi mới: PC1775613312114','PAYMENT'),(13,'2026-04-08 17:18:12.889259','/ui/cashbook/payments/32','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 297,000 đ',_binary '','Phiếu chi mới: PC1775643492735','PAYMENT'),(14,'2026-04-08 18:18:56.327228','/ui/orders/detail/DH1775578119335-125B','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1775578119335-125B','ORDER'),(15,'2026-04-09 13:19:47.178335','/ui/cashbook/payments/33','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 196,000 đ',_binary '','Phiếu chi mới: PC1775715587134','PAYMENT'),(16,'2026-04-09 14:24:53.543447','/ui/orders/detail/DH1775719493394-BE8B','Đơn hàng DH1775719493394-BE8B vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(17,'2026-04-09 16:31:11.496305','/ui/cashbook/payments/34','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 400,000 đ',_binary '','Phiếu chi mới: PC1775727071466','PAYMENT'),(18,'2026-04-09 22:52:59.883887','/ui/cashbook/payments/35','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 400,000 đ',_binary '','Phiếu chi mới: PC1775749979857','PAYMENT'),(19,'2026-04-12 15:42:38.650826','/ui/orders/detail/DH1775719493394-BE8B','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1775719493394-BE8B','ORDER'),(20,'2026-04-12 15:44:34.703625','/ui/cashbook/payments/36','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 1,039,896 đ',_binary '','Phiếu chi mới: PC1775983474668','PAYMENT'),(21,'2026-04-13 12:24:55.370055','/ui/orders/detail/DH1776057895287-F5A5','Đơn hàng DH1776057895287-F5A5 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(22,'2026-04-13 13:37:37.246550','/ui/orders/detail/DH1776057895287-F5A5','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1776057895287-F5A5','ORDER'),(23,'2026-04-14 15:24:35.976926','/ui/cashbook/payments/37','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 476,100 đ',_binary '','Phiếu chi mới: PC1776155075924','PAYMENT'),(24,'2026-04-14 20:51:47.815556','/ui/cashbook/payments/38','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 54,000 đ',_binary '','Phiếu chi mới: PC1776174707797','PAYMENT'),(25,'2026-04-19 22:33:36.852593','/ui/orders/detail/DH1776612816510-AD00','Đơn hàng DH1776612816510-AD00 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(26,'2026-04-20 10:49:11.151723','/ui/cashbook/payments/39','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 237,600 đ',_binary '','Phiếu chi mới: PC1776656951060','PAYMENT'),(27,'2026-04-22 13:29:55.983568','/ui/orders/detail/DH1776612816510-AD00','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1776612816510-AD00','ORDER'),(28,'2026-04-23 16:10:44.482940','/ui/imports/REI1776935444183','Phiếu nhập REI1776935444183 vừa được tạo trên hệ thống.',_binary '','Đơn nhập hàng mới','IMPORT'),(29,'2026-04-23 16:10:44.533346','/ui/cashbook/payments/40','Đã ghi nhận phiếu chi với 深圳市博诚电脑科技有限公司. Số tiền: 38,580,483 đ',_binary '','Phiếu chi mới: PC1776935444499','PAYMENT'),(30,'2026-04-27 23:05:05.386786','/ui/imports/REI1776935444183','Phiếu nhập REI1776935444183 đã được chỉnh sửa thông tin.',_binary '','Cập nhật đơn nhập hàng','IMPORT'),(31,'2026-05-06 22:39:33.382490','/ui/orders/detail/DH1778081973144-0EEC','Đơn hàng DH1778081973144-0EEC vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(32,'2026-05-06 22:42:32.924002','/ui/orders/detail/DH1778082152861-10C9','Đơn hàng DH1778082152861-10C9 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(33,'2026-05-06 22:42:41.075541','/ui/orders/detail/DH1778082152861-10C9','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778082152861-10C9','ORDER'),(34,'2026-05-06 22:45:49.228566','/ui/cashbook/detail/41','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 216,000 đ',_binary '','Phiếu chi mới: PC1778082349173','PAYMENT'),(35,'2026-05-09 22:19:32.192398','/ui/imports/REI1776935444183','Phiếu nhập REI1776935444183 đã được chỉnh sửa thông tin.',_binary '','Cập nhật đơn nhập hàng','IMPORT'),(36,'2026-05-09 22:19:55.356869','/ui/imports/REI1776935444183','Ghi nhận thanh toán 6095720đ cho phiếu nhập REI1776935444183',_binary '','Thanh toán phiếu nhập','IMPORT'),(37,'2026-05-09 22:19:55.437120','/ui/cashbook/detail/42','Đã ghi nhận phiếu chi với 深圳市博诚电脑科技有限公司. Số tiền: 6,095,720 đ',_binary '','Phiếu chi mới: PC1778339995368','PAYMENT'),(38,'2026-05-09 22:20:06.104816','/ui/imports/REI1776935444183','Hàng hóa của phiếu nhập REI1776935444183 đã được cộng vào Cửa hàng chính',_binary '','Nhập kho thành công','IMPORT'),(39,'2026-05-09 22:20:28.885518','/ui/orders/detail/DH1778081973144-0EEC','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778081973144-0EEC','ORDER'),(40,'2026-05-11 16:20:50.068481','/ui/orders/detail/DH1778491249917-9E0F','Đơn hàng DH1778491249917-9E0F vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(41,'2026-05-11 16:22:39.243647','/ui/cashbook/detail/43','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 108,000 đ',_binary '','Phiếu chi mới: PC1778491359225','PAYMENT'),(42,'2026-05-13 16:00:23.255987','/ui/orders/detail/DH1778662823037-502E','Đơn hàng DH1778662823037-502E vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(43,'2026-05-13 16:01:29.013284','/ui/orders/detail/DH1778491249917-9E0F','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778491249917-9E0F','ORDER'),(44,'2026-05-14 13:52:43.198448','/ui/orders/detail/DH1778741563073-6C2C','Đơn hàng DH1778741563073-6C2C vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(45,'2026-05-14 15:24:16.816862','/ui/orders/detail/DH1778741563073-6C2C','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778741563073-6C2C','ORDER'),(46,'2026-05-16 15:09:17.357113','/ui/orders/detail/DH1778662823037-502E','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778662823037-502E','ORDER'),(47,'2026-05-16 15:12:37.345478','/ui/orders/detail/DH1778919157290-AF19','Đơn hàng DH1778919157290-AF19 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(48,'2026-05-16 15:12:51.713707','/ui/orders/detail/DH1778919157290-AF19','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778919157290-AF19','ORDER'),(49,'2026-05-16 15:15:41.532414','/ui/orders/detail/DH1778919341487-FA51','Đơn hàng DH1778919341487-FA51 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(50,'2026-05-16 15:18:25.082282','/ui/orders/detail/DH1778919505024-3955','Đơn hàng DH1778919505024-3955 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(51,'2026-05-16 15:20:27.308461','/ui/orders/detail/DH1778919627250-5A19','Đơn hàng DH1778919627250-5A19 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(52,'2026-05-16 15:22:52.274624','/ui/orders/detail/DH1778919772229-CF2B','Đơn hàng DH1778919772229-CF2B vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(53,'2026-05-16 15:25:20.938142','/ui/orders/detail/DH1778919920878-6603','Đơn hàng DH1778919920878-6603 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(54,'2026-05-16 15:25:28.246150','/ui/orders/detail/DH1778919920878-6603','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778919920878-6603','ORDER'),(55,'2026-05-16 15:27:14.912617','/ui/orders/detail/DH1778920034834-4818','Đơn hàng DH1778920034834-4818 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(56,'2026-05-16 15:29:03.686391','/ui/orders/detail/DH1778920143631-EFB6','Đơn hàng DH1778920143631-EFB6 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(57,'2026-05-16 15:31:08.509015','/ui/cashbook/detail/44','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 270,000 đ',_binary '','Phiếu chi mới: PC1778920268480','PAYMENT'),(58,'2026-05-16 15:32:50.116220','/ui/cashbook/detail/45','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 108,000 đ',_binary '','Phiếu chi mới: PC1778920370099','PAYMENT'),(59,'2026-05-18 09:22:52.250100','/ui/orders/detail/DH1779070972025-B000','Đơn hàng DH1779070972025-B000 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(60,'2026-05-18 09:24:17.978709','/ui/orders/detail/DH1778919505024-3955','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778919505024-3955','ORDER'),(61,'2026-05-18 13:05:59.647955','/ui/orders/detail/DH1778919341487-FA51','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778919341487-FA51','ORDER'),(62,'2026-05-18 14:56:17.081359','/ui/orders/detail/DH1779090977014-3C70','Đơn hàng DH1779090977014-3C70 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(63,'2026-05-18 15:22:27.026857','/ui/orders/detail/DH1779090977014-3C70','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779090977014-3C70','ORDER'),(64,'2026-05-18 15:51:45.972083','/ui/orders/detail/DH1778920143631-EFB6','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778920143631-EFB6','ORDER'),(65,'2026-05-19 10:07:36.448447','/ui/orders/detail/DH1779160056365-6C2C','Đơn hàng DH1779160056365-6C2C vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(66,'2026-05-19 10:07:43.049532','/ui/orders/detail/DH1779160056365-6C2C','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779160056365-6C2C','ORDER'),(67,'2026-05-19 10:10:04.537700','/ui/orders/detail/DH1779160204491-B063','Đơn hàng DH1779160204491-B063 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(68,'2026-05-19 10:10:11.643418','/ui/orders/detail/DH1779160204491-B063','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779160204491-B063','ORDER'),(69,'2026-05-19 10:12:08.779366','/ui/orders/detail/DH1779160328733-4724','Đơn hàng DH1779160328733-4724 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(70,'2026-05-19 13:59:41.325781','/ui/orders/detail/DH1778920034834-4818','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778920034834-4818','ORDER'),(71,'2026-05-19 15:10:35.434083','/ui/orders/detail/DH1778919772229-CF2B','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778919772229-CF2B','ORDER'),(72,'2026-05-19 15:13:55.999237','/ui/orders/detail/DH1778919627250-5A19','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1778919627250-5A19','ORDER'),(73,'2026-05-19 19:01:59.895763','/ui/orders/detail/DH1779070972025-B000','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779070972025-B000','ORDER'),(74,'2026-05-21 16:44:26.316491','/ui/orders/detail/DH1779356666124-33C5','Đơn hàng DH1779356666124-33C5 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(75,'2026-05-21 23:07:23.500012','/ui/orders/detail/DH1779379643431-72F0','Đơn hàng DH1779379643431-72F0 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(76,'2026-05-21 23:42:11.273470','/ui/orders/detail/DH1779160328733-4724','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779160328733-4724','ORDER'),(77,'2026-05-22 13:29:37.494806','/ui/orders/detail/DH1779356666124-33C5','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779356666124-33C5','ORDER'),(78,'2026-05-22 14:48:05.256949','/ui/orders/detail/DH1779379643431-72F0','Đơn hàng đã chuyển sang trạng thái: Đã hủy',_binary '','Cập nhật đơn hàng DH1779379643431-72F0','ORDER'),(79,'2026-05-25 10:05:55.784268','/ui/orders/detail/DH1779678355516-07C0','Đơn hàng DH1779678355516-07C0 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(80,'2026-05-25 10:06:03.345561','/ui/orders/detail/DH1779678355516-07C0','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779678355516-07C0','ORDER'),(81,'2026-05-25 10:07:56.712030','/ui/orders/detail/DH1779678476651-5984','Đơn hàng DH1779678476651-5984 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(82,'2026-05-25 10:08:03.567683','/ui/orders/detail/DH1779678476651-5984','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779678476651-5984','ORDER'),(83,'2026-05-25 10:10:20.892586','/ui/orders/detail/DH1779678620822-2827','Đơn hàng DH1779678620822-2827 vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(84,'2026-05-25 10:10:28.257395','/ui/orders/detail/DH1779678620822-2827','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779678620822-2827','ORDER'),(85,'2026-05-25 10:12:35.457951','/ui/orders/detail/DH1779678755411-461F','Đơn hàng DH1779678755411-461F vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(86,'2026-05-25 10:14:38.157059','/ui/cashbook/detail/46','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 292,220 đ',_binary '','Phiếu chi mới: PC1779678878113','PAYMENT'),(87,'2026-05-25 13:00:13.789381','/ui/orders/detail/DH1779688813747-E1AB','Đơn hàng DH1779688813747-E1AB vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(88,'2026-06-05 09:23:36.399968','/ui/orders/detail/DH1779688813747-E1AB','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779688813747-E1AB','ORDER'),(89,'2026-06-05 09:23:41.815648','/ui/orders/detail/DH1779678755411-461F','Đơn hàng đã chuyển sang trạng thái: Hoàn thành',_binary '','Cập nhật đơn hàng DH1779678755411-461F','ORDER'),(90,'2026-06-05 09:26:45.425070','/ui/orders/detail/DH1780626405352-D1FA','Đơn hàng DH1780626405352-D1FA vừa được tạo thành công.',_binary '','Đơn hàng mới','ORDER'),(91,'2026-06-05 09:27:15.377833','/ui/cashbook/detail/47','Đã ghi nhận phiếu chi với Khách vãng lai. Số tiền: 108,000 đ',_binary '','Phiếu chi mới: PC1780626435336','PAYMENT');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_activities`
--

DROP TABLE IF EXISTS `order_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_activities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `order_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKk4q9wqownm6x25jx5pl7vojcu` (`order_id`),
  CONSTRAINT `FKk4q9wqownm6x25jx5pl7vojcu` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=147 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_activities`
--

LOCK TABLES `order_activities` WRITE;
/*!40000 ALTER TABLE `order_activities` DISABLE KEYS */;
INSERT INTO `order_activities` VALUES (1,'Tạo mới đơn hàng','2026-04-03 17:19:40.756497','admin','Khởi tạo đơn hàng thành công',10),(2,'Cập nhật thông tin','2026-04-03 17:20:20.280370','admin','Thay đổi thông tin chi tiết đơn hàng',10),(3,'Tạo mới đơn hàng','2026-04-03 17:21:40.379882','admin','Khởi tạo đơn hàng thành công',11),(4,'Cập nhật thông tin','2026-04-03 23:48:43.524800','admin','Thay đổi thông tin chi tiết đơn hàng',10),(5,'Cập nhật trạng thái','2026-04-03 23:48:47.612892','admin','Chuyển trạng thái từ [Khởi tạo] sang [Đang giao hàng]',10),(6,'Cập nhật trạng thái','2026-04-03 23:48:52.114256','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',10),(7,'Cập nhật thông tin','2026-04-03 23:51:20.363806','admin','Thay đổi thông tin chi tiết đơn hàng',11),(8,'Cập nhật thông tin','2026-04-03 23:51:39.713507','admin','Thay đổi thông tin chi tiết đơn hàng',11),(9,'Cập nhật trạng thái','2026-04-04 11:41:00.828142','admin','Chuyển trạng thái từ [Khởi tạo] sang [Đang giao hàng]',11),(10,'Cập nhật trạng thái','2026-04-04 11:41:03.547811','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',11),(11,'Tạo mới đơn hàng','2026-04-05 20:59:21.871596','admin','Khởi tạo đơn hàng thành công',12),(12,'Tạo mới đơn hàng','2026-04-05 21:02:40.774451','admin','Khởi tạo đơn hàng thành công',13),(13,'Tạo mới đơn hàng','2026-04-06 08:46:54.307893','admin','Khởi tạo đơn hàng thành công',14),(14,'Cập nhật thông tin','2026-04-06 09:35:26.141669','admin','Thay đổi thông tin chi tiết đơn hàng',14),(15,'Cập nhật trạng thái','2026-04-06 09:35:28.835145','admin','Chuyển trạng thái từ [Khởi tạo] sang [Đang giao hàng]',14),(16,'Cập nhật trạng thái','2026-04-06 09:35:31.545308','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',14),(17,'Cập nhật trạng thái','2026-04-07 10:11:57.001863','admin','Chuyển trạng thái từ [Khởi tạo] sang [Đang giao hàng]',12),(18,'Cập nhật trạng thái','2026-04-07 10:11:58.994523','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',12),(19,'Cập nhật thông tin','2026-04-07 16:12:06.032820','admin','Thay đổi thông tin chi tiết đơn hàng',13),(20,'Cập nhật thông tin','2026-04-07 21:18:22.679904','admin','Thay đổi thông tin chi tiết đơn hàng',13),(21,'Tạo mới đơn hàng','2026-04-07 23:03:04.380085','admin','Khởi tạo đơn hàng thành công',15),(22,'Xác nhận đơn','2026-04-07 23:03:04.380177','admin','Hệ thống tự động chuyển sang Đã xác nhận',15),(23,'Cập nhật trạng thái','2026-04-07 23:03:12.663074','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',15),(24,'Cập nhật trạng thái','2026-04-07 23:03:14.578208','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',15),(25,'Tạo mới đơn hàng','2026-04-07 23:08:39.383562','admin','Khởi tạo đơn hàng thành công',16),(26,'Xác nhận đơn','2026-04-07 23:08:39.383637','admin','Hệ thống tự động chuyển sang Đã xác nhận',16),(27,'Cập nhật trạng thái','2026-04-07 23:08:46.919929','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',16),(28,'Cập nhật trạng thái','2026-04-08 18:18:56.308484','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',16),(29,'Tạo mới đơn hàng','2026-04-09 14:24:53.475608','admin','Khởi tạo đơn hàng thành công',17),(30,'Xác nhận đơn','2026-04-09 14:24:53.476200','admin','Hệ thống tự động chuyển sang Đã xác nhận',17),(31,'Cập nhật trạng thái','2026-04-12 15:42:36.792923','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',17),(32,'Cập nhật trạng thái','2026-04-12 15:42:38.643492','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',17),(33,'Tạo mới đơn hàng','2026-04-13 12:24:55.345281','admin','Khởi tạo đơn hàng thành công',18),(34,'Xác nhận đơn','2026-04-13 12:24:55.345333','admin','Hệ thống tự động chuyển sang Đã xác nhận',18),(35,'Cập nhật thông tin','2026-04-13 13:00:46.578489','admin','Thay đổi thông tin chi tiết đơn hàng',18),(36,'Cập nhật trạng thái','2026-04-13 13:00:51.784529','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',18),(37,'Cập nhật trạng thái','2026-04-13 13:37:37.235711','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',18),(38,'Tạo mới đơn hàng','2026-04-19 22:33:36.690323','admin','Khởi tạo đơn hàng thành công',19),(39,'Xác nhận đơn','2026-04-19 22:33:36.690432','admin','Hệ thống tự động chuyển sang Đã xác nhận',19),(40,'Cập nhật trạng thái','2026-04-19 22:33:43.083982','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',19),(42,'Cập nhật trạng thái','2026-04-22 13:29:55.944724','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',19),(43,'Tạo mới đơn hàng','2026-05-06 22:39:33.302191','admin','Khởi tạo đơn hàng thành công',21),(44,'Xác nhận đơn','2026-05-06 22:39:33.302667','admin','Hệ thống tự động chuyển sang Đã xác nhận',21),(45,'Cập nhật trạng thái','2026-05-06 22:39:40.018703','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',21),(46,'Tạo mới đơn hàng','2026-05-06 22:42:32.903521','admin','Khởi tạo đơn hàng thành công',22),(47,'Xác nhận đơn','2026-05-06 22:42:32.903764','admin','Hệ thống tự động chuyển sang Đã xác nhận',22),(48,'Cập nhật trạng thái','2026-05-06 22:42:38.892057','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',22),(49,'Cập nhật trạng thái','2026-05-06 22:42:41.063040','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',22),(50,'Cập nhật trạng thái','2026-05-09 22:20:28.870460','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',21),(51,'Tạo mới đơn hàng','2026-05-11 16:20:49.998463','admin','Khởi tạo đơn hàng thành công',23),(52,'Xác nhận đơn','2026-05-11 16:20:49.998548','admin','Hệ thống tự động chuyển sang Đã xác nhận',23),(53,'Cập nhật trạng thái','2026-05-11 16:20:56.674799','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',23),(54,'Tạo mới đơn hàng','2026-05-13 16:00:23.187676','admin','Khởi tạo đơn hàng thành công',24),(55,'Xác nhận đơn','2026-05-13 16:00:23.187731','admin','Hệ thống tự động chuyển sang Đã xác nhận',24),(56,'Cập nhật thông tin','2026-05-13 16:01:06.577734','admin','Thay đổi thông tin chi tiết đơn hàng',24),(57,'Cập nhật trạng thái','2026-05-13 16:01:13.531309','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',24),(58,'Cập nhật trạng thái','2026-05-13 16:01:28.997776','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',23),(59,'Tạo mới đơn hàng','2026-05-14 13:52:43.162469','admin','Khởi tạo đơn hàng thành công',25),(60,'Xác nhận đơn','2026-05-14 13:52:43.162523','admin','Hệ thống tự động chuyển sang Đã xác nhận',25),(61,'Cập nhật trạng thái','2026-05-14 15:24:14.650405','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',25),(62,'Cập nhật trạng thái','2026-05-14 15:24:16.810700','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',25),(63,'Cập nhật trạng thái','2026-05-16 15:09:17.324714','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',24),(64,'Tạo mới đơn hàng','2026-05-16 15:12:37.322921','admin','Khởi tạo đơn hàng thành công',26),(65,'Xác nhận đơn','2026-05-16 15:12:37.323037','admin','Hệ thống tự động chuyển sang Đã xác nhận',26),(66,'Cập nhật trạng thái','2026-05-16 15:12:49.909700','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',26),(67,'Cập nhật trạng thái','2026-05-16 15:12:51.701682','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',26),(68,'Tạo mới đơn hàng','2026-05-16 15:15:41.514691','admin','Khởi tạo đơn hàng thành công',27),(69,'Xác nhận đơn','2026-05-16 15:15:41.515677','admin','Hệ thống tự động chuyển sang Đã xác nhận',27),(70,'Tạo mới đơn hàng','2026-05-16 15:18:25.052751','admin','Khởi tạo đơn hàng thành công',28),(71,'Xác nhận đơn','2026-05-16 15:18:25.052859','admin','Hệ thống tự động chuyển sang Đã xác nhận',28),(72,'Cập nhật trạng thái','2026-05-16 15:18:30.415773','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',28),(73,'Tạo mới đơn hàng','2026-05-16 15:20:27.284491','admin','Khởi tạo đơn hàng thành công',29),(74,'Xác nhận đơn','2026-05-16 15:20:27.284554','admin','Hệ thống tự động chuyển sang Đã xác nhận',29),(75,'Cập nhật trạng thái','2026-05-16 15:20:34.985361','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',29),(76,'Tạo mới đơn hàng','2026-05-16 15:22:52.257630','admin','Khởi tạo đơn hàng thành công',30),(77,'Xác nhận đơn','2026-05-16 15:22:52.257722','admin','Hệ thống tự động chuyển sang Đã xác nhận',30),(78,'Cập nhật trạng thái','2026-05-16 15:22:57.983552','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',30),(79,'Tạo mới đơn hàng','2026-05-16 15:25:20.923432','admin','Khởi tạo đơn hàng thành công',31),(80,'Xác nhận đơn','2026-05-16 15:25:20.923506','admin','Hệ thống tự động chuyển sang Đã xác nhận',31),(81,'Cập nhật trạng thái','2026-05-16 15:25:26.489697','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',31),(82,'Cập nhật trạng thái','2026-05-16 15:25:28.239212','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',31),(83,'Tạo mới đơn hàng','2026-05-16 15:27:14.878937','admin','Khởi tạo đơn hàng thành công',32),(84,'Xác nhận đơn','2026-05-16 15:27:14.879456','admin','Hệ thống tự động chuyển sang Đã xác nhận',32),(85,'Cập nhật trạng thái','2026-05-16 15:27:20.389515','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',32),(86,'Tạo mới đơn hàng','2026-05-16 15:29:03.669775','admin','Khởi tạo đơn hàng thành công',33),(87,'Xác nhận đơn','2026-05-16 15:29:03.669827','admin','Hệ thống tự động chuyển sang Đã xác nhận',33),(88,'Cập nhật trạng thái','2026-05-16 15:29:09.064387','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',33),(89,'Cập nhật trạng thái','2026-05-16 15:29:17.157753','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',27),(90,'Tạo mới đơn hàng','2026-05-18 09:22:52.169980','admin','Khởi tạo đơn hàng thành công',34),(91,'Xác nhận đơn','2026-05-18 09:22:52.170644','admin','Hệ thống tự động chuyển sang Đã xác nhận',34),(92,'Cập nhật trạng thái','2026-05-18 09:22:58.690144','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',34),(93,'Cập nhật trạng thái','2026-05-18 09:24:17.966384','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',28),(94,'Cập nhật trạng thái','2026-05-18 13:05:59.639437','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',27),(95,'Tạo mới đơn hàng','2026-05-18 14:56:17.059152','admin','Khởi tạo đơn hàng thành công',35),(96,'Xác nhận đơn','2026-05-18 14:56:17.059219','admin','Hệ thống tự động chuyển sang Đã xác nhận',35),(97,'Cập nhật trạng thái','2026-05-18 15:22:21.442282','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',35),(98,'Cập nhật trạng thái','2026-05-18 15:22:27.020979','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',35),(99,'Cập nhật trạng thái','2026-05-18 15:51:45.959849','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',33),(100,'Tạo mới đơn hàng','2026-05-19 10:07:36.425234','admin','Khởi tạo đơn hàng thành công',36),(101,'Xác nhận đơn','2026-05-19 10:07:36.425305','admin','Hệ thống tự động chuyển sang Đã xác nhận',36),(102,'Cập nhật trạng thái','2026-05-19 10:07:41.659276','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',36),(103,'Cập nhật trạng thái','2026-05-19 10:07:43.045430','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',36),(104,'Tạo mới đơn hàng','2026-05-19 10:10:04.516414','admin','Khởi tạo đơn hàng thành công',37),(105,'Xác nhận đơn','2026-05-19 10:10:04.516633','admin','Hệ thống tự động chuyển sang Đã xác nhận',37),(106,'Cập nhật trạng thái','2026-05-19 10:10:09.952171','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',37),(107,'Cập nhật trạng thái','2026-05-19 10:10:11.636049','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',37),(108,'Tạo mới đơn hàng','2026-05-19 10:12:08.764537','admin','Khởi tạo đơn hàng thành công',38),(109,'Xác nhận đơn','2026-05-19 10:12:08.764577','admin','Hệ thống tự động chuyển sang Đã xác nhận',38),(110,'Cập nhật trạng thái','2026-05-19 10:12:14.920584','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',38),(111,'Cập nhật trạng thái','2026-05-19 13:59:41.317089','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',32),(112,'Cập nhật trạng thái','2026-05-19 15:10:35.426711','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',30),(113,'Cập nhật trạng thái','2026-05-19 15:13:55.988200','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',29),(114,'Cập nhật trạng thái','2026-05-19 19:01:59.871379','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',34),(115,'Tạo mới đơn hàng','2026-05-21 16:44:26.250733','admin','Khởi tạo đơn hàng thành công',39),(116,'Xác nhận đơn','2026-05-21 16:44:26.251663','admin','Hệ thống tự động chuyển sang Đã xác nhận',39),(117,'Cập nhật trạng thái','2026-05-21 16:44:32.068300','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',39),(118,'Tạo mới đơn hàng','2026-05-21 23:07:23.474326','admin','Khởi tạo đơn hàng thành công',40),(119,'Xác nhận đơn','2026-05-21 23:07:23.474383','admin','Hệ thống tự động chuyển sang Đã xác nhận',40),(120,'Cập nhật trạng thái','2026-05-21 23:07:30.126861','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',40),(121,'Cập nhật trạng thái','2026-05-21 23:42:11.264079','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',38),(122,'Cập nhật trạng thái','2026-05-22 13:29:37.483755','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',39),(123,'Cập nhật trạng thái','2026-05-22 14:48:05.241994','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Đã hủy]',40),(124,'Tạo mới đơn hàng','2026-05-25 10:05:55.698854','admin','Khởi tạo đơn hàng thành công',41),(125,'Xác nhận đơn','2026-05-25 10:05:55.699426','admin','Hệ thống tự động chuyển sang Đã xác nhận',41),(126,'Cập nhật trạng thái','2026-05-25 10:06:01.738409','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',41),(127,'Cập nhật trạng thái','2026-05-25 10:06:03.336847','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',41),(128,'Tạo mới đơn hàng','2026-05-25 10:07:56.690862','admin','Khởi tạo đơn hàng thành công',42),(129,'Xác nhận đơn','2026-05-25 10:07:56.690894','admin','Hệ thống tự động chuyển sang Đã xác nhận',42),(130,'Cập nhật trạng thái','2026-05-25 10:08:02.077297','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',42),(131,'Cập nhật trạng thái','2026-05-25 10:08:03.554037','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',42),(132,'Tạo mới đơn hàng','2026-05-25 10:10:20.856114','admin','Khởi tạo đơn hàng thành công',43),(133,'Xác nhận đơn','2026-05-25 10:10:20.856170','admin','Hệ thống tự động chuyển sang Đã xác nhận',43),(134,'Cập nhật trạng thái','2026-05-25 10:10:26.441931','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',43),(135,'Cập nhật trạng thái','2026-05-25 10:10:28.249664','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',43),(136,'Tạo mới đơn hàng','2026-05-25 10:12:35.443895','admin','Khởi tạo đơn hàng thành công',44),(137,'Xác nhận đơn','2026-05-25 10:12:35.443980','admin','Hệ thống tự động chuyển sang Đã xác nhận',44),(138,'Cập nhật trạng thái','2026-05-25 10:12:40.862718','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',44),(139,'Tạo mới đơn hàng','2026-05-25 13:00:13.775149','admin','Khởi tạo đơn hàng thành công',45),(140,'Xác nhận đơn','2026-05-25 13:00:13.775222','admin','Hệ thống tự động chuyển sang Đã xác nhận',45),(141,'Cập nhật trạng thái','2026-06-05 09:23:34.797481','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',45),(142,'Cập nhật trạng thái','2026-06-05 09:23:36.386142','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',45),(143,'Cập nhật trạng thái','2026-06-05 09:23:41.803984','admin','Chuyển trạng thái từ [Đang giao hàng] sang [Hoàn thành]',44),(144,'Tạo mới đơn hàng','2026-06-05 09:26:45.406052','admin','Khởi tạo đơn hàng thành công',46),(145,'Xác nhận đơn','2026-06-05 09:26:45.406097','admin','Hệ thống tự động chuyển sang Đã xác nhận',46),(146,'Cập nhật trạng thái','2026-06-05 09:26:51.157828','admin','Chuyển trạng thái từ [Đã xác nhận] sang [Đang giao hàng]',46);
/*!40000 ALTER TABLE `order_activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_details`
--

DROP TABLE IF EXISTS `order_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `discount` decimal(38,2) DEFAULT NULL,
  `is_custom` bit(1) DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int NOT NULL,
  `serial_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sku` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_price` decimal(38,2) DEFAULT NULL,
  `unit_price` decimal(38,2) NOT NULL,
  `warranty_end_date` datetime(6) DEFAULT NULL,
  `warranty_months` int DEFAULT NULL,
  `warranty_start_date` datetime(6) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `product_id` bigint DEFAULT NULL,
  `cost_price` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjyu2qbqt8gnvno9oe9j2s2ldk` (`order_id`),
  KEY `FK4q98utpd73imf4yhttm3w0eax` (`product_id`),
  CONSTRAINT `FK4q98utpd73imf4yhttm3w0eax` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKjyu2qbqt8gnvno9oe9j2s2ldk` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=144 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_details`
--

LOCK TABLES `order_details` WRITE;
/*!40000 ALTER TABLE `order_details` DISABLE KEYS */;
INSERT INTO `order_details` VALUES (3,NULL,_binary '\0','','AULA F75 Max',1,' WKF75MAXUA251204466','AULAF75Max-GLACIER-BLUE',1282979.00,1282979.00,'2026-10-02 22:31:32.307113',6,'2026-04-02 22:31:32.307075',1,2,1059753.00),(7,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,' WKF75DA251006718','AULAF75-GLACIER-BLUE-REAPER',1005665.00,1005665.00,'2026-10-02 22:38:21.248399',6,'2026-04-02 22:38:21.248380',2,1,699814.87),(10,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,' WKF75DA251006716','AULAF75-GLACIER-BLUE-REAPER',1056439.00,1056439.00,'2026-10-02 22:40:20.641089',6,'2026-04-02 22:40:20.641058',3,1,699814.87),(13,NULL,_binary '\0','','Leobog Hi75C Pro - Astronaut - StrawBerry Mint',1,'W3Hi75YH260300138','LeobogHi75CPro-0C82',1333030.00,1333030.00,'2026-10-02 22:44:12.083438',6,'2026-04-02 22:44:12.083420',4,4,860000.00),(16,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,' WKF75DA260205817','AULAF75-GLACIER-BLUE-REAPER',924735.00,924735.00,'2026-10-02 22:48:16.115906',6,'2026-04-02 22:48:16.115880',5,1,699814.87),(19,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,'WKF75DA260206220','AULAF75-GLACIER-BLUE-REAPER',924735.00,924735.00,'2026-10-02 22:52:11.360916',6,'2026-04-02 22:52:11.360889',6,1,699814.87),(25,NULL,_binary '\0','','AULA F108 Pro - Grey Yellow - Reaper',1,NULL,'AULAF108Pro-GREY-YELLOW-REAPER',1349230.00,1349230.00,'2026-10-02 22:59:38.331786',6,'2026-04-02 22:59:38.331771',9,5,850000.00),(27,NULL,_binary '\0','','AULA F75 - Snow Fir Green - Reaper',1,'WKF75ZA260200615','AULAF75-SNOW-FIR-GREEN-REAPER',907033.00,907033.00,'2026-10-02 22:59:47.166845',6,'2026-04-02 22:59:47.166821',8,1,720586.50),(34,NULL,_binary '\0','','AULA F75 - Snow Fir Green - Reaper',1,'WKF75ZA251001764','AULAF75-SNOW-FIR-GREEN-REAPER',907033.00,907033.00,'2026-10-03 23:48:52.114011',6,'2026-04-03 23:48:52.113977',10,1,720586.50),(38,NULL,_binary '\0','','AULA F75 - Snow Fir Green - Reaper',1,'WKF75ZA251001762','AULAF75-SNOW-FIR-GREEN-REAPER',918942.00,918942.00,'2026-10-03 23:48:52.114011',6,'2026-04-03 23:48:52.113977',11,1,720586.50),(44,NULL,_binary '\0','','AULA NOVA75 - Galaxy Gray (Pro) - Star Vector (Seiya)',1,'WKNOVA75GA260100950','AULANOVA75-GALAXY-GRAY-PRO-STAR-VECTOR-SEIYA',1115255.00,1115255.00,'2026-10-06 09:35:31.545257',6,'2026-04-06 09:35:31.545236',14,3,837500.00),(46,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,'WKF75DA251006717','AULAF75-GLACIER-BLUE-REAPER',936075.00,936075.00,'2026-10-07 10:11:58.994427',6,'2026-04-07 10:11:58.994378',12,1,699814.87),(48,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,'WKF75DA251006715','AULAF75-GLACIER-BLUE-REAPER',936075.00,936075.00,'2026-10-06 09:35:31.545257',6,'2026-04-06 09:35:31.545236',13,1,699814.87),(51,NULL,_binary '\0','','AULA F75 - Snow Fir Green - Reaper',1,'WKF75ZA260100664 ','AULAF75-SNOW-FIR-GREEN-REAPER',907033.00,907033.00,'2026-10-07 23:03:14.560364',6,'2026-04-07 23:03:14.560338',15,1,720586.50),(54,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,'WKF75DA260206238','AULAF75-GLACIER-BLUE-REAPER',918157.00,918157.00,'2026-10-08 18:18:56.280491',6,'2026-04-08 18:18:56.280452',16,1,699814.87),(57,NULL,_binary '\0','','AULA F75 - Thunder Black - Grey Wood V3',1,'WKF75CA251008656','AULAF75-THUNDER-BLACK-GREY-WOOD-V3',1116875.00,1116875.00,'2026-10-09 18:18:56.280491',6,'2026-04-09 18:18:56.280452',17,1,1017363.00),(61,NULL,_binary '\0','','AULA NOVA75 - Galaxy Gray (Pro) - Star Vector (Seiya)',1,'WKNOVA75GA260100944','AULANOVA75-GALAXY-GRAY-PRO-STAR-VECTOR-SEIYA',1073845.00,1073845.00,'2026-10-13 18:18:56.280491',6,'2026-04-13 18:18:56.280452',18,3,837500.00),(65,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,'WKF75DA260206032','AULAF75-GLACIER-BLUE-REAPER',831075.00,831075.00,'2026-10-22 13:29:55.886811',6,'2026-04-22 13:29:55.886753',19,1,699814.87),(70,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,'WKF75DA260102768','AULAF75-GLACIER-BLUE-REAPER',819630.00,819630.00,'2026-11-06 22:42:41.026692',6,'2026-05-06 22:42:41.026666',22,1,699814.87),(71,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,'WKF75DA260207934','AULAF75-GLACIER-BLUE-REAPER',819630.00,819630.00,'2026-11-09 22:20:28.820578',6,'2026-05-09 22:20:28.820567',21,1,699814.87),(77,NULL,_binary '\0','','Leobog Hi75C Pro - Milky Brown - Turquoise',1,'HI75CccC','LeobogHi75CPro-MILKY-BROWN-TURQUOISE',1105615.00,1105615.00,'2026-11-13 16:01:28.982184',6,'2026-05-13 16:01:28.982116',23,4,911932.00),(80,NULL,_binary '\0','','AULA SC580 - Đen',1,'WMSC580BW251208247','AULASC580-ĐEN',345060.00,345060.00,'2026-11-14 15:24:16.780500',6,'2026-05-14 15:24:16.780448',25,6,266319.00),(81,NULL,_binary '\0','','AULA F75 - Glacier Blue - Reaper',1,'WKF75DA251006719','AULAF75-GLACIER-BLUE-REAPER',808290.00,808290.00,'2026-11-16 15:09:17.197544',6,'2026-05-16 15:09:17.197443',24,1,699814.87),(84,NULL,_binary '\0','','AULA F75 - Thunder Black - Grey Wood V3',1,'WKF75CA251008654','AULAF75-THUNDER-BLACK-GREY-WOOD-V3',1036018.00,1036018.00,'2026-11-16 15:12:51.683731',6,'2026-05-16 15:12:51.683709',26,1,1017363.00),(94,NULL,_binary '\0','','AULA F75 - Snow Fir Green - Reaper',1,'WKF75ZA260201134','AULAF75-SNOW-FIR-GREEN-REAPER',792806.00,792806.00,'2026-11-16 15:25:28.221835',6,'2026-05-16 15:25:28.221794',31,1,721010.00),(102,NULL,_binary '\0','','Leobog Hi75C Pro - Astronaut - StrawBerry Mint',1,'W3Hi75YH251200569','LeobogHi75CPro-0C82',1101544.00,1101544.00,'2026-11-18 09:24:17.943979',6,'2026-05-18 09:24:17.943918',28,4,996763.00),(103,NULL,_binary '\0','','AULA S75 Pro - Glacier Blue - Seiya (Star Vector)',1,'havanhoa0105','AULA-S75-Pro-GLACIER-BLUE-SEIYA-(STAR-VECTOR)',924420.00,924420.00,'2026-11-18 13:05:59.618424',6,'2026-05-18 13:05:59.618368',27,8,784686.00),(106,NULL,_binary '\0','','AULA SC580 - Đen',1,'trongkiler','AULASC580-ĐEN',345210.00,345210.00,'2026-08-18 15:22:27.002723',3,'2026-05-18 15:22:27.002711',35,6,266319.00),(107,NULL,_binary '\0','','AULA NOVA75 - Galaxy Gray (Pro) - Caramel Latte',1,'nvuhoanganh','AULANOVA75-GALAXY-GRAY-PRO-CARAMEL-LATTE',1166680.00,1166680.00,'2026-11-18 15:51:45.941024',6,'2026-05-18 15:51:45.940963',33,3,1017971.00),(110,NULL,_binary '\0','','AULA SC580SE - Trắng',1,'nhathatnha','AULA-SC580SE-TRANG',266170.00,266170.00,'2026-08-19 10:07:43.039531',3,'2026-05-19 10:07:43.039516',36,9,190870.00),(113,NULL,_binary '\0','','AULA S75 Pro - Glacier Blue - Seiya (Star Vector)',1,'WKS75ProTA260103393','AULA-S75-Pro-GLACIER-BLUE-SEIYA-(STAR-VECTOR)',962890.00,962890.00,'2026-11-19 10:10:11.624087',6,'2026-05-19 10:10:11.621574',37,8,784686.00),(116,NULL,_binary '\0','','AULA SC580SE - Đen',1,'ngdinnguynvithong218','AULA-SC580SE-ĐEN',266350.00,266350.00,'2026-08-19 13:59:41.292971',3,'2026-05-19 13:59:41.292640',32,9,190870.00),(117,NULL,_binary '\0','','AULA F87 Pro V2 - White Contours - Begonia',1,'0914194972','AULA-F87-PRO-V2-WHITE-CONTOURS-BEGONIA',1105615.00,1105615.00,'2026-11-19 15:10:35.411690',6,'2026-05-19 15:10:35.411679',30,10,911932.00),(118,NULL,_binary '\0','','AULA SC580SE - Đen',1,' muadoshoppe123','AULA-SC580SE-ĐEN',258608.00,258608.00,'2026-08-19 15:13:55.959024',3,'2026-05-19 15:13:55.956774',29,9,190870.00),(119,NULL,_binary '\0','','AULA SC580SE - Trắng',1,'ff61fsh_45','AULA-SC580SE-TRANG',266350.00,266350.00,'2026-08-19 19:01:59.732819',3,'2026-05-19 19:01:59.732699',34,9,190870.00),(124,NULL,_binary '\0','','AULA SC580SE - Đen',1,'WMSC580SEBW25101094','AULA-SC580SE-ĐEN',266350.00,266350.00,'2026-08-21 23:42:11.245189',3,'2026-05-21 23:42:11.245161',38,9,190870.00),(125,NULL,_binary '\0','','Leobog Hi75C Pro - Astronaut - StrawBerry Mint',1,'W3Hi75YH251200632','LeobogHi75CPro-0C82',1101544.00,1101544.00,'2026-11-22 13:29:37.462055',6,'2026-05-22 13:29:37.461983',39,4,996763.00),(126,NULL,_binary '\0','','AULA S75 Pro - Glacier Blue - Seiya (Star Vector)',1,'WKS75ProTA260103399','AULA-S75-Pro-GLACIER-BLUE-SEIYA-(STAR-VECTOR)',963130.00,963130.00,NULL,6,NULL,40,8,784686.00),(129,NULL,_binary '\0','','AULA SC580 - Đen',1,'WMSC580BW260100055','AULASC580-ĐEN',343590.00,343590.00,'2026-08-25 10:06:03.319898',3,'2026-05-25 10:06:03.319876',41,6,266319.00),(132,NULL,_binary '\0','','Leobog Hi75C Pro - Astronaut - StrawBerry Mint',1,'W3Hi75YH251200566','LeobogHi75CPro-0C82',1086264.00,1086264.00,'2026-11-25 10:08:03.541492',6,'2026-05-25 10:08:03.541480',42,4,996763.00),(135,NULL,_binary '\0','','AULA SC580SE - Trắng',1,'WMSC580SEPW260126742','AULA-SC580SE-TRANG',248771.00,248771.00,'2026-08-25 10:10:28.234399',3,'2026-05-25 10:10:28.234375',43,9,190870.00),(140,NULL,_binary '\0','','Leobog Hi75C Pro - Astronaut - StrawBerry Mint',1,NULL,'LeobogHi75CPro-0C82',1000000.00,1000000.00,NULL,0,NULL,45,4,996763.00),(141,NULL,_binary '\0','','Leobog Hi75C Pro - Contour Black - Turquoise',1,'B2HI75CP260100060','LeobogHi75CPro-3BB0',949550.00,949550.00,'2026-12-05 09:23:41.792564',6,'2026-06-05 09:23:41.792540',44,4,750000.00),(143,NULL,_binary '\0','','AULA SC580 - Đen',1,'WMSC580BW260100053','AULASC580-ĐEN',344940.00,344940.00,NULL,3,NULL,46,6,266319.00);
/*!40000 ALTER TABLE `order_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount_paid` decimal(38,2) DEFAULT NULL,
  `branch_id` bigint DEFAULT NULL,
  `cod_amount` decimal(38,2) DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `discount_amount` decimal(38,2) DEFAULT NULL,
  `expected_delivery_date` date DEFAULT NULL,
  `invoice_company_address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `invoice_company_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `invoice_tax_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `order_code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payment_method` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `reference_code` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `sales_channel_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ship_from_branch_id` bigint DEFAULT NULL,
  `ship_weight` double DEFAULT NULL,
  `shipping_address` text COLLATE utf8mb4_unicode_ci,
  `shipping_fee` decimal(38,2) DEFAULT NULL,
  `shipping_partner` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipping_type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `tracking_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKdhk2umg8ijjkg4njg6891trit` (`order_code`),
  KEY `FKpxtb8awmi0dk6smoh2vp1litg` (`customer_id`),
  CONSTRAINT `FKpxtb8awmi0dk6smoh2vp1litg` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=47 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,1282979.00,1,1282979.00,'2026-03-15 10:47:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775143880191-7DE8','TRANSFER','PAID','26031574S25MU0','SHOPEE',NULL,0,'',0.00,NULL,'PLATFORM','COMPLETED',1282979.00,'VN2687458043422',1),(2,1005665.00,1,1005665.00,'2026-03-15 16:18:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775144264210-6068','TRANSFER','PAID','2603157Q8PGNRJ','SHOPEE',NULL,0,'',0.00,NULL,'PLATFORM','COMPLETED',1005665.00,'VN261740741009Q',2),(3,1056439.00,1,1056439.00,'2026-03-16 22:40:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775144409844-57C4','CASH','PAID','2603169MNXN8X0','SHOPEE',NULL,0,'',0.00,NULL,'PLATFORM','COMPLETED',1056439.00,'169945049',3),(4,1333030.00,1,1333030.00,'2026-03-26 00:09:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775144642622-CBAB','CASH','PAID','2603264JXCPQMY','SHOPEE',NULL,0,'',0.00,NULL,'PLATFORM','COMPLETED',1333030.00,'VN265334154928P',4),(5,924735.00,1,924735.00,'2026-03-25 11:09:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775144886143-8E42','CASH','PAID','26032537BB5J6K','SHOPEE',NULL,0,'Tháp mẫn xá, Xã Văn Môn, Huyện Yên Phong, Bắc Ninh',0.00,NULL,'PLATFORM','COMPLETED',924735.00,'SPXVN061985351623',5),(6,924735.00,1,924735.00,'2026-03-30 12:10:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775145120806-E53D','CASH','PAID','260330FW2ETBNK','SHOPEE',NULL,0,'Toà Nhà Big Solution, 46 Đ: Số 23, Khu Đô Thị Thành Phố Giao Lưu, Bắc Từ Liêm, Phường Cổ Nhuế 2, Quận Bắc Từ Liêm, Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',924735.00,'VN260081186760V',6),(8,907033.00,1,907033.00,'2026-03-28 19:01:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775145443292-64F2','TRANSFER','PAID','260328BK48DAS2','SHOPEE',NULL,0,'Số 318, Đê La Thanh, SN3, Phường Ô Chợ Dừa, Quận Đống Đa, Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',907033.00,'2679IMTZ',8),(9,1349230.00,1,1349230.00,'2026-03-30 18:50:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775145565197-E1D0','CASH','PAID','260330GKDP18QM','SHOPEE',NULL,0,'Phường Quảng Yên, Thị Xã Quảng Yên, Quảng Ninh',0.00,NULL,'PLATFORM','COMPLETED',1349230.00,'SPXVN064432810833',9),(10,919777.00,1,919777.00,'2026-04-03 17:17:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775211580671-C864','TRANSFER','PAID','260403TB9WEYG5','SHOPEE',NULL,0,'Căn hộ 1506, Toà nhà 902 chung cư Starlake, Khu đô thị Tây Hồ Tây, Phường Cổ Nhuế 1, Quận Bắc Từ Liêm, Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',907033.00,'VN265335919146M',10),(11,935942.00,1,935942.00,'2026-04-03 17:20:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775211700329-A0D7','TRANSFER','PAID','260403TCNU5PA9','SHOPEE',NULL,0,'Gia Lâm- Hà Nội, Xã Kim Lan, Huyện Gia Lâm, Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',918942.00,'VN269589496953M',11),(12,936075.00,1,936075.00,'2026-04-05 08:55:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775397561695-E577','CASH','PAID','2604051HJNUXSJ','SHOPEE',NULL,0,'67 Vùi Thị Xuân, Phường An Hải, Thành phố Đà Nẵng',0.00,NULL,'PLATFORM','COMPLETED',936075.00,'SPXVN061680230604',12),(13,936075.00,1,936075.00,'2026-04-04 18:29:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775397760742-D331','CASH','PAID','2604040182VBG3','SHOPEE',NULL,0,'Số 8, ngõ 20 Văn Cao, Phường Gia Viên, Thành phố Hải Phòng',0.00,NULL,'PLATFORM','COMPLETED',936075.00,'SPXVN069116343454',13),(14,1115255.00,1,1115255.00,'2026-04-06 08:44:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775440014251-9A2C','CASH','PAID','2604052X1MYMYG','SHOPEE',NULL,0,'36A Dịch Vọng Hậu, Phường Dịch Vọng Hậu, Quận Cầu Giấy, Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',1115255.00,'VN261121351176R',14),(15,907033.00,1,907033.00,'2026-03-31 20:09:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775577784201-D832','CASH','PAID','260331K5D6XB7K','SHOPEE',NULL,0,'Số 3, Ngõ 115 Phố Nguyễn Văn Trỗi, Phường Phương Liệt, Quận Thanh Xuân, Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',907033.00,' IN-2-0IIDDVDFFL5JQ1AI9194',7),(16,918157.00,1,918157.00,'2026-04-07 09:47:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775578119335-125B','CASH','PAID','2604076NF3S7E6','SHOPEE',NULL,0,'72 Thiên Đức, Phường Vệ An, Thành Phố Bắc Ninh, Bắc Ninh',0.00,NULL,'PLATFORM','COMPLETED',918157.00,'SPXVN062168178654',15),(17,1116875.00,1,1116875.00,'2026-04-09 14:16:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1775719493394-BE8B','CASH','PAID','260409C4XB7MMN','SHOPEE',1,0,'Cty May Sông Hồng, 105 Nguyễn Đức\nThuận, Phường Thống Nhất, Thành\nPhố Nam Định, Nam Định',0.00,NULL,'PLATFORM','COMPLETED',1116875.00,'SPXVN060598622374',16),(18,1073845.00,1,1075465.00,'2026-04-13 12:22:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1776057895287-F5A5','CASH','PAID','260413NWNFT6YJ','SHOPEE',1,0,'90 Giảng Võ, Phường Cát Linh, Quận Đống Đa, Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',1073845.00,'VN260689823356C',17),(19,831075.00,1,831075.00,'2026-04-19 05:30:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1776612816510-AD00','CASH','PAID','2604197A9PKEEY','SHOPEE',1,0,'D8/16D khu phố 4, Thị Trấn Tân Túc, Huyện Bình Chánh, TP. Hồ Chí Minh',0.00,NULL,'PLATFORM','COMPLETED',831075.00,'SPXVN062396016414',18),(21,819630.00,1,819630.00,'2026-05-04 21:23:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778081973144-0EEC','CASH','PAID','260504H6CPFW30','SHOPEE',1,0,'dương đông- tp phú quốc - kien giang, Đặc khu Phú Quốc, Tỉnh An Giang',0.00,NULL,'PLATFORM','COMPLETED',819630.00,'GYWXMBUG',19),(22,819630.00,1,819630.00,'2026-05-04 22:40:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778082152861-10C9','CASH','PAID','260504HAFF8NK5','SHOPEE',1,0,'Star Tower Số 68, Duong Dương Đình Nghệ, Phường Cầu Giấy, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',819630.00,'SPXVN060173605135',20),(23,1105615.00,1,1105615.00,'2026-05-10 16:16:00.000000',0.00,NULL,NULL,NULL,NULL,'Đơn quên ko chụp số serial sản phẩm để bảo hành','DH1778491249917-9E0F','TRANSFER','PAID','2605103290J3QP','SHOPEE',1,0,'Chung cư Lucky Bắc Hà, số 30, Phạm Văn Đồng, Phường Nghĩa Đô, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',1105615.00,'SPXVN063392038325',21),(24,808290.00,1,0.00,'2026-05-12 15:58:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778662823037-502E','TRANSFER','PAID','2605128D8JBMKK','SHOPEE',1,0,'Nhà số 2, ngõ 56 phố Hương Viên, quận Hai Bà Trưng, Phường Hai Bà Trưng, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',808290.00,'SPXVN060373542205',22),(25,345060.00,1,345060.00,'2026-05-14 13:49:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778741563073-6C2C','TRANSFER','PAID','260514CPDQQJ1N','SHOPEE',1,0,'The Dewey Schools Starlake Tây Hồ Tây , Cổng 1, Lô H3, Khu Đô Thị Starlake Tây Hồ Tây, , Phường Xuân Đỉnh, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',345060.00,'VN264357780779W',23),(26,1036018.00,1,1036018.00,'2026-05-15 15:10:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778919157290-AF19','CASH','PAID','260515FM2Q4M96','SHOPEE',1,0,'Nguyệt Quế 21-48 Vinhomes Riverside, Phường Phúc Lợi, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',1036018.00,'IN-2-0IIDDVDFFLBV7CEIUJ77',24),(27,924420.00,1,924420.00,'2026-05-15 15:14:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778919341487-FA51','CASH','PAID','260515F0S2557Y','SHOPEE',1,0,'Ngã tư Tân Trà, Trương Đăng Quế, Phường Ngũ Hành Sơn, Thành phố Đà Nẵng',0.00,NULL,'PLATFORM','COMPLETED',924420.00,'SPXVN067734124955',25),(28,1101544.00,1,1101544.00,'2026-05-15 15:15:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778919505024-3955','CASH','PAID','260515FMEC0YU3','SHOPEE',1,0,'Đền Bùng, Số Nhà 24, Xã Tây Phương, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',1101544.00,'SPXVN064953096985',26),(29,258608.00,1,258608.00,'2026-05-15 15:18:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778919627250-5A19','CASH','PAID','260515FQDG4M78','SHOPEE',1,0,'16/23 đường số 18, Phường Thông Tây Hội, Thành phố Hồ Chí Minh',0.00,NULL,'PLATFORM','COMPLETED',258608.00,'SPXVN067393711215',27),(30,1105615.00,1,1105615.00,'2026-05-15 15:20:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778919772229-CF2B','CASH','PAID','260515FU0EDJ6H','SHOPEE',1,0,'49/4 Khúc Thừa Dụ, Phường Long Xuyên, Tỉnh An Giang',0.00,NULL,'PLATFORM','COMPLETED',1105615.00,'SPXVN069629303985',28),(31,792806.00,1,792806.00,'2026-05-15 15:23:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778919920878-6603','CASH','PAID','260515FKCCU6KN','SHOPEE',1,0,'106 Trần Phú, Phường Hà Đông, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',792806.00,'VN269052389666V',29),(32,266350.00,1,266350.00,'2026-05-15 15:25:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778920034834-4818','CASH','PAID','260515G112XN2T','SHOPEE',1,0,'QL26, Thôn 1, Xã Cư Prao, Tỉnh Đắk Lắk',0.00,NULL,'PLATFORM','COMPLETED',266350.00,'SPXVN066361315065',30),(33,1166680.00,1,1166680.00,'2026-05-15 15:27:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1778920143631-EFB6','CASH','PAID','260515G5269833','SHOPEE',1,0,'Số 220, Trần Não An Khánh, Phường An Khánh, Thành phố Hồ Chí Minh',0.00,NULL,'PLATFORM','COMPLETED',1166680.00,'SPXVN068279737275',31),(34,266350.00,1,266350.00,'2026-05-16 09:14:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779070972025-B000','CASH','PAID','260517JVFC944G','SHOPEE',1,0,'Ktx Đại Học Dược, Số 1a, Thọ Lão, Phường Hai Bà Trưng, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',266350.00,'SPXVN068765102365',32),(35,345210.00,1,345210.00,'2026-05-18 14:54:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779090977014-3C70','CASH','PAID','260518PVTWCM28','SHOPEE',1,0,'Tòa Nhà Vinaphone, Số 811, Đường\nGiải Phóng, , Phường Hoàng Mai,\nThành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',345210.00,'IN-2-0IIDDVDFFLH14OK2RFU5',33),(36,266170.00,1,266170.00,'2026-05-18 10:05:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779160056365-6C2C','TRANSFER','PAID','260518QB47FTJ4','SHOPEE',1,0,'Số 68, Đ . Lê Văn Lương, Phường Thanh Xuân, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',266170.00,'01KRXD4M3T3178YJ448MAGQDKN',34),(37,962890.00,1,962890.00,'2026-05-18 10:07:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779160204491-B063','CASH','PAID','260518QHA3X33S','SHOPEE',1,0,'Lankmark 72, phường Yên Hòa, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',962890.00,'VN263123065525L',35),(38,266350.00,1,266350.00,'2026-05-19 10:10:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779160328733-4724','CASH','PAID','260519QYNQGKFT','SHOPEE',1,0,'Số 322, Đường 23/8, Phường Bạc Liêu, Tỉnh Cà Mau',0.00,NULL,'PLATFORM','COMPLETED',266350.00,'SPXVN066362275995',36),(39,1101544.00,1,1101544.00,'2026-05-20 16:42:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779356666124-33C5','CASH','PAID','260520UVTRVGDH','SHOPEE',1,0,'77 Lê Hồng Phong, Phường Chũ, Tỉnh Bắc Ninh',0.00,NULL,'PLATFORM','COMPLETED',1101544.00,'GYTBTFWD',37),(40,0.00,1,963130.00,'2026-05-21 23:04:00.000000',0.00,NULL,NULL,NULL,NULL,'Hủy do: Khách từ chối nhận hàng','DH1779379643431-72F0',NULL,'UNPAID','2605211ACAWYRW','SHOPEE',1,0,'Toà nhà CT11, Kim Văn,  Phường Định Công, Thành phố Hà Nội',0.00,NULL,'PLATFORM','CANCELLED',963130.00,'SPXVN061365839225',38),(41,343590.00,1,343590.00,'2026-05-22 10:04:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779678355516-07C0','TRANSFER','PAID','2605234T147C5V','SHOPEE',1,0,'Toà nhà A, Toà Nhà số 82, Nguyễn Tuân, Phường Thanh Xuân, Thành phố Hà Nội',0.00,NULL,'PLATFORM','COMPLETED',343590.00,'01KS981B0HF0M8FYABP61NAXJT',39),(42,1086264.00,1,1086264.00,'2026-05-23 10:06:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779678476651-5984','CASH','PAID','2605234WCP0KWA','SHOPEE',1,0,'102 Quang Trung, Xã Đại Lộc, Thành phố Đà Nẵng',0.00,NULL,'PLATFORM','COMPLETED',1086264.00,'SPXVN063591121425',40),(43,248771.00,1,248771.00,'2026-05-23 10:08:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779678620822-2827','CASH','PAID','2605235UGGV6TF','SHOPEE',1,0,'Số 531b, Trần Nhân Tông, Tổ 24, Phường Nam Định, Tỉnh Ninh Bình',0.00,NULL,'PLATFORM','COMPLETED',248771.00,'SPXVN062482900065',41),(44,949550.00,1,949550.00,'2026-05-24 10:10:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779678755411-461F','CASH','PAID','2605248PS4VYT0','SHOPEE',1,0,'Đường 1, Khu Dân Cư Hai Lai, Phòng Trọ Số 4, Xã Châu Thành, Tỉnh An Giang',0.00,NULL,'PLATFORM','COMPLETED',949550.00,'SPXVN062683809975',42),(45,1000000.00,1,1000000.00,'2026-05-25 12:58:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1779688813747-E1AB','TRANSFER','PAID',NULL,'SHOPEE',1,0,'11 DUy Tân, Cầu Giấy, Hà Nội',0.00,'GRAB','MANUAL','COMPLETED',1000000.00,'',43),(46,0.00,1,344940.00,'2026-06-05 09:23:00.000000',0.00,NULL,NULL,NULL,NULL,'','DH1780626405352-D1FA',NULL,'UNPAID','26060484T2TBX1','SHOPEE',1,0,'Ấp Ô Tung A, Xã Cầu Kè, Tỉnh Vĩnh Long',0.00,NULL,'PLATFORM','SHIPPING',344940.00,'SPXVN068235435826',44);
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pricing_drafts`
--

DROP TABLE IF EXISTS `pricing_drafts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pricing_drafts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `draft_data` longtext COLLATE utf8mb4_unicode_ci,
  `updated_at` datetime(6) DEFAULT NULL,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfc4uqpf92t3ruew7jtu86ufhf` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pricing_drafts`
--

LOCK TABLES `pricing_drafts` WRITE;
/*!40000 ALTER TABLE `pricing_drafts` DISABLE KEYS */;
INSERT INTO `pricing_drafts` VALUES (2,'{\"tyGia\":3990,\"tongShip\":0,\"tongKhac\":840533,\"items\":[{\"name\":\"Leobog Hi75C Pro - Milky Brown - Turquoise\",\"sku\":\"LeobogHi75CPro-MILKY-BROWN-TURQUOISE\",\"qty\":1,\"priceCyn\":215},{\"name\":\"Leobog Hi75C Pro - Astronaut - StrawBerry Mint\",\"sku\":\"LeobogHi75CPro-0C82\",\"qty\":5,\"priceCyn\":235},{\"name\":\"AULA SC580SE - Đen\",\"sku\":\"AULA-SC580SE-ĐEN\",\"qty\":3,\"priceCyn\":45},{\"name\":\"AULA SC580SE - Trắng\",\"sku\":\"AULA-SC580SE-TRANG\",\"qty\":3,\"priceCyn\":45},{\"name\":\"AULA SC580 - Đen\",\"sku\":\"AULASC580-ĐEN\",\"qty\":3,\"priceCyn\":68},{\"name\":\"AULA SC580 - Trắng\",\"sku\":\"AULASC580-TRANG\",\"qty\":3,\"priceCyn\":68},{\"name\":\"AULA S75 Pro - Glacier Blue - Seiya (Star Vector)\",\"sku\":\"AULA-S75-Pro-GLACIER-BLUE-SEIYA-(STAR-VECTOR)\",\"qty\":10,\"priceCyn\":185},{\"name\":\"AULA F108 Pro - Grey Yellow - Reaper\",\"sku\":\"AULAF108Pro-GREY-YELLOW-REAPER\",\"qty\":2,\"priceCyn\":245},{\"name\":\"AULA F108 Pro - Glacier Blue - Caramel Latte\",\"sku\":\"AULAF108Pro-GLACIER-BLUE-CARAMEL-LATTE\",\"qty\":3,\"priceCyn\":255},{\"name\":\"AULA F108 Pro - Mocha Mousse - Caramel Latte\",\"sku\":\"AULAF108Pro-MOCHA-MOUSSE-CARAMEL-LATTE\",\"qty\":2,\"priceCyn\":255},{\"name\":\"AULA F75 - Snow Fir Green - Reaper\",\"sku\":\"AULAF75-SNOW-FIR-GREEN-REAPER\",\"qty\":8,\"priceCyn\":170},{\"name\":\"AULA F75 Max - Glacier Blue\",\"sku\":\"AULAF75Max-GLACIER-BLUE\",\"qty\":2,\"priceCyn\":225},{\"name\":\"AULA NOVA75 - Snow Moon White - Ice Shadow\",\"sku\":\"AULANOVA75-SNOW-MOON-WHITE-ICE-SHADOW\",\"qty\":1,\"priceCyn\":210},{\"name\":\"AULA NOVA75 - Galaxy Gray (Pro) - Caramel Latte\",\"sku\":\"AULANOVA75-GALAXY-GRAY-PRO-CARAMEL-LATTE\",\"qty\":5,\"priceCyn\":240},{\"name\":\"AULA NOVA75 - Night Shadow (Pro) - Caramel Latte\",\"sku\":\"AULANOVA75-NIGHT-SHADOW-PRO-CARAMEL-LATTE\",\"qty\":5,\"priceCyn\":240},{\"name\":\"AULA F87 Pro V2 - White Contours - Begonia\",\"sku\":\"AULA-F87-PRO-V2-WHITE-CONTOURS-BEGONIA\",\"qty\":2,\"priceCyn\":215}]}','2026-04-23 16:09:02.471347','admin');
/*!40000 ALTER TABLE `pricing_drafts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_attribute_values`
--

DROP TABLE IF EXISTS `product_attribute_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_attribute_values` (
  `attribute_id` bigint NOT NULL,
  `value` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  KEY `FK1oveqdcusmrm22vlr6m0u2hnn` (`attribute_id`),
  CONSTRAINT `FK1oveqdcusmrm22vlr6m0u2hnn` FOREIGN KEY (`attribute_id`) REFERENCES `product_attributes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_attribute_values`
--

LOCK TABLES `product_attribute_values` WRITE;
/*!40000 ALTER TABLE `product_attribute_values` DISABLE KEYS */;
INSERT INTO `product_attribute_values` VALUES (1,'Đen'),(1,'Trắng'),(2,'Đen'),(2,'Trắng'),(9,'Glacier Blue'),(10,'Glacier Blue'),(10,'Snow Fir Green'),(10,'Thunder Black'),(11,'Reaper'),(11,'Grey Wood V3'),(22,'Astronaut'),(22,'Contour Black'),(22,'Milky Brown'),(23,'Turquoise'),(23,'StrawBerry Mint'),(26,'White Contours'),(26,'Gradient Pink'),(26,'Gradient Black Pink'),(26,'Black Mist'),(26,'Gradient Grey'),(26,'Orange Juice'),(27,'Seiya'),(27,'Strawberry Mint'),(27,'X3'),(27,'Meteor Ice Cream'),(27,'Greywood V4'),(27,'Begonia'),(28,'Grey Yellow'),(28,'Glacier Blue'),(28,'Pink(Side-printed)'),(28,'Grey(Side-printed)'),(28,'White Yellow'),(28,'Mocha Mousse'),(29,'Reaper'),(29,'Caramel Latte'),(29,'Greywood V3'),(29,'Silent Candy'),(29,'Nimbus V3'),(30,'Snow Moon White'),(30,'Galaxy Gray (Pro)'),(30,'Dark Night (Pro)'),(30,'Night Shadow (Pro)'),(31,'Ice Shadow'),(31,'Star Vector (Seiya)'),(31,'Jade'),(31,'Light Feather (Silent)'),(31,'Begonia'),(31,'Caramel Latte');
/*!40000 ALTER TABLE `product_attribute_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_attributes`
--

DROP TABLE IF EXISTS `product_attributes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_attributes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcex46yvx4g18b2pn09p79h1mc` (`product_id`),
  CONSTRAINT `FKcex46yvx4g18b2pn09p79h1mc` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_attributes`
--

LOCK TABLES `product_attributes` WRITE;
/*!40000 ALTER TABLE `product_attributes` DISABLE KEYS */;
INSERT INTO `product_attributes` VALUES (1,'Phân loại',9),(2,'Phân loại',6),(9,'Phân loại',2),(10,'Phân loại',1),(11,'Switch',1),(22,'Phân loại',4),(23,'Switch',4),(26,'Phân loại',10),(27,'Switch',10),(28,'Phân loại',5),(29,'Switch',5),(30,'Phân loại',3),(31,'Switch',3);
/*!40000 ALTER TABLE `product_attributes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_variants`
--

DROP TABLE IF EXISTS `product_variants`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_variants` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cost_price` decimal(15,2) DEFAULT NULL,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price` decimal(15,2) DEFAULT NULL,
  `sku` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `stock_quantity` int DEFAULT NULL,
  `variant_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `product_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKq935p2d1pbjm39n0063ghnfgn` (`sku`),
  KEY `FKosqitn4s405cynmhb87lkvuau` (`product_id`),
  CONSTRAINT `FKosqitn4s405cynmhb87lkvuau` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_variants`
--

LOCK TABLES `product_variants` WRITE;
/*!40000 ALTER TABLE `product_variants` DISABLE KEYS */;
INSERT INTO `product_variants` VALUES (1,699814.87,'https://oms.mechkey.vn/media/02042026/CC63/img_1775125418918.webp',1050000.00,'AULAF75-GLACIER-BLUE-REAPER',11,'Glacier Blue - Reaper',1),(2,0.00,'https://oms.mechkey.vn/media/02042026/9EED/img_1775125421660.webp',1050000.00,'AULAF75-GLACIER-BLUE-GREY-WOOD-V3',0,'Glacier Blue - Grey Wood V3',1),(3,721010.00,'https://oms.mechkey.vn/media/02042026/5D3B/img_1775125425620.webp',1050000.00,'AULAF75-SNOW-FIR-GREEN-REAPER',8,'Snow Fir Green - Reaper',1),(4,0.00,'https://oms.mechkey.vn/media/02042026/90AA/img_1775125428175.webp',1050000.00,'AULAF75-SNOW-FIR-GREEN-GREY-WOOD-V3',0,'Snow Fir Green - Grey Wood V3',1),(5,1017363.00,'https://oms.mechkey.vn/media/02042026/BCB6/img_1775125430380.webp',1250000.00,'AULAF75-THUNDER-BLACK-REAPER',2,'Thunder Black - Reaper',1),(6,1017363.00,'https://oms.mechkey.vn/media/02042026/52BA/img_1775125433108.webp',1250000.00,'AULAF75-THUNDER-BLACK-GREY-WOOD-V3',3,'Thunder Black - Grey Wood V3',1),(7,1007051.00,'https://oms.mechkey.vn/media/02042026/613E/img_1775141316654.webp',1250000.00,'AULAF75Max-GLACIER-BLUE',4,'Glacier Blue',2),(8,807863.00,'https://oms.mechkey.vn/media/02042026/F7D4/img_1775141431595.webp',1250000.00,'AULANOVA75-SNOW-MOON-WHITE-ICE-SHADOW',2,'Snow Moon White - Ice Shadow',3),(9,0.00,'https://oms.mechkey.vn/media/02042026/EED8/img_1775141433978.webp',1250000.00,'AULANOVA75-SNOW-MOON-WHITE-STAR-VECTOR-(SEIYA)',0,'Snow Moon White - Star Vector (Seiya)',3),(10,0.00,'https://oms.mechkey.vn/media/23042026/4B77/img_1776938801803.png',1250000.00,'AULANOVA75-GALAXY-GRAY-PRO-ICE-SHADOW',0,'Galaxy Gray (Pro) - Ice Shadow',3),(11,837500.00,'https://oms.mechkey.vn/media/23042026/5A84/img_1776938794715.png',1250000.00,'AULANOVA75-GALAXY-GRAY-PRO-STAR-VECTOR-SEIYA',0,'Galaxy Gray (Pro) - Star Vector (Seiya)',3),(12,0.00,'https://oms.mechkey.vn/media/02042026/A048/img_1775141642943.webp',1350000.00,'LeobogHi75CPro-B599',0,'Astronaut - Turquoise',4),(13,996763.00,'https://oms.mechkey.vn/media/02042026/B577/img_1775141682401.webp',1350000.00,'LeobogHi75CPro-0C82',1,'Astronaut - StrawBerry Mint',4),(14,750000.00,'https://oms.mechkey.vn/media/02042026/928D/img_1775141696436.webp',1200000.00,'LeobogHi75CPro-3BB0',0,'Contour Black - Turquoise',4),(15,0.00,'https://oms.mechkey.vn/media/02042026/FDDF/img_1775141699304.webp',1200000.00,'LeobogHi75CPro-EBAF',0,'Contour Black - StrawBerry Mint',4),(16,1039179.00,'https://oms.mechkey.vn/media/23042026/9948/img_1776937908292.webp',1350000.00,'AULAF108Pro-GREY-YELLOW-REAPER',2,'Grey Yellow - Reaper',5),(17,266319.00,'https://oms.mechkey.vn/media/02042026/A316/img_1775143078362.webp',450000.00,'AULASC580-ĐEN',1,'Đen',6),(18,288425.00,'https://oms.mechkey.vn/media/02042026/064F/img_1775143097920.webp',450000.00,'AULASC580-TRANG',3,'Trắng',6),(19,0.00,'https://oms.mechkey.vn/media/16042026/D598/img_1776354091757.png',0.00,'AULA-S98-Pro-9F57',0,'Glacier Blue - Caramel Latte',7),(20,784686.00,'https://oms.mechkey.vn/media/16042026/B814/img_1776354325902.jpg',0.00,'AULA-S75-Pro-GLACIER-BLUE-SEIYA-(STAR-VECTOR)',8,'Glacier Blue - Seiya (Star Vector)',8),(21,190870.00,'https://oms.mechkey.vn/media/16042026/62F8/img_1776354847116.webp',0.00,'AULA-SC580SE-ĐEN',0,'Đen',9),(22,190870.00,'https://oms.mechkey.vn/media/16042026/9743/img_1776354850648.jpg',0.00,'AULA-SC580SE-TRANG',0,'Trắng',9),(23,0.00,'https://oms.mechkey.vn/media/23042026/D0DA/img_1776937903863.webp',0.00,'AULAF108Pro-GREY-YELLOW-CARAMEL-LATTE',0,'Grey Yellow - Caramel Latte',5),(24,0.00,'https://oms.mechkey.vn/media/23042026/FCA5/img_1776937910566.webp',0.00,'AULAF108Pro-GREY-YELLOW-GREYWOOD-V3',0,'Grey Yellow - Greywood V3',5),(25,0.00,'https://oms.mechkey.vn/media/23042026/4FD6/img_1776937912943.webp',0.00,'AULAF108Pro-GREY-YELLOW-SILENT-CANDY',0,'Grey Yellow - Silent Candy',5),(26,0.00,'https://oms.mechkey.vn/media/23042026/5A99/img_1776937915359.webp',0.00,'AULAF108Pro-GREY-YELLOW-NIMBUS-V3',0,'Grey Yellow - Nimbus V3',5),(27,0.00,'https://oms.mechkey.vn/media/23042026/6931/img_1776937918585.webp',0.00,'AULAF108Pro-GLACIER-BLUE-REAPER',0,'Glacier Blue - Reaper',5),(28,1081594.00,'https://oms.mechkey.vn/media/23042026/6A2A/img_1776937920655.webp',0.00,'AULAF108Pro-GLACIER-BLUE-CARAMEL-LATTE',3,'Glacier Blue - Caramel Latte',5),(29,0.00,'https://oms.mechkey.vn/media/23042026/A7AF/img_1776937923031.webp',0.00,'AULAF108Pro-GLACIER-BLUE-GREYWOOD-V3',0,'Glacier Blue - Greywood V3',5),(30,0.00,'https://oms.mechkey.vn/media/23042026/1FEF/img_1776937925462.webp',0.00,'AULAF108Pro-GLACIER-BLUE-SILENT-CANDY',0,'Glacier Blue - Silent Candy',5),(31,0.00,'https://oms.mechkey.vn/media/23042026/9FEE/img_1776937927605.webp',0.00,'AULAF108Pro-GLACIER-BLUE-NIMBUS-V3',0,'Glacier Blue - Nimbus V3',5),(32,0.00,'https://oms.mechkey.vn/media/23042026/E223/img_1776937930232.webp',0.00,'AULAF108Pro-PINKSIDE-PRINTED-REAPER',0,'Pink(Side-printed) - Reaper',5),(33,0.00,'https://oms.mechkey.vn/media/23042026/3FA8/img_1776937932763.webp',0.00,'AULAF108Pro-PINKSIDE-PRINTED-CARAMEL-LATTE',0,'Pink(Side-printed) - Caramel Latte',5),(34,0.00,'https://oms.mechkey.vn/media/23042026/6F6A/img_1776937935011.webp',0.00,'AULAF108Pro-PINKSIDE-PRINTED-GREYWOOD-V3',0,'Pink(Side-printed) - Greywood V3',5),(35,0.00,'https://oms.mechkey.vn/media/23042026/A9CA/img_1776937937165.webp',0.00,'AULAF108Pro-PINKSIDE-PRINTED-SILENT-CANDY',0,'Pink(Side-printed) - Silent Candy',5),(36,0.00,'https://oms.mechkey.vn/media/23042026/D322/img_1776937939756.webp',0.00,'AULAF108Pro-PINKSIDE-PRINTED-NIMBUS-V3',0,'Pink(Side-printed) - Nimbus V3',5),(37,0.00,'https://oms.mechkey.vn/media/23042026/C4B7/img_1776937942455.webp',0.00,'AULAF108Pro-GREYSIDE-PRINTED-REAPER',0,'Grey(Side-printed) - Reaper',5),(38,0.00,'https://oms.mechkey.vn/media/23042026/86E6/img_1776937944573.webp',0.00,'AULAF108Pro-GREYSIDE-PRINTED-CARAMEL-LATTE',0,'Grey(Side-printed) - Caramel Latte',5),(39,0.00,'https://oms.mechkey.vn/media/23042026/1D0F/img_1776937946771.webp',0.00,'AULAF108Pro-GREYSIDE-PRINTED-GREYWOOD-V3',0,'Grey(Side-printed) - Greywood V3',5),(40,0.00,'https://oms.mechkey.vn/media/23042026/35B8/img_1776937949194.webp',0.00,'AULAF108Pro-GREYSIDE-PRINTED-SILENT-CANDY',0,'Grey(Side-printed) - Silent Candy',5),(41,0.00,'https://oms.mechkey.vn/media/23042026/FC7F/img_1776937951350.webp',0.00,'AULAF108Pro-GREYSIDE-PRINTED-NIMBUS-V3',0,'Grey(Side-printed) - Nimbus V3',5),(42,0.00,'https://oms.mechkey.vn/media/23042026/6583/img_1776937954383.webp',0.00,'AULAF108Pro-WHITE-YELLOW-REAPER',0,'White Yellow - Reaper',5),(43,0.00,'https://oms.mechkey.vn/media/23042026/924A/img_1776937956994.webp',0.00,'AULAF108Pro-WHITE-YELLOW-CARAMEL-LATTE',0,'White Yellow - Caramel Latte',5),(44,0.00,'https://oms.mechkey.vn/media/23042026/92FC/img_1776937959175.webp',0.00,'AULAF108Pro-WHITE-YELLOW-GREYWOOD-V3',0,'White Yellow - Greywood V3',5),(45,0.00,'https://oms.mechkey.vn/media/23042026/17BF/img_1776937961274.webp',0.00,'AULAF108Pro-WHITE-YELLOW-SILENT-CANDY',0,'White Yellow - Silent Candy',5),(46,0.00,'https://oms.mechkey.vn/media/23042026/0A89/img_1776937964236.webp',0.00,'AULAF108Pro-WHITE-YELLOW-NIMBUS-V3',0,'White Yellow - Nimbus V3',5),(47,0.00,'https://oms.mechkey.vn/media/23042026/15E4/img_1776937967522.jpg',0.00,'AULAF108Pro-MOCHA-MOUSSE-REAPER',0,'Mocha Mousse - Reaper',5),(48,1081594.00,'https://oms.mechkey.vn/media/23042026/21F9/img_1776937970033.jpg',0.00,'AULAF108Pro-MOCHA-MOUSSE-CARAMEL-LATTE',2,'Mocha Mousse - Caramel Latte',5),(49,0.00,'https://oms.mechkey.vn/media/23042026/9C7A/img_1776937972544.jpg',0.00,'AULAF108Pro-MOCHA-MOUSSE-GREYWOOD-V3',0,'Mocha Mousse - Greywood V3',5),(50,0.00,'https://oms.mechkey.vn/media/23042026/899E/img_1776937974871.jpg',0.00,'AULAF108Pro-MOCHA-MOUSSE-SILENT-CANDY',0,'Mocha Mousse - Silent Candy',5),(51,0.00,'https://oms.mechkey.vn/media/23042026/9BD6/img_1776937977162.jpg',0.00,'AULAF108Pro-MOCHA-MOUSSE-NIMBUS-V3',0,'Mocha Mousse - Nimbus V3',5),(52,0.00,'https://oms.mechkey.vn/media/21042026/CFF5/img_1776736168554.webp',0.00,'AULA-F87-PRO-V2-WHITE-CONTOURS-SEIYA',0,'White Contours - Seiya',10),(53,0.00,'https://oms.mechkey.vn/media/21042026/4DAD/img_1776736171704.webp',0.00,'AULA-F87-PRO-V2-WHITE-CONTOURS-STRAWBERRY-MINT',0,'White Contours - Strawberry Mint',10),(54,0.00,'https://oms.mechkey.vn/media/21042026/96E8/img_1776736173940.webp',0.00,'AULA-F87-PRO-V2-WHITE-CONTOURS-X3',0,'White Contours - X3',10),(55,0.00,'https://oms.mechkey.vn/media/21042026/F78F/img_1776736176740.webp',0.00,'AULA-F87-PRO-V2-WHITE-CONTOURS-METEOR-ICE-CREAM',0,'White Contours - Meteor Ice Cream',10),(56,0.00,'https://oms.mechkey.vn/media/21042026/B74F/img_1776736179250.webp',0.00,'AULA-F87-PRO-V2-WHITE-CONTOURS-GREYWOOD-V4',0,'White Contours - Greywood V4',10),(57,0.00,'https://oms.mechkey.vn/media/21042026/8C71/img_1776736186050.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-PINK-SEIYA',0,'Gradient Pink - Seiya',10),(58,0.00,'https://oms.mechkey.vn/media/21042026/B676/img_1776736188498.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-PINK-STRAWBERRY-MINT',0,'Gradient Pink - Strawberry Mint',10),(59,0.00,'https://oms.mechkey.vn/media/21042026/0D9E/img_1776736190579.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-PINK-X3',0,'Gradient Pink - X3',10),(60,0.00,'https://oms.mechkey.vn/media/21042026/E865/img_1776736192297.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-PINK-METEOR-ICE-CREAM',0,'Gradient Pink - Meteor Ice Cream',10),(61,0.00,'https://oms.mechkey.vn/media/21042026/335F/img_1776736194556.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-PINK-GREYWOOD-V4',0,'Gradient Pink - Greywood V4',10),(62,0.00,'https://oms.mechkey.vn/media/21042026/45D8/img_1776736197648.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-BLACK-PINK-SEIYA',0,'Gradient Black Pink - Seiya',10),(63,0.00,'https://oms.mechkey.vn/media/21042026/3664/img_1776736200649.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-BLACK-PINK-STRAWBERRY-MINT',0,'Gradient Black Pink - Strawberry Mint',10),(64,0.00,'https://oms.mechkey.vn/media/21042026/7382/img_1776736202564.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-BLACK-PINK-X3',0,'Gradient Black Pink - X3',10),(65,0.00,'https://oms.mechkey.vn/media/21042026/165B/img_1776736204543.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-BLACK-PINK-METEOR-ICE-CREAM',0,'Gradient Black Pink - Meteor Ice Cream',10),(66,0.00,'https://oms.mechkey.vn/media/21042026/B3FE/img_1776736206759.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-BLACK-PINK-GREYWOOD-V4',0,'Gradient Black Pink - Greywood V4',10),(67,0.00,'https://oms.mechkey.vn/media/21042026/1E46/img_1776736212085.webp',0.00,'AULA-F87-PRO-V2-BLACK-MIST-SEIYA',0,'Black Mist - Seiya',10),(68,0.00,'https://oms.mechkey.vn/media/21042026/9632/img_1776736214395.webp',0.00,'AULA-F87-PRO-V2-BLACK-MIST-STRAWBERRY-MINT',0,'Black Mist - Strawberry Mint',10),(69,0.00,'https://oms.mechkey.vn/media/21042026/D995/img_1776736216264.webp',0.00,'AULA-F87-PRO-V2-BLACK-MIST-X3',0,'Black Mist - X3',10),(70,0.00,'https://oms.mechkey.vn/media/21042026/C585/img_1776736218498.webp',0.00,'AULA-F87-PRO-V2-BLACK-MIST-METEOR-ICE-CREAM',0,'Black Mist - Meteor Ice Cream',10),(71,0.00,'https://oms.mechkey.vn/media/21042026/78B3/img_1776736220652.webp',0.00,'AULA-F87-PRO-V2-BLACK-MIST-GREYWOOD-V4',0,'Black Mist - Greywood V4',10),(72,0.00,'https://oms.mechkey.vn/media/21042026/B157/img_1776736226386.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-GREY-SEIYA',0,'Gradient Grey - Seiya',10),(73,0.00,'https://oms.mechkey.vn/media/21042026/F85B/img_1776736234030.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-GREY-STRAWBERRY-MINT',0,'Gradient Grey - Strawberry Mint',10),(74,0.00,'https://oms.mechkey.vn/media/21042026/D284/img_1776736237001.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-GREY-X3',0,'Gradient Grey - X3',10),(75,0.00,'https://oms.mechkey.vn/media/21042026/8A04/img_1776736239038.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-GREY-METEOR-ICE-CREAM',0,'Gradient Grey - Meteor Ice Cream',10),(76,0.00,'https://oms.mechkey.vn/media/21042026/F176/img_1776736241810.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-GREY-GREYWOOD-V4',0,'Gradient Grey - Greywood V4',10),(77,0.00,'https://oms.mechkey.vn/media/21042026/6C1B/img_1776736251029.webp',0.00,'AULA-F87-PRO-V2-ORANGE-JUICE-SEIYA',0,'Orange Juice - Seiya',10),(78,0.00,'https://oms.mechkey.vn/media/21042026/9BC1/img_1776736253193.webp',0.00,'AULA-F87-PRO-V2-ORANGE-JUICE-STRAWBERRY-MINT',0,'Orange Juice - Strawberry Mint',10),(79,0.00,'https://oms.mechkey.vn/media/21042026/D420/img_1776736255311.webp',0.00,'AULA-F87-PRO-V2-ORANGE-JUICE-X3',0,'Orange Juice - X3',10),(80,0.00,'https://oms.mechkey.vn/media/21042026/E1D5/img_1776736257434.webp',0.00,'AULA-F87-PRO-V2-ORANGE-JUICE-METEOR-ICE-CREAM',0,'Orange Juice - Meteor Ice Cream',10),(81,0.00,'https://oms.mechkey.vn/media/21042026/8AA3/img_1776736259792.webp',0.00,'AULA-F87-PRO-V2-ORANGE-JUICE-GREYWOOD-V4',0,'Orange Juice - Greywood V4',10),(84,0.00,'https://oms.mechkey.vn/media/23042026/5BE1/img_1776938304151.webp',0.00,'AULANOVA75-SNOW-MOON-WHITE-JADE',0,'Snow Moon White - Jade',3),(85,0.00,'https://oms.mechkey.vn/media/23042026/391B/img_1776938316919.webp',0.00,'AULANOVA75-SNOW-MOON-WHITE-LIGHT-FEATHER-SILENT',0,'Snow Moon White - Light Feather (Silent)',3),(86,0.00,'https://oms.mechkey.vn/media/23042026/E60E/img_1776938320043.webp',0.00,'AULANOVA75-SNOW-MOON-WHITE-BEGONIA',0,'Snow Moon White - Begonia',3),(87,0.00,'https://oms.mechkey.vn/media/23042026/51AF/img_1776938322580.webp',0.00,'AULANOVA75-SNOW-MOON-WHITE-CARAMEL-LATTE',0,'Snow Moon White - Caramel Latte',3),(88,0.00,'https://oms.mechkey.vn/media/23042026/9D69/img_1776938779644.png',0.00,'AULANOVA75-GALAXY-GRAY-PRO-JADE',0,'Galaxy Gray (Pro) - Jade',3),(89,0.00,'https://oms.mechkey.vn/media/23042026/A8A7/img_1776938782258.png',0.00,'AULANOVA75-GALAXY-GRAY-PRO-LIGHT-FEATHER-SILENT',0,'Galaxy Gray (Pro) - Light Feather (Silent)',3),(90,0.00,'https://oms.mechkey.vn/media/23042026/0160/img_1776938784489.png',0.00,'AULANOVA75-GALAXY-GRAY-PRO-BEGONIA',0,'Galaxy Gray (Pro) - Begonia',3),(91,1017971.00,'https://oms.mechkey.vn/media/23042026/EAA4/img_1776938786897.png',0.00,'AULANOVA75-GALAXY-GRAY-PRO-CARAMEL-LATTE',4,'Galaxy Gray (Pro) - Caramel Latte',3),(92,0.00,'https://oms.mechkey.vn/media/23042026/49CF/img_1776938440343.jpg',0.00,'AULANOVA75-DARK-NIGHT-PRO-ICE-SHADOW',0,'Dark Night (Pro) - Ice Shadow',3),(93,0.00,'https://oms.mechkey.vn/media/23042026/D324/img_1776938434636.jpg',0.00,'AULANOVA75-DARK-NIGHT-PRO-STAR-VECTOR-SEIYA',0,'Dark Night (Pro) - Star Vector (Seiya)',3),(94,0.00,'https://oms.mechkey.vn/media/23042026/6F7E/img_1776938429150.jpg',0.00,'AULANOVA75-DARK-NIGHT-PRO-JADE',0,'Dark Night (Pro) - Jade',3),(95,0.00,'https://oms.mechkey.vn/media/23042026/A5F4/img_1776938427027.jpg',0.00,'AULANOVA75-DARK-NIGHT-PRO-LIGHT-FEATHER-SILENT',0,'Dark Night (Pro) - Light Feather (Silent)',3),(96,0.00,'https://oms.mechkey.vn/media/23042026/1340/img_1776938424500.jpg',0.00,'AULANOVA75-DARK-NIGHT-PRO-BEGONIA',0,'Dark Night (Pro) - Begonia',3),(97,0.00,'https://oms.mechkey.vn/media/23042026/5A5B/img_1776938421918.jpg',0.00,'AULANOVA75-DARK-NIGHT-PRO-CARAMEL-LATTE',0,'Dark Night (Pro) - Caramel Latte',3),(98,0.00,'https://oms.mechkey.vn/media/23042026/DFE1/img_1776938361953.webp',0.00,'AULANOVA75-NIGHT-SHADOW-PRO-ICE-SHADOW',0,'Night Shadow (Pro) - Ice Shadow',3),(99,0.00,'https://oms.mechkey.vn/media/23042026/D9D4/img_1776938364569.webp',0.00,'AULANOVA75-NIGHT-SHADOW-PRO-STAR-VECTOR-SEIYA',0,'Night Shadow (Pro) - Star Vector (Seiya)',3),(100,0.00,'https://oms.mechkey.vn/media/23042026/FC26/img_1776938366817.webp',0.00,'AULANOVA75-NIGHT-SHADOW-PRO-JADE',0,'Night Shadow (Pro) - Jade',3),(101,0.00,'https://oms.mechkey.vn/media/23042026/B5F2/img_1776938369237.webp',0.00,'AULANOVA75-NIGHT-SHADOW-PRO-LIGHT-FEATHER-SILENT',0,'Night Shadow (Pro) - Light Feather (Silent)',3),(102,0.00,'https://oms.mechkey.vn/media/23042026/9FB8/img_1776938371867.webp',0.00,'AULANOVA75-NIGHT-SHADOW-PRO-BEGONIA',0,'Night Shadow (Pro) - Begonia',3),(103,1017971.00,'https://oms.mechkey.vn/media/23042026/AD1F/img_1776938374303.webp',0.00,'AULANOVA75-NIGHT-SHADOW-PRO-CARAMEL-LATTE',5,'Night Shadow (Pro) - Caramel Latte',3),(104,911932.00,'https://oms.mechkey.vn/media/23042026/9732/img_1776933978939.webp',0.00,'LeobogHi75CPro-MILKY-BROWN-TURQUOISE',0,'Milky Brown - Turquoise',4),(105,0.00,'https://oms.mechkey.vn/media/23042026/76D7/img_1776933981489.webp',0.00,'LeobogHi75CPro-MILKY-BROWN-STRAWBERRY-MINT',0,'Milky Brown - StrawBerry Mint',4),(107,0.00,'https://oms.mechkey.vn/media/21042026/335F/img_1776736194556.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-PINK-BEGONIA',0,'Gradient Pink - Begonia',10),(108,0.00,'https://oms.mechkey.vn/media/21042026/B3FE/img_1776736206759.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-BLACK-PINK-BEGONIA',0,'Gradient Black Pink - Begonia',10),(109,0.00,'https://oms.mechkey.vn/media/21042026/C585/img_1776736218498.webp',0.00,'AULA-F87-PRO-V2-BLACK-MIST-BEGONIA',0,'Black Mist - Begonia',10),(110,0.00,'https://oms.mechkey.vn/media/21042026/F176/img_1776736241810.webp',0.00,'AULA-F87-PRO-V2-GRADIENT-GREY-BEGONIA',0,'Gradient Grey - Begonia',10),(111,0.00,'https://oms.mechkey.vn/media/21042026/9BC1/img_1776736253193.webp',0.00,'AULA-F87-PRO-V2-ORANGE-JUICE-BEGONIA',0,'Orange Juice - Begonia',10),(112,911932.00,'https://oms.mechkey.vn/media/21042026/CFF5/img_1776736168554.webp',0.00,'AULA-F87-PRO-V2-WHITE-CONTOURS-BEGONIA',1,'White Contours - Begonia',10);
/*!40000 ALTER TABLE `product_variants` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avg_import_price` decimal(15,2) DEFAULT NULL,
  `brand` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `condition_status` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `image_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `manage_stock` bit(1) DEFAULT NULL,
  `min_stock_level` int NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price` decimal(15,2) DEFAULT NULL,
  `sku` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `stock_quantity` int NOT NULL,
  `unit` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `warranty_period` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKfhmd06dsmj6k0n90swsh8ie9g` (`sku`),
  KEY `FKog2rp4qthbtt2lfyhfo32lsw9` (`category_id`),
  CONSTRAINT `FKog2rp4qthbtt2lfyhfo32lsw9` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,NULL,'Aula',NULL,'2026-04-02 17:25:04.455634','','https://oms.mechkey.vn/media/02042026/7784/img_1775141519108.webp',_binary '',0,'AULA F75',0.00,'AULAF75',24,'Chiếc','2026-05-16 15:25:28.280065',NULL,1),(2,NULL,'Aula',NULL,'2026-04-02 21:48:56.170173','','https://oms.mechkey.vn/media/02042026/6A83/img_1775141509913.webp',_binary '',0,'AULA F75 Max',0.00,'AULAF75Max',4,'Chiếc','2026-05-09 22:20:06.007423',NULL,1),(3,NULL,'Aula',NULL,'2026-04-02 21:51:13.396687','','https://oms.mechkey.vn/media/02042026/B6C9/img_1775141472036.webp',_binary '',0,'AULA NOVA75',0.00,'AULANOVA75',11,'Chiếc','2026-05-18 15:51:46.008881',NULL,1),(4,NULL,'Leobog',NULL,'2026-04-02 21:55:37.131710','','https://oms.mechkey.vn/media/02042026/EFFC/img_1775141723869.webp',_binary '',0,'Leobog Hi75C Pro',0.00,'LeobogHi75CPro',1,'Chiếc','2026-06-05 09:23:41.845794',NULL,1),(5,NULL,'Aula',NULL,'2026-04-02 21:59:39.426106','','https://oms.mechkey.vn/media/02042026/9908/img_1775141968420.webp',_binary '',0,'AULA F108 Pro',0.00,'AULAF108Pro',7,'Chiếc','2026-05-09 22:20:05.956799',NULL,1),(6,NULL,'Aula',NULL,'2026-04-02 22:20:42.095430','','https://oms.mechkey.vn/media/02042026/4503/img_1775142938824.webp',_binary '',0,'AULA SC580',0.00,'AULASC580',4,'Chiếc','2026-05-25 10:06:03.402660',NULL,6),(7,NULL,'Aula',NULL,'2026-04-16 22:42:13.248106','','https://oms.mechkey.vn/media/16042026/3654/img_1776354094114.png',_binary '',0,'AULA S98 Pro',0.00,'AULA-S98-Pro',0,'Chiếc','2026-04-16 22:42:13.573842',NULL,1),(8,NULL,'Aula',NULL,'2026-04-16 22:45:47.725522','','https://oms.mechkey.vn/media/16042026/B45D/img_1776354330049.jpg',_binary '',0,'AULA S75 Pro',0.00,'AULA-S75-Pro',8,'Chiếc','2026-05-19 10:10:11.670618',NULL,1),(9,NULL,'Aula',NULL,'2026-04-16 22:54:27.833138','','https://oms.mechkey.vn/media/16042026/D93F/img_1776354860617.jpg',_binary '',0,'AULA SC580SE',0.00,'AULA-SC580SE',0,'Chiếc','2026-05-25 10:10:28.301152',NULL,6),(10,NULL,'Aula',NULL,'2026-04-21 08:51:19.752207','','https://oms.mechkey.vn/media/21042026/EBBD/img_1776736264446.webp',_binary '',0,'AULA F87 Pro V2',0.00,'AULA-F87-PRO-V2',1,'Chiếc','2026-05-19 15:10:35.473866',NULL,1);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quotation_details`
--

DROP TABLE IF EXISTS `quotation_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quotation_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `quantity` int NOT NULL,
  `total_price` double DEFAULT NULL,
  `unit_price` double NOT NULL,
  `warranty` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `product_id` bigint NOT NULL,
  `quotation_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKaqydkynkh434ifin8tw1jehq6` (`product_id`),
  KEY `FKolxi2hah5a2praypvlyrxmiov` (`quotation_id`),
  CONSTRAINT `FKaqydkynkh434ifin8tw1jehq6` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`),
  CONSTRAINT `FKolxi2hah5a2praypvlyrxmiov` FOREIGN KEY (`quotation_id`) REFERENCES `quotations` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quotation_details`
--

LOCK TABLES `quotation_details` WRITE;
/*!40000 ALTER TABLE `quotation_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `quotation_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quotations`
--

DROP TABLE IF EXISTS `quotations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `quotations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `grand_total` double DEFAULT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `quotation_code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `quotation_date` date NOT NULL,
  `staff_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tax_percent` double DEFAULT NULL,
  `total_amount` double DEFAULT NULL,
  `valid_until` date DEFAULT NULL,
  `customer_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKembni47qrnsforfcdkke30w8s` (`quotation_code`),
  KEY `FKbv6vp77w2lpnag5v0b8keobm9` (`customer_id`),
  CONSTRAINT `FKbv6vp77w2lpnag5v0b8keobm9` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quotations`
--

LOCK TABLES `quotations` WRITE;
/*!40000 ALTER TABLE `quotations` DISABLE KEYS */;
/*!40000 ALTER TABLE `quotations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt_activities`
--

DROP TABLE IF EXISTS `receipt_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipt_activities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `creator_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receipt_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKj7xlxqttwulwru38lw2a2g889` (`receipt_id`),
  CONSTRAINT `FKj7xlxqttwulwru38lw2a2g889` FOREIGN KEY (`receipt_id`) REFERENCES `receipts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt_activities`
--

LOCK TABLES `receipt_activities` WRITE;
/*!40000 ALTER TABLE `receipt_activities` DISABLE KEYS */;
INSERT INTO `receipt_activities` VALUES (1,'Tạo mới phiếu nhập hàng','2026-04-02 22:03:44.335906','admin',1),(2,'Xác nhận nhập kho vào: Cửa hàng chính','2026-04-02 22:03:44.478130','admin',1),(3,'Tạo mới phiếu nhập hàng','2026-04-02 22:04:53.673094','admin',2),(4,'Xác nhận nhập kho vào: Cửa hàng chính','2026-04-02 22:04:53.703937','admin',2),(5,'Tạo mới phiếu nhập hàng','2026-04-02 22:06:57.823724','admin',3),(6,'Xác nhận nhập kho vào: Cửa hàng chính','2026-04-02 22:06:57.856883','admin',3),(7,'Tạo mới phiếu nhập hàng','2026-04-02 22:08:21.318287','admin',4),(8,'Xác nhận nhập kho vào: Cửa hàng chính','2026-04-02 22:08:21.337324','admin',4),(9,'Tạo mới phiếu nhập hàng','2026-04-02 22:09:38.912504','admin',5),(10,'Xác nhận nhập kho vào: Cửa hàng chính','2026-04-02 22:09:38.945715','admin',5),(11,'Tạo mới phiếu nhập hàng','2026-04-02 22:10:49.655254','admin',6),(12,'Xác nhận nhập kho vào: Cửa hàng chính','2026-04-02 22:10:49.694737','admin',6),(13,'Tạo mới phiếu nhập hàng','2026-04-02 22:23:58.927098','admin',7),(14,'Xác nhận nhập kho vào: Cửa hàng chính','2026-04-02 22:23:59.009471','admin',7),(15,'Tạo mới phiếu nhập hàng','2026-04-23 16:10:44.468335','admin',8),(16,'Cập nhật thông tin phiếu nhập','2026-04-27 23:05:05.368430','admin',8),(17,'Cập nhật thông tin phiếu nhập','2026-05-09 22:19:32.182709','admin',8),(18,'Thanh toán: 6095720đ (TRANSFER)','2026-05-09 22:19:55.351128','admin',8),(19,'Xác nhận nhập kho vào: Cửa hàng chính','2026-05-09 22:20:06.099929','admin',8);
/*!40000 ALTER TABLE `receipt_activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipt_details`
--

DROP TABLE IF EXISTS `receipt_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipt_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `import_price` decimal(38,2) DEFAULT NULL,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `sku` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `warranty_months` int DEFAULT NULL,
  `receipt_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgg9qo0w1vjcu9ridx36dyrhn2` (`receipt_id`),
  CONSTRAINT `FKgg9qo0w1vjcu9ridx36dyrhn2` FOREIGN KEY (`receipt_id`) REFERENCES `receipts` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipt_details`
--

LOCK TABLES `receipt_details` WRITE;
/*!40000 ALTER TABLE `receipt_details` DISABLE KEYS */;
INSERT INTO `receipt_details` VALUES (1,801173.00,NULL,6,'AULAF75-GLACIER-BLUE-REAPER',0,1),(2,1017363.00,NULL,2,'AULAF75-THUNDER-BLACK-REAPER',0,1),(3,801173.00,NULL,2,'AULAF75-SNOW-FIR-GREEN-REAPER',0,1),(4,1017363.00,NULL,5,'AULAF75-THUNDER-BLACK-GREY-WOOD-V3',0,1),(5,1059753.00,NULL,2,'AULAF75Max-GLACIER-BLUE',0,1),(6,630000.00,NULL,1,'AULAF75-GLACIER-BLUE-REAPER',0,2),(7,725000.00,NULL,1,'AULANOVA75-SNOW-MOON-WHITE-ICE-SHADOW',0,3),(8,825000.00,NULL,1,'AULANOVA75-GALAXY-GRAY-PRO-STAR-VECTOR-SEIYA',0,3),(9,860000.00,NULL,1,'LeobogHi75CPro-0C82',0,4),(10,850000.00,NULL,1,'AULAF108Pro-GREY-YELLOW-REAPER',0,5),(11,850000.00,NULL,1,'AULANOVA75-GALAXY-GRAY-PRO-STAR-VECTOR-SEIYA',0,5),(12,640000.00,NULL,9,'AULAF75-GLACIER-BLUE-REAPER',0,6),(13,640000.00,NULL,2,'AULAF75-SNOW-FIR-GREEN-REAPER',0,6),(14,200000.00,NULL,1,'AULASC580-ĐEN',0,7),(15,750000.00,NULL,1,'LeobogHi75CPro-3BB0',0,7),(48,911932.00,'Leobog Hi75C Pro - Milky Brown - Turquoise',1,'LeobogHi75CPro-MILKY-BROWN-TURQUOISE',12,8),(49,996763.00,'Leobog Hi75C Pro - Astronaut - StrawBerry Mint',5,'LeobogHi75CPro-0C82',12,8),(50,190870.00,'AULA SC580SE - Đen',3,'AULA-SC580SE-ĐEN',12,8),(51,190870.00,'AULA SC580SE - Trắng',3,'AULA-SC580SE-TRANG',12,8),(52,288425.00,'AULA SC580 - Đen',3,'AULASC580-ĐEN',12,8),(53,288425.00,'AULA SC580 - Trắng',3,'AULASC580-TRANG',12,8),(54,784686.00,'AULA S75 Pro - Glacier Blue - Seiya (Star Vector)',10,'AULA-S75-Pro-GLACIER-BLUE-SEIYA-(STAR-VECTOR)',12,8),(55,1039179.00,'AULA F108 Pro - Grey Yellow - Reaper',2,'AULAF108Pro-GREY-YELLOW-REAPER',12,8),(56,1081594.00,'AULA F108 Pro - Glacier Blue - Caramel Latte',3,'AULAF108Pro-GLACIER-BLUE-CARAMEL-LATTE',12,8),(57,1081594.00,'AULA F108 Pro - Mocha Mousse - Caramel Latte',2,'AULAF108Pro-MOCHA-MOUSSE-CARAMEL-LATTE',12,8),(58,721063.00,'AULA F75 - Snow Fir Green - Reaper',8,'AULAF75-SNOW-FIR-GREEN-REAPER',12,8),(59,954348.00,'AULA F75 Max - Glacier Blue',2,'AULAF75Max-GLACIER-BLUE',12,8),(60,890725.00,'AULA NOVA75 - Snow Moon White - Ice Shadow',1,'AULANOVA75-SNOW-MOON-WHITE-ICE-SHADOW',12,8),(61,1017971.00,'AULA NOVA75 - Galaxy Gray (Pro) - Caramel Latte',5,'AULANOVA75-GALAXY-GRAY-PRO-CARAMEL-LATTE',12,8),(62,1017971.00,'AULA NOVA75 - Night Shadow (Pro) - Caramel Latte',5,'AULANOVA75-NIGHT-SHADOW-PRO-CARAMEL-LATTE',12,8),(63,911932.00,'AULA F87 Pro V2 - White Contours - Begonia',2,'AULA-F87-PRO-V2-WHITE-CONTOURS-BEGONIA',12,8);
/*!40000 ALTER TABLE `receipt_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `receipts`
--

DROP TABLE IF EXISTS `receipts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `receipts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount_paid` decimal(38,2) DEFAULT NULL,
  `branch_id` bigint DEFAULT NULL,
  `branch_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `creator_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `discount` decimal(38,2) DEFAULT NULL,
  `import_status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `items_amount` decimal(38,2) DEFAULT NULL,
  `note` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `shipping_fee` decimal(38,2) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_amount` decimal(38,2) DEFAULT NULL,
  `supplier_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKpalhutkagd8lnbma35aqr048m` (`code`),
  KEY `FK4ksphcrrl2epyxvdqwo0gat9d` (`supplier_id`),
  CONSTRAINT `FK4ksphcrrl2epyxvdqwo0gat9d` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `receipts`
--

LOCK TABLES `receipts` WRITE;
/*!40000 ALTER TABLE `receipts` DISABLE KEYS */;
INSERT INTO `receipts` VALUES (1,15650431.00,1,'Cửa hàng chính','REI1775142224304','2026-03-05 22:03:00.000000','admin',0.00,'COMPLETED',15650431.00,'','PAID',0.00,'COMPLETED',15650431.00,1),(2,630000.00,1,'Cửa hàng chính','REI1775142293658','2026-03-17 22:04:00.000000','admin',0.00,'COMPLETED',630000.00,'','PAID',0.00,'COMPLETED',630000.00,2),(3,1550000.00,1,'Cửa hàng chính','REI1775142417804','2026-03-21 22:06:00.000000','admin',0.00,'COMPLETED',1550000.00,'','PAID',0.00,'COMPLETED',1550000.00,3),(4,860000.00,1,'Cửa hàng chính','REI1775142501301','2026-03-21 22:08:00.000000','admin',0.00,'COMPLETED',860000.00,'','PAID',0.00,'COMPLETED',860000.00,4),(5,1700000.00,1,'Cửa hàng chính','REI1775142578896','2026-03-21 22:09:00.000000','admin',0.00,'COMPLETED',1700000.00,'','PAID',0.00,'COMPLETED',1700000.00,5),(6,7040000.00,1,'Cửa hàng chính','REI1775142649635','2026-03-23 22:10:00.000000','admin',0.00,'COMPLETED',7040000.00,'','PAID',0.00,'COMPLETED',7040000.00,6),(7,950000.00,1,'Cửa hàng chính','REI1775143438896','2026-03-26 22:23:00.000000','admin',0.00,'COMPLETED',950000.00,'','PAID',0.00,'COMPLETED',950000.00,7),(8,44676203.00,1,'Cửa hàng chính','REI1776935444183','2026-04-23 16:09:00.000000','admin',0.00,'COMPLETED',42026670.00,'Đơn đặt hộ qua thuongdo.com. Mã đơn: 1204V00001','PAID',2649533.00,'COMPLETED',44676203.00,1);
/*!40000 ALTER TABLE `receipts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_activities`
--

DROP TABLE IF EXISTS `return_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_activities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `return_order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKiylsrh8s7r98re5pqtl7vd2c3` (`return_order_id`),
  CONSTRAINT `FKiylsrh8s7r98re5pqtl7vd2c3` FOREIGN KEY (`return_order_id`) REFERENCES `return_orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_activities`
--

LOCK TABLES `return_activities` WRITE;
/*!40000 ALTER TABLE `return_activities` DISABLE KEYS */;
/*!40000 ALTER TABLE `return_activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_order_details`
--

DROP TABLE IF EXISTS `return_order_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_order_details` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `quantity` int DEFAULT NULL,
  `refund_amount` decimal(38,2) DEFAULT NULL,
  `sku` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `unit_price` decimal(38,2) DEFAULT NULL,
  `return_order_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8i4y8s6m5kmd0t9xkg8wnl96j` (`return_order_id`),
  CONSTRAINT `FK8i4y8s6m5kmd0t9xkg8wnl96j` FOREIGN KEY (`return_order_id`) REFERENCES `return_orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_order_details`
--

LOCK TABLES `return_order_details` WRITE;
/*!40000 ALTER TABLE `return_order_details` DISABLE KEYS */;
/*!40000 ALTER TABLE `return_order_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `return_orders`
--

DROP TABLE IF EXISTS `return_orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `return_orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `note` text COLLATE utf8mb4_unicode_ci,
  `reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `refund_status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `restock_status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `return_code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `return_fee` decimal(38,2) DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_refund_amount` decimal(38,2) DEFAULT NULL,
  `order_id` bigint NOT NULL,
  `shop_return_fee` decimal(38,2) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKq56klbxljkmlpmquh7mqf7rrl` (`return_code`),
  KEY `FK7lptnhci1qqu8rbe82tqcjpcv` (`order_id`),
  CONSTRAINT `FK7lptnhci1qqu8rbe82tqcjpcv` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `return_orders`
--

LOCK TABLES `return_orders` WRITE;
/*!40000 ALTER TABLE `return_orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `return_orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sales_channels`
--

DROP TABLE IF EXISTS `sales_channels`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sales_channels` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sales_channels`
--

LOCK TABLES `sales_channels` WRITE;
/*!40000 ALTER TABLE `sales_channels` DISABLE KEYS */;
INSERT INTO `sales_channels` VALUES (1,_binary '','SHOPEE',NULL,'Shopee'),(2,_binary '','WEBSITE',NULL,'Website'),(3,_binary '','TIKTOK',NULL,'Tiktok'),(4,_binary '','FACEBOOK',NULL,'Facebook');
/*!40000 ALTER TABLE `sales_channels` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `suppliers`
--

DROP TABLE IF EXISTS `suppliers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `suppliers` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address_detail` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `assignee` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `code` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `country` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `district` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fax` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `province` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL,
  `tags` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `tax_code` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `ward` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `website` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK8kh5crh75ye2imfi5yv37p61o` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `suppliers`
--

LOCK TABLES `suppliers` WRITE;
/*!40000 ALTER TABLE `suppliers` DISABLE KEYS */;
INSERT INTO `suppliers` VALUES (1,'','Quang','NCC1775125583006','Trung Quốc','2026-04-02 17:26:23.007571',NULL,'','','深圳市博诚电脑科技有限公司','13427580916','','ACTIVE','','','2026-04-02 17:26:23.007700','','https://szbocheng.1688.com'),(2,'73 Khương Thượng','Quang','NCC1775125839587','Việt Nam','2026-04-02 17:30:39.587993',NULL,'','','Quốc Tú','0945845820','','ACTIVE','','','2026-04-02 17:30:39.588133','','https://www.facebook.com/quoctu97'),(3,'','Quang','NCC1775125861827','Việt Nam','2026-04-02 17:31:01.830933',NULL,'','','Nguyễn Hiếu','','','ACTIVE','','','2026-04-02 17:31:01.831071','','https://www.facebook.com/profile.php?id=61586841329402'),(4,'','Quang','NCC1775125918767','Việt Nam','2026-04-02 17:31:58.768174',NULL,'','','Lê Thành','','','ACTIVE','','','2026-04-02 17:31:58.768301','','https://www.facebook.com/ltaxFundy.3126'),(5,'299 Lương Thế Vinh','Quang','NCC1775125937803','Việt Nam','2026-04-02 17:32:17.804169',NULL,'','','Duy Long','0798981988','','ACTIVE','','','2026-04-02 17:32:17.804282','','https://www.facebook.com/longld.8888'),(6,'','Quang','NCC1775125956543','Việt Nam','2026-04-02 17:32:36.544009',NULL,'','','Trương Minh Trung','0971095835','','ACTIVE','','','2026-04-02 17:32:36.544433','','https://www.facebook.com/trung.lyo.54'),(7,'','Quang','NCC1775125987873','Việt Nam','2026-04-02 17:33:07.874020',NULL,'','','Công Nghĩa','','','ACTIVE','','','2026-04-02 17:33:07.874180','','https://www.facebook.com/nghia.congha');
/*!40000 ALTER TABLE `suppliers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_settings`
--

DROP TABLE IF EXISTS `system_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_settings` (
  `setting_key` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `setting_value` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_settings`
--

LOCK TABLES `system_settings` WRITE;
/*!40000 ALTER TABLE `system_settings` DISABLE KEYS */;
/*!40000 ALTER TABLE `system_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `full_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` enum('ADMIN','STAFF') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `username` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `branch_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`),
  KEY `FK9o70sp9ku40077y38fk4wieyk` (`branch_id`),
  CONSTRAINT `FK9o70sp9ku40077y38fk4wieyk` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,_binary '','hi.imkwang@gmail.com','Quang','$2a$10$SW3jur03TyYORfFRguXJOer3icfwHAodzIlXDkgQdfMQwR0OMwQpW','0971130397','ADMIN','admin',NULL),(2,_binary '',NULL,'Trang Moon','$2a$10$zfqRf12svi.K97C61qgNDucZzXz7gFKbUZeyDWdBC.yxEd0MfRFKq',NULL,'STAFF','trangmoon',1);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warranty_activities`
--

DROP TABLE IF EXISTS `warranty_activities`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warranty_activities` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `creator_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `ticket_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhanc43icth3ajxjl2wbe7520m` (`ticket_id`),
  CONSTRAINT `FKhanc43icth3ajxjl2wbe7520m` FOREIGN KEY (`ticket_id`) REFERENCES `warranty_tickets` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warranty_activities`
--

LOCK TABLES `warranty_activities` WRITE;
/*!40000 ALTER TABLE `warranty_activities` DISABLE KEYS */;
/*!40000 ALTER TABLE `warranty_activities` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `warranty_tickets`
--

DROP TABLE IF EXISTS `warranty_tickets`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `warranty_tickets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint DEFAULT NULL,
  `customer_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `customer_phone` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `issue_description` text COLLATE utf8mb4_unicode_ci,
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `receive_date` datetime(6) DEFAULT NULL,
  `repair_cost` decimal(38,2) DEFAULT NULL,
  `return_date` datetime(6) DEFAULT NULL,
  `serial_number` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('CANCELED','DONE','PROCESSING','RECEIVED','RETURNED') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ticket_code` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` enum('REPAIR','WARRANTY') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKtirrx4muy7pteos5eodk5e6vk` (`ticket_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `warranty_tickets`
--

LOCK TABLES `warranty_tickets` WRITE;
/*!40000 ALTER TABLE `warranty_tickets` DISABLE KEYS */;
/*!40000 ALTER TABLE `warranty_tickets` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'oms'
--

--
-- Dumping routines for database 'oms'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-05 11:27:11
