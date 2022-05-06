ALTER TABLE jobs ADD COLUMN symlink_target_uuid uuid REFERENCES jobs (uuid);
CREATE INDEX jobs_symlinks ON jobs (symlink_target_uuid)
    INCLUDE (uuid, namespace_name, name)
    WHERE symlink_target_uuid IS NOT NULL;