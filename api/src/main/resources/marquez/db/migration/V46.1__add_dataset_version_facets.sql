CREATE TABLE dataset_version_facets (
  uuid                 UUID PRIMARY KEY,
  created_at           TIMESTAMPTZ NOT NULL,
  dataset_version_uuid UUID REFERENCES dataset_versions(uuid),
  run_uuid             UUID REFERENCES runs(uuid),
  lineage_event_time   TIMESTAMPTZ NOT NULL,
  lineage_event_type   VARCHAR(64) NOT NULL,
  type                 VARCHAR(64) NOT NULL,
  name                 VARCHAR(255) NOT NULL,
  facet                JSONB NOT NULL
);

CREATE UNIQUE INDEX dataset_version_facets_dataset_version_uuid_name_idx
    ON dataset_version_facets (dataset_version_uuid, name);
