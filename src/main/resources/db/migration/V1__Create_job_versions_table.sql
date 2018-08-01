CREATE TABLE job_versions (
  id            SERIAL PRIMARY KEY,
  created_at    TIMESTAMP,
  updated_at    TIMESTAMP,
  job_id        INTEGER REFERENCES jobs(id),
  git_repo_uri  VARCHAR(256),
  git_sha       VARCHAR(256),
  latest_run_id INTEGER REFERENCES job_runs(id)
);
