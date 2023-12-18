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
import javax.annotation.Nullable;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.db.mappers.JobFacetsMapper;
import marquez.service.models.JobFacets;
import marquez.service.models.LineageEvent;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.postgresql.util.PGobject;

@RegisterRowMapper(JobFacetsMapper.class)
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

  @SqlUpdate(
      """
            INSERT INTO job_facets (
               created_at,
               job_uuid,
               job_version_uuid,
               lineage_event_time,
               name,
               facet
            ) VALUES (
               :createdAt,
               :jobUuid,
               :jobVersionUuid,
               :lineageEventTime,
               :name,
               :facet
            )
            """)
  void insertJobFacet(
      Instant createdAt,
      UUID jobUuid,
      UUID jobVersionUuid,
      Instant lineageEventTime,
      String name,
      PGobject facet);

  @SqlQuery(
      """
            SELECT
                run_uuid,
                JSON_AGG(facet ORDER BY lineage_event_time) AS facets
            FROM
            job_facets_view
            WHERE
                run_uuid = :runUuid
            GROUP BY
                run_uuid
            """)
  JobFacets findJobFacetsByRunUuid(UUID runUuid);

  @Transaction
  default void insertJobFacetsFor(
      @NonNull UUID jobUuid,
      @NonNull UUID jobVersionUuid,
      @NonNull Instant lineageEventTime,
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
                    jobVersionUuid,
                    lineageEventTime,
                    fieldName,
                    FacetUtils.toPgObject(fieldName, jsonNode.get(fieldName))));
  }

  @Transaction
  default void insertJobFacetsFor(
      @NonNull UUID jobUuid,
      @Nullable UUID runUuid,
      @NonNull Instant lineageEventTime,
      @Nullable String lineageEventType,
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
