CREATE OR REPLACE VIEW datasets_view
AS
SELECT uuid,
       type,
       created_at,
       updated_at,
       namespace_uuid,
       source_uuid,
       name,
       physical_name,
       description,
       current_version_uuid,
       last_modified_at,
       namespace_name,
       source_name,
       is_deleted
FROM datasets
WHERE is_hidden IS FALSE;