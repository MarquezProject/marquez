/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.UUID;
import java.util.stream.StreamSupport;
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
  String SPARK_UNKNOWN = "spark_unknown";
  String SPARK_LOGICAL_PLAN = "spark.logicalPlan";

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

  @SqlQuery("SELECT EXISTS (SELECT 1 FROM run_facets WHERE name = :name AND run_uuid = :runUuid)")
  boolean runFacetExists(String name, UUID runUuid);

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

    JsonNode jsonNode = Utils.getMapper().valueToTree(runFacet);
    StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(jsonNode.fieldNames(), Spliterator.DISTINCT), false)
        .filter(fieldName -> !fieldName.equalsIgnoreCase(SPARK_UNKNOWN))
        .filter(
            fieldName -> {
              if (fieldName.equalsIgnoreCase(SPARK_LOGICAL_PLAN)) {
                if (runFacetExists(fieldName, runUuid)) {
                  log.info(
                      "Facet '{}' has already been linked to run '{}', skipping...",
                      fieldName,
                      runUuid);
                  // row already exists
                  return false;
                }
              }
              return true;
            })
        .forEach(
            fieldName ->
                insertRunFacet(
                    UUID.randomUUID(),
                    now,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    fieldName,
                    FacetUtils.toPgObject(fieldName, jsonNode.get(fieldName))));
  }

  record RunFacetRow(
      UUID uuid,
      Instant createdAt,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      String name,
      PGobject facet) {}
}
