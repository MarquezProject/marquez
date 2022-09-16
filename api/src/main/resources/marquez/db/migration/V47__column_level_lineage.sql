/* SPDX-License-Identifier: Apache-2.0 */

CREATE TABLE column_level_lineage (
  output_dataset_version_uuid   uuid REFERENCES dataset_versions(uuid), -- allows join to run_id
  output_dataset_field_uuid     uuid REFERENCES dataset_fields(uuid),
  input_dataset_version_uuid    uuid REFERENCES dataset_versions(uuid), -- speed up graph column lineage graph traversal
  input_dataset_field_uuid      uuid REFERENCES dataset_fields(uuid),
  transformation_description    VARCHAR(255) NOT NULL,
  transformation_type           VARCHAR(255) NOT NULL,
  created_at                    TIMESTAMP NOT NULL,
  updated_at                    TIMESTAMP NOT NULL,
  UNIQUE (output_dataset_version_uuid, output_dataset_field_uuid, input_dataset_version_uuid, input_dataset_field_uuid)
);