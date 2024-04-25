CREATE TABLE dataset_schema_versions(
    uuid         UUID PRIMARY KEY,
    dataset_uuid UUID REFERENCES datasets(uuid),
    created_at   TIMESTAMPTZ NOT NULL
);
