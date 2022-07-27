/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import static marquez.db.LineageTestUtils.NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import marquez.db.BackfillTestUtils;
import marquez.db.JobDao;
import marquez.db.NamespaceDao;
import marquez.db.OpenLineageDao;
import marquez.db.RunArgsDao;
import marquez.db.RunDao;
import marquez.db.models.NamespaceRow;
import marquez.jdbi.JdbiExternalPostgresExtension.FlywaySkipRepeatable;
import marquez.jdbi.JdbiExternalPostgresExtension.FlywayTarget;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
// fix the flyway migration up to v44 since we depend on the database structure as it exists at this
// point in time. The migration will only ever be applied on a database at this version.
@FlywayTarget("44")
// As of the time of this migration, there were no repeatable migrations, so ignore any that are
// added
@FlywaySkipRepeatable()
class V44_2__BackfillAirflowParentRunsTest {

  static Jdbi jdbi;
  private static OpenLineageDao openLineageDao;
  private static JobDao jobDao;
  private static RunArgsDao runArgsDao;
  private static RunDao runDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    V44_2__BackfillAirflowParentRunsTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    jobDao = jdbi.onDemand(JobDao.class);
    runArgsDao = jdbi.onDemand(RunArgsDao.class);
    runDao = jdbi.onDemand(RunDao.class);
  }

  @Test
  public void testMigrateAirflowTasks() throws SQLException, JsonProcessingException {
    String dagName = "airflowDag";
    String task1Name = dagName + ".task1";
    NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    Instant now = Instant.now();
    NamespaceRow namespace =
        namespaceDao.upsertNamespaceRow(UUID.randomUUID(), now, NAMESPACE, "me");

    BackfillTestUtils.writeNewEvent(
        jdbi, task1Name, now, namespace, "schedule:00:00:00", task1Name);
    BackfillTestUtils.writeNewEvent(
        jdbi, "airflowDag.task2", now, namespace, "schedule:00:00:00", task1Name);

    BackfillTestUtils.writeNewEvent(jdbi, "a_non_airflow_task", now, namespace, null, null);

    jdbi.useHandle(
        handle -> {
          try {
            new V44_2__BackfillAirflowParentRuns()
                .migrate(
                    new Context() {
                      @Override
                      public Configuration getConfiguration() {
                        return null;
                      }

                      @Override
                      public Connection getConnection() {
                        return handle.getConnection();
                      }
                    });
          } catch (Exception e) {
            throw new AssertionError("Unable to execute migration", e);
          }
        });
    Optional<String> jobNameResult =
        jdbi.withHandle(
            h ->
                h.createQuery(
                        """
            SELECT name FROM jobs_view
            WHERE namespace_name=:namespace AND simple_name=:jobName
            """)
                    .bind("namespace", NAMESPACE)
                    .bind("jobName", dagName)
                    .mapTo(String.class)
                    .findFirst());
    assertThat(jobNameResult).isPresent();
  }
}
