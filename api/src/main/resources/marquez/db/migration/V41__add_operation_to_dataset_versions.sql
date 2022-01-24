alter table dataset_versions add column lifecycle_state VARCHAR(63);
alter table datasets add column is_deleted BOOLEAN DEFAULT FALSE;