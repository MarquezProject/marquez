package marquez.db;

import java.util.UUID;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;

public interface DatasetVersionFacetsDao {
  @SqlUpdate(
      """
      INSERT INTO dataset_version_facets (uuid, dataset_version_uuid, name, facet)
        VALUES (:uuid, :datasetVersionUuid, :name, :facet)
      """)
  void upsertDatasetVersionFacet(UUID uuid, UUID datasetVersionUuid, String name, PGobject facet);
}
