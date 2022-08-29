/* SPDX-License-Identifier: Apache-2.0 */

DROP TABLE column_level_lineage;

CREATE TABLE column_level_lineage (
                                      uuid                          uuid primary key,
                                      dataset_version_uuid          uuid REFERENCES dataset_versions(uuid),
                                      output_column_name            VARCHAR(255) NOT NULL,
                                      input_field                   VARCHAR(255) NOT NULL, -- reference dataset_fields.uuid
                                      transformation_description    VARCHAR(255) NOT NULL,
                                      transformation_type           VARCHAR(255) NOT NULL,
                                      created_at                    TIMESTAMP NOT NULL,
                                      updated_at                    TIMESTAMP NOT NULL,
                                      UNIQUE (dataset_version_uuid, output_column_name, input_field)
);

INSERT INTO column_level_lineage (uuid, dataset_version_uuid, output_column_name, input_field,
                                  transformation_description, transformation_type, created_at,
                                  updated_at)
VALUES (md5('whatever')::uuid, md5('dataset_version_uuid_example')::uuid,  'column_a', 'input_field_a', 'Identity transformation', 'IDENTITY', current_timestamp, current_timestamp);

INSERT INTO column_level_lineage (uuid, dataset_version_uuid, output_column_name, input_field,
                                  transformation_description, transformation_type, created_at,
                                  updated_at)
VALUES (md5('whatever')::uuid, md5('dataset_version_uuid_example')::uuid,  'column_a', 'input_field_b', 'Identity transformation', 'IDENTITY', current_timestamp, current_timestamp);