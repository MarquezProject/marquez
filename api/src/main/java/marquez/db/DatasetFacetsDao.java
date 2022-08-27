package marquez.db;

import java.time.Instant;
import java.util.UUID;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;

public interface DatasetFacetsDao {
  /* An {@code enum} used to determine the dataset facet type. */
  enum FacetType {
    DATASET,
    INPUT,
    OUTPUT
  }

  @SqlUpdate(
      """
      INSERT INTO dataset_version_facets (uuid, created_at, run_uuid, lineage_event_time, lineage_event_type, type, name, facet)
        VALUES (:uuid, :createdAt, :runUuid, lineageEventTime, :lineageEventType, :type, :name, :facet)
      """)
  void insertDatasetFacet(
      UUID uuid,
      Instant createdAt,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      FacetType type,
      String name,
      PGobject facet);
}
