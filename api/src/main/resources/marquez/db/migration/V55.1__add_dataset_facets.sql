CREATE TABLE dataset_facets (
  created_at            TIMESTAMPTZ NOT NULL,
  dataset_uuid          UUID REFERENCES datasets(uuid),
  dataset_version_uuid  UUID REFERENCES dataset_versions(uuid),
  run_uuid              UUID REFERENCES runs(uuid),
  lineage_event_time    TIMESTAMPTZ NOT NULL,
  lineage_event_type    VARCHAR(64) NOT NULL,
  type                  VARCHAR(64) NOT NULL,
  name                  VARCHAR(255) NOT NULL,
  facet                 JSONB NOT NULL
);
