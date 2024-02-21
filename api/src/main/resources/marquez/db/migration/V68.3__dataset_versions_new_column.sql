ALTER TABLE dataset_versions ADD COLUMN dataset_schema_version_uuid uuid REFERENCES dataset_schema_versions(uuid);
