CREATE TABLE run_facets (
  uuid               UUID PRIMARY KEY,
  created_at         TIMESTAMPTZ NOT NULL,
  run_uuid           UUID REFERENCES runs(uuid),
  lineage_event_time TIMESTAMPTZ NOT NULL,
  lineage_event_type VARCHAR(64) NOT NULL,
  name               VARCHAR(255) NOT NULL,
  facet              JSONB NOT NULL
);

CREATE UNIQUE INDEX run_facets_run_uuid_idx ON run_facets (run_uuid);
