/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;

/** The DAO for {@code dataset} facets. */
public interface DatasetFacetsDao {
  /* An {@code enum} used ... */
  enum Type {
    DATASET,
    INPUT,
    OUTPUT,
    UNKNOWN;
  }

  /* An {@code enum} used to determine the dataset facet. */
  enum DatasetFacet {
    DOCUMENTATION(Type.DATASET, "documentation"),
    DESCRIPTION(Type.DATASET, "description"),
    SCHEMA(Type.DATASET, "schema"),
    DATASOURCE(Type.DATASET, "dataSource"),
    LIFECYCLE_STATE_CHANGE(Type.DATASET, "lifecycleStateChange"),
    VERSION(Type.DATASET, "version"),
    COLUMN_LINEAGE(Type.DATASET, "columnLineage"),
    OWNERSHIP(Type.DATASET, "ownership"),
    DATA_QUALITY_METRICS(Type.INPUT, "dataQualityMetrics"),
    DATA_QUALITY_ASSERTIONS(Type.INPUT, "dataQualityAssertions"),
    OUTPUT_STATISTICS(Type.OUTPUT, "outputStatistics");

    final Type type;
    final String name;

    DatasetFacet(@NonNull final Type type, @NonNull final String name) {
      this.type = type;
      this.name = name;
    }

    Type getType() {
      return type;
    }

    String getName() {
      return name;
    }

    /** ... */
    public static Type typeFromName(@NonNull final String name) {
      return Arrays.stream(DatasetFacet.values())
          .filter(facet -> facet.getName().equalsIgnoreCase(name))
          .map(facet -> facet.getType())
          .findFirst()
          .orElse(Type.UNKNOWN);
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
      Type type,
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

    JsonNode jsonNode = Utils.getMapper().valueToTree(datasetFacets);
    StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(jsonNode.fieldNames(), Spliterator.DISTINCT), false)
        .forEach(
            fieldName ->
                insertDatasetFacet(
                    UUID.randomUUID(),
                    now,
                    datasetUuid,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    DatasetFacet.typeFromName(fieldName),
                    fieldName,
                    FacetUtils.toPgObject(fieldName, jsonNode.get(fieldName))));
  }

  record DatasetFacetRow(
      UUID uuid,
      Instant createdAt,
      UUID datasetUuid,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      DatasetFacetsDao.Type type,
      String name,
      PGobject facet) {}
}
