package marquez.db;

import java.time.Instant;
import java.util.UUID;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;

public interface RunFacetsDao {
  @SqlUpdate(
      """
      INSERT INTO dataset_version_facets (uuid, created_at, run_uuid, lineage_event_time, lineage_event_type, name, facet)
        VALUES (:uuid, :createdAt, :runUuid, lineageEventTime, :lineageEventType, :name, :facet)
      """)
  void insertRunFacet(
      UUID uuid,
      Instant createdAt,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      String name,
      PGobject facet);
}
