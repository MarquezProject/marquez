ALTER TABLE jobs DROP CONSTRAINT jobs_name_key;
CREATE UNIQUE INDEX jobs_nsguid_name_uniq_key ON jobs (namespace_guid, name);