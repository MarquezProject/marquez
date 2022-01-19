/* SPDX-License-Identifier: Apache-2.0 */

CREATE TABLE job_contexts (
  uuid       UUID PRIMARY KEY,
  created_at TIMESTAMP NOT NULL,
  context    TEXT NOT NULL,
  checksum   VARCHAR(255) UNIQUE NOT NULL
);
