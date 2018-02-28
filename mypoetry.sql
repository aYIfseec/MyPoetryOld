/*
Navicat MySQL Data Transfer

Source Server         : hyk
Source Server Version : 50717
Source Host           : 119.29.142.195:3306
Source Database       : mypoetry

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2018-02-28 19:21:37
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for t_upload_record
-- ----------------------------
DROP TABLE IF EXISTS `t_upload_record`;
CREATE TABLE `t_upload_record` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phoneNumber` varchar(255) NOT NULL,
  `poetryId` varchar(50) NOT NULL,
  `poetryTitle` varchar(80) NOT NULL,
  `recordPath` varchar(100) NOT NULL,
  `praiseCount` int(11) DEFAULT '0',
  `playCount` int(11) DEFAULT '0',
  `state` int(11) DEFAULT '1',
  `uploadTime` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `phoneNum` (`phoneNumber`),
  CONSTRAINT `phoneNum` FOREIGN KEY (`phoneNumber`) REFERENCES `t_user` (`phoneNumber`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phoneNumber` varchar(20) NOT NULL,
  `name` varchar(20) NOT NULL,
  `password` varchar(40) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `phoneNumber` (`phoneNumber`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Table structure for t_user_collection
-- ----------------------------
DROP TABLE IF EXISTS `t_user_collection`;
CREATE TABLE `t_user_collection` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phoneNumber` varchar(20) NOT NULL,
  `poetryId` varchar(50) NOT NULL,
  `poetryTitle` varchar(50) NOT NULL,
  `collectTime` date NOT NULL,
  PRIMARY KEY (`id`),
  KEY `phone` (`phoneNumber`),
  CONSTRAINT `phone` FOREIGN KEY (`phoneNumber`) REFERENCES `t_user` (`phoneNumber`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=39 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Procedure structure for user_do_play
-- ----------------------------
DROP PROCEDURE IF EXISTS `user_do_play`;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `user_do_play`(IN `record_id` int)
Begin 
update t_upload_record set playCount=playCount+1 where id=record_id;
End
;;
DELIMITER ;

-- ----------------------------
-- Procedure structure for user_do_praise
-- ----------------------------
DROP PROCEDURE IF EXISTS `user_do_praise`;
DELIMITER ;;
CREATE DEFINER=`root`@`%` PROCEDURE `user_do_praise`(IN `record_id` int)
Begin 
update t_upload_record set praiseCount=praiseCount+1 where id=record_id;
End
;;
DELIMITER ;
