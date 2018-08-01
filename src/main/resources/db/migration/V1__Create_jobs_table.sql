CREATE TABLE jobs (
  id                 SERIAL PRIMARY KEY,
  name               VARCHAR(64),
  created_at         TIMESTAMP,
  updated_at         TIMESTAMP,
  current_version    INTEGER REFERENCES job_versions(id),
  current_ownership  INTEGER REFERENCES ownerships(id),
  nominal_time       TIMESTAMP,
  category           VARCHAR(64),
  description        TEXT
);
