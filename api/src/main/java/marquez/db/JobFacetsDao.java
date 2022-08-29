package marquez.db;

import java.time.Instant;
import java.util.UUID;
import lombok.NonNull;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.postgresql.util.PGobject;

public interface JobFacetsDao {
  default void insertJobFacetsFor(@NonNull LineageEvent olEvent) {}

  @SqlUpdate(
      """
      INSERT INTO job_facets (uuid, created_at, run_uuid, lineage_event_time, lineage_event_type, name, facet)
        VALUES (:uuid, :createdAt, :jobUuid, :runUuid, lineageEventTime, :lineageEventType, :name, :facet)
      """)
  void insertJobFacet(
      UUID uuid,
      Instant createdAt,
      UUID jobUuid,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      String name,
      PGobject facet);
}
