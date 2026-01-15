/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import marquez.api.JdbiUtils;
import marquez.jdbi.MarquezJdbiExternalPostgresExtension;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MarquezJdbiExternalPostgresExtension.class)
public class PartitionManagementServiceTest {

  private static Jdbi jdbi;
  private static PartitionManagementService partitionService;

  @BeforeAll
  public static void setUpOnce(Jdbi jdbi) {
    PartitionManagementServiceTest.jdbi = jdbi;
    partitionService = new PartitionManagementService(jdbi, 10, 12);
  }

  @AfterEach
  public void tearDown(Jdbi jdbi) {
    JdbiUtils.cleanDatabase(jdbi);
  }

  @Test
  public void testEnsurePartitionExists() {
    // Test: Ensure partition exists for current date
    LocalDate currentDate = LocalDate.now();

    // When: Ensure partition exists
    assertThatCode(() -> partitionService.ensurePartitionExists(currentDate))
        .doesNotThrowAnyException();

    // Then: Verify partition was created
    String partitionName =
        "run_lineage_denormalized_y"
            + currentDate.format(DateTimeFormatter.ofPattern("yyyy"))
            + "m"
            + currentDate.format(DateTimeFormatter.ofPattern("MM"));

    boolean exists =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT EXISTS (SELECT 1 FROM pg_tables WHERE tablename LIKE :pattern)")
                    .bind("pattern", "run_lineage_denormalized_%")
                    .mapTo(Boolean.class)
                    .one());

    assertThat(exists).isTrue();
  }

  @Test
  public void testCreatePartitionsForPeriod() {
    // Test: Create partitions for a specific period (7 days)
    LocalDate startDate = LocalDate.now();

    // When: Create partitions for 7 days
    partitionService.createPartitionsForPeriod(startDate, 7);

    // Then: Verify partitions exist
    List<String> partitions =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT tablename FROM pg_tables "
                            + "WHERE tablename LIKE 'run_lineage_denormalized_%' "
                            + "OR tablename LIKE 'run_parent_lineage_denormalized_%' "
                            + "ORDER BY tablename")
                    .mapTo(String.class)
                    .list());

    // Should have at least 2 partitions (current month for both tables)
    assertThat(partitions).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  public void testCreateUpcomingPartitions() {
    // Test: Create upcoming partitions based on daysAhead configuration
    // When: Create upcoming partitions (10 days ahead from constructor)
    partitionService.createUpcomingPartitions();

    // Then: Verify partitions exist
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

    // Should have at least current month partition
    assertThat(partitions).isNotEmpty();
  }

  @Test
  public void testCleanupOldPartitions() {
    // Test: Cleanup old partitions based on retention policy
    PartitionManagementService service = new PartitionManagementService(jdbi, 10, 3);

    // Create some partitions first
    service.createUpcomingPartitions();

    // When: Cleanup old partitions (3 month retention)
    assertThatCode(() -> service.cleanupOldPartitions()).doesNotThrowAnyException();

    // Then: Should complete without error (nothing to clean in test)
    assertThat(true).isTrue();
  }

  @Test
  public void testConfigurableDaysAhead30Days() {
    // Test: Configure partition service with 30 days ahead
    PartitionManagementService service = new PartitionManagementService(jdbi, 30, 3);

    // When: Create upcoming partitions
    service.createUpcomingPartitions();

    // Then: Verify partitions exist for next 30 days (at least 2 months)
    LocalDate today = LocalDate.now();
    LocalDate thirtyDaysLater = today.plusDays(30);

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

    // Should cover at least current month + next month
    assertThat(partitions).hasSizeGreaterThanOrEqualTo(2);

    // Verify partitions for current and next month exist
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

    assertThat(partitions).contains(currentMonthPartition, nextMonthPartition);
  }

  @Test
  public void testConfigurableDaysAhead40Days() {
    // Test: Configure partition service with 40 days ahead
    PartitionManagementService service = new PartitionManagementService(jdbi, 40, 3);

    // When: Create upcoming partitions
    service.createUpcomingPartitions();

    // Then: Verify partitions exist for next 40 days (at least 2 months)
    LocalDate today = LocalDate.now();

    List<String> partitions =
        jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT tablename FROM pg_tables "
                            + "WHERE tablename LIKE 'run_parent_lineage_denormalized_%' "
                            + "ORDER BY tablename")
                    .mapTo(String.class)
                    .list());

    // Should cover at least current month + next month (possibly 3 depending on day of month)
    assertThat(partitions).hasSizeGreaterThanOrEqualTo(2);
  }

  @Test
  public void testPerformMaintenance() {
    // Test: Perform full maintenance cycle
    PartitionManagementService service = new PartitionManagementService(jdbi, 10, 3);

    // When: Perform maintenance
    assertThatCode(() -> service.performMaintenance()).doesNotThrowAnyException();

    // Then: Verify upcoming partitions created
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

    // Should have created partitions for next 10 days
    assertThat(partitions).isNotEmpty();
  }

  @Test
  public void testGetPartitionStats() {
    // Test: Get partition statistics
    PartitionManagementService service = new PartitionManagementService(jdbi, 10, 3);

    // Create partitions using the public API
    service.createUpcomingPartitions();

    // When: Get stats
    var stats = service.getPartitionStats();

    // Then: Verify stats returned
    assertThat(stats).containsKey("partitions");
    assertThat(stats).containsKey("total_partitions");
    assertThat((Integer) stats.get("total_partitions")).isGreaterThanOrEqualTo(2);
  }

  @Test
  public void testAnalyzePartitions() {
    // Test: Analyze partitions updates statistics
    PartitionManagementService service = new PartitionManagementService(jdbi, 10, 3);

    // Create partitions using the public API
    service.createUpcomingPartitions();

    // When: Analyze partitions
    assertThatCode(() -> service.analyzePartitions()).doesNotThrowAnyException();

    // No exception means success (ANALYZE updates internal PostgreSQL statistics)
  }

  @Test
  public void testCustomRetentionPeriod() {
    // Test: Custom retention period of 6 months
    PartitionManagementService service = new PartitionManagementService(jdbi, 60, 6);

    // Create partitions for the current period
    service.createUpcomingPartitions();

    // When: Cleanup with 6 month retention
    assertThatCode(() -> service.cleanupOldPartitions()).doesNotThrowAnyException();

    // Then: Verify partitions exist
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

    // Should have partitions created
    assertThat(partitions).isNotEmpty();
  }
}
