/* SPDX-License-Identifier: Apache-2.0 */

CREATE TABLE dataset_symlinks (
  dataset_uuid      UUID,
  name              VARCHAR NOT NULL,
  namespace_uuid    UUID REFERENCES namespaces(uuid),
  type              VARCHAR(64),
  is_primary        BOOLEAN DEFAULT FALSE,
  created_at        TIMESTAMP NOT NULL,
  updated_at        TIMESTAMP NOT NULL,
  UNIQUE (namespace_uuid, name)
);

CREATE INDEX dataset_symlinks_dataset_uuid on dataset_symlinks (dataset_uuid);

INSERT INTO dataset_symlinks (dataset_uuid, name, namespace_uuid, is_primary, created_at, updated_at)
SELECT d.uuid, d.name, d.namespace_uuid, TRUE, d.created_at, d.updated_at FROM datasets d;

DROP TYPE IF EXISTS DATASET_NAME;
CREATE TYPE DATASET_NAME AS (
    namespace       VARCHAR(255),
    name            VARCHAR(255)
);
