/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import marquez.api.JdbiUtils;
import marquez.db.FacetTestUtils;
import marquez.db.OpenLineageDao;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.flywaydb.core.api.migration.Context;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Test to measure performance of the V55 migration. Currently, not run within circle-ci. Requires
 * system property `-DrunPerfTest=true` to be executed
 */
@EnabledIfSystemProperty(named = "runPerfTest", matches = "true")
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
@Slf4j
public class V57_BackfillFacetsPerformanceTest {

  private static V57_1__BackfillFacets subject = new V57_1__BackfillFacets();
  private static Jdbi jdbi;

  private static OpenLineageDao openLineageDao;

  Context flywayContext = mock(Context.class);
  Connection connection = mock(Connection.class);

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    V57_BackfillFacetsPerformanceTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    log.info("Starting tear down");

    // clearing runs table with foreign key checks takes more than migration
    jdbi.inTransaction(
        handle -> {
          handle.execute("ALTER TABLE runs DISABLE TRIGGER ALL;");
          return null;
        });
    JdbiUtils.cleanDatabase(jdbi);
    jdbi.inTransaction(
        handle -> {
          handle.execute("ALTER TABLE runs ENABLE TRIGGER ALL;");
          return null;
        });
  }

  @BeforeEach
  public void beforeEach() {
    when(flywayContext.getConnection()).thenReturn(connection);
    log.info("Cleaning database");
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testMigration() throws Exception {
    log.info("Populating lineage table");
    prepareLineageEventsTable();
    log.info("Cleaning existing facets tables");
    clearFacetsTablesAndLock();

    log.info("Starting migration script");
    subject.setChunkSize(10000);
    subject.setManual(true);
    subject.setJdbi(jdbi);

    Instant before = Instant.now();
    subject.migrate(null);
    Instant after = Instant.now();

    log.info(
        "Successfully migrated {} lineage events, which resulted in {} job_facets rows, "
            + "{} run_facets rows and {} dataset_facets rows.",
        countTableRows("lineage_events"),
        countTableRows("job_facets"),
        countTableRows("run_facets"),
        countTableRows("dataset_facets"));
    log.info("Migration took {} seconds.", Duration.between(before, after).toSeconds());

    assertThat(countTableRows("dataset_facets") > countTableRows("lineage_events"));
    assertThat(countTableRows("job_facets") > countTableRows("lineage_events"));
    assertThat(countTableRows("run_facets") > countTableRows("lineage_events"));
  }

  private void prepareLineageEventsTable() {
    /**
     * Workaround to register uuid_generate_v4 function to generate uuids. gen_random_uuid() is
     * available since Postgres 13
     */
    jdbi.withHandle(h -> h.createCall("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"").invoke());

    // generate 10 rows
    IntStream.range(0, 10).forEach(i -> FacetTestUtils.createLineageWithFacets(openLineageDao));

    IntStream.range(0, 9).forEach(i -> duplicateLineageEvents(10));

    // duplicate 100 events 9 times to have 1000 events
    IntStream.range(0, 9).forEach(i -> duplicateLineageEvents(100));

    // duplicate 1000 events 9 times to have 10K events
    IntStream.range(0, 9).forEach(i -> duplicateLineageEvents(1000));

    // duplicate 10K events 9 times to have 100K events
    IntStream.range(0, 9).forEach(i -> duplicateLineageEvents(10000));

    // duplicate 100K events 9 times to have 1M events
    IntStream.range(0, 9).forEach(i -> duplicateLineageEvents(100000));

    // create run rows to fix run constrains
    jdbi.withHandle(
        h ->
            h.createCall(
                    """
            WITH single_run AS (
                SELECT * FROM runs limit 1
            )
            INSERT INTO runs
            SELECT
                 e.run_uuid as uuid,
                 sr.created_at,
                 sr.updated_at,
                 sr.job_version_uuid,
                 sr.run_args_uuid,
                 sr.nominal_start_time,
                 sr.nominal_end_time ,
                 sr.current_run_state,
                 sr.start_run_state_uuid,
                 sr.end_run_state_uuid,
                 sr.external_id,
                 sr.namespace_name,
                 sr.job_name,
                 sr.location,
                 sr.transitioned_at,
                 sr.started_at,
                 sr.ended_at,
                 sr.job_context_uuid,
                 sr.parent_run_uuid,
                 sr.job_uuid
            FROM single_run sr, lineage_events e
            ON CONFLICT DO NOTHING
            """)
                .invoke());

    log.info(
        "Generated {} lineage events, each of {} bytes size",
        countTableRows("lineage_events"),
        jdbi.withHandle(
            h ->
                h.createQuery(
                        "SELECT OCTET_LENGTH(event::text) AS bytes FROM lineage_events LIMIT 1")
                    .map(rs -> rs.getColumn("bytes", Integer.class))
                    .one()));
  }

  private void duplicateLineageEvents(int numberOfDuplicatedEvents) {
    jdbi.inTransaction(
        handle ->
            handle
                .createUpdate(
                    """
                        INSERT INTO lineage_events
                        SELECT
                            NOW(),
                            event,
                            event_type,
                            job_name,
                            job_namespace,
                            producer,
                            uuid_generate_v4(),
                            created_at
                        FROM lineage_events
                        LIMIT :numberOfDuplicatedEvents
                    """)
                .bind("numberOfDuplicatedEvents", numberOfDuplicatedEvents)
                .execute());
  }

  private void clearFacetsTablesAndLock() {
    Arrays.asList(
            "DELETE FROM job_facets",
            "DELETE FROM dataset_facets",
            "DELETE FROM run_facets",
            "DELETE FROM facet_migration_lock")
        .stream()
        .forEach(sql -> jdbi.inTransaction(handle -> handle.execute(sql)));
  }

  private int countTableRows(String table) {
    return jdbi.withHandle(
        h ->
            h.createQuery("SELECT COUNT(*) AS cnt FROM " + table)
                .map(rs -> rs.getColumn("cnt", Integer.class))
                .one());
  }
}
