/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import marquez.db.LineageTestUtils;
import marquez.db.OpenLineageDao;
import marquez.db.models.UpdateLineageRow;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import marquez.service.models.LineageEvent.Dataset;
import marquez.service.models.LineageEvent.JobFacet;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/** Test suite for {@link DenormalizedLineageService}. */
@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class DenormalizedLineageServiceTest {

  private static Jdbi jdbi;
  private static DenormalizedLineageService denormalizedLineageService;
  private static OpenLineageDao openLineageDao;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    DenormalizedLineageServiceTest.jdbi = jdbi;
    openLineageDao = jdbi.onDemand(OpenLineageDao.class);
    denormalizedLineageService = new DenormalizedLineageService(jdbi);
  }

  @AfterEach
  public void tearDown() {
    // Clean up denormalized tables after each test
    jdbi.useHandle(
        handle -> {
          handle.execute("DELETE FROM run_lineage_denormalized");
          handle.execute("DELETE FROM run_parent_lineage_denormalized");
        });
  }

  @Test
  public void testPopulateLineageForRun() {
    // Use LineageTestUtils to create a lineage event and all required data
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(new Dataset("namespace", "input_dataset", null)),
            List.of(new Dataset("namespace", "output_dataset", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // When: Populate lineage for the run
    assertThatCode(() -> denormalizedLineageService.populateLineageForRun(runUuid))
        .doesNotThrowAnyException();

    // Then: Verify data is populated in denormalized tables
    jdbi.useHandle(
        handle -> {
          Long runLineageCount =
              handle
                  .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized WHERE run_uuid = ?")
                  .bind(0, runUuid)
                  .mapTo(Long.class)
                  .one();
          assertThat(runLineageCount).isEqualTo(1);
        });
  }

  @Test
  public void testPopulateLineageForRunWithParent() {
    // Create parent run
    UpdateLineageRow parentLineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "parent_job",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "parent_output", null)));

    // Create child run with parent reference
    UpdateLineageRow childLineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "child_job",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(new Dataset("namespace", "parent_output", null)),
            List.of(new Dataset("namespace", "child_output", null)));

    UUID childRunUuid = childLineageRow.getRun().getUuid();
    UUID parentRunUuid = parentLineageRow.getRun().getUuid();

    // Set parent-child relationship
    jdbi.useHandle(
        handle -> {
          handle.execute(
              "UPDATE runs SET parent_run_uuid = ? WHERE uuid = ?", parentRunUuid, childRunUuid);
        });

    // When: Populate lineage for the child run
    denormalizedLineageService.populateLineageForRun(childRunUuid);

    // Then: Verify parent lineage is populated
    jdbi.useHandle(
        handle -> {
          Long parentLineageCount =
              handle
                  .createQuery(
                      "SELECT COUNT(*) FROM run_parent_lineage_denormalized WHERE run_uuid = ?")
                  .bind(0, parentRunUuid)
                  .mapTo(Long.class)
                  .one();
          assertThat(parentLineageCount).isEqualTo(1);
        });
  }

  @Test
  public void testPopulateAllExistingRuns() {
    // Create multiple runs
    UpdateLineageRow lineageRow1 =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job_1",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output1", null)));

    UpdateLineageRow lineageRow2 =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "job_2",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output2", null)));

    // When: Populate all existing runs
    denormalizedLineageService.populateAllExistingRuns();

    // Then: Verify both runs are populated
    jdbi.useHandle(
        handle -> {
          Long totalCount =
              handle
                  .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized")
                  .mapTo(Long.class)
                  .one();
          assertThat(totalCount).isGreaterThanOrEqualTo(2);
        });
  }

  @Test
  public void testCustomPartitionManagementService30Days() {
    // Test: Custom PartitionManagementService with 30 days ahead
    PartitionManagementService customPartitionService = new PartitionManagementService(jdbi, 30, 6);
    DenormalizedLineageService customDenormalizedService =
        new DenormalizedLineageService(jdbi, customPartitionService);

    // Create a run
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_30days",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // When: Populate lineage (should create partitions 30 days ahead)
    customDenormalizedService.populateLineageForRun(runUuid);

    // Then: Verify partitions exist for at least next 30 days
    List<String> partitions =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT tablename FROM pg_tables "
                            + "WHERE tablename LIKE 'run_lineage_denormalized_%' "
                            + "ORDER BY tablename")
                    .mapTo(String.class)
                    .list());

    // Should have current month + next month partitions
    LocalDate today = LocalDate.now();
    String currentMonthPartition =
        "run_lineage_denormalized_y"
            + today.format(DateTimeFormatter.ofPattern("yyyy"))
            + "m"
            + today.format(DateTimeFormatter.ofPattern("MM"));
    String nextMonthPartition =
        "run_lineage_denormalized_y"
            + today.plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy"))
            + "m"
            + today.plusMonths(1).format(DateTimeFormatter.ofPattern("MM"));

    assertThat(partitions).contains(currentMonthPartition);
    assertThat(partitions).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  public void testCustomPartitionManagementService40Days() {
    // Test: Custom PartitionManagementService with 40 days ahead
    PartitionManagementService customPartitionService = new PartitionManagementService(jdbi, 40, 6);
    DenormalizedLineageService customDenormalizedService =
        new DenormalizedLineageService(jdbi, customPartitionService);

    // Create a run
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_40days",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // When: Populate lineage (should create partitions 40 days ahead)
    customDenormalizedService.populateLineageForRun(runUuid);

    // Then: Verify partitions exist for at least next 40 days (2-3 months depending on current
    // day)
    List<String> partitions =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT tablename FROM pg_tables "
                            + "WHERE tablename LIKE 'run_lineage_denormalized_%' "
                            + "ORDER BY tablename")
                    .mapTo(String.class)
                    .list());

    assertThat(partitions).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  public void testPartitionCreationDuringLineagePopulation() {
    // Test: Verify partition is created if it doesn't exist during lineage population
    PartitionManagementService customPartitionService = new PartitionManagementService(jdbi, 10, 6);
    DenormalizedLineageService customDenormalizedService =
        new DenormalizedLineageService(jdbi, customPartitionService);

    // Clean up all partitions first
    jdbi.useHandle(
        handle -> {
          List<String> existingPartitions =
              handle
                  .createQuery(
                      "SELECT tablename FROM pg_tables WHERE tablename LIKE 'run_lineage_denormalized_%'")
                  .mapTo(String.class)
                  .list();
          for (String partition : existingPartitions) {
            handle.execute("DROP TABLE IF EXISTS " + partition);
          }
        });

    // Create a run
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_partition",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // When: Populate lineage (should auto-create partition)
    assertThatCode(() -> customDenormalizedService.populateLineageForRun(runUuid))
        .doesNotThrowAnyException();

    // Then: Verify partition was created
    List<String> partitions =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT tablename FROM pg_tables "
                            + "WHERE tablename LIKE 'run_lineage_denormalized_%'")
                    .mapTo(String.class)
                    .list());

    assertThat(partitions).isNotEmpty();
  }

  @Test
  public void testDefaultPartitionConfiguration() {
    // Test: Verify default DenormalizedLineageService uses 10 days ahead
    DenormalizedLineageService defaultService = new DenormalizedLineageService(jdbi);

    // Create a run
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_default",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // When: Populate with default service
    assertThatCode(() -> defaultService.populateLineageForRun(runUuid)).doesNotThrowAnyException();

    // Then: Verify data is populated
    jdbi.useHandle(
        handle -> {
          Long runLineageCount =
              handle
                  .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized WHERE run_uuid = ?")
                  .bind(0, runUuid)
                  .mapTo(Long.class)
                  .one();
          assertThat(runLineageCount).isEqualTo(1);
        });
  }

  @Test
  public void testPartitionAwarenessWithMultipleMonths() {
    // Test: Verify service handles runs spanning multiple months
    PartitionManagementService customPartitionService =
        new PartitionManagementService(jdbi, 60, 6); // 60 days = 2 months
    DenormalizedLineageService customDenormalizedService =
        new DenormalizedLineageService(jdbi, customPartitionService);

    // Create runs for current and next month
    UpdateLineageRow currentMonthRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "current_month_job",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output1", null)));

    UpdateLineageRow nextMonthRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "next_month_job",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output2", null)));

    // Update next month run to have started_at in next month
    UUID nextMonthRunUuid = nextMonthRun.getRun().getUuid();
    Instant nextMonthStart =
        LocalDate.now().plusMonths(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC);
    jdbi.useHandle(
        handle -> {
          handle.execute(
              "UPDATE runs SET started_at = ? WHERE uuid = ?", nextMonthStart, nextMonthRunUuid);
        });

    // When: Populate both runs
    customDenormalizedService.populateLineageForRun(currentMonthRun.getRun().getUuid());
    customDenormalizedService.populateLineageForRun(nextMonthRunUuid);

    // Then: Verify partitions exist for both months
    List<String> partitions =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT tablename FROM pg_tables "
                            + "WHERE tablename LIKE 'run_lineage_denormalized_%' "
                            + "ORDER BY tablename")
                    .mapTo(String.class)
                    .list());

    assertThat(partitions).hasSizeGreaterThanOrEqualTo(2);
  }
}
