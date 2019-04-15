ALTER TABLE datasources ADD COLUMN name VARCHAR(64) NOT NULL;
ALTER TABLE datasources ADD COLUMN connection_url VARCHAR(128) NOT NULL;
ALTER TABLE datasources DROP COLUMN type;
ALTER TABLE datasources DROP COLUMN name;
ALTER TABLE datasources ADD COLUMN name VARCHAR(64) NOT NULL;
ALTER TABLE datasets DROP COLUMN name;

CREATE TABLE db_table_infos(
    uuid UUID PRIMARY KEY,
    db_name VARCHAR(64) NOT NULL,
    db_schema_name VARCHAR(64) NOT NULL
);

ALTER TABLE db_table_versions ADD COLUMN db_table_info_uuid UUID 
    REFERENCES db_table_infos(uuid) NOT NULL;

ALTER TABLE db_table_versions ADD COLUMN db_table_name VARCHAR(64) NOT NULL;

ALTER TABLE db_table_versions ALTER COLUMN description DROP NOT NULL;

ALTER TABLE datasets DROP COLUMN current_version;
ALTER TABLE datasets ADD COLUMN current_version_uuid UUID;