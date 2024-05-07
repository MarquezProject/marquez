/* SPDX-License-Identifier: Apache-2.0 */
CREATE TABLE jobs_tag_mapping (
  job_uuid UUID REFERENCES jobs(uuid),
  tag_uuid     UUID REFERENCES tags(uuid),
  tagged_at    TIMESTAMPTZ NOT NULL,
  PRIMARY KEY (tag_uuid, job_uuid)
);


