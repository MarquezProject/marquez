-- this database migration should fail in an awkward way
-- this should succeed when doing clean DB install but fail when being run on an existing DB in version 54
ALTER TABLE dataset_symlinks ALTER COLUMN name TYPE VARCHAR(65);