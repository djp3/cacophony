# ************************************************************
# Sequel Pro SQL dump
# Version 4004
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: localhost (MySQL 5.5.29)
# Database: testDatabase
# Generation Time: 2013-03-11 23:38:05 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table milestonetest
# ------------------------------------------------------------

DROP TABLE IF EXISTS `milestonetest`;

CREATE TABLE `milestonetest` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(128) DEFAULT NULL,
  `x` double DEFAULT NULL,
  `y` double DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `point` point DEFAULT NULL,
  `configuration` varchar(1024) DEFAULT '{doTraining:false}',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `milestonetest` WRITE;
/*!40000 ALTER TABLE `milestonetest` DISABLE KEYS */;

INSERT INTO `milestonetest` (`id`, `name`, `x`, `y`, `weight`, `point`, `configuration`)
VALUES
	(1,'Power Source',-117.897256,33.662671,10,X'000000000101000000F2086EA46C795DC0FA804067D2D44040','{doTraining:false,features:[],target:{URL:\"http://localhost:2011/sense?sensor=powersource&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\"(.*)\"}}'),
	(2,'IP Address',-122.035977,37.323,10,X'000000000101000000209A79724D825EC0D34D621058A94240','{doTraining:false,features:[],target:{URL:\"http://localhost:2011/sense?sensor=ipaddress&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\"(.*)\"}}'),
	(3,'WiFi SSID',-124.1475128,40.79905584,10,X'00000000010100000073EF86D970095FC0293D367647664440','{doTraining:false,features:[1,2],target:{URL:\"http://localhost:2011/sense?sensor=wifi&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\"<<(.*):[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*>:[-]?[0-9][0-9]*>\"\n}}'),
	(4,'WIFI MAC',-118.1128944,33.75761197,10,X'000000000101000000B3F96EA939875DC0A41AD56DF9E04040','{doTraining:false,features:[],target:{URL:\"http://localhost:2011/sense?sensor=wifi&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\"<<.*:([0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*)>:[-]?[0-9][0-9]*>\"\n}}'),
	(5,'Volume',-122.0854,37.3812,10,X'00000000010100000005C58F3177855EC01B9E5E29CBB04240','{doTraining:false,features:[],target:{URL:\"http://localhost:2011/sense?sensor=volume&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\"(.*)\"}}'),
	(6,'Light',-122.3021732,38.307215,10,X'00000000010100000092EE42CE56935EC09AEB34D252274340','{doTraining:false,features:[4,5],target:{URL:\"http://localhost:2011/sense?sensor=light&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\"(.*)\"}}'),
	(7,'UI Activity',-122.1491087,37.42772227,10,X'000000000101000000248337FF8A895EC0DFB5749ABFB64240','{doTraining:false,features:[3,6],target:{URL:\"http://localhost:2011/sense?sensor=uiactivity&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\"(.*)\"}}'),
	(8,'Idle Time',-117.07153,32.772907,10,X'000000000101000000B4AB90F293445DC0BBECD79DEE624040','{doTraining:false,features:[7],target:{URL:\"http://localhost:2011/sense?sensor=idle&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\"(.*)\"}}'),
	(9,'Accelerometer Z',-122.4398944,37.7857039,10,X'000000000101000000636CD73A279C5EC07A6B05F291E44240','{doTraining:false,features:[6,8],target:{URL:\"http://localhost:2011/sense?sensor=accelerometer&version=1.0&format=html\",XPath:\"/html/body/div\",regEx:\",\\\\s*([0-9]*[.][0-9]*)[]]$\"}}');

/*!40000 ALTER TABLE `milestonetest` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
