-- MySQL dump 10.13  Distrib 5.1.41, for debian-linux-gnu (x86_64)
--
-- Host: 192.168.0.1    Database: baehnle10
-- ------------------------------------------------------
-- Server version	5.0.51a-3ubuntu5

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Not dumping tablespaces as no INFORMATION_SCHEMA.FILES table on this server
--

--
-- Current Database: `baehnle10`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `baehnle10` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `baehnle10`;

--
-- Table structure for table `locomotive`
--

DROP TABLE IF EXISTS `locomotive`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `locomotive` (
  `id` int(11) NOT NULL auto_increment,
  `locomotive_group_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) default NULL,
  `image` varchar(255) default NULL,
  `address` int(11) NOT NULL,
  `bus` int(11) NOT NULL,
  `locomotive_type_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `locomotive_locomotive_group_id` (`locomotive_group_id`),
  KEY `locomotive_locomotive_type_fk` (`locomotive_type_id`),
  CONSTRAINT `locomotive_locomotive_group_id` FOREIGN KEY (`locomotive_group_id`) REFERENCES `locomotive_group` (`id`),
  CONSTRAINT `locomotive_locomotive_type_fk` FOREIGN KEY (`locomotive_type_id`) REFERENCES `locomotive_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locomotive`
--

LOCK TABLES `locomotive` WRITE;
/*!40000 ALTER TABLE `locomotive` DISABLE KEYS */;
INSERT INTO `locomotive` VALUES (1,1,'Ae 6/6 Altdorf rot','Ae 6/6 Altdorf rot','',21,1,1),(2,1,'Ae 6/6 Appenzell i. Rh','Ae 6/6 Appenzell i. Rh','',49,1,1),(3,1,'Ae 6/6 Baselland gruen','Ae 6/6 Baselland gruen','',39,1,1),(4,1,'Ae 6/6 Bern gruen (Jüre)','Ae 6/6 Bern gruen (Jüre)','',33,1,1),(5,1,'Ae 6/6 Bern gruen (Mätthu)','Ae 6/6 Bern gruen (Mätthu)','',1,1,1),(6,1,'Ae 6/6 Schaffhausen','Ae 6/6 Schaffhausen','',32,1,1),(7,2,'Big Boy','UP Klasse 4000 Big Boy','',40,1,1),(8,2,'Dampflok DB','Dampflok DB','',5,1,1),(9,2,'Dampflok DB BR 003','Dampflok DB BR 003','',41,1,1),(10,2,'Dampflok DB BR 01','Dampflok DB BR 01','',35,1,1),(11,2,'Dampflok DB, BR 89','Dampflok DB, BR 89','',14,1,1),(12,2,'Dampflok-Grosi','Dampflok','',77,1,1),(13,3,'Diesellok DB BR 212','Diesellok DB BR 212','',12,1,1),(14,3,'Diesellok DB BR 216','Diesellok DB BR 216','',36,1,1),(15,3,'Diesellok DB Post','Diesellok DB Post','',17,1,1),(16,3,'Rangierlok Am 842 Cargo','Rangierlok Am 842 Cargo','',64,1,1),(17,3,'Rangierlok Ee 3/3 rot','Rangierlok Ee 3/3 rot','',60,1,1),(18,3,'Rangierlok SBB Te III ','Rangierlok SBB Te III','',20,1,1),(19,4,'Re 4/4 Bahn 2000 rot (Jöggu)','Re 4/4 Bahn 2000 rot (Jöggu)','',16,1,1),(20,4,'Re 4/4 Bahn 2000 rot (Jüre)','Re 4/4 Bahn 2000 rot (Jüre)','',34,1,1),(21,4,'Re 4/4 GBS braun','Re 4/4 GBS braun','',43,1,1),(22,4,'Re 4/4 II Tee creme-rot','Re 4/4 II tee creme-rot','',23,1,1),(23,4,'Re 4/4 II rot','Re 4/4 II rot','',25,1,1),(24,4,'Re 4/4 IV rot/grau','Re 4/4 IV rot/grau','',22,1,1),(25,5,'Re 460 Rigi rot','Re 460 Rigi rot','',6,1,1),(26,4,'Re 4/4 Bahn 2000 rot (Geri)','Re 4/4 SBB Bahn 2000 rot (Geri)','',54,1,1),(27,4,'Re 4/4 gruen (HAG)','Re 4/4 SBB gruen','',44,1,1),(28,5,'Re 460 Ascom','RE460 Ascom','',24,1,1),(29,5,'Re 460 Cargo','Re 460 Cargo','',9,1,1),(30,5,'Re 460 Miele','Re 460 Miele','',28,1,1),(31,5,'Re 460 Nord Vaudois rot','Re 460 Nord Vaudois rot','',18,1,1),(32,5,'Re 460 Post','Re 460 Post','',46,1,1),(33,5,'Re 460 Rola','Rola/Connecting Europe','',30,1,1),(34,5,'Re 460 SF DRS','Re460 SF DRS','',26,1,1),(35,5,'Re 460 Verkehrshaus Luzern','Re 460 Verkehrshaus Luzern','',11,1,1),(36,5,'Re 460 Zugkraft Aargau','Re 460 Zugkraft Aargau','',10,1,1),(37,5,'Re 460 la Gruyere rot','Re 460 la Gruyere rot','',45,1,1),(38,5,'Re 465 BLS Saas Fee','Re 465 BLS Saas Fee','',47,1,1),(39,6,'Re 6/6 Murgenthal rot','Re 6/6 Murgenthal rot','',29,1,1),(40,6,'Re 6/6 SBB Amsteg gruen','Re 6/6 SBB Amsteg gruen','',48,1,1),(41,6,'Re 6/6 zweiteilig gruen','Re 6/6 zweiteilig gruen','',27,1,1),(42,7,'Ae 3/6 II braun','Ae 3/6 II braun','',38,1,1),(43,7,'Ae 4/7 SBB grün','Ae 4/7 SBB grün','',3,1,1),(44,7,'Ae 8/14 Doppelschnauz gruen','Ae 8/14 Doppelschnauz gruen','',19,1,1),(45,7,'Ae 8/8 braun','Ae 8/8 braun','',62,1,1),(46,7,'BDe 4/4 SBB gruen','BDe 4/4 SBB gruen','',42,1,1),(47,7,'Krokodil Juere','Krokodil Juere','',50,1,1),(48,7,'Krokodil gruen','Krokodil gruen','',15,1,1),(49,7,'RBe 2/4 Blauer Pfeil','RBe 2/4 Blauer Pfeil','',52,1,1),(50,8,'E-Lok Schweden SJ braun','E-Lok Schweden SJ braun','',31,1,1),(51,8,'ICE 2','ICE 2','',37,1,1),(52,8,'Sante Fe','Sante Fe','',7,1,1),(53,1,'Ae 6/6 Altdorf rot (Fridu)','Ae 6/6 Altdorf rot (Fridu)','',61,1,1),(54,4,'Re 4/4 I rot','Re 4/4 I rot','',55,1,1);
/*!40000 ALTER TABLE `locomotive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locomotive_group`
--

DROP TABLE IF EXISTS `locomotive_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `locomotive_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locomotive_group`
--

LOCK TABLES `locomotive_group` WRITE;
/*!40000 ALTER TABLE `locomotive_group` DISABLE KEYS */;
INSERT INTO `locomotive_group` VALUES (1,'Ae 6/6'),(2,'Dampflok'),(3,'Rangierloks'),(4,'Re 4/4'),(5,'Re 460'),(6,'Re 6/6'),(7,'div. CH'),(8,'div. Int.');
/*!40000 ALTER TABLE `locomotive_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locomotive_type`
--

DROP TABLE IF EXISTS `locomotive_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `locomotive_type` (
  `id` int(11) NOT NULL auto_increment,
  `type_name` varchar(255) NOT NULL,
  `drivingSteps` int(11) NOT NULL,
  `functionCount` int(11) NOT NULL,
  `stepping` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `locomotive_type`
--

LOCK TABLES `locomotive_type` WRITE;
/*!40000 ALTER TABLE `locomotive_type` DISABLE KEYS */;
INSERT INTO `locomotive_type` VALUES (1,'DELTA',14,4,4),(2,'DIGITAL',28,5,2);
/*!40000 ALTER TABLE `locomotive_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route`
--

DROP TABLE IF EXISTS `route`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `route` (
  `id` int(11) NOT NULL auto_increment,
  `route_group_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `route_route_group_fk` (`route_group_id`),
  CONSTRAINT `route_route_group_fk` FOREIGN KEY (`route_group_id`) REFERENCES `route_group` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=99 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `route`
--

LOCK TABLES `route` WRITE;
/*!40000 ALTER TABLE `route` DISABLE KEYS */;
INSERT INTO `route` VALUES (1,1,1,'A 1'),(2,1,2,'A 2'),(3,1,3,'A 3'),(4,1,4,'A 4'),(5,1,5,'B 1'),(6,1,6,'B 2'),(7,1,7,'B 3'),(8,1,8,'B 4'),(9,2,9,'C 1'),(10,2,10,'C 2'),(11,2,11,'C 3'),(12,2,12,'C 4'),(13,2,13,'C 5'),(14,2,14,'C 6'),(15,2,15,'C 7'),(16,2,16,'C 8'),(17,2,17,'C 9'),(18,2,18,'C 10'),(19,2,19,'C 11'),(20,2,20,'C 12'),(21,2,21,'C 13'),(22,2,22,'C 14'),(23,2,23,'D 1'),(24,2,24,'D 2'),(25,2,25,'D 3'),(26,2,26,'D 4'),(27,2,27,'D 5'),(28,2,28,'D 6'),(30,2,29,'D 7'),(31,2,30,'D 8'),(32,2,31,'D 9'),(33,2,32,'D 10'),(34,2,33,'D 11'),(35,2,34,'D 12'),(36,2,35,'D 13'),(37,2,36,'D 14'),(38,4,37,'K 1'),(39,4,38,'K 2'),(40,4,39,'K 3'),(41,4,40,'K 4'),(42,4,41,'K 5'),(43,4,42,'K 6'),(44,4,43,'K 7'),(45,4,44,'K 8'),(46,4,45,'K 9'),(47,4,46,'K 10'),(48,4,47,'K 11'),(49,2,48,'E 1'),(50,2,49,'E 2'),(51,2,50,'E 3'),(52,2,51,'E 4'),(53,2,52,'E 5'),(54,2,53,'E 6'),(55,2,54,'E 7'),(56,2,55,'E 8'),(57,2,56,'E 9'),(58,2,57,'E 10'),(59,2,58,'E 11'),(60,2,59,'E 12'),(61,2,60,'E 13'),(62,2,61,'E 14'),(63,2,62,'F 1'),(64,2,63,'F 2'),(65,2,64,'F 3'),(66,2,65,'F 4'),(67,2,66,'F 5'),(68,2,67,'F 6'),(69,2,68,'F 7'),(70,2,69,'F 8'),(71,2,70,'F 9'),(72,2,71,'F 10'),(73,2,72,'F 11'),(74,2,73,'F 12'),(75,2,74,'F 13'),(76,2,75,'F 14'),(77,3,76,'G 1'),(78,3,77,'G 2'),(79,3,78,'G 3'),(82,3,79,'G 4'),(83,3,80,'G 5'),(84,3,81,'G 6'),(93,3,82,'H 1'),(94,3,83,'H 2'),(95,3,84,'H 3'),(96,3,85,'H 4'),(97,3,86,'H 5'),(98,3,87,'H 6');
/*!40000 ALTER TABLE `route` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route_group`
--

DROP TABLE IF EXISTS `route_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `route_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `weight` int(11) default NULL,
  `route_number_offset` int(11) NOT NULL default '0',
  `route_number_amount` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `route_group`
--

LOCK TABLES `route_group` WRITE;
/*!40000 ALTER TABLE `route_group` DISABLE KEYS */;
INSERT INTO `route_group` VALUES (1,'Bhf Links',NULL,0,0),(2,'Bhf Mitte',NULL,0,0),(3,'Bhf Rechts',NULL,0,0),(4,'Rangierbahnhof',NULL,0,0);
/*!40000 ALTER TABLE `route_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route_item`
--

DROP TABLE IF EXISTS `route_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `route_item` (
  `id` int(11) NOT NULL auto_increment,
  `route_id` int(11) NOT NULL,
  `turnout_id` int(11) NOT NULL,
  `routed_state` enum('STRAIGHT','LEFT','RIGHT') NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `route_item_route_id_fk` (`route_id`),
  KEY `route_item_turnout_id_fk` (`turnout_id`),
  CONSTRAINT `route_item_route_id_fk` FOREIGN KEY (`route_id`) REFERENCES `route` (`id`),
  CONSTRAINT `route_item_turnout_id_fk` FOREIGN KEY (`turnout_id`) REFERENCES `turnout` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=431 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `route_item`
--

LOCK TABLES `route_item` WRITE;
/*!40000 ALTER TABLE `route_item` DISABLE KEYS */;
INSERT INTO `route_item` VALUES (1,1,3,'STRAIGHT'),(2,1,6,'LEFT'),(3,1,7,'STRAIGHT'),(4,2,3,'STRAIGHT'),(5,2,6,'LEFT'),(6,2,7,'LEFT'),(7,3,3,'LEFT'),(8,3,4,'RIGHT'),(9,3,5,'LEFT'),(10,4,3,'LEFT'),(11,4,4,'RIGHT'),(12,4,5,'STRAIGHT'),(13,5,13,'LEFT'),(14,5,11,'RIGHT'),(15,5,10,'STRAIGHT'),(16,6,13,'LEFT'),(17,6,11,'RIGHT'),(18,6,10,'LEFT'),(19,7,13,'STRAIGHT'),(20,7,9,'LEFT'),(21,7,8,'LEFT'),(23,8,9,'LEFT'),(24,8,8,'STRAIGHT'),(25,9,15,'STRAIGHT'),(26,9,18,'LEFT'),(27,9,19,'STRAIGHT'),(28,9,20,'STRAIGHT'),(29,9,21,'STRAIGHT'),(30,9,22,'STRAIGHT'),(31,10,15,'STRAIGHT'),(33,10,19,'STRAIGHT'),(34,10,20,'STRAIGHT'),(35,10,21,'STRAIGHT'),(36,10,22,'LEFT'),(37,11,15,'STRAIGHT'),(38,11,18,'LEFT'),(39,11,19,'STRAIGHT'),(40,11,20,'STRAIGHT'),(41,11,21,'LEFT'),(42,12,15,'STRAIGHT'),(43,12,18,'LEFT'),(44,12,19,'STRAIGHT'),(45,12,20,'LEFT'),(46,13,15,'STRAIGHT'),(47,13,18,'LEFT'),(48,13,19,'LEFT'),(49,14,15,'LEFT'),(50,14,16,'STRAIGHT'),(51,14,17,'STRAIGHT'),(52,15,15,'LEFT'),(55,16,15,'LEFT'),(56,16,16,'LEFT'),(59,17,15,'LEFT'),(60,17,16,'LEFT'),(61,17,33,'RIGHT'),(64,18,15,'LEFT'),(65,18,16,'LEFT'),(66,18,33,'RIGHT'),(67,18,34,'STRAIGHT'),(70,15,16,'STRAIGHT'),(71,15,17,'STRAIGHT'),(72,16,33,'STRAIGHT'),(73,17,34,'LEFT'),(74,18,35,'LEFT'),(76,19,15,'LEFT'),(77,19,16,'LEFT'),(78,19,33,'RIGHT'),(79,19,34,'STRAIGHT'),(80,19,35,'STRAIGHT'),(81,19,36,'LEFT'),(82,20,15,'LEFT'),(83,20,16,'LEFT'),(84,20,33,'RIGHT'),(85,20,34,'STRAIGHT'),(86,20,35,'STRAIGHT'),(87,20,36,'STRAIGHT'),(88,20,37,'LEFT'),(89,21,15,'LEFT'),(90,21,16,'LEFT'),(91,21,33,'RIGHT'),(92,21,34,'STRAIGHT'),(93,21,35,'STRAIGHT'),(94,21,36,'STRAIGHT'),(95,21,37,'STRAIGHT'),(96,21,38,'LEFT'),(97,22,15,'LEFT'),(98,22,16,'LEFT'),(99,22,33,'RIGHT'),(100,22,34,'STRAIGHT'),(101,22,35,'STRAIGHT'),(102,22,36,'STRAIGHT'),(103,22,37,'STRAIGHT'),(104,22,38,'STRAIGHT'),(105,23,33,'LEFT'),(106,23,17,'STRAIGHT'),(107,23,18,'STRAIGHT'),(108,23,19,'STRAIGHT'),(109,23,20,'STRAIGHT'),(110,23,21,'STRAIGHT'),(111,23,22,'STRAIGHT'),(112,24,33,'LEFT'),(113,24,17,'STRAIGHT'),(114,24,18,'STRAIGHT'),(115,24,19,'STRAIGHT'),(116,24,20,'STRAIGHT'),(117,24,21,'STRAIGHT'),(118,24,22,'LEFT'),(119,25,33,'LEFT'),(120,25,17,'STRAIGHT'),(121,25,18,'STRAIGHT'),(122,25,19,'STRAIGHT'),(123,25,20,'STRAIGHT'),(124,25,21,'LEFT'),(125,26,33,'LEFT'),(126,26,17,'STRAIGHT'),(127,26,18,'STRAIGHT'),(128,26,19,'STRAIGHT'),(129,26,20,'LEFT'),(130,27,33,'LEFT'),(131,27,17,'STRAIGHT'),(132,27,18,'STRAIGHT'),(133,27,19,'LEFT'),(134,28,33,'LEFT'),(135,28,17,'STRAIGHT'),(136,28,18,'LEFT'),(137,30,33,'LEFT'),(138,30,17,'LEFT'),(139,31,33,'STRAIGHT'),(140,32,33,'RIGHT'),(141,32,34,'LEFT'),(142,33,33,'RIGHT'),(143,33,34,'STRAIGHT'),(144,33,35,'LEFT'),(145,34,33,'RIGHT'),(146,34,34,'STRAIGHT'),(147,34,35,'STRAIGHT'),(148,34,36,'LEFT'),(149,35,33,'RIGHT'),(150,35,34,'STRAIGHT'),(151,35,35,'STRAIGHT'),(152,35,36,'STRAIGHT'),(153,35,37,'LEFT'),(154,36,33,'RIGHT'),(155,36,34,'STRAIGHT'),(156,36,35,'STRAIGHT'),(157,36,36,'STRAIGHT'),(158,36,37,'STRAIGHT'),(159,36,38,'LEFT'),(160,37,33,'RIGHT'),(161,37,34,'STRAIGHT'),(162,37,35,'STRAIGHT'),(163,37,36,'STRAIGHT'),(164,37,37,'STRAIGHT'),(165,37,38,'STRAIGHT'),(166,38,77,'LEFT'),(167,39,77,'STRAIGHT'),(168,39,78,'LEFT'),(169,39,79,'STRAIGHT'),(170,39,80,'STRAIGHT'),(171,39,81,'STRAIGHT'),(172,40,77,'STRAIGHT'),(173,40,78,'LEFT'),(174,40,79,'STRAIGHT'),(175,40,80,'STRAIGHT'),(176,40,81,'LEFT'),(177,41,77,'STRAIGHT'),(178,41,78,'LEFT'),(179,41,79,'STRAIGHT'),(180,41,80,'LEFT'),(181,42,77,'STRAIGHT'),(182,42,78,'LEFT'),(183,42,79,'LEFT'),(184,43,77,'STRAIGHT'),(185,43,78,'STRAIGHT'),(186,44,77,'STRAIGHT'),(187,44,78,'RIGHT'),(188,44,82,'LEFT'),(189,45,77,'STRAIGHT'),(191,45,82,'STRAIGHT'),(193,46,77,'STRAIGHT'),(194,46,78,'RIGHT'),(195,46,82,'STRAIGHT'),(196,46,83,'STRAIGHT'),(197,46,84,'LEFT'),(198,47,77,'STRAIGHT'),(199,47,78,'RIGHT'),(200,47,82,'STRAIGHT'),(201,47,83,'STRAIGHT'),(202,47,84,'STRAIGHT'),(203,48,77,'RIGHT'),(204,49,29,'STRAIGHT'),(205,49,28,'LEFT'),(206,49,27,'STRAIGHT'),(207,49,26,'STRAIGHT'),(208,49,25,'STRAIGHT'),(209,49,24,'STRAIGHT'),(210,49,23,'STRAIGHT'),(211,50,29,'STRAIGHT'),(212,50,28,'LEFT'),(213,50,27,'STRAIGHT'),(214,50,26,'STRAIGHT'),(215,50,25,'STRAIGHT'),(216,50,24,'STRAIGHT'),(217,50,23,'LEFT'),(220,51,29,'STRAIGHT'),(221,51,28,'LEFT'),(223,51,27,'STRAIGHT'),(224,77,59,'STRAIGHT'),(225,51,26,'STRAIGHT'),(226,77,58,'STRAIGHT'),(227,51,25,'STRAIGHT'),(228,51,24,'LEFT'),(229,52,29,'STRAIGHT'),(231,52,28,'LEFT'),(232,52,27,'STRAIGHT'),(233,52,26,'STRAIGHT'),(235,52,25,'LEFT'),(237,78,59,'STRAIGHT'),(238,78,58,'LEFT'),(239,53,29,'STRAIGHT'),(240,53,28,'LEFT'),(241,53,27,'STRAIGHT'),(242,53,26,'LEFT'),(243,54,29,'STRAIGHT'),(244,54,28,'LEFT'),(245,54,27,'LEFT'),(249,79,59,'LEFT'),(250,55,29,'STRAIGHT'),(252,55,28,'STRAIGHT'),(256,56,29,'LEFT'),(258,56,44,'STRAIGHT'),(259,82,64,'LEFT'),(260,82,61,'RIGHT'),(261,82,63,'LEFT'),(262,57,29,'LEFT'),(263,83,64,'LEFT'),(264,83,61,'RIGHT'),(265,83,63,'STRAIGHT'),(266,83,62,'LEFT'),(267,57,44,'LEFT'),(268,57,43,'LEFT'),(269,84,64,'LEFT'),(270,84,61,'RIGHT'),(271,84,63,'STRAIGHT'),(272,84,62,'STRAIGHT'),(273,58,29,'LEFT'),(274,58,44,'LEFT'),(275,58,43,'STRAIGHT'),(276,58,42,'LEFT'),(277,59,29,'LEFT'),(278,59,44,'LEFT'),(281,59,43,'STRAIGHT'),(285,59,42,'STRAIGHT'),(286,59,41,'LEFT'),(292,60,29,'LEFT'),(294,60,44,'LEFT'),(296,60,43,'STRAIGHT'),(298,60,42,'STRAIGHT'),(300,60,41,'STRAIGHT'),(304,60,40,'LEFT'),(306,61,29,'LEFT'),(307,61,44,'LEFT'),(309,61,43,'STRAIGHT'),(310,61,42,'STRAIGHT'),(311,61,41,'STRAIGHT'),(312,61,40,'STRAIGHT'),(313,61,39,'LEFT'),(317,62,29,'LEFT'),(318,62,44,'LEFT'),(319,62,43,'STRAIGHT'),(322,62,42,'STRAIGHT'),(323,62,41,'STRAIGHT'),(326,62,40,'STRAIGHT'),(327,62,39,'STRAIGHT'),(332,63,45,'LEFT'),(333,63,44,'RIGHT'),(334,63,28,'STRAIGHT'),(335,63,27,'STRAIGHT'),(336,63,26,'STRAIGHT'),(337,63,25,'STRAIGHT'),(338,63,24,'STRAIGHT'),(339,63,23,'STRAIGHT'),(340,64,45,'LEFT'),(341,64,44,'RIGHT'),(342,64,28,'STRAIGHT'),(343,64,27,'STRAIGHT'),(344,64,26,'STRAIGHT'),(345,64,25,'STRAIGHT'),(346,64,24,'STRAIGHT'),(347,64,23,'LEFT'),(348,65,45,'LEFT'),(349,65,44,'RIGHT'),(350,65,28,'STRAIGHT'),(351,65,27,'STRAIGHT'),(352,65,26,'STRAIGHT'),(353,65,25,'STRAIGHT'),(354,65,24,'LEFT'),(355,66,45,'LEFT'),(356,66,44,'RIGHT'),(357,66,28,'STRAIGHT'),(358,66,27,'STRAIGHT'),(359,66,26,'STRAIGHT'),(360,66,25,'LEFT'),(361,67,45,'LEFT'),(362,67,44,'RIGHT'),(363,67,28,'STRAIGHT'),(364,67,27,'STRAIGHT'),(365,67,26,'LEFT'),(366,68,45,'LEFT'),(367,68,44,'RIGHT'),(368,68,28,'STRAIGHT'),(369,68,27,'LEFT'),(370,69,45,'LEFT'),(371,69,44,'RIGHT'),(372,69,28,'LEFT'),(373,70,45,'LEFT'),(374,70,44,'STRAIGHT'),(375,71,45,'STRAIGHT'),(376,71,43,'STRAIGHT'),(377,72,45,'STRAIGHT'),(378,72,43,'LEFT'),(379,72,42,'LEFT'),(380,73,45,'STRAIGHT'),(381,73,43,'LEFT'),(382,73,42,'STRAIGHT'),(384,73,41,'LEFT'),(385,74,45,'STRAIGHT'),(386,74,43,'LEFT'),(387,74,42,'STRAIGHT'),(388,74,41,'STRAIGHT'),(389,74,40,'LEFT'),(390,75,45,'STRAIGHT'),(391,75,43,'LEFT'),(392,75,42,'STRAIGHT'),(393,75,41,'STRAIGHT'),(394,75,40,'STRAIGHT'),(395,75,39,'LEFT'),(396,76,45,'STRAIGHT'),(397,76,43,'LEFT'),(398,76,42,'STRAIGHT'),(399,76,41,'STRAIGHT'),(400,76,40,'STRAIGHT'),(401,76,39,'STRAIGHT'),(402,93,48,'LEFT'),(403,93,51,'RIGHT'),(404,93,52,'STRAIGHT'),(405,93,53,'STRAIGHT'),(406,94,48,'LEFT'),(407,94,51,'RIGHT'),(408,94,52,'STRAIGHT'),(409,94,53,'LEFT'),(410,95,48,'LEFT'),(411,95,51,'RIGHT'),(412,95,52,'LEFT'),(413,96,48,'STRAIGHT'),(414,96,54,'LEFT'),(415,96,55,'LEFT'),(416,97,48,'STRAIGHT'),(417,97,54,'LEFT'),(418,97,55,'STRAIGHT'),(419,97,56,'LEFT'),(421,98,54,'LEFT'),(422,98,55,'STRAIGHT'),(423,98,56,'STRAIGHT'),(424,8,13,'STRAIGHT'),(425,10,18,'LEFT'),(426,77,60,'LEFT'),(427,78,60,'LEFT'),(428,79,60,'LEFT'),(429,45,78,'RIGHT'),(430,45,83,'LEFT');
/*!40000 ALTER TABLE `route_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout`
--

DROP TABLE IF EXISTS `turnout`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `turnout` (
  `id` int(11) NOT NULL auto_increment,
  `turnout_group_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `description` varchar(255) default NULL,
  `default_state` enum('STRAIGHT','LEFT','RIGHT') NOT NULL,
  `orientation` enum('NORTH','EAST','SOUTH','WEST') NOT NULL,
  `turnout_type_id` int(11) NOT NULL,
  `address1` int(11) NOT NULL,
  `address2` int(11) NOT NULL,
  `bus1` int(11) NOT NULL,
  `bus2` int(11) NOT NULL,
  `address1_switched` tinyint(4) NOT NULL,
  `address2_switched` tinyint(4) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `turnout_turnout_group_id_fk` (`turnout_group_id`),
  KEY `turnout_turnout_type_fk` (`turnout_type_id`),
  KEY `turnout_address1_fk` (`address1`),
  KEY `turnout_address2_fk` (`address2`),
  CONSTRAINT `turnout_turnout_group_id_fk` FOREIGN KEY (`turnout_group_id`) REFERENCES `turnout_group` (`id`),
  CONSTRAINT `turnout_turnout_type_fk` FOREIGN KEY (`turnout_type_id`) REFERENCES `turnout_type` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=85 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `turnout`
--

LOCK TABLES `turnout` WRITE;
/*!40000 ALTER TABLE `turnout` DISABLE KEYS */;
INSERT INTO `turnout` VALUES (2,3,1,NULL,'STRAIGHT','EAST',2,103,0,1,0,0,0),(3,3,2,NULL,'STRAIGHT','EAST',2,102,0,1,0,0,0),(4,3,3,NULL,'STRAIGHT','EAST',3,186,185,1,1,0,0),(5,3,4,NULL,'STRAIGHT','EAST',2,104,0,1,0,0,0),(6,3,5,NULL,'STRAIGHT','EAST',1,188,0,1,0,1,0),(7,3,6,NULL,'STRAIGHT','EAST',2,187,0,1,0,1,0),(8,3,7,NULL,'STRAIGHT','EAST',2,216,0,1,0,0,0),(9,3,8,NULL,'STRAIGHT','EAST',1,172,0,1,0,1,0),(10,3,9,NULL,'STRAIGHT','EAST',2,171,0,1,0,0,0),(11,3,10,NULL,'STRAIGHT','EAST',3,169,170,1,1,1,1),(12,3,11,NULL,'STRAIGHT','EAST',2,101,0,1,0,0,0),(13,3,12,NULL,'STRAIGHT','EAST',2,213,0,1,0,1,0),(14,3,13,NULL,'STRAIGHT','EAST',2,151,0,1,0,1,0),(15,2,14,NULL,'STRAIGHT','EAST',2,137,0,1,0,0,0),(16,2,15,NULL,'STRAIGHT','EAST',2,138,0,1,0,1,0),(17,2,16,NULL,'STRAIGHT','EAST',1,140,0,1,0,0,0),(18,2,17,NULL,'STRAIGHT','EAST',1,139,0,1,0,0,0),(19,2,18,NULL,'STRAIGHT','EAST',2,194,0,1,0,1,0),(20,2,19,NULL,'STRAIGHT','EAST',2,193,0,1,0,0,0),(21,2,20,NULL,'STRAIGHT','EAST',2,195,0,1,0,0,0),(22,2,21,NULL,'STRAIGHT','EAST',2,196,0,1,0,0,0),(23,2,22,NULL,'STRAIGHT','EAST',2,152,0,1,0,0,0),(24,2,23,NULL,'STRAIGHT','EAST',2,150,0,1,0,0,0),(25,2,24,NULL,'STRAIGHT','EAST',2,149,0,1,0,1,0),(26,2,25,NULL,'STRAIGHT','EAST',2,203,0,1,0,0,0),(27,2,26,NULL,'STRAIGHT','EAST',1,204,0,1,0,0,0),(28,2,27,NULL,'STRAIGHT','EAST',1,201,0,1,0,1,0),(29,2,28,NULL,'STRAIGHT','EAST',2,202,0,1,0,1,0),(30,2,29,NULL,'STRAIGHT','EAST',2,142,0,1,0,0,0),(31,2,30,NULL,'STRAIGHT','EAST',2,144,0,1,0,0,0),(32,2,31,NULL,'STRAIGHT','EAST',2,189,0,1,0,1,0),(33,2,32,NULL,'STRAIGHT','EAST',3,191,192,1,1,1,1),(34,2,33,NULL,'STRAIGHT','EAST',1,190,0,1,0,0,0),(35,2,34,NULL,'STRAIGHT','EAST',2,134,0,1,0,0,0),(36,2,35,NULL,'STRAIGHT','EAST',2,133,0,1,0,0,0),(37,2,36,NULL,'STRAIGHT','EAST',2,136,0,1,0,0,0),(38,2,37,NULL,'STRAIGHT','EAST',2,135,0,1,0,1,0),(39,2,38,NULL,'STRAIGHT','EAST',2,127,0,1,0,0,0),(40,2,39,NULL,'STRAIGHT','EAST',2,128,0,1,0,1,0),(41,2,40,NULL,'STRAIGHT','EAST',2,126,0,1,0,0,0),(42,2,41,NULL,'STRAIGHT','EAST',2,125,0,1,0,0,0),(43,2,42,NULL,'STRAIGHT','EAST',1,114,0,1,0,1,0),(44,2,43,NULL,'STRAIGHT','EAST',3,113,116,1,1,1,0),(45,2,44,NULL,'STRAIGHT','EAST',2,115,0,1,0,0,0),(46,4,45,NULL,'STRAIGHT','EAST',2,184,0,1,0,1,0),(47,4,46,NULL,'STRAIGHT','EAST',2,183,0,1,0,1,0),(48,4,47,NULL,'STRAIGHT','EAST',2,158,0,1,0,0,0),(49,4,48,NULL,'STRAIGHT','EAST',2,159,0,1,0,0,0),(50,4,49,NULL,'STRAIGHT','EAST',2,157,0,1,0,0,0),(51,4,50,NULL,'STRAIGHT','EAST',3,106,105,1,1,0,1),(52,4,51,NULL,'STRAIGHT','EAST',2,108,0,1,0,0,0),(53,4,52,NULL,'STRAIGHT','EAST',2,107,0,1,0,1,0),(54,4,53,NULL,'STRAIGHT','EAST',1,12,0,1,0,1,0),(55,4,54,NULL,'STRAIGHT','EAST',2,11,0,1,0,0,0),(56,4,55,NULL,'STRAIGHT','EAST',2,10,0,1,0,0,0),(57,4,56,NULL,'STRAIGHT','EAST',2,9,0,1,0,1,0),(58,4,57,NULL,'STRAIGHT','EAST',2,177,0,1,0,1,0),(59,4,58,NULL,'STRAIGHT','EAST',2,179,0,1,0,1,0),(60,4,59,NULL,'STRAIGHT','EAST',1,141,0,1,0,0,0),(61,4,60,NULL,'STRAIGHT','EAST',3,220,219,1,1,0,1),(62,4,61,NULL,'STRAIGHT','EAST',2,178,0,1,0,1,0),(63,4,62,NULL,'STRAIGHT','EAST',2,180,0,1,0,1,0),(64,4,63,NULL,'STRAIGHT','EAST',2,217,0,1,0,0,0),(65,4,64,NULL,'STRAIGHT','EAST',2,218,0,1,0,0,0),(66,4,65,NULL,'STRAIGHT','EAST',2,143,0,1,0,0,0),(67,5,66,NULL,'STRAIGHT','EAST',2,124,0,1,0,1,0),(68,5,67,NULL,'STRAIGHT','EAST',2,122,0,1,0,1,0),(69,5,68,NULL,'STRAIGHT','EAST',2,131,0,1,0,0,0),(70,5,69,NULL,'STRAIGHT','EAST',2,130,0,1,0,0,0),(71,5,70,NULL,'STRAIGHT','EAST',2,129,0,1,0,1,0),(72,5,71,NULL,'STRAIGHT','EAST',4,199,0,1,0,0,0),(73,5,72,NULL,'STRAIGHT','EAST',4,197,0,1,0,0,0),(74,5,73,NULL,'STRAIGHT','EAST',4,198,0,1,0,0,0),(75,5,74,NULL,'STRAIGHT','EAST',2,145,0,1,0,1,0),(76,5,75,NULL,'STRAIGHT','EAST',2,146,0,1,0,1,0),(77,5,76,NULL,'STRAIGHT','EAST',3,148,147,1,1,1,0),(78,5,77,NULL,'STRAIGHT','EAST',3,163,165,1,1,0,0),(79,5,78,NULL,'STRAIGHT','EAST',2,164,0,1,0,0,0),(80,5,79,NULL,'STRAIGHT','EAST',2,161,0,1,0,0,0),(81,5,80,NULL,'STRAIGHT','EAST',2,162,0,1,0,1,0),(82,5,81,NULL,'STRAIGHT','EAST',2,166,0,1,0,1,0),(83,5,82,NULL,'STRAIGHT','EAST',2,167,0,1,0,0,0),(84,5,83,NULL,'STRAIGHT','EAST',2,168,0,1,0,0,0);
/*!40000 ALTER TABLE `turnout` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout_group`
--

DROP TABLE IF EXISTS `turnout_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `turnout_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `weight` int(11) default NULL,
  `turnout_number_offset` int(11) NOT NULL default '0',
  `turnout_number_amount` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `turnout_group`
--

LOCK TABLES `turnout_group` WRITE;
/*!40000 ALTER TABLE `turnout_group` DISABLE KEYS */;
INSERT INTO `turnout_group` VALUES (2,'Bhf Mitte',NULL,0,0),(3,'Bhf Links',NULL,0,0),(4,'Bhf Rechts',NULL,0,0),(5,'Rangierbhf',NULL,0,0);
/*!40000 ALTER TABLE `turnout_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout_type`
--

DROP TABLE IF EXISTS `turnout_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `turnout_type` (
  `id` int(11) NOT NULL auto_increment,
  `type_name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `turnout_type`
--

LOCK TABLES `turnout_type` WRITE;
/*!40000 ALTER TABLE `turnout_type` DISABLE KEYS */;
INSERT INTO `turnout_type` VALUES (1,'DOUBLECROSS'),(2,'DEFAULT'),(3,'THREEWAY'),(4,'CUTTER');
/*!40000 ALTER TABLE `turnout_type` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2010-05-24 12:38:31
