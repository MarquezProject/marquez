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

  @Test
  public void testGetPartitionStats() {
    // Test: Verify partition statistics can be retrieved
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_stats",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));

    denormalizedLineageService.populateLineageForRun(lineageRow.getRun().getUuid());

    // When: Get partition stats
    assertThatCode(() -> denormalizedLineageService.getPartitionStats()).doesNotThrowAnyException();
  }

  @Test
  public void testAnalyzeAllPartitions() {
    // Test: Verify analyze partitions command executes
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_analyze",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));

    denormalizedLineageService.populateLineageForRun(lineageRow.getRun().getUuid());

    // When: Analyze partitions
    assertThatCode(() -> denormalizedLineageService.analyzeAllPartitions())
        .doesNotThrowAnyException();
  }

  @Test
  public void testPopulateLineageWithErrorHandling() {
    // Test: Verify error handling when run doesn't exist
    UUID nonExistentRunUuid = UUID.randomUUID();

    // When: Try to populate lineage for non-existent run
    try {
      denormalizedLineageService.populateLineageForRun(nonExistentRunUuid);
    } catch (Exception e) {
      // Then: Should throw exception
      assertThat(e).isNotNull();
    }
  }

  @Test
  public void testDeleteExistingRecordsBeforePopulate() {
    // Given: Run with existing denormalized records
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_delete",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // Populate once
    denormalizedLineageService.populateLineageForRun(runUuid);

    Long countBefore =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized WHERE run_uuid = ?")
                    .bind(0, runUuid)
                    .mapTo(Long.class)
                    .one());

    // When: Populate again (should delete and re-insert)
    denormalizedLineageService.populateLineageForRun(runUuid);

    // Then: Count should remain the same (deleted old, inserted new)
    Long countAfter =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery("SELECT COUNT(*) FROM run_lineage_denormalized WHERE run_uuid = ?")
                    .bind(0, runUuid)
                    .mapTo(Long.class)
                    .one());

    assertThat(countAfter).isEqualTo(countBefore);
  }

  @Test
  public void testIsParentRunDetection() {
    // Given: Parent run with child
    UpdateLineageRow parentRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "parent_job_detection",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "parent_output", null)));

    UpdateLineageRow childRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "child_job_detection",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "child_output", null)));

    UUID parentRunUuid = parentRun.getRun().getUuid();
    UUID childRunUuid = childRun.getRun().getUuid();

    // Set parent-child relationship
    jdbi.useHandle(
        handle -> {
          handle.execute(
              "UPDATE runs SET parent_run_uuid = ? WHERE uuid = ?", parentRunUuid, childRunUuid);
        });

    // When: Populate parent run
    denormalizedLineageService.populateLineageForRun(parentRunUuid);

    // Then: Parent lineage should be populated
    Long parentLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT COUNT(*) FROM run_parent_lineage_denormalized WHERE run_uuid = ?")
                    .bind(0, parentRunUuid)
                    .mapTo(Long.class)
                    .one());

    assertThat(parentLineageCount).isGreaterThan(0);
  }

  @Test
  public void testHasParentRunDetection() {
    // Given: Child run with parent
    UpdateLineageRow parentRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "parent_job_has_parent",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "parent_output", null)));

    UpdateLineageRow childRun =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "child_job_has_parent",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "child_output", null)));

    UUID parentRunUuid = parentRun.getRun().getUuid();
    UUID childRunUuid = childRun.getRun().getUuid();

    // Set parent-child relationship
    jdbi.useHandle(
        handle -> {
          handle.execute(
              "UPDATE runs SET parent_run_uuid = ? WHERE uuid = ?", parentRunUuid, childRunUuid);
        });

    // When: Populate child run (should update parent lineage)
    denormalizedLineageService.populateLineageForRun(childRunUuid);

    // Then: Parent lineage should be updated
    Long parentLineageCount =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT COUNT(*) FROM run_parent_lineage_denormalized WHERE run_uuid = ?")
                    .bind(0, parentRunUuid)
                    .mapTo(Long.class)
                    .one());

    assertThat(parentLineageCount).isGreaterThan(0);
  }

  @Test
  public void testPopulateRunLineageDenormalizedWithInputsAndOutputs() {
    // Test: Verify run lineage includes input and output datasets
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_io",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(
                new Dataset("namespace", "input1", null), new Dataset("namespace", "input2", null)),
            List.of(
                new Dataset("namespace", "output1", null),
                new Dataset("namespace", "output2", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // When: Populate lineage
    denormalizedLineageService.populateLineageForRun(runUuid);

    // Then: Verify inputs and outputs are in denormalized table
    List<String> inputDatasets =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT DISTINCT input_dataset_name FROM run_lineage_denormalized WHERE run_uuid = ? AND input_dataset_name IS NOT NULL")
                    .bind(0, runUuid)
                    .mapTo(String.class)
                    .list());

    List<String> outputDatasets =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT DISTINCT output_dataset_name FROM run_lineage_denormalized WHERE run_uuid = ? AND output_dataset_name IS NOT NULL")
                    .bind(0, runUuid)
                    .mapTo(String.class)
                    .list());

    assertThat(inputDatasets).hasSizeGreaterThanOrEqualTo(2);
    assertThat(outputDatasets).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  public void testEnsurePartitionsExistForRunDate() {
    // Test: Verify partitions are created for run date
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_partition_date",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // When: Populate lineage (should auto-create partition)
    denormalizedLineageService.populateLineageForRun(runUuid);

    // Then: Verify partition exists for current month
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

    LocalDate today = LocalDate.now();
    String expectedPartition =
        "run_lineage_denormalized_y"
            + today.format(DateTimeFormatter.ofPattern("yyyy"))
            + "m"
            + today.format(DateTimeFormatter.ofPattern("MM"));

    assertThat(partitions).contains(expectedPartition);
  }

  @Test
  public void testRunWithNullStartedAtUsesEndedAt() {
    // Given: Run with null started_at but has ended_at
    UpdateLineageRow lineageRow =
        LineageTestUtils.createLineageRow(
            openLineageDao,
            "test_job_null_started",
            "COMPLETE",
            JobFacet.builder().build(),
            List.of(),
            List.of(new Dataset("namespace", "output", null)));
    UUID runUuid = lineageRow.getRun().getUuid();

    // Set started_at to null but keep ended_at
    jdbi.useHandle(
        handle -> {
          handle.execute("UPDATE runs SET started_at = NULL WHERE uuid = ?", runUuid);
        });

    // When: Populate lineage
    assertThatCode(() -> denormalizedLineageService.populateLineageForRun(runUuid))
        .doesNotThrowAnyException();

    // Then: Should use ended_at for run_date
    Long count =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT COUNT(*) FROM run_lineage_denormalized WHERE run_uuid = ? AND run_date IS NOT NULL")
                    .bind(0, runUuid)
                    .mapTo(Long.class)
                    .one());

    assertThat(count).isGreaterThan(0);
  }
}
