-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.0.38-Ubuntu_0ubuntu1-log


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema adhocrailway
--

CREATE DATABASE IF NOT EXISTS adhocrailway;
USE adhocrailway;

--
-- Definition of table `adhocrailway`.`locomotive`
--

DROP TABLE IF EXISTS `adhocrailway`.`locomotive`;
CREATE TABLE  `adhocrailway`.`locomotive` (
  `id` int(11) NOT NULL auto_increment,
  `locomotive_group_id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `image` varchar(255) NOT NULL,
  `address` int(11) NOT NULL,
  `bus` int(11) NOT NULL,
  `locomotive_type_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `locomotive_locomotive_group_id` (`locomotive_group_id`),
  KEY `locomotive_locomotive_type_fk` (`locomotive_type_id`),
  CONSTRAINT `locomotive_locomotive_group_id` FOREIGN KEY (`locomotive_group_id`) REFERENCES `locomotive_group` (`id`),
  CONSTRAINT `locomotive_locomotive_type_fk` FOREIGN KEY (`locomotive_type_id`) REFERENCES `locomotive_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`locomotive`
--

/*!40000 ALTER TABLE `locomotive` DISABLE KEYS */;
LOCK TABLES `locomotive` WRITE;
INSERT INTO `adhocrailway`.`locomotive` VALUES  (1,2,'Bernaa','Bern desc','bern.png',2,1,2),
 (2,1,'ascom','asocm','ascom.png',1212,1,2),
 (3,6,'sdasdf','sdf','dsf',12,1,3),
 (4,6,'213','sadsadfsads','sdsaf',123123,12,3),
 (5,6,'asdfsdf','sadf','dsaf',21,123,3),
 (6,6,'sdf','sadfsadf','adfsadf',123,12,2),
 (7,6,'sadfasfd','sdf','Sdf',23,12,3),
 (8,2,'asdsafd','sadfsadf','sdfsadf',12314,123,3),
 (9,2,'sadsdfsadf','sadfsadf','ssaf',123,23,3),
 (10,2,'a','a','a',1,1,3),
 (11,2,'asdf','sdf','sdf',0,0,3),
 (12,2,'213','fdsgfdg','sdfdsa',324,143,3),
 (13,3,'asdf','sadf','sdf',1,23,3),
 (14,1,'sadf','dsaf','sdf',12,12,3),
 (15,1,'dfsdf','sdf','sdf',131,112,2);
UNLOCK TABLES;
/*!40000 ALTER TABLE `locomotive` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`locomotive_group`
--

DROP TABLE IF EXISTS `adhocrailway`.`locomotive_group`;
CREATE TABLE  `adhocrailway`.`locomotive_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`locomotive_group`
--

/*!40000 ALTER TABLE `locomotive_group` DISABLE KEYS */;
LOCK TABLES `locomotive_group` WRITE;
INSERT INTO `adhocrailway`.`locomotive_group` VALUES  (1,'Ae 6/6'),
 (2,'Re 460'),
 (3,'asdf'),
 (6,'123');
UNLOCK TABLES;
/*!40000 ALTER TABLE `locomotive_group` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`locomotive_type`
--

DROP TABLE IF EXISTS `adhocrailway`.`locomotive_type`;
CREATE TABLE  `adhocrailway`.`locomotive_type` (
  `id` int(11) NOT NULL auto_increment,
  `type_name` varchar(255) NOT NULL,
  `drivingSteps` int(11) NOT NULL,
  `functionCount` int(11) NOT NULL,
  `stepping` int(11) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`locomotive_type`
--

/*!40000 ALTER TABLE `locomotive_type` DISABLE KEYS */;
LOCK TABLES `locomotive_type` WRITE;
INSERT INTO `adhocrailway`.`locomotive_type` VALUES  (2,'Digital',28,5,4),
 (3,'Delta',14,1,2);
UNLOCK TABLES;
/*!40000 ALTER TABLE `locomotive_type` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`route`
--

DROP TABLE IF EXISTS `adhocrailway`.`route`;
CREATE TABLE  `adhocrailway`.`route` (
  `id` int(11) NOT NULL,
  `route_group_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `number` (`number`),
  KEY `route_route_group_fk` (`route_group_id`),
  CONSTRAINT `route_route_group_fk` FOREIGN KEY (`route_group_id`) REFERENCES `route_group` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`route`
--

/*!40000 ALTER TABLE `route` DISABLE KEYS */;
LOCK TABLES `route` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `route` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`route_group`
--

DROP TABLE IF EXISTS `adhocrailway`.`route_group`;
CREATE TABLE  `adhocrailway`.`route_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`route_group`
--

/*!40000 ALTER TABLE `route_group` DISABLE KEYS */;
LOCK TABLES `route_group` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `route_group` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`route_item`
--

DROP TABLE IF EXISTS `adhocrailway`.`route_item`;
CREATE TABLE  `adhocrailway`.`route_item` (
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

--
-- Dumping data for table `adhocrailway`.`route_item`
--

/*!40000 ALTER TABLE `route_item` DISABLE KEYS */;
LOCK TABLES `route_item` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `route_item` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`turnout`
--

DROP TABLE IF EXISTS `adhocrailway`.`turnout`;
CREATE TABLE  `adhocrailway`.`turnout` (
  `id` int(11) NOT NULL auto_increment,
  `turnout_group_id` int(11) NOT NULL,
  `number` int(11) NOT NULL,
  `desc` varchar(255) NOT NULL,
  `default_state` enum('STRAIGHT','LEFT','RIGHT') NOT NULL,
  `orientation` enum('NORTH','EAST','SOUTH','WEST') NOT NULL,
  `turnout_type_id` int(11) NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `number` (`number`),
  KEY `turnout_turnout_group_id_fk` (`turnout_group_id`),
  KEY `turnout_turnout_type_fk` (`turnout_type_id`),
  CONSTRAINT `turnout_turnout_group_id_fk` FOREIGN KEY (`turnout_group_id`) REFERENCES `turnout_group` (`id`),
  CONSTRAINT `turnout_turnout_type_fk` FOREIGN KEY (`turnout_type_id`) REFERENCES `turnout_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`turnout`
--

/*!40000 ALTER TABLE `turnout` DISABLE KEYS */;
LOCK TABLES `turnout` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `turnout` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`turnout_address`
--

DROP TABLE IF EXISTS `adhocrailway`.`turnout_address`;
CREATE TABLE  `adhocrailway`.`turnout_address` (
  `id` int(11) NOT NULL auto_increment,
  `turnout_id` int(11) NOT NULL,
  `address` int(11) NOT NULL,
  `bus` int(11) NOT NULL,
  `switched` tinyint(1) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `turnout_address_turnout_id_fk` (`turnout_id`),
  CONSTRAINT `turnout_address_turnout_id_fk` FOREIGN KEY (`turnout_id`) REFERENCES `turnout` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`turnout_address`
--

/*!40000 ALTER TABLE `turnout_address` DISABLE KEYS */;
LOCK TABLES `turnout_address` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `turnout_address` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`turnout_group`
--

DROP TABLE IF EXISTS `adhocrailway`.`turnout_group`;
CREATE TABLE  `adhocrailway`.`turnout_group` (
  `id` int(11) NOT NULL auto_increment,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`turnout_group`
--

/*!40000 ALTER TABLE `turnout_group` DISABLE KEYS */;
LOCK TABLES `turnout_group` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `turnout_group` ENABLE KEYS */;


--
-- Definition of table `adhocrailway`.`turnout_type`
--

DROP TABLE IF EXISTS `adhocrailway`.`turnout_type`;
CREATE TABLE  `adhocrailway`.`turnout_type` (
  `id` int(11) NOT NULL auto_increment,
  `type_name` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `adhocrailway`.`turnout_type`
--

/*!40000 ALTER TABLE `turnout_type` DISABLE KEYS */;
LOCK TABLES `turnout_type` WRITE;
UNLOCK TABLES;
/*!40000 ALTER TABLE `turnout_type` ENABLE KEYS */;




/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
