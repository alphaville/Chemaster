ALTER TABLE `Compound` DROP COLUMN `fingerprint`;

ALTER TABLE `Similarity` ADD COLUMN `ext_tanimoto` DOUBLE;
ALTER TABLE `Similarity` ADD COLUMN `estate_tanimoto` DOUBLE;


