/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE job_versions ALTER COLUMN job_context_uuid DROP NOT NULL;

-- Implementation required for streaming CDC support
ALTER TABLE job_versions_io_mapping REPLICA IDENTITY FULL;