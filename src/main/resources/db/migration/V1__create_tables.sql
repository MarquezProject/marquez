CREATE TABLE owners (
  id         SERIAL PRIMARY KEY,
  name       VARCHAR(64) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE jobs (
  id                SERIAL PRIMARY KEY,
  name              VARCHAR(64) NOT NULL,
  created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP,
  current_version   INTEGER,
  current_ownership INTEGER,
  nominal_time      TIMESTAMP,
  category          VARCHAR(64),
  description       TEXT NOT NULL
);

CREATE TABLE ownerships (
  id         SERIAL PRIMARY KEY,
  started_at TIMESTAMP,
  endeded_at TIMESTAMP,
  job_id     INTEGER REFERENCES jobs(id),
  owner_id   INTEGER REFERENCES owners(id)
);

CREATE TABLE job_runs (
  id               SERIAL PRIMARY KEY,
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  run_id           VARCHAR(255) NOT NULL,
  started_at       TIMESTAMP,
  endeded_at       TIMESTAMP,
  latest_heartbeat TIMESTAMP
);

CREATE TABLE job_run_states (
  id              SERIAL PRIMARY KEY,
  transitioned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  job_run_id      INTEGER REFERENCES job_runs(id),
  state           INTEGER
);

CREATE TABLE job_versions (
  id            SERIAL PRIMARY KEY,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP,
  job_id        INTEGER REFERENCES jobs(id),
  git_repo_uri  VARCHAR(255),
  git_sha       VARCHAR(255),
  latest_run_id INTEGER REFERENCES job_runs(id)
);

CREATE TABLE datasets (
  id              SERIAL PRIMARY KEY,
  name            VARCHAR(64) NOT NULL,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP,
  type            INTEGER,
  origin          INTEGER,
  current_version INTEGER,
  description     TEXT NOT NULL
);
