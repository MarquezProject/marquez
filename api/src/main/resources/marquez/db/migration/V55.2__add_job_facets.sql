CREATE TABLE job_facets (
  created_at         TIMESTAMPTZ NOT NULL,
  job_uuid           UUID REFERENCES jobs(uuid),
  run_uuid           UUID REFERENCES runs(uuid),
  lineage_event_time TIMESTAMPTZ NOT NULL,
  lineage_event_type VARCHAR(64) NOT NULL,
  name               VARCHAR(255) NOT NULL,
  facet              JSONB NOT NULL
);

CREATE INDEX job_facets_job_uuid_run_uuid_idx ON job_facets (job_uuid, run_uuid);
