/*
 * Copyright 2018-2026 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.migrations;

import java.util.List;
import java.util.UUID;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import marquez.service.DenormalizedLineageService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.api.migration.JavaMigration;
import org.jdbi.v3.core.Jdbi;

/**
 * Java migration to backfill existing run data into the denormalized lineage tables.
 *
 * <p>This migration populates the run_lineage_denormalized and run_parent_lineage_denormalized
 * tables with historical run data. It processes runs in configurable chunks to handle large
 * datasets efficiently.
 *
 * <p><b>Automatic Execution Limit:</b><br>
 * This migration will automatically run during deployment ONLY if the runs table has less than
 * 100,000 runs. For larger datasets, the migration will skip automatic execution to prevent
 * long-running migrations that could timeout or block deployments. New runs will still be populated
 * automatically as OpenLineage events arrive.
 *
 * <p><b>Performance Characteristics:</b>
 *
 * <ul>
 *   <li>Default chunk size: 5000 runs
 *   <li>Automatic execution limit: 100,000 runs
 *   <li>Processes runs in descending order by created_at
 *   <li>Includes progress tracking for datasets > 10K runs
 *   <li>Handles failures gracefully - continues processing remaining runs
 * </ul>
 *
 * <p><b>Manual Execution for Large Datasets:</b><br>
 * For datasets exceeding 100K runs, run this migration manually during a maintenance window:
 *
 * <pre>
 * java -jar marquez-api.jar db migrate marquez.yml
 * </pre>
 *
 * For even better performance on very large datasets:
 *
 * <pre>
 * java -jar marquez-api.jar db migrate --chunkSize 10000 marquez.yml
 * </pre>
 *
 * <p><b>Note:</b> This migration can be skipped for fresh installations with no existing run data.
 * New runs will be automatically populated into denormalized tables as OpenLineage events arrive.
 */
@Slf4j
public class V77__backfill_denormalized_lineage_tables implements JavaMigration {

  public static int DEFAULT_CHUNK_SIZE = 5000;
  public static int MAX_RUNS_FOR_AUTO_MIGRATION = 100000; // 100K runs limit for automatic migration

  private static final String COUNT_RUNS_SQL = "SELECT COUNT(*) FROM runs";
  private static final String ESTIMATE_COUNT_RUNS_SQL =
      "SELECT reltuples AS cnt FROM pg_class WHERE relname = 'runs'";
  private static final String GET_RUNS_CHUNK_SQL =
      "SELECT uuid FROM runs ORDER BY created_at DESC LIMIT :chunkSize OFFSET :offset";

  @Setter private Integer chunkSize = null;
  @Setter private boolean manual = false;
  @Setter private Jdbi jdbi;

  public int getChunkSize() {
    return chunkSize != null ? chunkSize : DEFAULT_CHUNK_SIZE;
  }

  @Override
  public MigrationVersion getVersion() {
    return MigrationVersion.fromVersion("77");
  }

  @Override
  public void migrate(Context context) throws Exception {
    log.info("Starting backfill of denormalized lineage tables with existing run data");

    if (context != null) {
      jdbi = Jdbi.create(context.getConnection());
    }

    int estimatedRunsCount = estimateCountRuns();

    if (estimatedRunsCount < 0) {
      log.info("Vacuuming runs table to get accurate estimate");
      jdbi.withHandle(h -> h.execute("VACUUM runs;"));
      log.info("Vacuuming runs table finished");
      estimatedRunsCount = estimateCountRuns();
    }

    log.info("Estimated {} runs in runs table", estimatedRunsCount);

    if (estimatedRunsCount == 0 && countRuns() == 0) {
      log.info("Runs table is empty - no historical data to backfill");
      log.info(
          "Denormalized tables will be populated automatically as new OpenLineage events arrive");
      return;
    }

    if (!manual && estimatedRunsCount >= MAX_RUNS_FOR_AUTO_MIGRATION) {
      log.warn(
          """
              ==================================================
              ==================================================
              ==================================================
              MARQUEZ INSTANCE TOO BIG TO RUN AUTO UPGRADE.
              YOU NEED TO RUN MIGRATION MANUALLY.
              FOR MORE DETAILS, PLEASE REFER TO:
              https://github.com/MarquezProject/marquez/blob/main/api/src/main/resources/marquez/db/migration/V77__readme.md
              ==================================================
              ==================================================
              ==================================================
              """);
      // We end migration successfully although no data has been backfilled to denormalized tables
      return;
    }

    if (estimatedRunsCount > 0) {
      log.info(
          "Starting backfill for {} runs with chunk size {}", estimatedRunsCount, getChunkSize());

      if (estimatedRunsCount > 50000) {
        log.warn(
            "Large dataset detected ({} runs). This migration may take significant time to complete.",
            estimatedRunsCount);
        log.warn(
            "Estimated duration: {} minutes",
            (estimatedRunsCount / 1000)); // Rough estimate: ~1K runs/minute
      }
    }

    DenormalizedLineageService denormalizedLineageService = new DenormalizedLineageService(jdbi);

    log.info("Configured chunkSize is {}", getChunkSize());
    int totalProcessed = 0;
    int totalFailed = 0;
    boolean doBackfill = true;

    // Calculate estimated chunks for progress tracking
    int estimatedChunks = (int) Math.ceil((double) estimatedRunsCount / getChunkSize());
    if (estimatedChunks > 1) {
      log.info("Estimated {} chunks to process for {} runs", estimatedChunks, estimatedRunsCount);
    }

    for (int offset = 0; doBackfill; offset += getChunkSize()) {
      final int currentOffset = offset;
      List<UUID> runUuids =
          jdbi.withHandle(
              h ->
                  h.createQuery(GET_RUNS_CHUNK_SQL)
                      .bind("chunkSize", getChunkSize())
                      .bind("offset", currentOffset)
                      .mapTo(UUID.class)
                      .list());

      if (runUuids.isEmpty()) {
        doBackfill = false;
        break;
      }

      log.info("Processing chunk of {} runs (offset: {})", runUuids.size(), offset);

      int processedInChunk = 0;
      int failedInChunk = 0;
      for (UUID runUuid : runUuids) {
        try {
          denormalizedLineageService.populateLineageForRun(runUuid);
          processedInChunk++;
        } catch (Exception e) {
          log.error("Failed to backfill lineage for run: {}", runUuid, e);
          failedInChunk++;
          // Continue processing remaining runs
        }
      }

      totalProcessed += processedInChunk;
      totalFailed += failedInChunk;

      // Enhanced progress logging for large datasets
      if (estimatedRunsCount > 10000) {
        double progressPercent = (double) totalProcessed / estimatedRunsCount * 100;
        log.info(
            "Processed {} runs in this chunk ({} failed). Total processed: {} / {} ({}%)",
            processedInChunk,
            failedInChunk,
            totalProcessed,
            estimatedRunsCount,
            String.format("%.1f", progressPercent));
      } else {
        log.info(
            "Processed {} runs in this chunk ({} failed). Total processed: {}",
            processedInChunk,
            failedInChunk,
            totalProcessed);
      }
    }

    log.info(
        "Backfill completed. Total runs processed: {} ({} successful, {} failed)",
        totalProcessed + totalFailed,
        totalProcessed,
        totalFailed);

    if (totalFailed > 0) {
      log.warn(
          "{} runs failed to backfill. Check logs above for specific run UUIDs and error details.",
          totalFailed);
    }

    if (estimatedRunsCount > 10000) {
      log.info(
          "Backfill summary: {} runs processed with chunk size {}. Denormalized tables ready for high-performance lineage queries.",
          totalProcessed,
          getChunkSize());
    }
  }

  @Override
  public String getDescription() {
    return "Backfill denormalized lineage tables with existing run data";
  }

  @Override
  public Integer getChecksum() {
    return null;
  }

  @Override
  public boolean isUndo() {
    return false;
  }

  @Override
  public boolean canExecuteInTransaction() {
    return false;
  }

  @Override
  public boolean isBaselineMigration() {
    return false;
  }

  private int estimateCountRuns() {
    return jdbi.withHandle(h -> h.createQuery(ESTIMATE_COUNT_RUNS_SQL).mapTo(Integer.class).one());
  }

  private int countRuns() {
    return jdbi.withHandle(h -> h.createQuery(COUNT_RUNS_SQL).mapTo(Integer.class).one());
  }
}
