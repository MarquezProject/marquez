/*
 * Copyright 2018-2023 contributors to the Marquez project
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
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;

/** The DAO for {@code job} facets. */
public interface JobFacetsDao {

  @SqlUpdate(
      """
      INSERT INTO job_facets (
         created_at,
         job_uuid,
         run_uuid,
         lineage_event_time,
         lineage_event_type,
         name,
         facet
      ) VALUES (
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

    JsonNode jsonNode = Utils.getMapper().valueToTree(jobFacet);
    StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(jsonNode.fieldNames(), Spliterator.DISTINCT), false)
        .forEach(
            fieldName ->
                insertJobFacet(
                    now,
                    jobUuid,
                    runUuid,
                    lineageEventTime,
                    lineageEventType,
                    fieldName,
                    FacetUtils.toPgObject(fieldName, jsonNode.get(fieldName))));
  }

  record JobFacetRow(
      Instant createdAt,
      UUID jobUuid,
      UUID runUuid,
      Instant lineageEventTime,
      String lineageEventType,
      String name,
      PGobject facet) {}
}
