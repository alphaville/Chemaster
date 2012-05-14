--
-- ADD THE COLUMN `fingerprint` VARCHAR(4000)
-- TO `Compound`
--
USE `chemaster`;
ALTER TABLE `Compound` ADD `fingerprint` VARCHAR(4000);

alter table Property drop software;
alter table Property drop software_version;

alter table PropertyValue add software varchar(255);
alter table PropertyValue add software_version varchar(40);