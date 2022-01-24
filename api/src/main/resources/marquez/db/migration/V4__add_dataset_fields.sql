/* SPDX-License-Identifier: Apache-2.0 */

CREATE TABLE dataset_fields (
  uuid         UUID PRIMARY KEY,
  type         VARCHAR(64) NOT NULL,
  created_at   TIMESTAMP NOT NULL,
  updated_at   TIMESTAMP NOT NULL,
  dataset_uuid UUID REFERENCES datasets(uuid),
  name         VARCHAR(255) NOT NULL,
  description  TEXT,
  UNIQUE (dataset_uuid, name)
);

CREATE TABLE dataset_versions_field_mapping (
  dataset_version_uuid UUID REFERENCES dataset_versions(uuid),
  dataset_field_uuid   UUID REFERENCES dataset_fields(uuid),
  PRIMARY KEY (dataset_version_uuid, dataset_field_uuid)
);
