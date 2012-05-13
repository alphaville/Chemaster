USE `chemaster`;

LOCK TABLE `Property` WRITE;
INSERT IGNORE INTO `Property` (`name`,`unit`,`type`,`description`,`isExperimental`,`tag`) 
VALUES ('IUPAC Name','','string','IUPAC Name',false,'Identifier'),
('CID','','integer','PubMed ID Number',false,'Identifier, PubChem'); 
UNLOCK TABLE ;