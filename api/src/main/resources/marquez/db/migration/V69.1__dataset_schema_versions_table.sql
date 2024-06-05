/* SPDX-License-Identifier: Apache-2.0 */
CREATE TABLE dataset_schema_versions(
    uuid         UUID PRIMARY KEY,
    dataset_uuid UUID REFERENCES datasets(uuid) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ NOT NULL
);
