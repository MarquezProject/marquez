CREATE TABLE owners (
  id          SERIAL PRIMARY KEY,
  started_at  TIMESTAMP,
  endeded_at  TIMESTAMP,
  job_id      INTEGER REFERENCES jobs(id),
  owner_id    INTEGER REFERENCES owners(id)
);
