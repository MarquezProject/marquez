CREATE TABLE dataset_facets (
  uuid                 UUID PRIMARY KEY,
  created_at           TIMESTAMPTZ NOT NULL,
  dataset_uuid         UUID REFERENCES datasets(uuid),
  run_uuid             UUID REFERENCES runs(uuid),
  lineage_event_time   TIMESTAMPTZ NOT NULL,
  lineage_event_type   VARCHAR(64) NOT NULL,
  type                 VARCHAR(64) NOT NULL,
  name                 VARCHAR(255) NOT NULL,
  facet                JSONB NOT NULL
);

CREATE INDEX dataset_facets_dataset_uuid_run_uuid_idx ON dataset_facets (dataset_uuid, run_uuid);
