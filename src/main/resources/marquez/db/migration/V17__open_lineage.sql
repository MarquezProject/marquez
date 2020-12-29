CREATE TABLE lineage_event (
  event_time timestamp with time zone,
  event_type text,
  run_id text,
  job_name text,
  job_namespace text,
  inputs jsonb,
  outputs jsonb,
  producer text,
  CONSTRAINT lineage_event_pk
    PRIMARY KEY(event_time, event_type, run_id, job_name, job_namespace)
);

create extension if not exists "uuid-ossp";
alter table sources alter column uuid set default uuid_generate_v4();
alter table namespaces alter column uuid set default uuid_generate_v4();
alter table owners alter column uuid set default uuid_generate_v4();
alter table namespace_ownerships alter column uuid set default uuid_generate_v4();
alter table tags alter column uuid set default uuid_generate_v4();
alter table runs alter column uuid set default uuid_generate_v4();
alter table run_states alter column uuid set default uuid_generate_v4();
alter table run_args alter column uuid set default uuid_generate_v4();
alter table jobs alter column uuid set default uuid_generate_v4();
alter table job_versions alter column uuid set default uuid_generate_v4();
alter table job_contexts alter column uuid set default uuid_generate_v4();
alter table datasets alter column uuid set default uuid_generate_v4();
alter table dataset_fields alter column uuid set default uuid_generate_v4();
alter table dataset_versions alter column uuid set default uuid_generate_v4();
ALTER TABLE dataset_versions ADD CONSTRAINT dataset_versions_version UNIQUE(version);
ALTER TABLE job_versions ADD CONSTRAINT job_versions_version UNIQUE(version);