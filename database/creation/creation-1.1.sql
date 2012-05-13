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
 `inchikey` VARCHAR(27),
 `uploadedAs` VARCHAR(10) comment 'How Uploaded - File formal',
 `source` VARCHAR(255),
 `fingerprint` VARCHAR(4000),
  PRIMARY KEY (`inchikey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
--
-- Compound Representation
--
DROP TABLE IF EXISTS `Representation`;
CREATE TABLE `Representation` (
 `name` VARCHAR(40),
 `content` LONGTEXT,
 `compound` VARCHAR(27) NOT NULL,
 `comment` VARCHAR(255),
  CONSTRAINT `compound_representation` FOREIGN KEY (`compound`) 
    REFERENCES `Compound` (`inchikey`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
--
-- Condition
--
DROP TABLE IF EXISTS `Condition`;
CREATE TABLE `Condition` (
  `name` VARCHAR(255),
  `str_value` VARCHAR(255),
  `dbl_value` DOUBLE,
  `bin_value` BOOLEAN,
  PRIMARY KEY(`name`)
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
  `name` VARCHAR(255),
  `unit` VARCHAR(255),
  `type` VARCHAR(255),
  `condition` VARCHAR(255),
  `description` text,
  `isExperimental` BOOLEAN,
  `tag` VARCHAR(255) comment 'For descriptors: Category (topological, etc)',
  `software` VARCHAR(255),
  `software_version` VARCHAR(40),
  PRIMARY KEY(`name`),
  CONSTRAINT `property_to_condition` FOREIGN KEY (`condition`) 
    REFERENCES `Condition` (`name`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
--
-- Bibref
--
DROP TABLE IF EXISTS `Bibref`;
CREATE TABLE `Bibref` (
  `id` VARCHAR(255) COLLATE utf8_bin NOT NULL,
  `abstract` text COLLATE utf8_bin,
  `address` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `annotation` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `author` VARCHAR(255) COLLATE utf8_bin NOT NULL,
  `bibType` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `bookTitle` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `chapter` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `copyright` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `crossref` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `edition` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `editor` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `isbn` VARCHAR(20) COLLATE utf8_bin DEFAULT NULL,
  `issn` VARCHAR(20) COLLATE utf8_bin DEFAULT NULL,
  `journal` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `bibkey` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `keywords` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `number` int(11) unsigned DEFAULT NULL,
  `pages` VARCHAR(32) COLLATE utf8_bin DEFAULT NULL,
  `series` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `title` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
  `url` VARCHAR(255) COLLATE utf8_bin DEFAULT NULL,
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
  `property` VARCHAR(255) NOT NULL,
  `compound` VARCHAR(27) NOT NULL,
  `str_value` VARCHAR(255),
  `dbl_value` DOUBLE,
  `bin_value` BOOLEAN,
  `bibref` VARCHAR(255),
  `comment` VARCHAR(255),
  PRIMARY KEY (`property`,`compound`),
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
  `name` VARCHAR(255),  
  PRIMARY KEY(`name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

--
-- Dataset-to-Property
--
DROP TABLE IF EXISTS `DatasetProperty`;
CREATE TABLE `DatasetProperty` (
  `dataset` VARCHAR(255) NOT NULL,
  `property` VARCHAR(255) NOT NULL,    
  PRIMARY KEY (`dataset`,`property`),
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
  `dataset` VARCHAR(255) NOT NULL,
  `compound` VARCHAR(27) NOT NULL,    
  PRIMARY KEY (`dataset`,`compound`),
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
SELECT `compound` AS `InChiKey`, 
`str_value` AS `IUPAC` FROM PropertyValue 
INNER JOIN `Compound` ON `Compound`.`inchikey`=`PropertyValue`.`compound`
WHERE `str_value` IS NOT NULL;

----------------------------
-- ADD SOME DATA -----------
----------------------------
LOCK TABLE `Property` WRITE;
INSERT IGNORE INTO `Property` (`name`,`unit`,`type`,`description`,`isExperimental`,`tag`) 
VALUES ('IUPAC Name','','string','IUPAC Name',false,'Identifier'),
('CID','','integer','PubMed ID Number',false,'Identifier, PubChem'); 
UNLOCK TABLE ;