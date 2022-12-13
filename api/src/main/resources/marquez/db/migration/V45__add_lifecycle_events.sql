CREATE TABLE lifecycle_events (
  uuid             UUID PRIMARY KEY,
  transitioned_at  TIMESTAMP NOT NULL,
  namespace_name   VARCHAR(255) NOT NULL,
  state            VARCHAR(64) NOT NULL,
  message          TEXT
);
