-- 
-- DATABASE VERSION : 1.2.0
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
INSERT INTO `Version` (`First`,`Second`,`Third`) VALUES (1,1,3); 
UNLOCK TABLE ;
--
-- Compound
--
DROP TABLE IF EXISTS `Compound`;
CREATE TABLE `Compound` (
 `inchikey` VARCHAR(27),
 `uploadedAs` VARCHAR(10) comment 'How Uploaded - File formal',
 `source` VARCHAR(255),
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
  PRIMARY KEY (`name`,`compound`),
  CONSTRAINT `compound_representation` FOREIGN KEY (`compound`) 
    REFERENCES `Compound` (`inchikey`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE INDEX `representation_name` ON `Representation` (`name`) USING BTREE;
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
  `software` VARCHAR(255),
  `software_version` VARCHAR(40),
  PRIMARY KEY (`property`,`compound`),
 CONSTRAINT `propertyValue_to_prop` FOREIGN KEY (`property`) 
    REFERENCES `Property` (`name`) ON DELETE CASCADE,
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


--
-- Similarity
--
DROP TABLE IF EXISTS `Similarity`;
CREATE TABLE `Similarity` (
  `hash` VARCHAR(32) NOT NULL,
  `compound1` VARCHAR(27) NOT NULL,
  `compound2` VARCHAR(27) NOT NULL,    
  `tanimoto` DOUBLE NOT NULL,
  `ext_tanimoto`  DOUBLE NOT NULL,
  `estate_tanimoto` DOUBLE NOT NULL,
  PRIMARY KEY (`hash`),
 CONSTRAINT `similar1` FOREIGN KEY (`compound1`) 
    REFERENCES `Compound` (`inchikey`),
CONSTRAINT `similar2` FOREIGN KEY (`compound2`) 
    REFERENCES `Compound` (`inchikey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;

CREATE INDEX `pairwise_similar` ON `Similarity` (`compound1`,`compound2`) USING BTREE;
CREATE INDEX `pairwise_similar_c1` ON `Similarity` (`compound1`) USING BTREE;
CREATE INDEX `pairwise_similar_c2` ON `Similarity` (`compound2`) USING BTREE;
CREATE INDEX `pairwise_similar_tan` ON `Similarity` (`tanimoto`) USING BTREE;
CREATE INDEX `pairwise_similar_all` ON `Similarity` (`compound1`,`compound2`,`tanimoto`) USING BTREE;

----------------------------
-- VIEWS -------------------
----------------------------

-- InChiKey vs IUPAC
DROP VIEW IF EXISTS `IUPAC`;
CREATE VIEW IUPAC AS 
SELECT `compound` AS `InChiKey`, 
`str_value` AS `IUPAC` FROM PropertyValue 
INNER JOIN `Compound` ON `Compound`.`inchikey`=`PropertyValue`.`compound`
WHERE `str_value` IS NOT NULL AND `PropertyValue`.`property`='IUPAC Name';

-- InChiKey vs Traditional Name
DROP VIEW IF EXISTS `TName`;
CREATE VIEW `TName` AS 
SELECT `compound` AS `InChiKey`, 
`str_value` AS `IUPAC` FROM `PropertyValue` 
INNER JOIN `Compound` ON `Compound`.`inchikey`=`PropertyValue`.`compound`
WHERE `str_value` IS NOT NULL AND `PropertyValue`.`property`='Traditional Name';

-- InChiKey vs IUPAC
CREATE VIEW CID AS 
SELECT `compound` AS `InChiKey`, 
`dbl_value` AS `CID` FROM PropertyValue 
INNER JOIN `Compound` ON `Compound`.`inchikey`=`PropertyValue`.`compound`
WHERE `dbl_value` IS NOT NULL;

----------------------------
-- ADD SOME DATA -----------
----------------------------
LOCK TABLE `Property` WRITE;
INSERT IGNORE INTO `Property` (`name`,`unit`,`type`,`description`,`isExperimental`,`tag`) 
VALUES ('IUPAC Name','','string','IUPAC Name',false,'Identifier'),
('CID','','integer','PubMed ID Number',false,'Identifier, PubChem'),
('Traditional Name','','string','Traditional Name',false,'Identifier'); 
UNLOCK TABLE ;

--
--- Population
--

USE `chemaster`;

LOCK TABLE `Property` WRITE;
-- CDK::MOLECULAR DESCRIPTORS
INSERT IGNORE INTO `Property` (`name`,`unit`,`type`,`description`,`isExperimental`,`tag`) 
VALUES ('IUPAC Name','','string','IUPAC Name',false,'Identifier'),
('CID','','integer','PubMed ID Number',false,'Identifier, PubChem'),
('ALogP','','double','Calculates atom additive logP and molar refractivity values as described by Ghose and Crippen',false,'CDK::Molecular::ALOGP'),
('ALogp2','','double','Calculates atom additive logP and molar refractivity values as described by Ghose and Crippen',false,'CDK::Molecular::ALOGP'),
('AMR','','double','Calculates atom additive logP and molar refractivity values as described by Ghose and Crippen',false,'CDK::Molecular::ALOGP'),
('apol','','double','Descriptor that calculates the sum of the atomic polarizabilities (including implicit hydrogens).',false,'CDK::Molecular::APol'),
('nAcid','','integer','Returns the number of acidic groups.',false,'CDK::Molecular::AcidicGroupCount'),
('nA','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nR','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nN','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nD','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nC','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nF','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nQ','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nE','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nG','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nH','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nI','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nP','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nL','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nK','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nM','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nS','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nT','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nY','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nV','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('nW','','integer','Returns the number of amino acids found in the system',false,'CDK::Molecular::AminoAcidCount'),
('naAromAtom','','double','Descriptor based on the number of aromatic atoms of a molecule.',false,'CDK::Molecular::AromaticAtomsCount'),
('nAromBond','','double','Descriptor based on the number of aromatic bonds of a molecule.',false,'CDK::Molecular::AromaticBondsCount'),
('nAtom','','double','Descriptor based on the number of atoms of a certain element type.',false,'CDK::Molecular::AtomCount'),
('ATSc1','','double','The Moreau-Broto autocorrelation descriptors using partial charges',false,'CDK::Molecular::AutocorrelationCharge'),
('ATSc2','','double','The Moreau-Broto autocorrelation descriptors using partial charges',false,'CDK::Molecular::AutocorrelationCharge'),
('ATSc3','','double','The Moreau-Broto autocorrelation descriptors using partial charges',false,'CDK::Molecular::AutocorrelationCharge'),
('ATSc4','','double','The Moreau-Broto autocorrelation descriptors using partial charges',false,'CDK::Molecular::AutocorrelationCharge'),
('ATSc5','','double','The Moreau-Broto autocorrelation descriptors using partial charges',false,'CDK::Molecular::AutocorrelationCharge'),
('ATSm1','','double','The Moreau-Broto autocorrelation descriptors using atomic weight',false,'CDK::Molecular::AutocorrelationMass'),
('ATSm2','','double','The Moreau-Broto autocorrelation descriptors using atomic weight',false,'CDK::Molecular::AutocorrelationMass'),
('ATSm3','','double','The Moreau-Broto autocorrelation descriptors using atomic weight',false,'CDK::Molecular::AutocorrelationMass'),
('ATSm4','','double','The Moreau-Broto autocorrelation descriptors using atomic weight',false,'CDK::Molecular::AutocorrelationMass'),
('ATSm5','','double','The Moreau-Broto autocorrelation descriptors using atomic weight',false,'CDK::Molecular::AutocorrelationMass'),
('ATSp1','','double','The Moreau-Broto autocorrelation descriptors using polarizability',false,'CDK::Molecular::AutocorrelationPolarizability'),
('ATSp2','','double','The Moreau-Broto autocorrelation descriptors using polarizability',false,'CDK::Molecular::AutocorrelationPolarizability'),
('ATSp3','','double','The Moreau-Broto autocorrelation descriptors using polarizability',false,'CDK::Molecular::AutocorrelationPolarizability'),
('ATSp4','','double','The Moreau-Broto autocorrelation descriptors using polarizability',false,'CDK::Molecular::AutocorrelationPolarizability'),
('ATSp5','','double','The Moreau-Broto autocorrelation descriptors using polarizability',false,'CDK::Molecular::AutocorrelationPolarizability'),
('BCUTw-1l','','double','Eigenvalue based descriptor noted for its utility in chemical diversity described by Pearlman et al. .',false,'CDK::Molecular::BCUT'),
('BCUTw-1h','','double','Eigenvalue based descriptor noted for its utility in chemical diversity described by Pearlman et al. .',false,'CDK::Molecular::BCUT'),
('BCUTc-1l','','double','Eigenvalue based descriptor noted for its utility in chemical diversity described by Pearlman et al. .',false,'CDK::Molecular::BCUT'),
('BCUTc-1h','','double','Eigenvalue based descriptor noted for its utility in chemical diversity described by Pearlman et al. .',false,'CDK::Molecular::BCUT'),
('BCUTp-1l','','double','Eigenvalue based descriptor noted for its utility in chemical diversity described by Pearlman et al. .',false,'CDK::Molecular::BCUT'),
('BCUTp-1h','','double','Eigenvalue based descriptor noted for its utility in chemical diversity described by Pearlman et al. .',false,'CDK::Molecular::BCUT'),
('bpol','','integer','Returns the number of basic groups.',false,'CDK::Molecular::BPol'),
('nBase','','integer','Returns the number of basic groups.',false,'CDK::Molecular::BasicGroupCount'),
('nB','','double','Descriptor based on the number of bonds of a certain bond order.',false,'CDK::Molecular::BondCount'),
('PPSA-1','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('PPSA-2','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('PPSA-3','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('PNSA-1','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('PNSA-2','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('PNSA-3','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('DPSA-1','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('DPSA-2','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('DPSA-3','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('FPSA-1','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('FPSA-2','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('FPSA-3','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('FNSA-1','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('FNSA-2','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('FNSA-3','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('WPSA-1','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('WPSA-2','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('WPSA-3','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('WNSA-1','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('WNSA-2','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('WNSA-3','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('RPCG','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('RNCG','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('RPCS','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('RNCS','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('THSA','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('TPSA','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('RHSA','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('RPSA','','double','A variety of descriptors combining surface area and partial charge information.',false,'CDK::Molecular::CPSA'),
('C1SP1','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('C2SP1','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('C1SP2','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('C2SP2','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('C3SP2','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('C1SP3','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('C2SP3','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('C3SP3','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('C4SP3','','double','Characterizes the carbon connectivity in terms of hybridization.',false,'CDK::Molecular::CarbonTypes'),
('SCH-3','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('SCH-4','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('SCH-5','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('SCH-6','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('SCH-7','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('VCH-3','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('VCH-4','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('VCH-4','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('VCH-6','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('VCH-7','','double','Evaluates the Kier & Hall Chi chain indices of orders 3,4,5 and 6.',false,'CDK::Molecular::ChiChain'),
('SC-3','','double','Evaluates the Kier & Hall Chi cluster indices of orders 3,4,5,6 and 7.',false,'CDK::Molecular::ChiCluster'),
('SC-4','','double','Evaluates the Kier & Hall Chi cluster indices of orders 3,4,5,6 and 7.',false,'CDK::Molecular::ChiCluster'),
('SC-5','','double','Evaluates the Kier & Hall Chi cluster indices of orders 3,4,5,6 and 7.',false,'CDK::Molecular::ChiCluster'),
('SC-6','','double','Evaluates the Kier & Hall Chi cluster indices of orders 3,4,5,6 and 7.',false,'CDK::Molecular::ChiCluster'),
('VC-3','','double','Evaluates the Kier & Hall Chi cluster indices of orders 3,4,5,6 and 7.',false,'CDK::Molecular::ChiCluster'),
('VC-4','','double','Evaluates the Kier & Hall Chi cluster indices of orders 3,4,5,6 and 7.',false,'CDK::Molecular::ChiCluster'),
('VC-5','','double','Evaluates the Kier & Hall Chi cluster indices of orders 3,4,5,6 and 7.',false,'CDK::Molecular::ChiCluster'),
('VC-6','','double','Evaluates the Kier & Hall Chi cluster indices of orders 3,4,5,6 and 7.',false,'CDK::Molecular::ChiCluster'),
('SPC-4','','double','Evaluates the Kier & Hall Chi path cluster indices of orders 4,5 and 6.',false,'CDK::Molecular::ChiPathCluster'),
('SPC-5','','double','Evaluates the Kier & Hall Chi path cluster indices of orders 4,5 and 6.',false,'CDK::Molecular::ChiPathCluster'),
('SPC-6','','double','Evaluates the Kier & Hall Chi path cluster indices of orders 4,5 and 6.',false,'CDK::Molecular::ChiPathCluster'),
('VPC-4','','double','Evaluates the Kier & Hall Chi path cluster indices of orders 4,5 and 6.',false,'CDK::Molecular::ChiPathCluster'),
('VPC-5','','double','Evaluates the Kier & Hall Chi path cluster indices of orders 4,5 and 6.',false,'CDK::Molecular::ChiPathCluster'),
('VPC-6','','double','Evaluates the Kier & Hall Chi path cluster indices of orders 4,5 and 6.',false,'CDK::Molecular::ChiPathCluster'),
('SP-0','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('SP-1','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('SP-2','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('SP-3','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('SP-4','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('SP-5','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('SP-6','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('SP-7','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('VP-0','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('VP-1','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('VP-2','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('VP-3','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('VP-4','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('VP-5','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('VP-6','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('VP-7','','double','Evaluates the Kier & Hall Chi path indices of orders 0,1,2,3,4,5,6 and 7.',false,'CDK::Molecular::ChiPath'),
('ECCEN','','double','A topological descriptor combining distance and adjacency information.',false,'CDK::Molecular::EccentricConnectivityIndex'),
('FMF','','double','Descriptor characterizing molecular complexity in terms of its Murcko framework.',false,'CDK::Molecular::FMF'),
('fragC','','double','Class that returns the complexity of a system. The complexity is defined as @cdk.cite{Nilakantan06}.',false,'CDK::Molecular::FragmentComplexity'),
('GRAV-1','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('GRAV-2','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('GRAV-3','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('GRAVH-1','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('GRAVH-2','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('GRAVH-3','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('GRAV-4','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('GRAV-5','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('GRAV-6','','double','Descriptor characterizing the mass distribution of the molecule.',false,'CDK::Molecular'),
('nHBAcc','','double','Descriptor that calculates the number of hydrogen bond acceptors.',false,'CDK::Molecular'),
('nHBDon','','double','Descriptor that calculates the number of hydrogen bond donors.',false,'CDK::Molecular'),
('HybRatio','','double','Characterizes molecular complexity in terms of carbon hybridization states.',false,'CDK::Molecular'),
('MolIP','','double','Descriptor that evaluates the ionization potential.',false,'CDK::Molecular'),
('Kier1','','double','Descriptor that calculates Kier and Hall kappa molecular shape indices.',false,'CDK::Molecular'),
('Kier2','','double','Descriptor that calculates Kier and Hall kappa molecular shape indices.',false,'CDK::Molecular'),
('Kier3','','double','Descriptor that calculates Kier and Hall kappa molecular shape indices.',false,'CDK::Molecular'),
('khs.sLi','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssBe','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssssBe','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssBH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssB','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssssB','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sCH3','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dCH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssCH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.tCH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dsCH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aaCH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssCH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ddC','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.tsC','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dssC','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aasC','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aaaC','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssssC','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sNH3','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sNH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dNH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssNH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aaNH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.tN','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssNH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dsN','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aaN','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssN','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ddsN','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aasN','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssssN','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sOH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dO','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssO','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aaO','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sF','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sSiH3','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssSiH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssSiH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssssSi','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sPH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssPH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssP','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dsssP','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssssP','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sSH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dS','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssS','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aaS','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dssS','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ddssS','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sCl','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sGeH3','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssGeH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssGeH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssssGe','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sAsH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssAsH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssAs','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssdAs','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sssssAs','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sSeH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dSe','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ssSe','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.aaSe','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.dssSe','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.ddssSe','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'),
('khs.sBr','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.sSnH3','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.ssSnH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.sssSnH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.ssssSn','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.sI','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.sPbH3','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.ssPbH2','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.sssPbH','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('khs.ssssPb','','double','Counts the number of occurrences of the E-state fragments',false,'CDK::Molecular'), 	                                 
('nAtomLC','','integer','Returns the number of atoms in the largest chain.',false,'CDK::Molecular'), 	                                 
('nAtomP','','integer','Returns the number of atoms in the largest pi chain.',false,'CDK::Molecular'), 	                                 
('LOBMAX','','double','Calculates the ratio of length to breadth.',false,'CDK::Molecular'), 	                                 
('LOBMIN','','double','Calculates the ratio of length to breadth.',false,'CDK::Molecular'), 	                                 
('nAtomLAC','','double','Returns the number of atoms in the longest aliphatic chain.',false,'CDK::Molecular'), 	                                 
('MDEC-11','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'), 	                                 
('MDEC-12','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'), 	                                 
('MDEC-13','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'), 	                                 
('MDEC-14','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'), 	                                 
('MDEC-22','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEC-23','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEC-24','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEC-33','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEC-34','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEC-44','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEO-11','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEO-12','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEO-22','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEN-11','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEN-12','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEN-13','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEN-22','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEN-23','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MDEN-33','','double','Evaluate molecular distance edge descriptors for C, N and O.',false,'CDK::Molecular'),
('MLogP','','double','Descriptor that calculates the LogP based on a simple equation using the number of carbons and hetero atoms.',false,'CDK::Molecular'),         
('MOMI-X','','double','Descriptor that calculates the principal moments of inertia and ratios of the principal moments. Als calculates the radius of gyration.',false,'CDK::Molecular'),
('MOMI-Y','','double','Descriptor that calculates the principal moments of inertia and ratios of the principal moments. Als calculates the radius of gyration.',false,'CDK::Molecular'),
('MOMI-Z','','double','Descriptor that calculates the principal moments of inertia and ratios of the principal moments. Als calculates the radius of gyration.',false,'CDK::Molecular'),
('MOMI-XY','','double','Descriptor that calculates the principal moments of inertia and ratios of the principal moments. Als calculates the radius of gyration.',false,'CDK::Molecular'),
('MOMI-XZ','','double','Descriptor that calculates the principal moments of inertia and ratios of the principal moments. Als calculates the radius of gyration.',false,'CDK::Molecular'),
('MOMI-YZ','','double','Descriptor that calculates the principal moments of inertia and ratios of the principal moments. Als calculates the radius of gyration.',false,'CDK::Molecular'),
('MOMI-R','','double','Descriptor that calculates the principal moments of inertia and ratios of the principal moments. Als calculates the radius of gyration.',false,'CDK::Molecular'),
('PetitjeanNumber','','double','Descriptor that calculates the Petitjean Number of a molecule.',false,'CDK::Molecular'),
('topoShape','','double','The topological and geometric shape indices described Petitjean and Bath et al. respectively. Both measure the anisotropy in a molecule.',false,'CDK::Molecular'),
('geomShape','','double','The topological and geometric shape indices described Petitjean and Bath et al. respectively. Both measure the anisotropy in a molecule.',false,'CDK::Molecular'),
('nRotB','','integer','Descriptor that calculates the number of nonrotatable bonds on a molecule.',false,'CDK::Molecular'),
('LipinskiFailures','','double','This Class contains a method that returns the number failures of the Lipinskis Rule Of Five.',false,'CDK::Molecular'),
('TopoPSA','','double','Calculation of topological polar surface area based on fragment contributions.',false,'CDK::Molecular'),
('VABC','','double','Describes the volume of a molecule.',false,'CDK::Molecular'),
('VAdjMat','','double','Descriptor that calculates the vertex adjacency information of a molecule.',false,'CDK::Molecular'),
('Wlambda1.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Wlambda2.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Wlambda3.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Wnu1.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Wnu2.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Wgamma1.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Wgamma2.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Wgamma3.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Weta1.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Weta2.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('Weta3.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('WT.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('WA.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('WV.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('WK.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('WG.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),
('WD.unity','','double','Holistic descriptors described by Todeschini et al.',false,'CDK::Molecular'),  
('WTPT-1','','double','The weighted path (molecular ID) descriptors described by Randic. They characterize molecular branching.',false,'CDK::Molecular'),  
('WTPT-2','','double','The weighted path (molecular ID) descriptors described by Randic. They characterize molecular branching.',false,'CDK::Molecular'),  
('WTPT-3','','double','The weighted path (molecular ID) descriptors described by Randic. They characterize molecular branching.',false,'CDK::Molecular'),  
('WTPT-4','','double','The weighted path (molecular ID) descriptors described by Randic. They characterize molecular branching.',false,'CDK::Molecular'),  
('WTPT-5','','double','The weighted path (molecular ID) descriptors described by Randic. They characterize molecular branching.',false,'CDK::Molecular'),  
('WPATH','','double','This class calculates Wiener path number and Wiener polarity number.',false,'CDK::Molecular'),  
('WPOL','','double','This class calculates Wiener path number and Wiener polarity number.',false,'CDK::Molecular'),  
('XLogP','','double','Prediction of logP based on the atom-type method called XLogP.',false,'CDK::Molecular'),  
('Zagreb','','double','The sum of the squared atom degrees of all heavy atoms.',false,'CDK::Molecular'); 

INSERT IGNORE INTO `Property` (`name`,`unit`,`type`,`description`,`isExperimental`,`tag`) 
VALUES ('aNeg','','integer','Descriptor that calculates the number of not-Hs substituents of an atom.',false,'CDK::Atom::AtomDegree'),
('aHyb','','double','Descriptor that returns the hybridization state of an atom.',false,'CDK::Atom::AtomHybridization'),
('hybr','','double','Descriptor that returns the hybridization state of an atom.',false,'CDK::Atom::AtomHybridizationVSEPR'),
('val','','double','Descriptor that calculates the valence of an atom.',false,'CDK::Atom::AtomValence'),
('bondsToAtom','','double','Descriptor based on the number of bonds on the shortest path between two atoms.',false,'CDK::Atom::BondsToAtom'),
('covalentRadius','','double','Descriptor that returns the covalent radius of a given atom.',false,'CDK::Atom::CovalentRadius'), 	 	
('distanceToAtom','','double','Descriptor that calculates the 3D distance between two atoms.',false,'CDK::Atom::DistanceToAtom'),
('effAtomPol','','double','Descriptor that calculates the effective polarizability of a given heavy atom.',false,'CDK::Atom::EffectiveAtomPolarizability'),
('ipAtomicHOSE','','double','',false,'CDK::Atom::IPAtomicHOSE'),
('ipAtomicLearning','','double','',false,'CDK::Atom::IPAtomicLearning'),
('indAtomHardnesss','','double','This class calculates the atomic hardness of a given atom.',false,'CDK::Atom::InductiveAtomicHardness'),
('indAtomSoftness','','double','This class calculates the atomic softness of a given atom.',false,'CDK::Atom::InductiveAtomicSoftness'),
('protonInArmaticSystem','','double','This Class contains a method that returns 1 if the protons is directly bonded to an aromatic system, it returns 2 if the distance between aromatic system and proton is 2 bonds, and 0 for other positions.',false,'CDK::Atom::IsProtonInAromaticSystem'),
('protonInConjSystem','','double','This Class contains a method that returns true if the protons is directly bonded to a pi system.',false,'CDK::Atom::IsProtonInConjugatedPiSystem'),
('pepe','','double','Descriptor that calculates pi partial charges in pi-bonded systems of an heavy atom.',false,'CDK::Atom::PartialPiCharge');
UNLOCK TABLE ;