CREATE TABLE owners (
  guid       UUID PRIMARY KEY,
  name       VARCHAR(64) UNIQUE NOT NULL,
  created_at TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE jobs (
  guid              UUID PRIMARY KEY,
  name              VARCHAR(64) UNIQUE NOT NULL,
  created_at        TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP with time zone,
  current_version   INTEGER,
  current_ownership INTEGER,
  nominal_time      TIMESTAMP with time zone,
  category          VARCHAR(64),
  description       TEXT NOT NULL
);

CREATE TABLE ownerships (
  guid       UUID PRIMARY KEY,
  started_at TIMESTAMP with time zone,
  ended_at   TIMESTAMP with time zone,
  job_guid   UUID REFERENCES jobs(guid),
  owner_guid UUID REFERENCES owners(guid)
);

CREATE TABLE job_versions (
  guid            UUID PRIMARY KEY,
  created_at      TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP with time zone,
  input_dataset   VARCHAR(64) NOT NULL,
  output_dataset  VARCHAR(64) NOT NULL,
  job_guid        UUID REFERENCES jobs(guid),
  git_repo_uri    VARCHAR(255),
  git_sha         VARCHAR(255),
  latest_run_guid UUID
);

CREATE TABLE job_runs (
  guid                        UUID PRIMARY KEY,
  created_at                  TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP,
  job_version_guid            UUID REFERENCES job_versions(guid),
  run_guid                    VARCHAR(255) UNIQUE NOT NULL,
  started_at                  TIMESTAMP with time zone,
  ended_at                    TIMESTAMP with time zone,
  input_dataset_version_guid  UUID,
  output_dataset_version_guid UUID,
  latest_heartbeat            TIMESTAMP with time zone
);

CREATE TABLE job_run_states (
  guid            UUID PRIMARY KEY,
  transitioned_at TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP,
  job_run_guid    UUID REFERENCES job_runs(guid),
  state           INTEGER
);

CREATE TABLE datasets (
  guid            UUID PRIMARY KEY,
  name            VARCHAR(64) UNIQUE NOT NULL,
  created_at      TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP with time zone,
  type            INTEGER,
  origin          INTEGER,
  current_version INTEGER,
  description     TEXT NOT NULL
);

CREATE TABLE dbs (
  guid           UUID PRIMARY KEY,
  name           VARCHAR(64) UNIQUE NOT NULL,
  created_at     TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP,
  type           INTEGER,
  connection_url VARCHAR(255),
  description    TEXT NOT NULL
);

CREATE TABLE db_table_versions (
  guid          UUID PRIMARY KEY,
  created_at    TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP,
  dataset_guid  UUID REFERENCES datasets(guid),
  db_guid       UUID REFERENCES dbs(guid),
  description TEXT NOT NULL
);

CREATE TABLE iceberg_table_versions (
  guid                 UUID PRIMARY KEY,
  created_at           TIMESTAMP with time zone DEFAULT CURRENT_TIMESTAMP,
  dataset_guid         UUID REFERENCES datasets(guid),
  previous_snapshot_id BIGINT,
  current_snapshot_id  BIGINT,
  metadata_location    VARCHAR(255)
);
