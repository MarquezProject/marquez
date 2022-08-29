package marquez.db;

import static marquez.db.Columns.toPgObject;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;

/** The DAO for {@code dataset} facets. */
public interface DatasetFacetsDao {
  /* An {@code enum} used to determine the dataset facet type. */
  enum Facet {
    DOCUMENTATION("dataset", "documentation"),
    SCHEMA("dataset", "schema"),
    DATASOURCE("dataset", "dataSource"),
    LIFECYCLE_STATE_CHANGE("dataset", "lifecycleStateChange"),
    VERSION("dataset", "version"),
    COLUMN_LINEAGE("dataset", "columnLineage"),
    OWNERSHIP("dataset", "ownership"),
    DATA_QUALITY_METRICS("input", "dataQualityMetrics"),
    DATA_QUALITY_ASSERTIONS("input", "dataQualityAssertions"),
    OUTPUT_STATISTICS("output", "outputStatistics");

    /* .. */
    static final String UNKNOWN = "UNKNOWN";

    final String type;
    final String name;

    Facet(@NonNull final String type, @NonNull final String name) {
      this.type = type.toUpperCase();
      this.name = name;
    }

    String getType() {
      return type;
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
  }

  /**
   * @param uuid
   * @param createdAt
   * @param datasetUuid
   * @param runUuid
   * @param lineageEventTime
   * @param lineageEventType
   * @param type
   * @param name
   * @param facet
   */
  @SqlUpdate(
      """
          INSERT INTO dataset_facets (
             uuid,
             created_at,
             dataset_uuid,
             run_uuid,
             lineage_event_time,
             lineage_event_type,
             type,
             name,
             facet
          ) VALUES (
             :uuid,
             :createdAt,
             :datasetUuid,
             :runUuid,
             :lineageEventTime,
             :lineageEventType,
             :type,
             :name,
             :facet
          )
      """)
  void insertDatasetFacet(
      UUID uuid,
      Instant createdAt,
      UUID datasetUuid,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      String type,
      String name,
      PGobject facet);

  /**
   * @param datasetUuid
   * @param runUuid
   * @param lineageEventTime
   * @param lineageEventType
   * @param datasetFacets
   */
  @Transaction
  default void insertDatasetFacetsFor(
      @NonNull UUID datasetUuid,
      @NonNull UUID runUuid,
      @NonNull Instant lineageEventTime,
      @NonNull String lineageEventType,
      @NonNull LineageEvent.DatasetFacets datasetFacets) {
    final Instant now = Instant.now();

    // Add ...
    Optional.ofNullable(datasetFacets.getDocumentation())
        .ifPresent(
            documentation ->
                insertDatasetFacet(
                    UUID.randomUUID(),
                    now,
                    datasetUuid,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    Facet.DOCUMENTATION.getType(),
                    Facet.DOCUMENTATION.getName(),
                    toPgObject(documentation)));

    // Add ...
    Optional.ofNullable(datasetFacets.getSchema())
        .ifPresent(
            schema ->
                insertDatasetFacet(
                    UUID.randomUUID(),
                    now,
                    datasetUuid,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    Facet.SCHEMA.getType(),
                    Facet.SCHEMA.getName(),
                    toPgObject(schema)));

    // Add ...
    Optional.ofNullable(datasetFacets.getDataSource())
        .ifPresent(
            datasourceDataset ->
                insertDatasetFacet(
                    UUID.randomUUID(),
                    now,
                    datasetUuid,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    Facet.DATASOURCE.getType(),
                    Facet.DATASOURCE.getName(),
                    toPgObject(datasourceDataset)));

    // Add ...
    Optional.ofNullable(datasetFacets.getLifecycleStateChange())
        .ifPresent(
            lifecycleStateChange ->
                insertDatasetFacet(
                    UUID.randomUUID(),
                    now,
                    datasetUuid,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    Facet.LIFECYCLE_STATE_CHANGE.getType(),
                    Facet.LIFECYCLE_STATE_CHANGE.getName(),
                    toPgObject(lifecycleStateChange)));

    // Add ..
    Optional.ofNullable(datasetFacets.getAdditionalFacets())
        .ifPresent(
            additional ->
                additional.forEach(
                    (name, facet) -> {
                      Optional.ofNullable(Facet.fromName(name))
                          .ifPresentOrElse(
                              (x) -> {
                                insertDatasetFacet(
                                    UUID.randomUUID(),
                                    now,
                                    datasetUuid,
                                    runUuid,
                                    lineageEventTime,
                                    lineageEventType,
                                    x.getType(),
                                    x.getName(),
                                    toPgObject(facet));
                              },
                              () -> {
                                insertDatasetFacet(
                                    UUID.randomUUID(),
                                    now,
                                    datasetUuid,
                                    runUuid,
                                    lineageEventTime,
                                    lineageEventType,
                                    Facet.UNKNOWN,
                                    name,
                                    toPgObject(facet));
                              });
                    }));
  }
}
