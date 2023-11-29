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
import lombok.extern.slf4j.Slf4j;
import marquez.db.JobFacetsDao;
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
import org.postgresql.util.PGobject;

/** Test to validate if a job_version_column is filled properly within job_facets table */
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
@FlywayTarget("66.3")
@FlywaySkipRepeatable()
@Slf4j
public class V66_3_JobFacetsBackfillJobVersionTest {

  private static V66_3_JobFacetsBackfillJobVersion migration =
      new V66_3_JobFacetsBackfillJobVersion();
  static Jdbi jdbi;
  private static OpenLineageDao openLineageDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    V66_3_JobFacetsBackfillJobVersionTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @Test
  public void testBackfill() throws SQLException, JsonProcessingException {
    NamespaceDao namespaceDao = jdbi.onDemand(NamespaceDao.class);
    Instant now = Instant.now();
    NamespaceRow namespace =
        namespaceDao.upsertNamespaceRow(UUID.randomUUID(), now, NAMESPACE, "me");

    // (1) Generate run and job row
    RunRow run = writeNewEvent(jdbi, "job", now, namespace, null, null);

    // (2) Insert job_facet row
    PGobject facet = new PGobject();
    facet.setType("json");
    facet.setValue("{\"someFacet\":1}");
    jdbi.onDemand(JobFacetsDao.class)
        .insertJobFacet(
            run.getCreatedAt(),
            run.getJobUuid(),
            run.getUuid(),
            run.getCreatedAt(),
            "COMPLETE",
            "someJobFacet",
            facet);

    // (3) Run Migration
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
            new V66_3_JobFacetsBackfillJobVersion().migrate(context);
          } catch (Exception e) {
            throw new AssertionError("Unable to execute migration", e);
          }
        });

    // (4) Verify job_version column in job_facets table is updated
    Optional<UUID> jobVersionUuid =
        jdbi.withHandle(
            h ->
                h.createQuery("SELECT job_version_uuid FROM job_facets WHERE name=:name")
                    .bind("name", "someJobFacet")
                    .mapTo(UUID.class)
                    .findFirst());
    assertThat(jobVersionUuid).isPresent().get().isEqualTo(run.getJobVersionUuid().get());
  }
}
