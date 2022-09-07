/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import static marquez.db.BackfillTestUtils.writeNewEvent;
import static marquez.db.LineageTestUtils.NAMESPACE;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import marquez.db.NamespaceDao;
import marquez.db.OpenLineageDao;
import marquez.db.models.NamespaceRow;
import marquez.db.models.RunRow;
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
class V44_3_BackfillJobsWithParentsTest {

  static Jdbi jdbi;
  private static OpenLineageDao openLineageDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    V44_3_BackfillJobsWithParentsTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @Test
  public void testBackfill() throws SQLException, JsonProcessingException {
    NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    Instant now = Instant.now();
    NamespaceRow namespace =
        namespaceDao.upsertNamespaceRow(UUID.randomUUID(), now, NAMESPACE, "me");
    String parentName = "parentJob";
    RunRow parentRun = writeNewEvent(jdbi, parentName, now, namespace, null, null);

    String task1Name = "task1";
    writeNewEvent(jdbi, task1Name, now, namespace, parentRun.getUuid().toString(), parentName);
    writeNewEvent(jdbi, "task2", now, namespace, parentRun.getUuid().toString(), parentName);

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
            new V44_1__UpdateRunsWithJobUUID().migrate(context);
            new V44_3_BackfillJobsWithParents().migrate(context);
          } catch (Exception e) {
            throw new AssertionError("Unable to execute migration", e);
          }
        });

    Optional<String> jobName =
        jdbi.withHandle(
            h ->
                h.createQuery("SELECT name FROM jobs_view WHERE simple_name=:jobName")
                    .bind("jobName", task1Name)
                    .mapTo(String.class)
                    .findFirst());
    assertThat(jobName).isPresent().get().isEqualTo(parentName + "." + task1Name);
  }
}
