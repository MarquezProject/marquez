CREATE TABLE dataset_schema_versions_field_mapping(
    dataset_schema_version_uuid UUID REFERENCES dataset_schema_versions(uuid),
    dataset_field_uuid          UUID REFERENCES dataset_fields(uuid),
    PRIMARY KEY (dataset_schema_version_uuid, dataset_field_uuid)
);
