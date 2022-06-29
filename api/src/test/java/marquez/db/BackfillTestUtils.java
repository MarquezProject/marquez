/*
 * Copyright 2018-2022 contributors to the Marquez project
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
import java.util.UUID;
import marquez.common.Utils;
import marquez.common.models.JobType;
import marquez.db.models.ExtendedRunRow;
import marquez.db.models.JobRow;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunArgsRow;
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

  public static void writeNewEvent(
      Jdbi jdbi,
      String jobName,
      Instant now,
      NamespaceRow namespace,
      String parentRunId,
      String parentJobName)
      throws SQLException, JsonProcessingException {
    OpenLineageDao openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    JobDao jobDao = jdbi.onDemand(JobDao.class);
    RunArgsDao runArgsDao = jdbi.onDemand(RunArgsDao.class);
    RunDao runDao = jdbi.onDemand(RunDao.class);
    PGobject pgInputs = new PGobject();
    pgInputs.setType("json");
    pgInputs.setValue("[]");
    JobRow jobRow =
        jobDao.upsertJob(
            UUID.randomUUID(),
            JobType.BATCH,
            now,
            namespace.getUuid(),
            NAMESPACE,
            jobName,
            "description",
            null,
            null,
            null,
            pgInputs);

    RunArgsRow runArgsRow =
        runArgsDao.upsertRunArgs(
            UUID.randomUUID(), now, "{}", Utils.checksumFor(ImmutableMap.of()));
    UUID runUuid = UUID.randomUUID();
    ExtendedRunRow runRow =
        runDao.upsert(
            runUuid,
            null,
            runUuid.toString(),
            now,
            jobRow.getUuid(),
            null,
            runArgsRow.getUuid(),
            now,
            now,
            namespace.getUuid(),
            namespace.getName(),
            jobName,
            null,
            null);

    NominalTimeRunFacet nominalTimeRunFacet = new NominalTimeRunFacet();
    nominalTimeRunFacet.setNominalStartTime(
        Instant.now().atZone(LOCAL_ZONE).truncatedTo(ChronoUnit.HOURS));
    nominalTimeRunFacet.setNominalEndTime(
        nominalTimeRunFacet.getNominalStartTime().plus(1, ChronoUnit.HOURS));
    LineageEvent event =
        new LineageEvent(
            COMPLETE,
            Instant.now().atZone(LOCAL_ZONE),
            new Run(
                runUuid.toString(),
                new RunFacet(
                    nominalTimeRunFacet,
                    new ParentRunFacet(
                        LineageTestUtils.PRODUCER_URL,
                        LineageTestUtils.SCHEMA_URL,
                        new RunLink(parentRunId),
                        new JobLink(NAMESPACE, parentJobName)),
                    ImmutableMap.of("airflow_version", ImmutableMap.of("version", "abc")))),
            new LineageEvent.Job(
                NAMESPACE, jobName, new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP)),
            Collections.emptyList(),
            Collections.emptyList(),
            PRODUCER_URL.toString());
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
  }
}
