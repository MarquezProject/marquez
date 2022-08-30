package marquez.db;

import static marquez.db.Columns.toPgObject;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;

/** The DAO for {@code job} facets. */
public interface JobFacetsDao {
  /* An {@code enum} used to ... */
  enum Facet {
    SOURCE_CODE_LOCATION("sourceCodeLocation"),
    SOURCE_CODE("sourceCode"),
    SQL("sql"),
    OWNERSHIP("ownership");

    final String name;

    Facet(@NonNull final String name) {
      this.name = name;
    }

    String getName() {
      return name;
    }

    /** ... */
    public static Facet fromName(@NonNull final String name) {
      for (final Facet facet : Facet.values()) {
        if (facet.name().equalsIgnoreCase(name)) {
          return facet;
        }
      }
      return null;
    }

    /** ... */
    public ObjectNode asJson(@NonNull Object facetValue) {
      return asJson(name, facetValue);
    }

    /** ... */
    public static ObjectNode asJson(@NonNull final String facetName, @NonNull Object facetValue) {
      final ObjectNode facetAsJson = Utils.getMapper().createObjectNode();
      facetAsJson.putPOJO(facetName, facetValue);
      return facetAsJson;
    }
  }

  @SqlUpdate(
      """
      INSERT INTO job_facets (
         uuid,
         created_at,
         job_uuid,
         run_uuid,
         lineage_event_time,
         lineage_event_type,
         name,
         facet
      ) VALUES (
         :uuid,
         :createdAt,
         :jobUuid,
         :runUuid,
         :lineageEventTime,
         :lineageEventType,
         :name,
         :facet
      )
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

  @Transaction
  default void insertJobFacetsFor(
      @NonNull UUID jobUuid,
      @NonNull UUID runUuid,
      @NonNull Instant lineageEventTime,
      @NonNull String lineageEventType,
      @NonNull LineageEvent.JobFacet jobFacet) {
    final Instant now = Instant.now();

    // Add ...
    Optional.ofNullable(jobFacet.getSourceCodeLocation())
        .ifPresent(
            sourceCodeLocation ->
                insertJobFacet(
                    UUID.randomUUID(),
                    now,
                    jobUuid,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    Facet.SOURCE_CODE_LOCATION.getName(),
                    toPgObject(Facet.SOURCE_CODE_LOCATION.asJson(sourceCodeLocation))));

    // Add ...
    Optional.ofNullable(jobFacet.getSql())
        .ifPresent(
            sql ->
                insertJobFacet(
                    UUID.randomUUID(),
                    now,
                    jobUuid,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    Facet.SQL.getName(),
                    toPgObject(Facet.SQL.asJson(sql))));

    // Add ..
    Optional.ofNullable(jobFacet.getAdditionalFacets())
        .ifPresent(
            additional ->
                additional.forEach(
                    (name, facet) -> {
                      Optional.ofNullable(Facet.fromName(name))
                          .ifPresentOrElse(
                              (x) -> {
                                insertJobFacet(
                                    UUID.randomUUID(),
                                    now,
                                    jobUuid,
                                    runUuid,
                                    lineageEventTime,
                                    lineageEventType,
                                    x.getName(),
                                    toPgObject(x.asJson(facet)));
                              },
                              () -> {
                                insertJobFacet(
                                    UUID.randomUUID(),
                                    now,
                                    jobUuid,
                                    runUuid,
                                    lineageEventTime,
                                    lineageEventType,
                                    name,
                                    toPgObject(Facet.asJson(name, facet)));
                              });
                    }));
  }
}
