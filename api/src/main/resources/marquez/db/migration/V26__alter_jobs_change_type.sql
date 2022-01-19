/* SPDX-License-Identifier: Apache-2.0 */

ALTER TABLE jobs
  ALTER COLUMN namespace_name TYPE VARCHAR;
ALTER TABLE jobs
  ALTER COLUMN name TYPE VARCHAR;
ALTER TABLE job_versions
    ALTER COLUMN namespace_name TYPE VARCHAR;
ALTER TABLE job_versions
    ALTER COLUMN job_name TYPE VARCHAR;