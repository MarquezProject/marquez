package marquez.db;

import java.util.UUID;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;

public interface JobVersionFacetsDao {
  @SqlUpdate(
      """
      INSERT INTO job_version_facets (uuid, job_version_uuid, name, facet)
        VALUES (:uuid, :jobVersionUuid, :name, :facet)
      """)
  void upsertJobVersionFacet(UUID uuid, UUID jobVersionUuid, String name, PGobject facet);
}
