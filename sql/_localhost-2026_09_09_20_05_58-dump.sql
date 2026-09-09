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
INSERT INTO `category` VALUES (0,'未分类','2025-12-02 17:32:32'),(1,'时令水果','2025-12-02 17:32:32'),(2,'新鲜蔬菜','2025-12-02 17:32:32'),(3,'根茎菌菇','2025-12-02 17:32:32'),(4,'粮油米面','2025-12-02 17:32:32'),(5,'肉禽蛋品','2025-12-02 17:32:32'),(6,'水产海鲜','2025-12-02 17:32:32'),(7,'地方特产','2025-12-02 17:32:32');
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
  CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `order_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_item_produce` FOREIGN KEY (`produce_id`) REFERENCES `produce_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=113 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单项明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
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
) ENGINE=InnoDB AUTO_INCREMENT=37 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='套餐-农产品关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `package_book`
--

LOCK TABLES `package_book` WRITE;
/*!40000 ALTER TABLE `package_book` DISABLE KEYS */;
INSERT INTO `package_book` VALUES (5,2,84,1),(6,2,85,1),(7,2,86,1),(8,3,78,1),(9,3,80,1),(10,3,81,1),(11,4,76,1),(12,4,77,1),(13,4,79,1),(14,5,87,1),(15,5,88,1),(16,5,89,1),(17,5,90,1),(18,6,91,1),(19,6,92,1),(20,7,51,1),(21,7,56,1),(22,7,57,1),(23,7,63,1),(24,1,54,1),(25,1,101,1),(26,1,102,1),(27,1,103,1),(28,9,82,1),(29,9,83,1),(30,8,105,1),(31,9,84,1),(32,9,85,1),(33,8,66,1),(34,8,69,1),(35,8,72,1),(36,8,74,1);
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
INSERT INTO `produce_info` VALUES (51,'烟台红富士苹果',1,'烟台市果品合作社',39.90,195,'果形端正、脆甜多汁，山东烟台产地直发，常温可存放两周',1,'https://img10.360buyimg.com/N1/jfs/t1/384506/1/2034/105865/6953ba8aF139a8586/d944ef533d6cd083.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(52,'海南贵妃芒',1,'海南三亚热带果业',45.00,148,'果肉细腻无纤维，香气浓郁，热带风味十足',1,'https://img10.360buyimg.com/n1/jfs/t1/314284/33/15035/135605/686b3f71Ff06ae0aa/36e516b205514744.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(53,'赣南脐橙',1,'江西赣南果业',35.00,294,'皮薄汁多、酸甜适口，富含维生素C',1,'https://img10.360buyimg.com/n1/jfs/t1/415869/5/15708/153390/69de39afF07549539/0083320320d87372.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(54,'陕西洛川苹果',1,'陕西洛川果业',68.00,181,'高原日照充足，果肉紧实、甜度高',1,'https://img14.360buyimg.com/n1/jfs/t1/408665/5/3768/91040/69c1f5fbF2dccfb14/025e320320fadc9e.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(55,'广西百香果',1,'广西南宁果蔬基地',42.80,249,'果香浓郁，可鲜食也可泡水，酸甜开胃',1,'https://img10.360buyimg.com/N1/jfs/t1/131212/7/40487/148147/65ae522cF1f0b7817/d78fdcba8ef92cf7.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(56,'新疆库尔勒香梨',1,'新疆库尔勒香梨合作社',29.90,120,'皮薄核小、清甜多汁，入口即化',1,'https://img10.360buyimg.com/N1/jfs/t1/509365/16/15760/95240/6a9e2a16F743a8bdc/0083320320c96ae4.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(57,'云南蒙自石榴',1,'云南蒙自石榴种植园',18.80,218,'籽粒饱满、汁水清甜，颗颗晶莹',1,'https://img10.360buyimg.com/n1/jfs/t1/292017/24/21564/21104/68a6aec2Fdb947196/81e3470e8add05a1.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(58,'福建平和蜜柚',1,'福建平和蜜柚基地',22.50,160,'果大皮薄、果肉清甜微酸，耐储存',1,'https://img14.360buyimg.com/n1/jfs/t1/519382/14/4648/136514/6a9e7605F7eb44de8/0083320320d69caf.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(59,'海南香蕉',1,'海南乐东香蕉园',39.50,190,'自然熟成，果肉绵软香甜',1,'https://img10.360buyimg.com/N1/jfs/t1/212566/34/5460/56458/619c9f6bE6c7ace3a/0485ecb38f138a70.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(60,'广东砂糖橘',1,'广东四会柑橘合作社',35.80,210,'皮薄易剥、清甜化渣，冬季时令水果',1,'https://img10.360buyimg.com/N1/jfs/t1/41476/37/19253/724703/63293cb2Eccdd33df/2429c8f2da0b90e5.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(61,'陕西猕猴桃',1,'陕西周至猕猴桃基地',38.00,170,'果肉翠绿、酸甜适口，维C含量高',1,'https://img10.360buyimg.com/N1/jfs/t1/497496/13/5543/181049/6a75389aF3ba3ae8b/0083320320437283.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(62,'山东大樱桃',1,'山东烟台樱桃园',26.80,280,'果大核小、色泽深红，脆甜多汁',1,'https://img10.360buyimg.com/N1/jfs/t1/500172/4/8592/190434/6a827167F12530823/0083320320530511.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(63,'浙江仙居杨梅',1,'浙江仙居杨梅合作社',48.00,140,'果肉细腻、酸甜生津，夏季限定',1,'https://img10.360buyimg.com/N1/jfs/t1/436503/23/14920/404621/6a12d66eF7279b2df/008396096008e6dd.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(64,'云南冰糖橙',1,'云南玉溪橙园',52.00,89,'皮薄化渣、清甜不酸，果汁充盈',1,'https://img10.360buyimg.com/N1/jfs/t20261218/372918/37/17920/160296/6943ccbdF5e543229/0919320320314120.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(65,'广西沃柑',1,'广西武鸣沃柑基地',29.80,128,'果肉脆嫩、甜度高，易剥皮',1,'https://m.360buyimg.com/mobilecms/s750x750_jfs/t1/389822/30/8805/181766/697b1156F0d27f7da/0083320320d6bbcb.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(66,'有机菠菜',2,'山东寿光蔬菜合作社',89.00,181,'叶绿根红、口感清甜，有机种植无农药残留',1,'https://img10.360buyimg.com/N1/jfs/t1/249688/10/27727/218625/6752b3a0F8f31dbd7/bf6a997f230d14eb.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(67,'大白菜',2,'河北张家口蔬菜基地',129.00,108,'叶球紧实、帮薄味甜，适合炖煮腌渍',1,'https://img14.360buyimg.com/n1/jfs/t1/18718/12/16222/189579/62860b31E663374e8/9407ea6a7fe40cfb.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(68,'沙瓤西红柿',2,'山东寿光蔬菜合作社',149.00,80,'自然成熟，沙瓤多汁，酸甜浓郁',1,'https://img10.360buyimg.com/N1/jfs/t1/303928/36/24634/131640/689425f2Fc3d7ff8a/91948cb2716f1df5.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(69,'水果黄瓜',2,'辽宁大连蔬菜基地',79.80,100,'顶花带刺、清脆爽口，可直接生食',1,'https://img10.360buyimg.com/N1/jfs/t1/406352/36/14628/1092662/69c10580F4b2458ce/093532032021e132.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(70,'新鲜西兰花',2,'云南昆明蔬菜基地',69.00,150,'花球紧实、色泽翠绿，焯水凉拌皆宜',1,'https://img10.360buyimg.com/N1/jfs/t1/453950/14/12056/204347/6a32386dF271ab62d/00833203209d19d9.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(71,'紫皮长茄子',2,'江苏徐州蔬菜基地',128.00,95,'皮薄肉厚、少籽易熟，适合红烧',1,'https://img10.360buyimg.com/N1/jfs/t1/510353/34/10713/922783/6a9552dbF7ebdfb0f/093532032095525d.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(72,'甜玉米',2,'吉林公主岭玉米基地',45.00,48,'颗粒饱满、清甜多汁，蒸煮即食',1,'https://img10.360buyimg.com/N1/jfs/t1/434532/15/18872/230374/6a0d8644F8b91e283/00834f94f998ed4a.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(73,'新鲜莲藕',2,'湖北洪湖莲藕基地',69.90,120,'孔小肉厚、脆嫩拉丝，煲汤清炒皆可',1,'https://img10.360buyimg.com/N1/jfs/t1/492752/32/8415/613633/6a7485f8F59bc1296/09353203202ea8b7.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(74,'鲜嫩芦笋',2,'江苏南通芦笋基地',79.00,130,'茎嫩尖脆、清香味美，低脂高纤维',1,'https://img10.360buyimg.com/N1/jfs/t4978/84/1448491972/229093/920c116b/58f084c2N931fe7bc.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(75,'有机胡萝卜',2,'山东潍坊有机农场',58.00,0,'色泽橙红、口感清甜，富含胡萝卜素',1,'https://img10.360buyimg.com/N1/jfs/t3055/183/4211725261/121863/d26b473/58396f0eN95b41f90.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(76,'黄心土豆',3,'内蒙古乌兰察布马铃薯基地',99.00,220,'淀粉含量高、口感绵密，适合炖煮',1,'https://m.360buyimg.com/mobilecms/s750x750_jfs/t20261222/374142/14/16456/1119999/69493600F6e801d1d/09193203208a3fb6.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(77,'荷兰土豆',3,'甘肃定西马铃薯基地',88.00,100,'个头均匀、皮薄易削，炒制不易碎',1,'https://img14.360buyimg.com/n1/jfs/t1/427738/31/21142/92929/69ff3821F3c7163e8/0083320320d11cbd.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(78,'山东大姜',3,'山东莱芜生姜合作社',198.00,70,'辛辣浓郁、姜味足，厨房常备',1,'https://img14.360buyimg.com/n1/jfs/t1/273784/23/28878/51718/681a182dF9bef80eb/d9b74f3695fbf47a.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(79,'紫皮大蒜',3,'山东金乡大蒜基地',78.00,110,'蒜瓣饱满、辛辣味浓，易剥皮',1,'https://img14.360buyimg.com/n1/jfs/t1/434554/22/18017/128168/6a0d4fe6F9ef7b302/00833203200bf422.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(80,'白洋葱',3,'甘肃张掖洋葱基地',118.00,85,'肉质厚实、甜味足，炒制增香',1,'https://img14.360buyimg.com/n1/jfs/t1/235640/8/25710/79271/66d03cdbF576d5e1e/3fc047b9a1a32ed6.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(81,'新鲜山药',3,'河南焦作铁棍山药基地',238.00,60,'粉糯细腻、口感绵软，适合煲汤',1,'https://img14.360buyimg.com/n1/jfs/t1/348099/20/13361/100642/68ef7955F17386607/01c596969aa9d0f9.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(82,'鲜香菇',3,'福建古田食用菌基地',45.00,150,'菌盖厚实、香气浓郁，鲜嫩爽滑',1,'https://img14.360buyimg.com/n1/jfs/t1/481667/12/7610/366086/6a61768dF84dd5504/00833e83e82dbaad.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(83,'金针菇',3,'江苏灌南食用菌基地',42.00,160,'菌柄细长、口感爽脆，涮煮皆宜',1,'https://img14.360buyimg.com/n1/jfs/t1/279616/3/8631/84797/67e2597eF14776f0c/e3581743d915115b.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(84,'杏鲍菇',3,'河北平泉食用菌基地',58.00,300,'菌肉肥厚、口感似鲍，煎炒烤制均可',1,'https://img14.360buyimg.com/n1/jfs/t1/423855/33/15440/67707/69eecb7cF6085e1d0/0083320320bf7ffa.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(85,'黑木耳',3,'黑龙江东宁黑木耳基地',128.00,250,'朵大肉厚、泡发率高，凉拌炒制皆宜',1,'https://img14.360buyimg.com/n1/jfs/t1/406419/28/12035/183768/69c1f62bF231f4155/0083320320a2386f.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(86,'银耳',3,'福建古田食用菌基地',68.00,280,'胶质丰富、泡发饱满，适合炖煮甜汤',1,'https://img10.360buyimg.com/N1/jfs/t1/487101/28/11072/546657/6a749621F50323869/0935320320a72706.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(87,'东北珍珠米',4,'黑龙江五常稻米合作社',79.00,220,'颗粒圆润、米香浓郁，煮饭软糯有弹性',1,'https://img10.360buyimg.com/N1/jfs/t1/338990/3/6600/136675/68b927fcF98d75374/4bf9e240454206ac.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(88,'五常稻花香大米',4,'黑龙江五常稻米合作社',55.00,240,'粒长饱满、饭香四溢，冷饭不回生',1,'https://img10.360buyimg.com/N1/jfs/t1/510137/3/13111/153002/6a9e8e80F5dee763c/0083320320e8aea3.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(89,'花生油',4,'山东鲁花粮油',15.00,350,'物理压榨、香味浓郁，适合中式炒菜',1,'https://img14.360buyimg.com/n1/jfs/t1/511290/9/11975/177578/6a9e8b32Faa38fa36/0083320320b537f3.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(90,'黑芝麻香油',4,'河南驻马店粮油厂',29.80,200,'小磨工艺、香气醇厚，凉拌调味佳品',1,'https://img10.360buyimg.com/N1/jfs/t1/512073/34/7675/169248/6a952705Fdc4ed6f2/00833273279e809f.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(91,'土鸡蛋',5,'湖北神农架土鸡养殖场',49.80,180,'蛋黄色泽金黄、口感香浓，散养土鸡所产',1,'https://img14.360buyimg.com/n1/jfs/t1/413708/17/1559/78285/69ccdae2Ffc4db561/0083320320329a22.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(92,'农家散养鸭蛋',5,'江苏高邮鸭业',118.00,90,'蛋黄油润起沙，适合腌制咸鸭蛋',1,'https://img10.360buyimg.com/N1/jfs/t1/280111/23/12926/109188/67ea2abcFa0d6a196/e2b15a9ddf185944.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(93,'散养土鸡',5,'山东沂蒙山土鸡养殖场',58.00,140,'肉质紧实、汤色清亮，炖汤鲜美',1,'https://img10.360buyimg.com/N1/jfs/t1/112268/23/40718/157633/64e58390Fe5818c38/30ebc668998f8772.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(94,'黑猪五花肉',5,'黑龙江北大荒黑猪养殖场',88.00,80,'肥瘦相间、肉质细嫩，红烧炖煮皆宜',1,'https://img10.360buyimg.com/N1/jfs/t1/486986/22/1180/88953/6a5d93e0F5fe5516a/00831f41f4467831.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(95,'牛腱子肉',5,'内蒙古锡林郭勒牧场',98.00,100,'筋膜丰富、卤制入味，切片紧实',1,'https://img10.360buyimg.com/N1/jfs/t1/435353/15/18372/245187/6a0e7313F8cd21a12/00833203203aa526.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(96,'新鲜带鱼',5,'浙江舟山渔场',65.00,110,'银白光亮、肉质细嫩，清蒸红烧均可',1,'https://img14.360buyimg.com/n1/jfs/t1/463361/6/9997/119932/6a3f191eF3413f628/0083320320e744ec.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(97,'厄瓜多尔白虾',6,'厄瓜多尔进口海鲜',168.00,70,'虾体饱满、肉质弹嫩，冷链直发',1,'https://img14.360buyimg.com/n1/jfs/t1/443508/24/5584/160349/6a1938c1Fad79f0d1/008332032090eaee.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(98,'阿拉斯加帝王蟹',6,'阿拉斯加深海渔业',128.00,84,'蟹肉饱满、鲜甜紧实，深海捕捞',1,'https://img14.360buyimg.com/n1/jfs/t1/297621/39/17985/183753/68597240Fb74398a8/0884e71d9993b1fa.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(99,'挪威三文鱼',6,'挪威海产进口商',78.00,95,'油脂分布均匀、入口即化，可生食',1,'https://img14.360buyimg.com/n1/jfs/t1/523050/12/2858/189538/6a9fe9d1Fa801d0b9/00833203201cb29a.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(100,'大连生蚝',6,'辽宁大连海产基地',89.00,105,'肉质肥美、汁水鲜甜，炭烤蒜蓉皆宜',1,'https://img14.360buyimg.com/n1/jfs/t1/337557/7/13630/249010/68db82b8F69cc7a81/68cd1c917a0e578b.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(101,'黄心猕猴桃',1,'陕西眉县猕猴桃基地',32.00,100,'果心金黄、甜度高，果肉细腻',1,'https://img14.360buyimg.com/n1/jfs/t1/484368/29/9260/206158/6a69d29eFa7ca4409/00833203202dd5c4.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(102,'云南蓝莓',1,'云南曲靖蓝莓基地',35.00,90,'果粒饱满、酸甜适口，花青素丰富',1,'https://img10.360buyimg.com/N1/jfs/t1/437247/16/15686/200830/6a101d07F19005b87/00833e83e8793ee1.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(103,'智利车厘子',1,'智利进口果品',37.00,120,'果大色深、脆甜多汁，冬季进口水果',1,'https://img14.360buyimg.com/n1/jfs/t1/338269/33/14976/164302/68d5fa34F1db3fa4a/f3ef6373011c6cf3.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06'),(105,'有机蔬菜礼盒',7,'山东寿光蔬菜合作社',128.00,60,'精选当季有机蔬菜组合装，产地直发新鲜到家',1,'https://img14.360buyimg.com/n1/jfs/t1/389713/22/18712/157528/69896a1dFf52eb48c/0083320320738819.png','2025-12-02 17:32:36','2026-01-02 18:53:06'),(106,'云南野生菌干货',7,'云南楚雄山珍合作社',68.00,45,'松茸、牛肝菌等山珍干货，煲汤提鲜',1,'https://img14.360buyimg.com/n1/jfs/t1/524648/7/155/219906/6a9e42dcF10cd703f/0083320320334c02.jpg','2025-12-02 17:32:36','2026-01-02 18:53:06');
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
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='农产品套餐主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `produce_package`
--

LOCK TABLES `produce_package` WRITE;
/*!40000 ALTER TABLE `produce_package` DISABLE KEYS */;
INSERT INTO `produce_package` VALUES (1,'时令水果尝鲜装','烟台苹果、洛川苹果、云南蓝莓、智利车厘子组合，一次尝遍四季鲜果','https://img14.360buyimg.com/n1/jfs/t1/437247/16/15686/200830/6a101d07F19005b87/00833e83e8793ee1.png',172.00,139.00,4,1,1,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(2,'菌菇干货组合装','鲜香菇、黑木耳、银耳组合，菌香浓郁，煲汤炒菜皆宜','https://img14.360buyimg.com/n1/jfs/t1/481667/12/7610/366086/6a61768dF84dd5504/00833e83e82dbaad.jpg',254.00,199.00,3,1,1,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(3,'厨房常备调味套装','大姜、大蒜、洋葱、山药组合，日常烹饪必备食材','https://img14.360buyimg.com/n1/jfs/t1/348099/20/13361/100642/68ef7955F17386607/01c596969aa9d0f9.jpg',225.80,199.00,0,1,0,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(4,'根茎蔬菜家庭装','土豆、荷兰土豆、紫皮大蒜组合，根茎类家常食材，耐储存易烹饪','https://img14.360buyimg.com/n1/jfs/t1/510353/34/10713/922783/6a9552dbF7ebdfb0f/093532032095525d.png',265.00,229.00,0,1,0,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(5,'米面粮油优选装','珍珠米、稻花香大米、花生油、香油组合，厨房主食一站购齐','https://img10.360buyimg.com/N1/jfs/t1/510137/3/13111/153002/6a9e8e80F5dee763c/0083320320e8aea3.jpg',332.90,299.00,0,1,0,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(6,'肉禽蛋品精选装','土鸡蛋、农家散养鸭蛋、散养土鸡组合，源头直供冷链配送','https://img14.360buyimg.com/n1/jfs/t1/280111/23/12926/109188/67ea2abcFa0d6a196/e2b15a9ddf185944.jpg',75.30,59.00,0,1,0,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(7,'地方特产礼盒装','烟台苹果、库尔勒香梨、蒙自石榴、仙居杨梅组合，产地直采送礼自用皆宜','https://img10.360buyimg.com/N1/jfs/t1/426640/34/21828/121326/69fc3272F63741e50/093532032030d808.png',169.79,129.00,0,1,0,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(8,'有机蔬菜轻食装','有机蔬菜礼盒、有机菠菜、水果黄瓜、甜玉米组合，低脂健康轻食首选','https://img10.360buyimg.com/N1/jfs/t1/389713/22/18712/157528/69896a1dFf52eb48c/0083320320738819.png',132.80,109.00,0,1,0,'2026-06-11 10:40:41','2026-06-11 10:40:40'),(9,'菌菇山珍组合装','鲜香菇、金针菇、杏鲍菇、黑木耳组合，煲汤炒菜鲜味十足','https://img14.360buyimg.com/n1/jfs/t1/279616/3/8631/84797/67e2597eF14776f0c/e3581743d915115b.jpg',92.00,79.00,0,1,0,'2026-06-11 10:40:41','2026-06-11 10:40:40');
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

-- Dump completed on 2026-09-09 20:05:58
