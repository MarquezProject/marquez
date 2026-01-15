/*
 * Copyright 2018-2024 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import java.time.LocalDate;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

/** Service to handle event-driven population of denormalized lineage tables. */
@Slf4j
public class DenormalizedLineageService {

  private final Jdbi jdbi;
  private final PartitionManagementService partitionManagementService;

  public DenormalizedLineageService(@NonNull final Jdbi jdbi) {
    this.jdbi = jdbi;
    this.partitionManagementService = new PartitionManagementService(jdbi, 10, 12);
  }

  public DenormalizedLineageService(
      @NonNull final Jdbi jdbi,
      @NonNull final PartitionManagementService partitionManagementService) {
    this.jdbi = jdbi;
    this.partitionManagementService = partitionManagementService;
  }

  /**
   * Populates denormalized lineage tables for a specific run when it completes. This replaces the
   * materialized view refresh approach with event-driven updates.
   *
   * <p>Logic: - Always populate run_lineage_denormalized for the run (like run_lineage_view) - Only
   * populate run_parent_lineage_denormalized when the run is a parent run AND event is COMPLETE
   * (indicating no more children will be added to this parent)
   */
  public void populateLineageForRun(@NonNull final UUID runUuid) {
    try {
      log.info("Populating denormalized lineage tables for run: {}", runUuid);

      jdbi.useTransaction(
          handle -> {
            // Step 1: Ensure partitions exist for the run date
            ensurePartitionsExist(handle, runUuid);

            // Step 2: Delete existing records for this run
            deleteExistingRecords(handle, runUuid);

            // Step 3: Always populate run_lineage_denormalized for the run
            populateRunLineageDenormalized(handle, runUuid);

            // Step 4: Populate run_parent_lineage_denormalized
            // - If this run is a parent (has children), populate for this run
            // - If this run is a child (has parent), populate for the parent run
            if (isParentRun(handle, runUuid)) {
              populateRunParentLineageDenormalized(handle, runUuid);
            } else if (hasParentRun(handle, runUuid)) {
              UUID parentRunUuid = getParentRunUuid(handle, runUuid);
              populateRunParentLineageDenormalized(handle, parentRunUuid);
            }
          });

      log.info("Successfully populated denormalized lineage tables for run: {}", runUuid);
    } catch (Exception e) {
      log.error("Failed to populate denormalized lineage tables for run: {}", runUuid, e);
      throw e;
    }
  }

  private void deleteExistingRecords(org.jdbi.v3.core.Handle handle, UUID runUuid) {
    log.debug("Deleting existing records for run: {}", runUuid);

    int deletedRunLineage =
        handle
            .createUpdate("DELETE FROM run_lineage_denormalized WHERE run_uuid = :runUuid")
            .bind("runUuid", runUuid)
            .execute();

    int deletedParentLineage =
        handle
            .createUpdate("DELETE FROM run_parent_lineage_denormalized WHERE run_uuid = :runUuid")
            .bind("runUuid", runUuid)
            .execute();

    log.debug(
        "Deleted {} run_lineage_denormalized and {} run_parent_lineage_denormalized records for run: {}",
        deletedRunLineage,
        deletedParentLineage,
        runUuid);
  }

  private void populateRunLineageDenormalized(org.jdbi.v3.core.Handle handle, UUID runUuid) {
    log.debug("Populating run_lineage_denormalized for run: {}", runUuid);

    String insertQuery =
        """
        INSERT INTO run_lineage_denormalized (
            run_uuid, namespace_name, job_name, state, created_at, updated_at,
            started_at, ended_at, job_uuid, job_version_uuid, input_version_uuid,
            input_dataset_uuid, output_version_uuid, output_dataset_uuid,
            input_dataset_namespace, input_dataset_name, input_dataset_version,
            input_dataset_version_uuid, output_dataset_namespace, output_dataset_name,
            output_dataset_version, output_dataset_version_uuid, uuid, parent_run_uuid, run_date
        )
        SELECT
            r.uuid AS run_uuid,
            r.namespace_name,
            r.job_name,
            r.current_run_state AS state,
            r.created_at,
            r.updated_at,
            r.started_at,
            r.ended_at,
            r.job_uuid,
            r.job_version_uuid,
            rim.dataset_version_uuid AS input_version_uuid,
            dvin.dataset_uuid AS input_dataset_uuid,
            dvout.uuid AS output_version_uuid,
            dvout.dataset_uuid AS output_dataset_uuid,
            dvin.namespace_name AS input_dataset_namespace,
            dvin.dataset_name AS input_dataset_name,
            dvin.version AS input_dataset_version,
            dvin.uuid AS input_dataset_version_uuid,
            dvout.namespace_name AS output_dataset_namespace,
            dvout.dataset_name AS output_dataset_name,
            dvout.version AS output_dataset_version,
            dvout.uuid AS output_dataset_version_uuid,
            r.uuid as uuid,
            r.parent_run_uuid as parent_run_uuid,
            DATE(COALESCE(r.started_at, r.ended_at)) as run_date
        FROM runs r
        LEFT JOIN runs_input_mapping rim ON rim.run_uuid = r.uuid
        LEFT JOIN dataset_versions dvin ON dvin.uuid = rim.dataset_version_uuid
        LEFT JOIN dataset_versions dvout ON dvout.run_uuid = r.uuid
        WHERE r.uuid = :runUuid
        """;

    int insertedRows = handle.createUpdate(insertQuery).bind("runUuid", runUuid).execute();

    log.debug("Inserted {} rows into run_lineage_denormalized for run: {}", insertedRows, runUuid);
  }

  private void populateRunParentLineageDenormalized(org.jdbi.v3.core.Handle handle, UUID runUuid) {
    log.debug("Populating run_parent_lineage_denormalized for run: {}", runUuid);

    String insertQuery =
        """
        INSERT INTO run_parent_lineage_denormalized (
            run_uuid, namespace_name, job_name, state, created_at, updated_at,
            started_at, ended_at, job_uuid, job_version_uuid, input_version_uuid,
            input_dataset_uuid, output_version_uuid, output_dataset_uuid,
            input_dataset_namespace, input_dataset_name, input_dataset_version,
            input_dataset_version_uuid, output_dataset_namespace, output_dataset_name,
            output_dataset_version, output_dataset_version_uuid, uuid, parent_run_uuid, run_date
        )
        SELECT DISTINCT
            COALESCE(r.parent_run_uuid,r.uuid) AS run_uuid,
            rp.namespace_name,
            rp.job_name,
            rp.current_run_state AS state,
            rp.created_at,
            rp.updated_at,
            rp.started_at,
            rp.ended_at,
            rp.job_uuid,
            rp.job_version_uuid,
            rim.dataset_version_uuid AS input_version_uuid,
            dvin.dataset_uuid AS input_dataset_uuid,
            dvout.uuid AS output_version_uuid,
            dvout.dataset_uuid AS output_dataset_uuid,
            dvin.namespace_name AS input_dataset_namespace,
            dvin.dataset_name AS input_dataset_name,
            dvin.version AS input_dataset_version,
            dvin.uuid AS input_dataset_version_uuid,
            dvout.namespace_name AS output_dataset_namespace,
            dvout.dataset_name AS output_dataset_name,
            dvout.version AS output_dataset_version,
            dvout.uuid AS output_dataset_version_uuid,
            r.uuid as uuid,
            r.parent_run_uuid as parent_run_uuid,
            DATE(COALESCE(r.started_at, r.ended_at)) as run_date
        FROM runs r
        LEFT JOIN runs_input_mapping rim ON rim.run_uuid = r.uuid
        LEFT JOIN dataset_versions dvin ON dvin.uuid = rim.dataset_version_uuid
        LEFT JOIN dataset_versions dvout ON dvout.run_uuid = r.uuid
        LEFT JOIN runs rp ON rp.uuid=r.parent_run_uuid
        WHERE r.parent_run_uuid = :runUuid
        """;

    int insertedRows = handle.createUpdate(insertQuery).bind("runUuid", runUuid).execute();

    log.debug(
        "Inserted {} rows into run_parent_lineage_denormalized for run: {}", insertedRows, runUuid);
  }

  /** Check if a run is a parent run (has child runs). */
  private boolean isParentRun(org.jdbi.v3.core.Handle handle, UUID runUuid) {
    Integer childCount =
        handle
            .createQuery("SELECT COUNT(*) FROM runs WHERE parent_run_uuid = :runUuid")
            .bind("runUuid", runUuid)
            .mapTo(Integer.class)
            .one();

    boolean isParent = childCount > 0;
    log.debug("Run {} has {} child runs, isParent: {}", runUuid, childCount, isParent);
    return isParent;
  }

  /** Check if a run has a parent run. */
  private boolean hasParentRun(org.jdbi.v3.core.Handle handle, UUID runUuid) {
    UUID parentRunUuid =
        handle
            .createQuery("SELECT parent_run_uuid FROM runs WHERE uuid = :runUuid")
            .bind("runUuid", runUuid)
            .mapTo(UUID.class)
            .findOne()
            .orElse(null);

    boolean hasParent = parentRunUuid != null;
    log.debug("Run {} has parent run: {}, hasParent: {}", runUuid, parentRunUuid, hasParent);
    return hasParent;
  }

  /** Get the parent run UUID for a given run. */
  private UUID getParentRunUuid(org.jdbi.v3.core.Handle handle, UUID runUuid) {
    UUID parentRunUuid =
        handle
            .createQuery("SELECT parent_run_uuid FROM runs WHERE uuid = :runUuid")
            .bind("runUuid", runUuid)
            .mapTo(UUID.class)
            .one();

    log.debug("Run {} has parent run: {}", runUuid, parentRunUuid);
    return parentRunUuid;
  }

  /**
   * Ensure partitions exist for the given run date. This method calls the partition management
   * service to create partitions if they don't exist.
   */
  private void ensurePartitionsExist(org.jdbi.v3.core.Handle handle, UUID runUuid) {
    log.debug("Ensuring partitions exist for run: {}", runUuid);

    // Get the run date for this run
    String runDateStr =
        handle
            .createQuery(
                "SELECT DATE(COALESCE(started_at, ended_at, created_at)) FROM runs WHERE uuid = :runUuid")
            .bind("runUuid", runUuid)
            .mapTo(String.class)
            .one();

    // Convert to LocalDate and ensure partitions exist
    LocalDate runDate = LocalDate.parse(runDateStr);
    partitionManagementService.ensurePartitionExists(runDate);
  }

  /** Get partition statistics for monitoring. */
  public void getPartitionStats() {
    try {
      jdbi.useHandle(
          handle -> {
            var runLineageStats =
                handle
                    .createQuery(
                        "SELECT * FROM get_partition_stats('run_lineage_denormalized_partitioned')")
                    .mapToMap()
                    .list();

            var parentLineageStats =
                handle
                    .createQuery(
                        "SELECT * FROM get_partition_stats('run_parent_lineage_denormalized')")
                    .mapToMap()
                    .list();

            log.info("Run lineage partition stats: {}", runLineageStats);
            log.info("Parent lineage partition stats: {}", parentLineageStats);
          });
    } catch (Exception e) {
      log.error("Failed to get partition statistics", e);
    }
  }

  /** Analyze all partitions to update statistics. */
  public void analyzeAllPartitions() {
    try {
      jdbi.useHandle(
          handle -> {
            handle
                .createUpdate("SELECT analyze_all_partitions('run_lineage_denormalized')")
                .execute();
            handle
                .createUpdate("SELECT analyze_all_partitions('run_parent_lineage_denormalized')")
                .execute();
            log.info("Analyzed all partitions");
          });
    } catch (Exception e) {
      log.error("Failed to analyze partitions", e);
    }
  }

  /**
   * Bulk populate all existing runs into the denormalized tables. This is useful for initial
   * migration from materialized views.
   */
  public void populateAllExistingRuns() {
    try {
      log.info("Starting bulk population of all existing runs into denormalized tables");

      jdbi.useTransaction(
          handle -> {
            // Clear existing data
            handle.execute("DELETE FROM run_lineage_denormalized");
            handle.execute("DELETE FROM run_parent_lineage_denormalized");

            // Populate run_lineage_denormalized for all runs
            String bulkInsertRunLineage =
                """
            INSERT INTO run_lineage_denormalized (
                run_uuid, namespace_name, job_name, state, created_at, updated_at,
                started_at, ended_at, job_uuid, job_version_uuid, input_version_uuid,
                input_dataset_uuid, output_version_uuid, output_dataset_uuid,
                input_dataset_namespace, input_dataset_name, input_dataset_version,
                input_dataset_version_uuid, output_dataset_namespace, output_dataset_name,
                output_dataset_version, output_dataset_version_uuid, uuid, parent_run_uuid, run_date
            )
            SELECT
                r.uuid AS run_uuid,
                r.namespace_name,
                r.job_name,
                r.current_run_state AS state,
                r.created_at,
                r.updated_at,
                r.started_at,
                r.ended_at,
                r.job_uuid,
                r.job_version_uuid,
                rim.dataset_version_uuid AS input_version_uuid,
                dvin.dataset_uuid AS input_dataset_uuid,
                dvout.uuid AS output_version_uuid,
                dvout.dataset_uuid AS output_dataset_uuid,
                dvin.namespace_name AS input_dataset_namespace,
                dvin.dataset_name AS input_dataset_name,
                dvin.version AS input_dataset_version,
                dvin.uuid AS input_dataset_version_uuid,
                dvout.namespace_name AS output_dataset_namespace,
                dvout.dataset_name AS output_dataset_name,
                dvout.version AS output_dataset_version,
                dvout.uuid AS output_dataset_version_uuid,
                r.uuid as uuid,
                r.parent_run_uuid as parent_run_uuid,
                DATE(COALESCE(r.started_at, r.ended_at)) as run_date
            FROM runs r
            LEFT JOIN runs_input_mapping rim ON rim.run_uuid = r.uuid
            LEFT JOIN dataset_versions dvin ON dvin.uuid = rim.dataset_version_uuid
            LEFT JOIN dataset_versions dvout ON dvout.run_uuid = r.uuid
            """;

            int runLineageRows = handle.createUpdate(bulkInsertRunLineage).execute();

            // Populate run_parent_lineage_denormalized for all runs with parents
            String bulkInsertParentLineage =
                """
            INSERT INTO run_parent_lineage_denormalized (
                run_uuid, namespace_name, job_name, state, created_at, updated_at,
                started_at, ended_at, job_uuid, job_version_uuid, input_version_uuid,
                input_dataset_uuid, output_version_uuid, output_dataset_uuid,
                input_dataset_namespace, input_dataset_name, input_dataset_version,
                input_dataset_version_uuid, output_dataset_namespace, output_dataset_name,
                output_dataset_version, output_dataset_version_uuid, uuid, parent_run_uuid, run_date
            )
            SELECT DISTINCT
                COALESCE(r.parent_run_uuid,r.uuid) AS run_uuid,
                rp.namespace_name,
                rp.job_name,
                rp.current_run_state AS state,
                rp.created_at,
                rp.updated_at,
                rp.started_at,
                rp.ended_at,
                rp.job_uuid,
                rp.job_version_uuid,
                rim.dataset_version_uuid AS input_version_uuid,
                dvin.dataset_uuid AS input_dataset_uuid,
                dvout.uuid AS output_version_uuid,
                dvout.dataset_uuid AS output_dataset_uuid,
                dvin.namespace_name AS input_dataset_namespace,
                dvin.dataset_name AS input_dataset_name,
                dvin.version AS input_dataset_version,
                dvin.uuid AS input_dataset_version_uuid,
                dvout.namespace_name AS output_dataset_namespace,
                dvout.dataset_name AS output_dataset_name,
                dvout.version AS output_dataset_version,
                dvout.uuid AS output_dataset_version_uuid,
                r.uuid as uuid,
                r.parent_run_uuid as parent_run_uuid,
                DATE(COALESCE(r.started_at, r.ended_at)) as run_date
            FROM runs r
            LEFT JOIN runs_input_mapping rim ON rim.run_uuid = r.uuid
            LEFT JOIN dataset_versions dvin ON dvin.uuid = rim.dataset_version_uuid
            LEFT JOIN dataset_versions dvout ON dvout.run_uuid = r.uuid
            LEFT JOIN runs rp ON rp.uuid=r.parent_run_uuid
            WHERE r.parent_run_uuid is not null
            """;

            int parentLineageRows = handle.createUpdate(bulkInsertParentLineage).execute();

            log.info(
                "Bulk population completed: {} run_lineage_denormalized rows, {} run_parent_lineage_denormalized rows",
                runLineageRows,
                parentLineageRows);
          });

    } catch (Exception e) {
      log.error("Failed to bulk populate existing runs into denormalized tables", e);
      throw e;
    }
  }
}
