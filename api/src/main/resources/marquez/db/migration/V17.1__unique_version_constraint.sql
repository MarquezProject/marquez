/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE dataset_versions ADD CONSTRAINT dataset_versions_version UNIQUE(version);
ALTER TABLE job_versions ADD CONSTRAINT job_versions_version UNIQUE(version);