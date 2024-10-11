CREATE OR REPLACE VIEW datasets_view AS
SELECT d.uuid,
    d.type,
    d.created_at,
    d.updated_at,
    CASE
    	 WHEN (d.namespace_name = namespaces.name AND d.name = symlinks.name) THEN d.namespace_uuid
    	 ELSE namespaces.uuid
    END
    AS namespace_uuid ,
    d.source_uuid,
    CASE
    	 WHEN (d.namespace_name = namespaces.name and d.name = symlinks.name) THEN d.name
    	 ELSE symlinks.name
    END
    AS name,
    array(SELECT ROW(namespaces.name::character varying(255), symlinks.name::character varying(255))::dataset_name) AS dataset_symlinks,
    d.physical_name,
    d.description,
    d.current_version_uuid,
    d.last_modified_at,
    CASE
    	 WHEN (d.namespace_name = namespaces.name AND d.name = symlinks.name) THEN d.namespace_name
    	 ELSE namespaces.name
    END
    AS namespace_name,
    d.source_name,
    d.is_deleted
   FROM datasets d
     JOIN dataset_symlinks symlinks ON d.uuid = symlinks.dataset_uuid
     JOIN namespaces ON symlinks.namespace_uuid = namespaces.uuid
  WHERE d.is_hidden is false;
