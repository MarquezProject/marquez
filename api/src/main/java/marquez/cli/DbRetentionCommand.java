/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import static marquez.db.DbRetention.DEFAULT_DRY_RUN;
import static marquez.db.DbRetention.DEFAULT_NUMBER_OF_ROWS_PER_BATCH;
import static marquez.db.DbRetention.DEFAULT_RETENTION_DAYS;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.MarquezConfig;
import marquez.db.DbRetention;
import marquez.db.exceptions.DbRetentionException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;

/**
 * A command to apply a one-off ad-hoc retention policy directly to source, dataset, and job
 * metadata collected by Marquez.
 *
 * <h2>Usage</h2>
 *
 * For example, to override the {@code retention-days}:
 *
 * <pre>{@code
 * java -jar marquez-api.jar db-retention --retention-days 30 marquez.yml
 * }</pre>
 */
@Slf4j
public class DbRetentionCommand extends ConfiguredCommand<MarquezConfig> {
  private static final String DB_SOURCE_NAME = "ad-hoc-db-retention-source";

  /* Args for 'db-retention' command. */
  private static final String CMD_ARG_NUMBER_OF_ROWS_PER_BATCH = "numberOfRowsPerBatch";
  private static final String CMD_ARG_RETENTION_DAYS = "retentionDays";
  private static final String CMD_ARG_DRY_RUN = "dryRun";

  /* Define 'db-retention' command. */
  public DbRetentionCommand() {
    super("db-retention", "apply one-off ad-hoc retention policy directly to database");
  }

  @Override
  public void configure(@NonNull net.sourceforge.argparse4j.inf.Subparser subparser) {
    super.configure(subparser);
    // Arg '--number-of-rows-per-batch'
    subparser
        .addArgument("--number-of-rows-per-batch")
        .dest(CMD_ARG_NUMBER_OF_ROWS_PER_BATCH)
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_NUMBER_OF_ROWS_PER_BATCH)
        .help("the number of rows deleted per batch");
    // Arg '--retention-days'
    subparser
        .addArgument("--retention-days")
        .dest(CMD_ARG_RETENTION_DAYS)
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_RETENTION_DAYS)
        .help("the number of days to retain metadata");
    // Arg '--dry-run'
    subparser
        .addArgument("--dry-run")
        .dest(CMD_ARG_DRY_RUN)
        .type(Boolean.class)
        .required(false)
        .setDefault(DEFAULT_DRY_RUN)
        .action(Arguments.storeTrue())
        .help(
            "only output an estimate of metadata deleted by the retention policy, "
                + "without applying the policy on database");
  }

  @Override
  protected void run(
      @NonNull Bootstrap<MarquezConfig> bootstrap,
      @NonNull Namespace namespace,
      @NonNull MarquezConfig config)
      throws Exception {
    final int numberOfRowsPerBatch = namespace.getInt(CMD_ARG_NUMBER_OF_ROWS_PER_BATCH);
    final int retentionDays = namespace.getInt(CMD_ARG_RETENTION_DAYS);
    final boolean dryRun = namespace.getBoolean(CMD_ARG_DRY_RUN);

    // Configure connection.
    final DataSourceFactory sourceFactory = config.getDataSourceFactory();
    final ManagedDataSource source =
        sourceFactory.build(bootstrap.getMetricRegistry(), DB_SOURCE_NAME);

    // Open connection.
    final Jdbi jdbi = Jdbi.create(source);
    jdbi.installPlugin(new PostgresPlugin()); // Add postgres support.

    try {
      // Attempt to apply a database retention policy. An exception is thrown on failed retention
      // policy attempts requiring we handle the throwable and log the error.
      DbRetention.retentionOnDbOrError(jdbi, numberOfRowsPerBatch, retentionDays, dryRun);
    } catch (DbRetentionException errorOnDbRetention) {
      log.error(
          "Failed to apply retention policy of '{}' days to database!",
          retentionDays,
          errorOnDbRetention);
    }
  }
}
