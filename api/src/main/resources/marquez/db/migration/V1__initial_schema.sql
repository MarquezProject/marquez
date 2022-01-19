/* SPDX-License-Identifier: Apache-2.0 */

CREATE TABLE namespaces (
  uuid               UUID PRIMARY KEY,
  created_at         TIMESTAMP NOT NULL,
  updated_at         TIMESTAMP NOT NULL,
  name               VARCHAR(64) UNIQUE NOT NULL,
  description        TEXT,
  current_owner_name VARCHAR(64)
);

CREATE TABLE owners (
  uuid       UUID PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  name       VARCHAR(64) UNIQUE NOT NULL
);

CREATE TABLE namespace_ownerships (
  uuid           UUID PRIMARY KEY,
  started_at     TIMESTAMP NOT NULL,
  ended_at       TIMESTAMP,
  namespace_uuid UUID REFERENCES namespaces(uuid),
  owner_uuid     UUID REFERENCES owners(uuid),
  UNIQUE (namespace_uuid, owner_uuid)
);

CREATE TABLE sources (
  uuid           UUID PRIMARY KEY,
  type           VARCHAR(64) NOT NULL,
  created_at     TIMESTAMP NOT NULL,
  updated_at     TIMESTAMP NOT NULL,
  name           VARCHAR(64) UNIQUE NOT NULL,
  connection_url VARCHAR(255) NOT NULL,
  description    TEXT,
  UNIQUE (name, connection_url)
);

CREATE TABLE datasets (
  uuid                 UUID PRIMARY KEY,
  type                 VARCHAR(64) NOT NULL,
  created_at           TIMESTAMP NOT NULL,
  updated_at           TIMESTAMP NOT NULL,
  namespace_uuid       UUID REFERENCES namespaces(uuid),
  source_uuid          UUID REFERENCES sources(uuid),
  name                 VARCHAR(255) NOT NULL,
  physical_name        VARCHAR(255) NOT NULL,
  description          TEXT,
  current_version_uuid UUID,
  UNIQUE (namespace_uuid, source_uuid, name, physical_name)
);

CREATE TABLE jobs (
  uuid                 UUID PRIMARY KEY,
  type                 VARCHAR(64) NOT NULL,
  created_at           TIMESTAMP NOT NULL,
  updated_at           TIMESTAMP NOT NULL,
  namespace_uuid       UUID REFERENCES namespaces(uuid),
  name                 VARCHAR(255) UNIQUE NOT NULL,
  description          TEXT,
  current_version_uuid UUID,
  UNIQUE (namespace_uuid, name)
);

CREATE TABLE job_versions (
  uuid            UUID PRIMARY KEY,
  created_at      TIMESTAMP NOT NULL,
  updated_at      TIMESTAMP NOT NULL,
  job_uuid        UUID REFERENCES jobs(uuid),
  version         UUID NOT NULL,
  location        VARCHAR(255) NOT NULL,
  latest_run_uuid UUID,
  UNIQUE (job_uuid, version)
);

CREATE TABLE job_versions_io_mapping (
  job_version_uuid UUID REFERENCES job_versions(uuid),
  dataset_uuid     UUID REFERENCES datasets(uuid),
  io_type          VARCHAR(64) NOT NULL,
  PRIMARY KEY (job_version_uuid, dataset_uuid, io_type) 
);

CREATE TABLE run_args (
  uuid       UUID PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  args       VARCHAR(255) NOT NULL,
  checksum   VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE runs (
  uuid               UUID PRIMARY KEY,
  created_at         TIMESTAMP NOT NULL,
  updated_at         TIMESTAMP NOT NULL,
  job_version_uuid   UUID REFERENCES job_versions(uuid),
  run_args_uuid      UUID REFERENCES run_args(uuid),
  nominal_start_time TIMESTAMP,
  nominal_end_time   TIMESTAMP,
  current_run_state  VARCHAR(64)
);

CREATE TABLE run_states (
  uuid            UUID PRIMARY KEY,
  transitioned_at TIMESTAMP NOT NULL,
  run_uuid        UUID REFERENCES runs(uuid),
  state           VARCHAR(64) NOT NULL
);

CREATE TABLE dataset_versions (
  uuid         UUID PRIMARY KEY,
  created_at   TIMESTAMP NOT NULL,
  dataset_uuid UUID REFERENCES datasets(uuid),
  version      UUID NOT NULL,
  run_uuid     UUID,
  UNIQUE (dataset_uuid, version)
);

CREATE TABLE stream_versions (
  dataset_version_uuid UUID REFERENCES dataset_versions(uuid),
  schema_location      VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE runs_input_mapping (
  run_uuid             UUID REFERENCES runs(uuid),
  dataset_version_uuid UUID REFERENCES dataset_versions(uuid),
  PRIMARY KEY (run_uuid, dataset_version_uuid) 
);
