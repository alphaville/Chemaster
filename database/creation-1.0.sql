-- 
-- DATABASE VERSION : 1.0.0
--
DROP DATABASE IF EXISTS `chemaster`;
CREATE DATABASE `chemaster` DEFAULT CHARACTER SET utf8 COLLATE utf8_bin;
USE `chemaster`;
--
-- Version
--
DROP TABLE IF EXISTS `Version`;
CREATE TABLE `Version` (
 `First` int(11) NOT NULL,
 `Second` int(11) NOT NULL,
 `Third` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
LOCK TABLE `Version` WRITE;
INSERT INTO `Version` (`First`,`Second`,`Third`) VALUES (1,0,0); 
UNLOCK TABLE ;
--
-- Compound
--
DROP TABLE IF EXISTS `Compound`;
CREATE TABLE `Compound` (
 `inchikey` varchar(27),
 `uploadedAs` varchar(10) comment 'How Uploaded - File formal',
 `source` varchar(255),
  PRIMARY KEY (`inchikey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
--
-- Compound Representation
--
DROP TABLE IF EXISTS `Representation`;
CREATE TABLE `Representation` (
 `name` varchar(40),
 `content` LONGTEXT,
 `compound` varchar(27) not null,
 `comment` varchar(255),
  CONSTRAINT `compound_representation` FOREIGN KEY (`compound`) 
    REFERENCES `Compound` (`inchikey`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
--
-- Condition
--
DROP TABLE IF EXISTS `Condition`;
CREATE TABLE `Condition` (
  `name` varchar(255),
  `str_value` varchar(255),
  `dbl_value` double,
  `bin_value` boolean,
  primary key(`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
--
-- Property
--
-- Comment on the field tag:
-- This serves as a classifier for the descriptor. e.g. 
-- it can be "Constitutional Indices"::"BasicDescriptors"
-- 
DROP TABLE IF EXISTS `Property`;
CREATE TABLE `Property` (
  `name` varchar(255),
  `unit` varchar(255),
  `type` varchar(255),
  `condition` varchar(255),
  `description` text,
  `isExperimental` boolean,
  `tag` varchar(255) comment 'For descriptors: Category (topological, etc)',
  `software` varchar(255),
  `software_version` varchar(40),
  primary key(`name`),
  CONSTRAINT `property_to_condition` FOREIGN KEY (`condition`) 
    REFERENCES `Condition` (`name`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
--
-- Bibref
--
DROP TABLE IF EXISTS `Bibref`;
CREATE TABLE `Bibref` (
  `id` varchar(255) COLLATE utf8_bin NOT NULL,
  `abstract` text COLLATE utf8_bin,
  `address` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `annotation` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `author` varchar(255) COLLATE utf8_bin NOT NULL,
  `bibType` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `bookTitle` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `chapter` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `copyright` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `crossref` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `edition` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `editor` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `isbn` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `issn` varchar(20) COLLATE utf8_bin DEFAULT NULL,
  `journal` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `bibkey` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `keywords` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `number` int(11) unsigned DEFAULT NULL,
  `pages` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `series` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `title` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `url` varchar(255) COLLATE utf8_bin DEFAULT NULL,
  `volume` int(11) unsigned DEFAULT NULL,
  `year` int(11) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `index_bibtex_id` (`id`) USING BTREE,
  KEY `index_bibtex_author` (`author`) USING BTREE,
  KEY `index_bibtex_booktitle` (`bookTitle`) USING BTREE,
  KEY `index_bibtex_url` (`url`) USING BTREE  
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
--
-- Property Value
--
DROP TABLE IF EXISTS `PropertyValue`;
CREATE TABLE `PropertyValue` (
  `property` varchar(255) not null,
  `compound` varchar(27) not null,
  `str_value` varchar(255),
  `dbl_value` double,
  `bin_value` boolean,
  `bibref` varchar(255),
  `comment` varchar(255),
  primary key (`property`,`compound`),
 CONSTRAINT `propertyValue_to_prop` FOREIGN KEY (`property`) 
    REFERENCES `Property` (`name`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `propertyValue_to_compound` FOREIGN KEY (`compound`) 
    REFERENCES `Compound` (`inchikey`) ON DELETE CASCADE ON UPDATE CASCADE,
CONSTRAINT `propertyValue_to_bibref` FOREIGN KEY (`bibref`)
    REFERENCES `Bibref` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


--
-- Dataset
--
DROP TABLE IF EXISTS `Dataset`;
CREATE TABLE `Dataset` (
  `name` varchar(255),  
  primary key(`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Dataset-to-Property
--
DROP TABLE IF EXISTS `DatasetProperty`;
CREATE TABLE `DatasetProperty` (
  `dataset` varchar(255) not null,
  `property` varchar(255) not null,    
  primary key (`dataset`,`property`),
 CONSTRAINT `pointer_to_dataset` FOREIGN KEY (`dataset`) 
    REFERENCES `Dataset` (`name`) ON DELETE NO ACTION ON UPDATE CASCADE,
 CONSTRAINT `pointer_to_property` FOREIGN KEY (`property`) 
    REFERENCES `Property` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Dataset-to-Compound
--
DROP TABLE IF EXISTS `DatasetCompound`;
CREATE TABLE `DatasetCompound` (
  `dataset` varchar(255) not null,
  `compound` varchar(27) not null,    
  primary key (`dataset`,`compound`),
 CONSTRAINT `pointer_to_dataset2` FOREIGN KEY (`dataset`) 
    REFERENCES `Dataset` (`name`) ON DELETE NO ACTION ON UPDATE CASCADE,
 CONSTRAINT `pointer_to_compound` FOREIGN KEY (`compound`) 
    REFERENCES `Compound` (`inchikey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


----------------------------
-- VIEWS -------------------
----------------------------

-- InChiKey vs IUPAC
CREATE VIEW InChiKey_IUPAC AS 
SELECT `compound` as `InChiKey`, 
`str_value` as `IUPAC` FROM PropertyValue 
INNER JOIN `Compound` ON `Compound`.`inchikey`=`PropertyValue`.`compound`
WHERE `str_value` IS NOT NULL;