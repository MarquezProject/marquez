/* SPDX-License-Identifier: Apache-2.0 */
ALTER TABLE dataset_versions ADD COLUMN dataset_schema_version_uuid uuid REFERENCES dataset_schema_versions(uuid);

-- Implementation required for streaming CDC support
ALTER TABLE dataset_versions REPLICA IDENTITY FULL;