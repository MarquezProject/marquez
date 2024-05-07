/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.db.LineageTestUtils.LOCAL_ZONE;
import static marquez.db.LineageTestUtils.NAMESPACE;
import static marquez.db.LineageTestUtils.PRODUCER_URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import marquez.common.Utils;
import marquez.common.models.DatasetType;
import marquez.db.models.DatasetRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
import marquez.db.models.RunRow;
import marquez.db.models.SourceRow;
import marquez.service.models.LineageEvent;
import marquez.service.models.LineageEvent.JobFacet;
import marquez.service.models.LineageEvent.JobLink;
import marquez.service.models.LineageEvent.NominalTimeRunFacet;
import marquez.service.models.LineageEvent.ParentRunFacet;
import marquez.service.models.LineageEvent.Run;
import marquez.service.models.LineageEvent.RunFacet;
import marquez.service.models.LineageEvent.RunLink;
import org.jdbi.v3.core.Jdbi;
import org.postgresql.util.PGobject;
import org.testcontainers.shaded.com.google.common.collect.ImmutableMap;

public class BackfillTestUtils {
  public static final String COMPLETE = "COMPLETE";

  public static RunRow writeNewEvent(
      Jdbi jdbi,
      String jobName,
      Instant now,
      NamespaceRow namespace,
      String parentRunId,
      String parentJobName)
      throws SQLException, JsonProcessingException {
    OpenLineageDao openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    RunArgsDao runArgsDao = jdbi.onDemand(RunArgsDao.class);
    RunDao runDao = jdbi.onDemand(RunDao.class);
    UUID jobUuid = writeJob(jdbi, jobName, now, namespace);
    UUID jobVersionUuid =
        jdbi.withHandle(
            h -> {
              return h.createQuery(
                      """
                  INSERT INTO job_versions (uuid, created_at, updated_at, job_uuid, version, location,namespace_uuid, namespace_name, job_name)
                  VALUES (:uuid, :now, :now, :jobUuid, :version, :location, :namespaceUuid, :namespaceName, :jobName)
                  RETURNING uuid
                  """)
                  .bind("uuid", UUID.randomUUID())
                  .bind("now", now)
                  .bind("jobUuid", jobUuid)
                  .bind("version", UUID.randomUUID())
                  .bind("location", "location")
                  .bind("namespaceUuid", namespace.getUuid())
                  .bind("namespaceName", namespace.getName())
                  .bind("jobName", jobName)
                  .mapTo(UUID.class)
                  .first();
            });

    RunArgsRow runArgsRow =
        runArgsDao.upsertRunArgs(
            UUID.randomUUID(), now, "{}", Utils.checksumFor(ImmutableMap.of()));
    UUID runUuid = UUID.randomUUID();
    RunRow runRow =
        runDao.upsert(
            runUuid,
            null,
            runUuid.toString(),
            now,
            jobUuid,
            jobVersionUuid,
            runArgsRow.getUuid(),
            now,
            now,
            namespace.getName(),
            jobName,
            null);

    NominalTimeRunFacet nominalTimeRunFacet = new NominalTimeRunFacet();
    nominalTimeRunFacet.setNominalStartTime(
        Instant.now().atZone(LOCAL_ZONE).truncatedTo(ChronoUnit.HOURS));
    nominalTimeRunFacet.setNominalEndTime(
        nominalTimeRunFacet.getNominalStartTime().plus(1, ChronoUnit.HOURS));
    Optional<ParentRunFacet> parentRun =
        Optional.ofNullable(parentRunId)
            .map(
                runId ->
                    new ParentRunFacet(
                        PRODUCER_URL,
                        LineageTestUtils.SCHEMA_URL,
                        new RunLink(runId),
                        new JobLink(NAMESPACE, parentJobName)));
    LineageEvent event =
        LineageEvent.builder()
            .eventType(COMPLETE)
            .eventTime(Instant.now().atZone(LOCAL_ZONE))
            .run(
                new Run(
                    runUuid.toString(),
                    new RunFacet(
                        nominalTimeRunFacet,
                        parentRun.orElse(null),
                        ImmutableMap.of("airflow_version", ImmutableMap.of("version", "abc")))))
            .job(new LineageEvent.Job(NAMESPACE, jobName, JobFacet.builder().build()))
            .inputs(
                Collections.singletonList(
                    new LineageEvent.Dataset(
                        "namespace", "dataset_a", LineageEvent.DatasetFacets.builder().build())))
            .outputs(
                Collections.singletonList(
                    new LineageEvent.Dataset(
                        "namespace", "dataset_b", LineageEvent.DatasetFacets.builder().build())))
            .producer(PRODUCER_URL.toString())
            .build();
    PGobject eventJson = new PGobject();
    eventJson.setType("json");
    eventJson.setValue(Utils.getMapper().writeValueAsString(event));
    openLineageDao.createLineageEvent(
        COMPLETE,
        Instant.now(),
        runRow.getUuid(),
        jobName,
        namespace.getName(),
        eventJson,
        PRODUCER_URL.toString());
    return runRow;
  }

  public static UUID writeJob(Jdbi jdbi, String jobName, Instant now, NamespaceRow namespace)
      throws SQLException {
    PGobject pgInputs = new PGobject();
    pgInputs.setType("json");
    pgInputs.setValue("[]");
    return jdbi.withHandle(
        h -> {
          return h.createQuery(
                  """
                  INSERT INTO jobs (uuid, type, created_at, updated_at, namespace_uuid, name, namespace_name, current_inputs, simple_name)
                  VALUES (:uuid, :type, :now, :now, :namespaceUuid, :name, :namespaceName, :currentInputs, :simpleName)
                  RETURNING uuid
                  """)
              .bind("uuid", UUID.randomUUID())
              .bind("type", marquez.client.models.JobType.BATCH)
              .bind("now", now)
              .bind("namespaceUuid", namespace.getUuid())
              .bind("name", jobName)
              .bind("namespaceName", namespace.getName())
              .bind("currentInputs", pgInputs)
              .bind("simpleName", jobName)
              .mapTo(UUID.class)
              .first();
        });
  }

  public static UUID writeJobVersion(
      Jdbi jdbi, UUID jobUuid, String location, String jobName, NamespaceRow namespace)
      throws SQLException {
    return jdbi.withHandle(
        h -> {
          return h.createQuery(
                  """
                  INSERT INTO job_versions (
                      uuid,
                      created_at,
                      updated_at,
                      job_uuid,
                      location,
                      version,
                      job_name,
                      namespace_uuid,
                      namespace_name
                  )
                  VALUES (
                      :uuid,
                      :created_at,
                      :updated_at,
                      :job_uuid,
                      :location,
                      :version,
                      :job_name,
                      :namespace_uuid,
                      :namespace_name
                  )
                  RETURNING uuid
                  """)
              .bind("uuid", UUID.randomUUID())
              .bind("created_at", Instant.now())
              .bind("updated_at", Instant.now())
              .bind("job_uuid", jobUuid)
              .bind("location", location)
              .bind("version", UUID.randomUUID())
              .bind("job_name", jobUuid)
              .bind("namespace_uuid", namespace.getUuid())
              .bind("namespace_name", namespace.getName())
              .mapTo(UUID.class)
              .first();
        });
  }

  public static DatasetRow writeDataset(Jdbi jdbi, NamespaceRow namespaceRow, String datasetName) {
    DatasetDao datasetDao = jdbi.onDemand(DatasetDao.class);

    SourceRow sourceRow =
        jdbi.onDemand(SourceDao.class)
            .upsert(UUID.randomUUID(), "type", Instant.now(), "name", "http://a");

    return datasetDao.upsert(
        UUID.randomUUID(),
        DatasetType.DB_TABLE,
        Instant.now(),
        namespaceRow.getUuid(),
        namespaceRow.getName(),
        sourceRow.getUuid(),
        "sourceName",
        datasetName,
        "",
        "",
        false);
  }

  public static UUID writeJobIOMapping(Jdbi jdbi, UUID jobUuid, UUID datasetUuid)
      throws SQLException {
    return jdbi.withHandle(
        h -> {
          return h.createQuery(
                  """
                  INSERT INTO job_versions_io_mapping (
                      job_version_uuid,
                      dataset_uuid,
                      io_type,
                      job_uuid,
                      is_current_job_version
                  )
                  VALUES (:job_version_uuid, :dataset_uuid, :io_type, :job_uuid, TRUE)
                  RETURNING uuid
                  """)
              .bind("job_version_uuid", UUID.randomUUID())
              .bind("dataset_uuid", Instant.now())
              .bind("io_type", Instant.now())
              .bind("job_uuid", jobUuid)
              .mapTo(UUID.class)
              .first();
        });
  }
}
