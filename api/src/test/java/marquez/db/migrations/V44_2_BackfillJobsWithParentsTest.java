/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import static marquez.db.BackfillTestUtils.writeNewEvent;
import static marquez.db.LineageTestUtils.NAMESPACE;
import static marquez.db.LineageTestUtils.createLineageRow;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.JobName;
import marquez.db.JobDao;
import marquez.db.LineageTestUtils;
import marquez.db.NamespaceDao;
import marquez.db.OpenLineageDao;
import marquez.db.models.NamespaceRow;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.Job;
import marquez.service.models.LineageEvent.JobFacet;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
class V44_2_BackfillJobsWithParentsTest {

  static Jdbi jdbi;
  private static OpenLineageDao openLineageDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    V44_2_BackfillJobsWithParentsTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @Test
  public void testBackfill() throws SQLException, JsonProcessingException {
    String parentName = "parentJob";
    UpdateLineageRow parentJob =
        createLineageRow(
            openLineageDao,
            parentName,
            "COMPLETE",
            new JobFacet(null, null, null, LineageTestUtils.EMPTY_MAP),
            Collections.emptyList(),
            Collections.emptyList());

    NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    Instant now = Instant.now();
    NamespaceRow namespace =
        namespaceDao.upsertNamespaceRow(UUID.randomUUID(), now, NAMESPACE, "me");

    String task1Name = "task1";
    writeNewEvent(
        jdbi, task1Name, now, namespace, parentJob.getRun().getUuid().toString(), parentName);
    writeNewEvent(
        jdbi, "task2", now, namespace, parentJob.getRun().getUuid().toString(), parentName);

    jdbi.useHandle(
        handle -> {
          try {
            Context context =
                new Context() {
                  @Override
                  public Configuration getConfiguration() {
                    return null;
                  }

                  @Override
                  public Connection getConnection() {
                    return handle.getConnection();
                  }
                };
            // apply migrations in order
            new V43_1__UpdateRunsWithJobUUID().migrate(context);
            new V44_2_BackfillJobsWithParents().migrate(context);
          } catch (Exception e) {
            throw new AssertionError("Unable to execute migration", e);
          }
        });

    JobDao jobDao = jdbi.onDemand(JobDao.class);
    Optional<Job> jobByName = jobDao.findJobByName(NAMESPACE, task1Name);
    assertThat(jobByName)
        .isPresent()
        .get()
        .hasFieldOrPropertyWithValue("name", new JobName(parentName + "." + task1Name));
  }
}
