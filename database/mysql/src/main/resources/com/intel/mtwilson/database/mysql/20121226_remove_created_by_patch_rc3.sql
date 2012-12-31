-- The below SQL Commands removes the dependency of the below columns which are not being used anymore in Mt.Wilson. This data would be
-- retrieved from the audit log database instead.
-- created_by
-- updated_by
-- created_on
-- updated_on

-- First we need to modify the tables  referencing the tbl_db_portal_user ID column for storing the created/updated columns. 
-- We do not need to track this information in these tables since it would be captured by the Audit Log database. 
-- Same is the case with the created/updated on fields.

-- Change the mw_module_manifest table
ALTER TABLE `mw_module_manifest` DROP FOREIGN KEY `Module_Created_By` ;
ALTER TABLE `mw_module_manifest` DROP COLUMN `Created_By` , DROP INDEX `Module_Created_By` ;

ALTER TABLE `mw_module_manifest` DROP FOREIGN KEY `Module_Last_Updated_By` ;
ALTER TABLE `mw_module_manifest` DROP COLUMN `Updated_By` , DROP INDEX `Module_Last_Updated_By` ;

ALTER TABLE `mw_module_manifest` DROP COLUMN `Created_On` ;
ALTER TABLE `mw_module_manifest` DROP COLUMN `Updated_On`;

-- Change the mw_pcr_manifest table
ALTER TABLE `mw_pcr_manifest` DROP FOREIGN KEY  `PCR_Created_By` ;
ALTER TABLE `mw_pcr_manifest` DROP COLUMN `Created_By` , DROP INDEX `PCR_Created_By` ;

ALTER TABLE `mw_pcr_manifest` DROP FOREIGN KEY `PCR_Last_Updated_By`;
ALTER TABLE `mw_pcr_manifest` DROP COLUMN `Updated_By` , DROP INDEX `PCR_Last_Updated_By` ;

ALTER TABLE `mw_pcr_manifest` DROP COLUMN `Created_On` ;
ALTER TABLE `mw_pcr_manifest` DROP COLUMN `Updated_On` ;

-- Change the mw_hosts table
ALTER TABLE `mw_hosts` DROP COLUMN `Created_On` ;
ALTER TABLE `mw_hosts` DROP COLUMN `Updated_On`;

-- Now that all the referenced columns are dropped, we can finally drop the tbl_db_portal_user table as well.
DROP TABLE `tbl_db_portal_user`;

INSERT INTO `mw_changelog` (`ID`, `APPLIED_AT`, `DESCRIPTION`) VALUES (20121226120200,NOW(),'Patch for RC3 to remove created_by, updated_by, created_on & updated_on fields');
