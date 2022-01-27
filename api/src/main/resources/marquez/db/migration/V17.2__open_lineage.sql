/* SPDX-License-Identifier: Apache-2.0 */

CREATE TABLE lineage_events (
  event_time timestamp with time zone,
  event jsonb,
  event_type text,
  run_id text,
  job_name text,
  job_namespace text,
  producer text,
  CONSTRAINT lineage_event_pk
   PRIMARY KEY(event_time, event)
);
