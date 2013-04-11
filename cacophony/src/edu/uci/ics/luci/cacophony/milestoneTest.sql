# ************************************************************
# Sequel Pro SQL dump
# Version 4004
#
# http://www.sequelpro.com/
# http://code.google.com/p/sequel-pro/
#
# Host: localhost (MySQL 5.6.10)
# Database: testDatabase
# Generation Time: 2013-04-11 22:15:37 +0000
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
  `point` point DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `configuration` varchar(1024) DEFAULT '{doTraining:false}',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

LOCK TABLES `milestonetest` WRITE;
/*!40000 ALTER TABLE `milestonetest` DISABLE KEYS */;

INSERT INTO `milestonetest` (`id`, `name`, `point`, `weight`, `configuration`)
VALUES
	(1,'power_source',X'000000000101000000F2086EA46C795DC0FA804067D2D44040',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ \"time\", \"weekend\" ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_mac\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?sensor=powersource&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \"(.*)\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorCategory\" } }\n\n\n'),
	(2,'ip_address',X'000000000101000000209A79724D825EC0D34D621058A94240',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ \"time\", \"weekend\" ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_mac\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?sensor=ipaddress&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \"(.*)\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorCategory\" } }\n\n\n'),
	(3,'wifi_ssid',X'00000000010100000073EF86D970095FC0293D367647664440',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_mac\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?sensor=wifi&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \"<<(.*):[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*>:[-]?[0-9][0-9]*>\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorCategory\" } }\n\n\n'),
	(4,'wifi_mac',X'000000000101000000B3F96EA939875DC0A41AD56DF9E04040',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_ssid\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?sensor=wifi&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \"<<.*:([0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*:[0-9a-f]*)>:[-]?[0-9][0-9]*>\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorCategory\" } }\n\n\n\n\n'),
	(5,'volume',X'00000000010100000005C58F3177855EC01B9E5E29CBB04240',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ \"time\" ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_mac\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?sensor=volume&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \"(.*)\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorNumber\" } }\n\n\n\n\n'),
	(6,'light',X'00000000010100000092EE42CE56935EC09AEB34D252274340',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ \"time\" ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_mac\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?sensor=light&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \"(.*)\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorNumber\" } }\n\n\n\n\n'),
	(7,'ui_activity',X'000000000101000000248337FF8A895EC0DFB5749ABFB64240',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ \"time\" ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_mac\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?uiactivity=light&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \"(.*)\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorNumber\" } }\n\n\n\n\n'),
	(8,'idle_time',X'000000000101000000B4AB90F293445DC0BBECD79DEE624040',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_mac\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?uiactivity=idle&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \"(.*)\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorNumber\" } }\n\n\n\n\n'),
	(9,'accelerometer_z',X'000000000101000000636CD73A279C5EC07A6B05F291E44240',10,'{ \"do_training\": false, \"features\": [ { \"namespace\": null, \"names\": [ ] }, { \"namespace\": \"edu.uci.ics.luci.cacophony.milestone\", \"names\": [ \"wifi_mac\" ] } ], \"target\": { \"format\": \"html\", \"url\": \"http://localhost:2011/sense?acclerometer=idle&version=1.0&format=html\", \"xpath\": \"/html/body/div\", \"regular_expression\": \",\\\\s*([0-9]*[.][0-9]*)[]]$\", \"translator_class\": \"edu.uci.ics.luci.cacophony.node.TranslatorNumber\" } }\n\n\n\n\n');

/*!40000 ALTER TABLE `milestonetest` ENABLE KEYS */;
UNLOCK TABLES;



/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
