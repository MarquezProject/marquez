CREATE TABLE owners (
  id         SERIAL PRIMARY KEY,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  name       VARCHAR(64)
);

CREATE TABLE jobs (
  id                SERIAL PRIMARY KEY,
  name              VARCHAR(64),
  created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP,
  current_version   INTEGER,
  current_ownership INTEGER,
  nominal_time      TIMESTAMP,
  category          VARCHAR(64),
  description       TEXT
);

CREATE TABLE ownerships (
  id         SERIAL PRIMARY KEY,
  started_at TIMESTAMP,
  endeded_at TIMESTAMP,
  job_id     INTEGER REFERENCES jobs(id),
  owner_id   INTEGER REFERENCES owners(id)
);

CREATE TABLE job_runs (
  id            SERIAL PRIMARY KEY,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE job_versions (
  id            SERIAL PRIMARY KEY,
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at    TIMESTAMP,
  job_id        INTEGER REFERENCES jobs(id),
  git_repo_uri  VARCHAR(256),
  git_sha       VARCHAR(256),
  latest_run_id INTEGER REFERENCES job_runs(id)
);
