package marquez.db;

import java.util.UUID;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;

public interface RunFacetsDao {
  @SqlUpdate(
      """
      INSERT INTO dataset_version_facets (uuid, run_uuid, name, facet)
        VALUES (:uuid, :runUuid, :name, :facet)
      """)
  void upsertRunFacet(UUID uuid, UUID runUuid, String name, PGobject facet);
}
