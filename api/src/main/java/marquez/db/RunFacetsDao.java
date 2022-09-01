package marquez.db;

import static marquez.db.Columns.toPgObject;

import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The DAO for {@code run} facets. */
public interface RunFacetsDao {
  Logger log = LoggerFactory.getLogger(RunFacetsDao.class);

  /* An {@code enum} used to determine the dataset facet type. */
  enum Facet {
    NOMINAL_TIME("nominalTime"),
    PARENT("parent"),
    ERROR_MESSAGE("errorMessage"),

    // ...
    SPARK_LOGICAL_PLAN("spark.logicalPlan"),
    SPARK_UNKNOWN("spark_unknown");

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
        if (facet.getName().equalsIgnoreCase(name)) {
          return facet;
        }
      }
      return null;
    }

    /** ... */
    public boolean isSparkUnknown() {
      return this == SPARK_UNKNOWN;
    }

    /** ... */
    public boolean isSparkLogicalPlan() {
      return this == SPARK_LOGICAL_PLAN;
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

  /**
   * @param uuid
   * @param createdAt
   * @param runUuid
   * @param lineageEventTime
   * @param lineageEventType
   * @param name
   * @param facet
   */
  @SqlUpdate(
      """
      INSERT INTO run_facets (
         uuid,
         created_at,
         run_uuid,
         lineage_event_time,
         lineage_event_type,
         name,
         facet
      ) VALUES (
         :uuid,
         :createdAt,
         :runUuid,
         :lineageEventTime,
         :lineageEventType,
         :name,
         :facet
      )
      """)
  void insertRunFacet(
      UUID uuid,
      Instant createdAt,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      String name,
      PGobject facet);

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM run_facets WHERE name = :name)")
  boolean runFacetExists(String name);

  /**
   * @param runUuid
   * @param lineageEventTime
   * @param lineageEventType
   * @param runFacet
   */
  @Transaction
  default void insertRunFacetsFor(
      @NonNull UUID runUuid,
      @NonNull Instant lineageEventTime,
      @NonNull String lineageEventType,
      @NonNull LineageEvent.RunFacet runFacet) {
    final Instant now = Instant.now();

    // Add ...
    Optional.ofNullable(runFacet.getNominalTime())
        .ifPresent(
            nominalTime ->
                insertRunFacet(
                    UUID.randomUUID(),
                    now,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    Facet.NOMINAL_TIME.getName(),
                    toPgObject(Facet.NOMINAL_TIME.asJson(nominalTime))));

    // Add ...
    Optional.ofNullable(runFacet.getParent())
        .ifPresent(
            parent ->
                insertRunFacet(
                    UUID.randomUUID(),
                    now,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    Facet.PARENT.getName(),
                    toPgObject(Facet.PARENT.asJson(parent))));

    // Add ..
    Optional.ofNullable(runFacet.getAdditionalFacets())
        .ifPresent(
            additional ->
                additional.forEach(
                    (name, facet) -> {
                      Optional.ofNullable(Facet.fromName(name))
                          .ifPresentOrElse(
                              (x) -> {
                                if (x.isSparkUnknown()) {
                                  // ...
                                  return;
                                }
                                if (x.isSparkLogicalPlan()) {
                                  if (runFacetExists(x.getName())) {
                                    log.info(
                                        "Facet '{}' has already been linked to run '{}', skipping...",
                                        x.getName(),
                                        runUuid);
                                    // ...
                                    return;
                                  }
                                }
                                insertRunFacet(
                                    UUID.randomUUID(),
                                    now,
                                    runUuid,
                                    lineageEventTime,
                                    lineageEventType,
                                    x.getName(),
                                    toPgObject(x.asJson(facet)));
                              },
                              () -> {
                                insertRunFacet(
                                    UUID.randomUUID(),
                                    now,
                                    runUuid,
                                    lineageEventTime,
                                    lineageEventType,
                                    name,
                                    toPgObject(Facet.asJson(name, facet)));
                              });
                    }));
  }
}
