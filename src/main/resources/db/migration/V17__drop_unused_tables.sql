ALTER TABLE job_runs DROP COLUMN job_run_definition_guid;
ALTER TABLE db_table_versions DROP COLUMN db_guid;

DROP TABLE ownerships;
DROP TABLE job_run_definitions;
DROP TABLE stream_dataset_versions;
DROP TABLE iceberg_dataset_versions;
DROP TABLE db_dataset_versions;
DROP TABLE dbs;
DROP TABLE dataset_versions;
