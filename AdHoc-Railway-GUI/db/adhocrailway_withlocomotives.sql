-- MySQL dump 10.11
--
-- Host: localhost    Database: baehnle09
-- ------------------------------------------------------
-- Server version	5.0.75-0ubuntu10.2

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
-- Current Database: `baehnle09`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `baehnle09` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `baehnle09`;

--
-- Table structure for table `locomotive`
--

DROP TABLE IF EXISTS `locomotive`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
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
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `locomotive`
--

LOCK TABLES `locomotive` WRITE;
/*!40000 ALTER TABLE `locomotive` DISABLE KEYS */;
INSERT INTO `locomotive` VALUES (1,1,'Ae 6/6 Altdorf rot','Ae 6/6 Altdorf rot','',21,1,1),(2,1,'Ae 6/6 Appenzell i. Rh','Ae 6/6 Appenzell i. Rh','',49,1,1),(3,1,'Ae 6/6 Baselland gruen','Ae 6/6 Baselland gruen','',39,1,1),(4,1,'Ae 6/6 Bern gruen (Jüre)','Ae 6/6 Bern gruen (Jüre)','',33,1,1),(5,1,'Ae 6/6 Bern gruen (Mätthu)','Ae 6/6 Bern gruen (Mätthu)','',1,1,1),(6,1,'Ae 6/6 Schaffhausen','Ae 6/6 Schaffhausen','',32,1,1),(7,2,'Big Boy','UP Klasse 4000 Big Boy','',40,1,1),(8,2,'Dampflok DB','Dampflok DB','',5,1,1),(9,2,'Dampflok DB BR 003','Dampflok DB BR 003','',41,1,1),(10,2,'Dampflok DB BR 01','Dampflok DB BR 01','',35,1,1),(11,2,'Dampflok DB, BR 89','Dampflok DB, BR 89','',14,1,1),(12,2,'Dampflok-Grosi','Dampflok','',77,1,1),(13,3,'Diesellok DB BR 212','Diesellok DB BR 212','',12,1,1),(14,3,'Diesellok DB BR 216','Diesellok DB BR 216','',36,1,1),(15,3,'Diesellok DB Post','Diesellok DB Post','',17,1,1),(16,3,'Rangierlok Am 842 Cargo','Rangierlok Am 842 Cargo','',64,1,1),(17,3,'Rangierlok Ee 3/3 rot','Rangierlok Ee 3/3 rot','',60,1,1),(18,3,'Rangierlok SBB Te III ','Rangierlok SBB Te III','',20,1,1),(19,4,'Re 4/4 Bahn 2000 rot (Jöggu)','Re 4/4 Bahn 2000 rot (Jöggu)','',16,1,1),(20,4,'Re 4/4 Bahn 2000 rot (Jüre)','Re 4/4 Bahn 2000 rot (Jüre)','',34,1,1),(21,4,'Re 4/4 GBS braun','Re 4/4 GBS braun','',43,1,1),(22,4,'Re 4/4 II Tee creme-rot','Re 4/4 II tee creme-rot','',23,1,1),(23,4,'Re 4/4 II rot','Re 4/4 II rot','',25,1,1),(24,4,'Re 4/4 IV rot/grau','Re 4/4 IV rot/grau','',22,1,1),(25,4,'Re 4/4 Rigi rot','Re 4/4 Rigi rot','',6,1,1),(26,4,'Re 4/4 SBB Bahn 2000 rot (Geri)','Re 4/4 SBB Bahn 2000 rot (Geri)','',54,1,1),(27,4,'Re 4/4 SBB gruen (HAG)','Re 4/4 SBB gruen','',44,1,1),(28,5,'Re 460 Ascom','RE460 Ascom','',24,1,1),(29,5,'Re 460 Cargo','Re 460 Cargo','',9,1,1),(30,5,'Re 460 Miele','Re 460 Miele','',28,1,1),(31,5,'Re 460 Nord Vaudois rot','Re 460 Nord Vaudois rot','',18,1,1),(32,5,'Re 460 Post','Re 460 Post','',46,1,1),(33,5,'Re 460 Rola','Rola/Connecting Europe','',30,1,1),(34,5,'Re 460 SF DRS','Re460 SF DRS','',26,1,1),(35,5,'Re 460 Verkehrshaus Luzern','Re 460 Verkehrshaus Luzern','',11,1,1),(36,5,'Re 460 Zugkraft Aargau','Re 460 Zugkraft Aargau','',10,1,1),(37,5,'Re 460 la Gruyere rot','Re 460 la Gruyere rot','',45,1,1),(38,5,'Re 465 BLS Saas Fee','Re 465 BLS Saas Fee','',47,1,1),(39,6,'Re 6/6 Murgenthal rot','Re 6/6 Murgenthal rot','',29,1,1),(40,6,'Re 6/6 SBB Amsteg gruen','Re 6/6 SBB Amsteg gruen','',48,1,1),(41,6,'Re 6/6 zweiteilig gruen','Re 6/6 zweiteilig gruen','',27,1,1),(42,7,'Ae 3/6 II braun','Ae 3/6 II braun','',38,1,1),(43,7,'Ae 4/7 SBB grün','Ae 4/7 SBB grün','',3,1,1),(44,7,'Ae 8/14 Doppelschnauz gruen','Ae 8/14 Doppelschnauz gruen','',19,1,1),(45,7,'Ae 8/8 braun','Ae 8/8 braun','',62,1,1),(46,7,'BDe 4/4 SBB gruen','BDe 4/4 SBB gruen','',42,1,1),(47,7,'Krokodil Juere','Krokodil Juere','',50,1,1),(48,7,'Krokodil gruen','Krokodil gruen','',15,1,1),(49,7,'RBe 2/4 Blauer Pfeil','RBe 2/4 Blauer Pfeil','',52,1,1),(50,8,'E-Lok Schweden SJ braun','E-Lok Schweden SJ braun','',31,1,1),(51,8,'ICE 2','ICE 2','',7,37,1),(52,8,'Sante Fe','Sante Fe','',7,1,1);
/*!40000 ALTER TABLE `locomotive` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `locomotive_group`
--

DROP TABLE IF EXISTS `locomotive_group`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `locomotive_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

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
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `locomotive_type` (
  `id` int(11) NOT NULL auto_increment,
  `type_name` varchar(255) NOT NULL,
  `drivingSteps` int(11) NOT NULL,
  `functionCount` int(11) NOT NULL,
  `stepping` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=51 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

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
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `route` (
  `id` int(11) NOT NULL auto_increment,
  `route_group_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `route_route_group_fk` (`route_group_id`),
  CONSTRAINT `route_route_group_fk` FOREIGN KEY (`route_group_id`) REFERENCES `route_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `route`
--

LOCK TABLES `route` WRITE;
/*!40000 ALTER TABLE `route` DISABLE KEYS */;
/*!40000 ALTER TABLE `route` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route_group`
--

DROP TABLE IF EXISTS `route_group`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `route_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `weight` int(11) default NULL,
  `route_number_offset` int(11) NOT NULL default '0',
  `route_number_amount` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `route_group`
--

LOCK TABLES `route_group` WRITE;
/*!40000 ALTER TABLE `route_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `route_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `route_item`
--

DROP TABLE IF EXISTS `route_item`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `route_item`
--

LOCK TABLES `route_item` WRITE;
/*!40000 ALTER TABLE `route_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `route_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout`
--

DROP TABLE IF EXISTS `turnout`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `turnout`
--

LOCK TABLES `turnout` WRITE;
/*!40000 ALTER TABLE `turnout` DISABLE KEYS */;
/*!40000 ALTER TABLE `turnout` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout_group`
--

DROP TABLE IF EXISTS `turnout_group`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `turnout_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  `weight` int(11) default NULL,
  `turnout_number_offset` int(11) NOT NULL default '0',
  `turnout_number_amount` int(11) NOT NULL default '0',
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `turnout_group`
--

LOCK TABLES `turnout_group` WRITE;
/*!40000 ALTER TABLE `turnout_group` DISABLE KEYS */;
/*!40000 ALTER TABLE `turnout_group` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `turnout_type`
--

DROP TABLE IF EXISTS `turnout_type`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `turnout_type` (
  `id` int(11) NOT NULL auto_increment,
  `type_name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

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

-- Dump completed on 2009-05-30 11:22:06
