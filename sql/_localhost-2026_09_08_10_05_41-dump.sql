-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: agricultural_products_market
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_category_name` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (0,'未分类','2026-06-15 16:28:32'),(1,'文学类','2025-12-02 17:32:32'),(2,'科技类','2025-12-02 17:32:32'),(3,'历史类','2025-12-02 17:32:32'),(4,'少儿类','2025-12-02 17:32:32'),(5,'经管类','2025-12-02 17:32:32'),(6,'艺术类','2025-12-02 17:32:32');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coupon`
--

DROP TABLE IF EXISTS `coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `coupon_name` varchar(50) NOT NULL COMMENT '优惠券名称',
  `coupon_type` tinyint NOT NULL COMMENT '券类型 1-满减 2-无门槛 3-折扣',
  `full_money` decimal(10,2) DEFAULT '0.00' COMMENT '满减门槛(无门槛为0)',
  `reduce_money` decimal(10,2) DEFAULT '0.00' COMMENT '减免金额',
  `discount` decimal(3,2) DEFAULT '1.00' COMMENT '折扣比例(0.01-1.00)',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '总发放数量',
  `used_count` int NOT NULL DEFAULT '0' COMMENT '已领取数量',
  `start_time` datetime NOT NULL COMMENT '领取开始时间',
  `end_time` datetime NOT NULL COMMENT '领取结束时间',
  `valid_days` int NOT NULL DEFAULT '7' COMMENT '领取后有效天数',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-下架 1-正常',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coupon`
--

LOCK TABLES `coupon` WRITE;
/*!40000 ALTER TABLE `coupon` DISABLE KEYS */;
INSERT INTO `coupon` VALUES (1,'618大促',2,0.00,10.00,1.00,50,7,'2026-06-11 00:00:00','2026-06-27 00:00:00',14,1,'2026-06-11 08:51:02'),(2,'限时优惠',1,100.00,20.00,1.00,90,2,'2026-06-16 00:00:00','2026-06-27 00:00:00',14,1,'2026-06-16 09:20:29'),(3,'9折优惠券',3,0.00,0.00,0.90,100,2,'2026-06-16 00:00:00','2026-06-27 00:00:00',14,1,'2026-06-16 09:21:24'),(4,'满200-30',1,200.00,30.00,1.00,50,2,'2026-06-16 00:00:00','2026-06-27 00:00:00',14,1,'2026-06-16 09:40:09');
/*!40000 ALTER TABLE `coupon` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_info`
--

DROP TABLE IF EXISTS `order_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `address_id` bigint NOT NULL COMMENT '地址ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_status` tinyint DEFAULT '0' COMMENT '支付状态：0-未支付，1-已支付',
  `order_status` varchar(20) DEFAULT '待发货' COMMENT '订单状态：待发货/已发货/已完成',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `user_coupon_id` bigint DEFAULT NULL COMMENT '使用的用户优惠券ID',
  `coupon_discount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠券抵扣金额',
  `pay_amount` decimal(10,2) DEFAULT '0.00' COMMENT '实付金额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_pay_status` (`pay_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `fk_order_address` (`address_id`),
  CONSTRAINT `fk_order_address` FOREIGN KEY (`address_id`) REFERENCES `user_address` (`id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_info`
--

LOCK TABLES `order_info` WRITE;
/*!40000 ALTER TABLE `order_info` DISABLE KEYS */;
INSERT INTO `order_info` VALUES (47,'5e840be60db54106b37fa22293490de8',10,7,8319.00,1,'待发货','2026-06-16 15:19:39','2026-06-16 15:19:19','2026-06-16 15:19:39',NULL,831.90,7487.10),(48,'cb0f75b8ce044ff8b918de298d7cdd8b',3,3,128.00,0,'已取消',NULL,'2026-06-16 15:32:17','2026-06-16 16:05:00',NULL,0.00,128.00),(49,'2ef5a8d84e9c45208999104074e5a109',3,3,29.80,1,'待发货','2026-06-16 15:36:58','2026-06-16 15:36:39','2026-06-16 15:36:58',NULL,0.00,29.80),(50,'0cdfe15099e24f1f861c613e84196541',10,7,129.00,1,'待发货','2026-06-16 15:40:19','2026-06-16 15:40:02','2026-06-16 15:40:19',NULL,0.00,129.00),(51,'2417594662ba4b158b9f9c5f77ee39b0',3,3,59.89,0,'已取消',NULL,'2026-06-17 08:56:03','2026-06-17 09:30:01',NULL,0.00,59.89),(52,'f7532c74cfe949ddab4f5f368cc10c98',3,3,38.00,0,'已取消',NULL,'2026-06-17 10:11:40','2026-06-17 10:22:27',NULL,0.00,38.00),(53,'794e479ae52f4312bf850913542e2a2f',3,3,45.00,1,'待发货','2026-06-17 10:20:10','2026-06-17 10:17:36','2026-06-17 10:20:10',NULL,0.00,45.00),(54,'c787ae75a4b84ed1a79a40e6585baead',3,3,29.80,1,'待发货','2026-06-17 10:23:03','2026-06-17 10:22:40','2026-06-17 10:23:03',NULL,0.00,29.80),(55,'ab8b4f742cde48bd87cb5fddc2282104',3,3,35.00,1,'待发货','2026-06-17 10:26:46','2026-06-17 10:26:23','2026-06-17 10:26:46',NULL,0.00,35.00),(56,'5c1b6e20495d47ab98018b4a6037e13d',3,3,35.00,1,'待发货','2026-06-17 10:28:50','2026-06-17 10:28:35','2026-06-17 10:28:50',NULL,0.00,35.00),(57,'81dc41551ecb462f857bdcefc0683ce7',3,3,35.00,1,'待发货','2026-06-17 10:35:11','2026-06-17 10:34:51','2026-06-17 10:35:11',NULL,0.00,35.00),(58,'f7102f0f699444b0bbc7692cfb775f87',3,3,18.80,1,'待发货','2026-06-17 10:41:36','2026-06-17 10:41:16','2026-06-17 10:41:36',NULL,0.00,18.80),(59,'ab2a41070b1c43419c9b6429f0330cc9',3,3,35.00,1,'待发货','2026-06-17 14:09:25','2026-06-17 14:08:58','2026-06-17 14:09:25',NULL,0.00,35.00),(60,'d638b65d97e7449dae4c9ae4fdfafb7e',3,3,45.00,1,'已发货','2026-06-17 14:10:50','2026-06-17 14:10:34','2026-06-21 20:22:15',NULL,0.00,45.00),(61,'db8df086b8644d6e85ff683c738bae93',13,8,52.00,0,'已取消',NULL,'2026-06-17 15:50:23','2026-06-17 16:21:10',NULL,10.00,42.00),(62,'9affcb97aacb41f8a917e5e9b42c59b3',13,8,162.89,1,'已完成','2026-06-17 15:52:18','2026-06-17 15:51:29','2026-06-17 16:11:40',NULL,0.00,162.89),(63,'f9b95df372064332a84304c5d1d64fce',3,3,406.89,0,'待发货',NULL,'2026-06-21 20:19:25','2026-06-21 20:22:07',NULL,10.00,396.89);
/*!40000 ALTER TABLE `order_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单项ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `produce_id` bigint NOT NULL COMMENT '农产品ID',
  `produce_name` varchar(100) NOT NULL COMMENT '农产品名称（冗余）',
  `price` decimal(10,2) NOT NULL COMMENT '购买单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `package_id` bigint DEFAULT NULL COMMENT '套餐ID',
  `item_type` tinyint NOT NULL DEFAULT '1' COMMENT '明细类型 1-普通农产品 2-套餐',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `fk_item_produce` (`produce_id`),
  CONSTRAINT `fk_item_produce` FOREIGN KEY (`produce_id`) REFERENCES `produce_info` (`id`),
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `order_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单项明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (85,47,84,'哈利·波特与魔法石',58.00,1,'2026-06-16 15:19:19',2,2),(86,47,85,'小猪佩奇绘本全集',128.00,1,'2026-06-16 15:19:19',2,2),(87,47,86,'汪汪队立大功故事书',68.00,1,'2026-06-16 15:19:19',2,2),(88,47,75,'云计算概论',58.00,140,'2026-06-16 15:19:19',NULL,1),(89,48,98,'梵高传',128.00,1,'2026-06-16 15:32:17',NULL,1),(90,49,65,'傲慢与偏见',29.80,1,'2026-06-16 15:36:39',NULL,1),(91,50,67,'Java核心技术 卷1',129.00,1,'2026-06-16 15:40:02',NULL,1),(92,51,51,'平凡的世界',59.89,1,'2026-06-17 08:56:03',NULL,1),(93,52,61,'追风筝的人',38.00,1,'2026-06-17 10:11:40',NULL,1),(94,53,52,'百年孤独',45.00,1,'2026-06-17 10:17:36',NULL,1),(95,54,65,'傲慢与偏见',29.80,1,'2026-06-17 10:22:40',NULL,1),(96,55,53,'活着',35.00,1,'2026-06-17 10:26:23',NULL,1),(97,56,53,'活着',35.00,1,'2026-06-17 10:28:35',NULL,1),(98,57,53,'活着',35.00,1,'2026-06-17 10:34:51',NULL,1),(99,58,57,'骆驼祥子',18.80,1,'2026-06-17 10:41:16',NULL,1),(100,59,53,'活着',35.00,1,'2026-06-17 14:08:58',NULL,1),(101,60,52,'百年孤独',45.00,1,'2026-06-17 14:10:34',NULL,1),(102,61,64,'罪与罚',52.00,1,'2026-06-17 15:50:23',NULL,1),(103,62,51,'平凡的世界',59.89,1,'2026-06-17 15:51:29',NULL,1),(104,62,53,'活着',35.00,1,'2026-06-17 15:51:29',NULL,1),(105,62,54,'红楼梦',68.00,1,'2026-06-17 15:51:29',NULL,1),(106,63,84,'哈利·波特与魔法石',58.00,1,'2026-06-21 20:19:25',2,2),(107,63,85,'小猪佩奇绘本全集',128.00,1,'2026-06-21 20:19:25',2,2),(108,63,86,'汪汪队立大功故事书',68.00,1,'2026-06-21 20:19:25',2,2),(109,63,51,'平凡的世界',59.89,1,'2026-06-21 20:19:25',NULL,1),(110,63,52,'百年孤独',45.00,1,'2026-06-21 20:19:25',NULL,1),(111,63,53,'活着',35.00,1,'2026-06-21 20:19:25',NULL,1),(112,63,54,'红楼梦',68.00,1,'2026-06-21 20:19:25',NULL,1);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `package_book`
--

DROP TABLE IF EXISTS `package_book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `package_book` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `package_id` bigint NOT NULL COMMENT '套餐ID',
  `produce_id` bigint NOT NULL COMMENT '农产品ID',
  `produce_num` int NOT NULL DEFAULT '1' COMMENT '套餐内该农产品数量',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_package_produce` (`package_id`,`produce_id`),
  KEY `idx_produce_id` (`produce_id`)
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='套餐-农产品关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `package_book`
--

LOCK TABLES `package_book` WRITE;
/*!40000 ALTER TABLE `package_book` DISABLE KEYS */;
INSERT INTO `package_book` VALUES (5,2,84,1),(6,2,85,1),(7,2,86,1),(8,3,91,1),(9,3,92,1),(10,3,93,1),(11,4,76,1),(12,4,77,1),(13,4,79,1),(14,5,66,1),(15,5,67,1),(16,5,72,1),(17,5,73,1),(18,6,59,1),(19,6,60,1),(20,7,51,1),(21,7,52,1),(22,7,53,1),(23,7,56,1),(24,1,54,1),(25,1,101,1),(26,1,102,1),(27,1,103,1),(30,8,105,1);
/*!40000 ALTER TABLE `package_book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produce_info`
--

DROP TABLE IF EXISTS `produce_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produce_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '农产品ID',
  `produce_name` varchar(100) NOT NULL COMMENT '农产品名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `manufacturer` varchar(50) DEFAULT '' COMMENT '生产商',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `stock` int DEFAULT '0' COMMENT '库存数量',
  `description` text COMMENT '农产品简介',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-上架，0-下架',
  `cover_url` varchar(255) DEFAULT '' COMMENT '封面图片URL',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_produce_name` (`produce_name`),
  CONSTRAINT `fk_produce_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农产品信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produce_info`
--

LOCK TABLES `produce_info` WRITE;
/*!40000 ALTER TABLE `produce_info` DISABLE KEYS */;
INSERT INTO `produce_info` VALUES (51,'平凡的世界',1,'路遥',59.89,194,'讲述中国70年代中期到80年代中期城乡社会生活的历史性变迁',1,'https://img12.360buyimg.com/n2/s480x480_jfs/t1/206945/15/12187/88781/61b1bda3E08b3531f/01e7ee1492ad156c.jpg','2025-12-02 17:32:36','2025-12-31 09:09:42'),(52,'百年孤独',1,'加西亚·马尔克斯',45.00,147,'魔幻现实主义文学的代表作，讲述布恩迪亚家族七代人的故事',1,'https://img13.360buyimg.com/n2/s480x480_jfs/t1/220857/23/50561/205771/675021c7F47169d75/e039d48484e4f083.jpg','2025-12-02 17:32:36','2025-12-30 11:32:31'),(53,'活着',1,'余华',35.00,293,'讲述福贵一生的坎坷经历，展现生命的韧性',1,'https://img1.360buyimg.com/n6/jfs/t1/328594/11/14695/98813/68b69b48Fa5c2d17c/2ab2df250e6892ae.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(54,'红楼梦',1,'曹雪芹',68.00,180,'中国古典四大名著之一，描绘贾史王薛四大家族的兴衰',1,'https://img1.360buyimg.com/n6/jfs/t1/215138/16/936/118590/616d7cf3E7dc5624a/69de18e2ca388e09.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(55,'三体',1,'刘慈欣',42.80,249,'科幻文学巅峰之作，讲述地球人类文明与三体文明的信息交流、生死搏杀',1,'https://img1.360buyimg.com/n6/jfs/t1/164841/18/9312/290430/603efa56E93277906/9910812c6871df42.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(56,'围城',1,'钱钟书',29.90,120,'以幽默的笔触描绘知识分子的生活百态',1,'https://img1.360buyimg.com/n2/jfs/t1/194252/12/26373/248912/62f4a2bbEa0e0f809/4dd0faa5ba89de6e.jpg','2025-12-02 17:32:36','2025-12-30 11:32:33'),(57,'骆驼祥子',1,'老舍',18.80,218,'讲述北平一个普通人力车夫的悲剧命运',1,'https://img1.360buyimg.com/n6/jfs/t1/225719/3/29493/65340/67340618Fb4cdff9f/2c2070b56a526857.jpg','2025-12-02 17:32:36','2025-12-31 09:06:04'),(58,'边城',1,'沈从文',22.50,160,'以湘西小城为背景，描绘纯美的人性与爱情',1,'https://img1.360buyimg.com/n6/jfs/t1/116426/19/39757/149459/64acb3d8F6b77c7da/c5f72757097a15b1.jpg','2025-12-02 17:32:36','2025-12-30 11:32:37'),(59,'白夜行',1,'东野圭吾',39.50,190,'悬疑小说，讲述一对少男少女跨越数十年的黑暗故事',1,'https://img1.360buyimg.com/n6/jfs/t1/364464/29/5725/134090/6926a19fF6321aae4/1564268f0e981f40.jpg','2025-12-02 17:32:36','2025-12-30 11:32:37'),(60,'解忧杂货店',1,'东野圭吾',35.80,210,'温情治愈系小说，讲述杂货店解决人们烦恼的故事',1,'https://img1.360buyimg.com/n6/jfs/t1/89604/2/23747/86806/63b3626fF288a268c/43ab61ffaeeac56b.jpg','2025-12-02 17:32:36','2025-12-30 11:32:36'),(61,'追风筝的人',1,'卡勒德·胡赛尼',38.00,170,'讲述阿富汗少年的成长与救赎',1,'https://img1.360buyimg.com/n6/jfs/t1/194169/23/49520/108167/670cfc20F344a73bb/138a986c7a96f8ba.jpg','2025-12-02 17:32:36','2025-12-03 14:07:40'),(62,'小王子',1,'安托万·德·圣-埃克苏佩里',26.80,280,'童话式哲理小说，探讨爱与责任',1,'https://img1.360buyimg.com/n6/jfs/t23689/239/924083817/295130/f70c2a63/5b486051N16d1762e.jpg','2025-12-02 17:32:36','2025-12-03 14:30:54'),(63,'霍乱时期的爱情',1,'加西亚·马尔克斯',48.00,140,'讲述一段跨越半个多世纪的爱情故事',1,'https://img1.360buyimg.com/n6/jfs/t1/39030/20/24376/139401/66e6f118F87b55077/17a92357849d94f3.jpg','2025-12-02 17:32:36','2025-12-03 14:30:54'),(64,'罪与罚',1,'陀思妥耶夫斯基',52.00,89,'探讨人性、道德与犯罪的经典文学作品',1,'https://img1.360buyimg.com/n6/jfs/t1/9397/27/15340/472131/5c6fc6dcEee69a712/e91516233431651f.jpg','2025-12-02 17:32:36','2025-12-03 14:30:54'),(65,'傲慢与偏见',1,'简·奥斯汀',29.80,128,'讲述伊丽莎白与达西的爱情故事，反映社会阶层与婚恋观',1,'https://img1.360buyimg.com/n6/jfs/t1/352486/5/3870/128563/692e895fFde97be46/bc8bdec109aea645.jpg','2025-12-02 17:32:36','2025-12-03 14:37:09'),(66,'Python编程：从入门到实践',2,'埃里克·马瑟斯',89.00,181,'零基础入门Python，包含基础语法与实战项目',1,'https://img1.360buyimg.com/n6/jfs/t1/144677/35/33070/86745/65386e35Fbcbb6eca/56ab3155e000383d.jpg','2025-12-02 17:32:36','2025-12-03 14:37:09'),(67,'Java核心技术 卷1',2,'凯·S·霍斯特曼',129.00,108,'Java经典教程，覆盖基础语法与面向对象编程',1,'https://img1.360buyimg.com/n6/jfs/t1/189748/31/37739/76109/652784b3F1fe030ac/9ad5c0befcfae280.jpg','2025-12-02 17:32:36','2025-12-31 09:23:44'),(68,'深入理解计算机系统',2,'兰德尔·E·布莱恩特',149.00,80,'从底层讲解计算机系统的工作原理',1,'https://img1.360buyimg.com/n6/jfs/t1/210381/34/35330/92249/652784b3Fa2a4c84e/861dd926fb15a3dd.jpg','2025-12-02 17:32:36','2025-12-03 14:37:09'),(69,'数据结构与算法分析',2,'马克·艾伦·维斯',79.80,100,'系统讲解数据结构与算法的设计与分析',1,'https://img1.360buyimg.com/n6/jfs/t1/159063/9/50829/177473/6742b91aFe78c51ca/916e9a8801400335.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(70,'机器学习',2,'周志华',69.00,150,'被称为“西瓜书”，零基础入门机器学习',1,'https://img1.360buyimg.com/n6/jfs/t1/95569/15/44527/55829/66f13501Fdf22202a/a2549e9c9888a616.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(71,'深度学习',2,'伊恩·古德费洛',128.00,95,'深度学习领域的经典教材，覆盖核心算法与实践',1,'https://img1.360buyimg.com/n6/jfs/t1/324602/8/17378/160326/68bd5376F7d566266/b0687a44ee6fd95b.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(72,'MySQL必知必会',2,'本·福达',45.00,200,'快速掌握MySQL基础与常用操作',1,'https://img1.360buyimg.com/n6/jfs/t1/218805/14/49859/153862/6742e7f1F076229e1/d5794ef9ce1d7940.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(73,'Redis设计与实现',2,'黄健宏',69.90,120,'深入讲解Redis的内部实现原理',1,'https://img1.360buyimg.com/n6/jfs/t1/113566/34/40282/55383/6527665dF284498b4/0a757b4df19fbf2f.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(74,'Spring Boot实战',2,'克雷格·沃斯',79.00,130,'快速上手Spring Boot开发，包含实战案例',1,'https://img1.360buyimg.com/n6/jfs/t1/178750/10/32515/106620/63ef3446F3dd2b7e0/a152c871a03a03e5.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(75,'云计算概论',2,'王鹏',58.00,0,'系统讲解云计算的概念、架构与应用',1,'https://img1.360buyimg.com/n6/jfs/t1/160130/28/29881/56532/630dd9dfE24d05f7c/32032e4fdfa495f8.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(76,'明朝那些事儿',3,'当年明月',99.00,220,'以通俗的语言讲述明朝三百年的历史',1,'https://img1.360buyimg.com/n6/jfs/t1/71751/28/18966/249790/628af2aeE81e4bc59/c6aa0fe0b02154d3.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(77,'史记',3,'司马迁',88.00,100,'中国第一部纪传体通史，记载从上古到汉武帝时期的历史',1,'https://img1.360buyimg.com/n6/jfs/t1/64987/10/19644/146552/62b3e552E56adeebf/41fb94a9fc67c323.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(78,'资治通鉴',3,'司马光',198.00,70,'编年体通史，涵盖16朝1362年的历史',1,'https://img1.360buyimg.com/n6/jfs/t1/263693/10/27453/270497/67c66fe6Fe50c22a3/c41023b8acf5bfa7.png','2025-12-02 17:32:36','2025-12-03 23:30:01'),(79,'中国通史',3,'吕思勉',78.00,110,'系统梳理中国从远古到近代的历史脉络',1,'https://img1.360buyimg.com/n6/jfs/t1/174299/26/21695/267709/62455e04E3c29d74d/1b019aef5f3dfc1c.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(80,'全球通史',3,'斯塔夫里阿诺斯',118.00,85,'从全球视角讲述人类文明的发展历程',1,'https://img1.360buyimg.com/n6/jfs/t1/241565/5/14512/110958/66b58865F753d58f8/b076361d9594828b.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(81,'大秦帝国',3,'孙皓晖',238.00,60,'全景式展现秦国从崛起到统一的历史',1,'https://img1.360buyimg.com/n6/jfs/t1/309090/21/8107/107900/684687d6F006e27e4/89b5772ca8c6d219.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(82,'唐朝穿越指南',3,'森林鹿',45.00,150,'以趣味方式讲解唐朝的社会生活与文化',1,'https://img1.360buyimg.com/n6/jfs/t1/157562/4/31459/93229/634b192cE6fe6558d/932bdbded79be87c.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(83,'两宋风云',3,'袁腾飞',42.00,160,'讲述两宋时期的历史事件与人物',1,'https://img1.360buyimg.com/n6/jfs/t1/325923/25/19589/104460/68c4d319F13036fc8/513751c214c49533.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(84,'哈利·波特与魔法石',4,'J.K.罗琳',58.00,299,'哈利·波特系列第一部，讲述魔法世界的冒险故事',1,'https://img1.360buyimg.com/n6/jfs/t1/48982/31/24690/172173/669f1358F76be0ce0/1f33103249da7db5.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(85,'小猪佩奇绘本全集',4,'英国快乐瓢虫出版公司',128.00,249,'适合低龄儿童的启蒙绘本，讲述佩奇一家的日常',1,'https://img1.360buyimg.com/n6/jfs/t7897/300/736979266/365564/a63366a9/59969a04N9de5811a.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(86,'汪汪队立大功故事书',4,'美国尼克儿童频道',68.00,279,'以汪汪队救援为主题的儿童故事书',1,'https://img1.360buyimg.com/n6/jfs/t1/317263/35/13093/149378/686747aeFd936ebad/c2c6f3f2b3f682bf.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(87,'十万个为什么',4,'叶永烈',79.00,220,'解答儿童常见的科学问题，科普启蒙',1,'https://img1.360buyimg.com/n6/jfs/t1/242713/15/35695/234782/68f23332Fdc1469b9/35954962c2d73bc5.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(88,'海底小纵队探险记',4,'英国Vampire Squid Productions有限公司',55.00,240,'以海底探险为主题的儿童故事书',1,'https://img1.360buyimg.com/n6/jfs/t1/231150/39/14900/158822/65f3b6b2F60296354/2f1f33069aa28964.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(89,'查理九世',4,'雷欧幻像',15.00,350,'冒险解谜类儿童小说，锻炼逻辑思维',1,'https://img1.360buyimg.com/n6/jfs/t1/297947/1/10453/312003/6834289cF963bd9e9/4f0374607c1bc17c.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(90,'大头儿子小头爸爸',4,'郑春华',29.80,200,'经典儿童故事，讲述父子间的温馨日常',0,'https://img1.360buyimg.com/n6/jfs/t1/203358/2/52711/123599/675024b6F4964d1a3/84e082ed3b03dbc0.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(91,'穷爸爸富爸爸',5,'罗伯特·清崎',49.80,180,'讲述财商思维的培养，改变财富观念',1,'https://img1.360buyimg.com/n6/jfs/t1/368414/2/6594/153489/69266d43F591ec775/d8707e3f20213754.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(92,'原则',5,'瑞·达利欧',118.00,90,'桥水创始人分享的生活与工作原则',1,'https://img1.360buyimg.com/n6/jfs/t1/103733/10/43072/54110/64e593ddFb026508c/2bf088868d88ec2b.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(93,'高效能人士的七个习惯',5,'史蒂芬·柯维',58.00,140,'提升个人效率与领导力的经典书籍',1,'https://img1.360buyimg.com/n6/jfs/t1/46907/8/23979/95671/65af63d3Fcd52174a/bf01a94238543c5a.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(94,'商业模式新生代',5,'亚历山大·奥斯特瓦德',88.00,80,'讲解商业模式的设计与创新',1,'https://img1.360buyimg.com/n6/jfs/t3631/87/372490931/116665/b2f14e14/58081278N668e02f2.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(95,'经济学原理',5,'曼昆',98.00,100,'经济学入门经典，覆盖微观与宏观经济学',1,'https://img1.360buyimg.com/n6/jfs/t1/235375/13/13598/132199/662b75bcFd962e39e/fccdb111f142f67e.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(96,'精益创业',5,'埃里克·莱斯',65.00,110,'讲述创业过程中的精益思维与实践',1,'https://img1.360buyimg.com/n6/jfs/t1/360431/30/7074/97480/691d5983F0826c48a/e9b272256040f6f0.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(97,'艺术的故事',6,'贡布里希',168.00,70,'梳理西方艺术的发展历程，讲解艺术作品的内涵',1,'https://img1.360buyimg.com/n6/jfs/t1/160181/16/37841/97456/64577ad8Fcfb3bfb9/e7330026c174a9a0.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(98,'梵高传',6,'史蒂文·奈菲',128.00,84,'讲述梵高的艺术人生与创作历程',1,'https://img1.360buyimg.com/n6/jfs/t1/358732/11/12049/721648/691deddbF1c3fe62c/abe1a67f91123297.png','2025-12-02 17:32:36','2025-12-03 23:30:01'),(99,'中国书法史',6,'钟明善',78.00,95,'系统讲解中国书法的起源与发展',1,'https://img1.360buyimg.com/n6/jfs/t4327/66/1897166420/91102/29d2b5c1/58c7c2bfN2de7f0ea.jpg','2025-12-02 17:32:36','2025-12-03 23:30:01'),(100,'设计中的设计',6,'原研哉',89.00,105,'探讨设计的本质与价值，结合实际案例',1,'https://img1.360buyimg.com/n6/jfs/t1/21081/7/5862/77228/5c467a31E655c68a6/41593aa155fc1ee8.jpg','2025-12-02 17:32:36','2026-06-16 16:56:28'),(101,'三国演义',1,'罗贯中',32.00,100,'',1,'https://img14.360buyimg.com/n2/s480x480_jfs/t1/16717/5/32074/137940/6539d17bF06bb9910/f260d5d8742c2070.jpg','2026-06-11 10:27:38','2026-06-11 10:27:38'),(102,'水浒传',1,'施耐庵',35.00,90,'',1,'https://img13.360buyimg.com/n2/s480x480_jfs/t1/228772/5/777/178224/6539d167Fe188accf/52a8b4526649f5d8.jpg','2026-06-11 10:29:39','2026-06-11 10:29:39'),(103,'西游记',1,'吴承恩',37.00,8,'',1,'https://img11.360buyimg.com/n2/s480x480_jfs/t1/262456/33/26357/101093/67c32700F2195be2a/9b1269e4dee46fdb.jpg','2026-06-11 10:30:56','2026-06-17 16:50:37'),(105,'test1',0,'1',0.01,3,'',1,'222','2026-06-17 16:59:45','2026-06-17 16:59:45');
/*!40000 ALTER TABLE `produce_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `produce_package`
--

DROP TABLE IF EXISTS `produce_package`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `produce_package` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '套餐ID',
  `package_name` varchar(100) NOT NULL COMMENT '套餐名称',
  `package_desc` varchar(255) DEFAULT '' COMMENT '套餐简介',
  `cover_img` varchar(255) DEFAULT '' COMMENT '套餐封面图',
  `original_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '单买总价',
  `package_price` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '套餐售卖价',
  `sales` int NOT NULL DEFAULT '0' COMMENT '销量',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-下架 1-正常上架',
  `is_recommend` tinyint NOT NULL DEFAULT '0' COMMENT '是否推荐 0-否 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status_recommend` (`status`,`is_recommend`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农产品套餐主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produce_package`
--

LOCK TABLES `produce_package` WRITE;
/*!40000 ALTER TABLE `produce_package` DISABLE KEYS */;
INSERT INTO `produce_package` VALUES (1,'四大名著全套','中国古典四大名著完整版，传世经典，老少皆宜，文学收藏首选','https://img13.360buyimg.com/n2/s480x480_jfs/t1/438362/2/6769/53893/6a126648F9bf555d2/091432032014f6a1.jpg',172.00,139.00,4,1,1,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(2,'儿童启蒙绘本故事集','经典儿童故事合集，趣味阅读，陪伴孩子快乐成长','https://img1.360buyimg.com/n6/jfs/t1/48982/31/24690/172173/669f1358F76be0ce0/1f33103249da7db5.jpg',254.00,199.00,3,1,1,'2026-06-11 11:11:36','2026-06-11 11:11:35'),(3,'经管励志套装','财商、思维、习惯全方位提升，职场人士自我提升优选','https://img1.360buyimg.com/n6/jfs/t1/368414/2/6594/153489/69266d43F591ec775/d8707e3f20213754.jpg',225.80,199.00,0,1,0,'2026-06-16 09:42:30','2026-06-16 09:42:29'),(4,'中国历史通史套装','通俗历史 + 正史典籍组合，全方位了解中国上下千年历史','https://img1.360buyimg.com/n6/jfs/t1/71751/28/18966/249790/628af2aeE81e4bc59/c6aa0fe0b02154d3.jpg',265.00,229.00,0,1,0,'2026-06-16 09:43:43','2026-06-16 09:43:42'),(5,'编程技术全家桶','后端开发必备技术丛书，覆盖编程语言、数据库、缓存，从入门到进阶','https://img1.360buyimg.com/n6/jfs/t1/144677/35/33070/86745/65386e35Fbcbb6eca/56ab3155e000383d.jpg',332.90,299.00,0,1,0,'2026-06-16 09:45:07','2026-06-16 09:45:06'),(6,'东野圭吾悬疑套装','东野圭吾两大代表作，悬疑与温情兼具，推理小说爱好者精选套装','https://img1.360buyimg.com/n6/jfs/t1/364464/29/5725/134090/6926a19fF6321aae4/1564268f0e981f40.jpg',75.30,59.00,0,1,0,'2026-06-16 09:46:08','2026-06-16 09:46:08'),(7,'当代文学精选合集','现当代文学高分作品合集，感悟人生百态，文学爱好者必入','https://img12.360buyimg.com/n2/s480x480_jfs/t1/206945/15/12187/88781/61b1bda3E08b3531f/01e7ee1492ad156c.jpg',169.79,129.00,0,1,0,'2026-06-16 09:46:56','2026-06-16 09:46:55');
/*!40000 ALTER TABLE `produce_package` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart`
--

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车项ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `produce_id` bigint DEFAULT NULL COMMENT '农产品ID',
  `quantity` int DEFAULT '1' COMMENT '农产品数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `package_id` bigint DEFAULT NULL COMMENT '套餐ID，为空代表普通商品',
  `cart_type` tinyint NOT NULL DEFAULT '1' COMMENT '购物项类型 1-普通商品 2-套餐',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_produce` (`user_id`,`produce_id`),
  KEY `idx_produce_id` (`produce_id`),
  CONSTRAINT `fk_cart_produce` FOREIGN KEY (`produce_id`) REFERENCES `produce_info` (`id`),
  CONSTRAINT `fk_cart_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=115 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
INSERT INTO `shopping_cart` VALUES (92,10,77,100,'2026-06-17 10:10:00','2026-06-17 10:10:00',NULL,1);
/*!40000 ALTER TABLE `shopping_cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '登录用户名',
  `password` varchar(100) NOT NULL COMMENT '加密密码（BCrypt）',
  `role` varchar(30) NOT NULL COMMENT '角色：ROLE_VISITOR/ROLE_USER/ROLE_ADMIN',
  `status` tinyint DEFAULT '1' COMMENT '账号状态：1-启用，0-禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `email` varchar(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `user_pk` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (3,'admin','$2a$10$JZIOgoiYw2JUaq0ydiOU/e/yS6TaRyUgnYur0Khf3Zt0FuAAaAxDy','ROLE_ADMIN',1,'2025-12-01 14:46:11','2026-06-10 16:32:07','1296401848a@gmail.com'),(10,'ayanami','$2a$10$YZJRn3Hvj80cD3QHA2ulbugtAOu64Klgj.i13.gJI3MHHVBN1r3oK','ROLE_USER',1,'2026-06-16 15:14:00','2026-06-16 15:14:00','12345678@163.com'),(11,'lisi','$2a$10$cLcdOivBXIHx.iNNNa29t.f5PZEyZbCowry76cSizgcacqrsqbOqW','ROLE_USER',1,'2026-06-17 09:31:08','2026-06-17 09:31:08','123456@qq.com'),(13,'wangwu','$2a$10$bhyAOotzzJfgjvWWw77xuuvjPghjVMmDKi7NfihMOipNp2wT1RhBC','ROLE_USER',1,'2026-06-17 15:46:39','2026-06-17 15:46:39','123456@163.com');
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_address`
--

DROP TABLE IF EXISTS `user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `recipient` varchar(20) NOT NULL COMMENT '收件人姓名',
  `phone` varchar(11) NOT NULL COMMENT '联系电话',
  `address` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认地址：1-是，0-否',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_address_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_address`
--

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
INSERT INTO `user_address` VALUES (3,3,'w','13322790904','广东省广州市黄埔区红山街道大沙东路辅路广州航海学院黄埔校区',1,'2025-12-05 11:29:14'),(7,10,'chen','15988888888','广东省揭阳市榕城区仙桥街道揭阳职业技术学院',1,'2026-06-16 15:19:01'),(8,13,'wu','13388888888','广东省广州市黄埔区文冲街道大沙东路广州航海学院黄埔校区',1,'2026-06-17 15:50:14');
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_coupon`
--

DROP TABLE IF EXISTS `user_coupon`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券模板ID',
  `coupon_name` varchar(50) NOT NULL COMMENT '券名称',
  `coupon_type` tinyint NOT NULL,
  `full_money` decimal(10,2) DEFAULT '0.00',
  `reduce_money` decimal(10,2) DEFAULT '0.00',
  `discount` decimal(3,2) DEFAULT '1.00',
  `valid_start` datetime NOT NULL COMMENT '使用开始时间',
  `valid_end` datetime NOT NULL COMMENT '使用截止时间',
  `use_status` tinyint NOT NULL DEFAULT '0' COMMENT '0-未使用 1-已使用 2-已过期',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID',
  `receive_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-优惠券关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_coupon`
--

LOCK TABLES `user_coupon` WRITE;
/*!40000 ALTER TABLE `user_coupon` DISABLE KEYS */;
INSERT INTO `user_coupon` VALUES (12,3,1,'618大促',2,0.00,10.00,1.00,'2026-06-17 14:08:15','2026-07-01 14:08:15',1,63,'2026-06-17 14:08:15'),(13,13,1,'618大促',2,0.00,10.00,1.00,'2026-06-17 15:47:27','2026-07-01 15:47:27',1,61,'2026-06-17 15:47:27');
/*!40000 ALTER TABLE `user_coupon` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-08 10:05:41
