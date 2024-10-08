/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import static marquez.api.models.ApiModelGenerator.newRunEvents;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newNamespaceName;
import static marquez.db.models.DbModelGenerator.newDatasetRowWith;
import static marquez.db.models.DbModelGenerator.newJobRowWith;
import static marquez.db.models.DbModelGenerator.newNamespaceRow;
import static marquez.db.models.DbModelGenerator.newSourceRow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import io.openlineage.client.OpenLineage;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import marquez.db.models.DatasetRow;
import marquez.db.models.IntervalMetric;
import marquez.db.models.JobRow;
import marquez.db.models.LineageMetric;
import marquez.db.models.NamespaceRow;
import marquez.db.models.SourceRow;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.junit5.JdbiExtension;
import org.jdbi.v3.testing.junit5.tc.JdbiTestcontainersExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Tag("DataAccessTests, IntegrationTests")
@Testcontainers
public class StatsTest {
  static final DockerImageName POSTGRES_16 = DockerImageName.parse("postgres:16");

  @Container
  @Order(1)
  static final PostgreSQLContainer<?> DB_CONTAINER = new PostgreSQLContainer<>(POSTGRES_16);

  // Defined statically to significantly improve overall test execution.
  @RegisterExtension
  @Order(2)
  static final JdbiExtension jdbiExtension =
      JdbiTestcontainersExtension.instance(DB_CONTAINER)
          .withPlugin(new SqlObjectPlugin())
          .withPlugin(new PostgresPlugin())
          .withPlugin(new Jackson2Plugin())
          .withInitializer(
              (source, handle) -> {
                // Apply migrations.
                DbMigration.migrateDbOrError(source);
              });

  // Wraps test database connection.
  static TestingDb DB;

  @BeforeAll
  public static void setUpOnce() {
    // Wrap jdbi configured for running container.
    DB = TestingDb.newInstance(jdbiExtension.getJdbi());
  }

  @AfterEach
  public void tearDown() {
    try (final Handle handle = DB.open()) {
      handle.execute("DELETE FROM lineage_events");
      handle.execute("DELETE FROM job_versions");
      handle.execute("DELETE FROM jobs");
      handle.execute("DELETE FROM datasets");
      handle.execute("DELETE FROM sources");
      handle.execute("DELETE FROM namespaces");
    }
  }

  @Test
  public void testGetStatsForLineageEvents() {
    // (1) Configure OL.
    final URI olProducer = URI.create("https://test.com/test");
    final OpenLineage ol = new OpenLineage(olProducer);

    // (2) Add namespace and job for OL events.
    final String namespaceName = newNamespaceName().getValue();
    final String jobName = newJobName().getValue();

    // (3) Create some 1 hour old OL events.
    int hourEvents = 4;
    final Set<OpenLineage.RunEvent> hourEventSet =
        newRunEvents(
            ol, Instant.now().minus(1, ChronoUnit.HOURS), namespaceName, jobName, hourEvents);
    DB.insertAll(hourEventSet);

    // (4) Create some 2 day old OL events.
    int dayEvents = 2;
    final Set<OpenLineage.RunEvent> dayEventSet =
        newRunEvents(
            ol, Instant.now().minus(2, ChronoUnit.DAYS), namespaceName, jobName, dayEvents);
    DB.insertAll(dayEventSet);

    // (5) Create some 10 second old OL events.
    int secondEvents = 1;
    final Set<OpenLineage.RunEvent> secondEventSet =
        newRunEvents(
            ol, Instant.now().minus(10, ChronoUnit.SECONDS), namespaceName, jobName, secondEvents);
    DB.insertAll(secondEventSet);

    // (6) Materialize views to flush out view data.
    try (final Handle handle = DB.open()) {
      DbTestUtils.materializeViews(handle);
    } catch (Exception e) {
      fail("failed to apply dry run", e);
    }

    List<LineageMetric> lastDayLineageMetrics = DB.lastDayLineageMetrics();
    List<LineageMetric> lastWeekLineageMetrics = DB.lastWeekLineageMetrics("UTC");

    assertThat(lastDayLineageMetrics).isNotEmpty();
    assertThat(lastDayLineageMetrics.get(lastDayLineageMetrics.size() - 2).getComplete())
        .isEqualTo(hourEvents);
    assertThat(lastDayLineageMetrics.get(lastDayLineageMetrics.size() - 1).getComplete())
        .isEqualTo(secondEvents);

    assertThat(lastWeekLineageMetrics).isNotEmpty();
    assertThat(lastWeekLineageMetrics.get(lastWeekLineageMetrics.size() - 3).getComplete())
        .isEqualTo(dayEvents);
    assertThat(lastWeekLineageMetrics.get(lastWeekLineageMetrics.size() - 1).getComplete())
        .isEqualTo(secondEvents + hourEvents);
  }

  @Test
  public void testGetStatsForJobs() {

    // (1) Insert a new namespace.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());

    // (2) Insert a new job.
    final JobRow jobRow = DB.upsert(newJobRowWith(namespaceRow.getUuid(), namespaceRow.getName()));
    DB.upsert(jobRow);

    // (3) Retrieve last day and last week job metrics.
    List<IntervalMetric> intervalMetricsDay = DB.lastDayJobMetrics();
    assertThat(intervalMetricsDay).isNotEmpty();

    Optional<Integer> countDay =
        intervalMetricsDay.stream().map(IntervalMetric::getCount).reduce(Integer::sum);
    assertThat(countDay).isPresent();
    assertThat(countDay.get()).isEqualTo(1);

    List<IntervalMetric> intervalMetricsWeek = DB.lastWeekJobMetrics("UTC");
    assertThat(intervalMetricsWeek).isNotEmpty();

    Optional<Integer> countWeek =
        intervalMetricsWeek.stream().map(IntervalMetric::getCount).reduce(Integer::sum);
    assertThat(countWeek).isPresent();
    assertThat(countWeek.get()).isEqualTo(1);
  }

  @Test
  public void testGetStatsForDatasets() {
    // (1) Insert a new namespace.
    final NamespaceRow namespaceRow = DB.upsert(newNamespaceRow());

    // (2) Insert a new source.
    final SourceRow sourceRow = DB.upsert(newSourceRow());
    DB.upsert(sourceRow);

    // (3) Insert a new dataset.
    final DatasetRow datasetRow =
        DB.upsert(
            newDatasetRowWith(
                namespaceRow.getUuid(),
                namespaceRow.getName(),
                sourceRow.getUuid(),
                sourceRow.getName()));
    DB.upsert(datasetRow);

    // (4) Retrieve last day and last week dataset metrics.
    List<IntervalMetric> intervalMetricsDay = DB.lastDayDatasetMetrics();
    assertThat(intervalMetricsDay).isNotEmpty();

    Optional<Integer> countDay =
        intervalMetricsDay.stream().map(IntervalMetric::getCount).reduce(Integer::sum);
    assertThat(countDay).isPresent();
    assertThat(countDay.get()).isEqualTo(1);

    List<IntervalMetric> intervalMetricsWeek = DB.lastWeekDatasetMetrics("UTC");
    assertThat(intervalMetricsWeek).isNotEmpty();

    Optional<Integer> countWeek =
        intervalMetricsWeek.stream().map(IntervalMetric::getCount).reduce(Integer::sum);
    assertThat(countWeek).isPresent();
    assertThat(countWeek.get()).isEqualTo(1);
  }

  @Test
  public void testGetStatsForSources() {

    // (1) Insert a new source.
    final SourceRow sourceRow = DB.upsert(newSourceRow());
    DB.upsert(sourceRow);

    // (2) Retrieve last day source metrics.
    List<IntervalMetric> intervalMetricsDay = DB.lastDaySourceMetrics();
    assertThat(intervalMetricsDay).isNotEmpty();

    Optional<Integer> countDay =
        intervalMetricsDay.stream().map(IntervalMetric::getCount).reduce(Integer::sum);
    assertThat(countDay).isPresent();
    assertThat(countDay.get()).isGreaterThanOrEqualTo(1);

    List<IntervalMetric> intervalMetricsWeek = DB.lastWeekSourceMetrics("UTC");

    Optional<Integer> countWeek =
        intervalMetricsWeek.stream().map(IntervalMetric::getCount).reduce(Integer::sum);
    assertThat(countWeek).isPresent();
    assertThat(countWeek.get()).isEqualTo(1);
  }
}
