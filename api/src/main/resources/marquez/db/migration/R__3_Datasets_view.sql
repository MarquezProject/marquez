CREATE OR REPLACE VIEW datasets_view
AS
SELECT d.uuid,
       d.type,
       d.created_at,
       d.updated_at,
       d.namespace_uuid,
       d.source_uuid,
       d.name,
       array_agg(CAST((namespaces.name, symlinks.name) AS DATASET_NAME)) AS dataset_symlinks,
       d.physical_name,
       d.description,
       d.current_version_uuid,
       d.last_modified_at,
       d.namespace_name,
       d.source_name,
       d.is_deleted
FROM datasets d
JOIN dataset_symlinks symlinks ON d.uuid = symlinks.dataset_uuid
INNER JOIN namespaces ON symlinks.namespace_uuid = namespaces.uuid
WHERE d.is_hidden IS FALSE
GROUP BY d.uuid;