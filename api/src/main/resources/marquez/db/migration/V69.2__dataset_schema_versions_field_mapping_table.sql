/* SPDX-License-Identifier: Apache-2.0 */
CREATE TABLE dataset_schema_versions_field_mapping(
    dataset_schema_version_uuid UUID REFERENCES dataset_schema_versions(uuid) ON DELETE CASCADE,
    dataset_field_uuid          UUID REFERENCES dataset_fields(uuid) ON DELETE CASCADE,
    PRIMARY KEY (dataset_schema_version_uuid, dataset_field_uuid)
);
