/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

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
import net.sourceforge.argparse4j.inf.Namespace;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.postgres.PostgresPlugin;

/**
 * A command to apply retention policy to source, dataset, and job metadata collected by Marquez.
 *
 * <h2>Usage</h2>
 *
 * For example, to override the {@code retention-days}:
 *
 * <pre>{@code
 * java -jar marquez-api.jar db-retention --retention-days 14 marquez.yml
 * }</pre>
 */
@Slf4j
public class DbRetentionCommand extends ConfiguredCommand<MarquezConfig> {
  private static final String DB_SOURCE_NAME = "db-retention-source";

  /* Args for db-retention command. */
  private static final String CMD_ARG_RETENTION_DAYS = "retentionDays";

  /* Define db-retention command. */
  public DbRetentionCommand() {
    super("db-retention", "apply retention policy to database");
  }

  @Override
  public void configure(@NonNull final net.sourceforge.argparse4j.inf.Subparser subparser) {
    super.configure(subparser);
    subparser
        .addArgument("--retention-days")
        .dest(CMD_ARG_RETENTION_DAYS)
        .type(Integer.class)
        .required(false)
        .setDefault(DEFAULT_RETENTION_DAYS)
        .help("the number of days to retain metadata");
  }

  @Override
  protected void run(
      @NonNull Bootstrap<MarquezConfig> bootstrap,
      @NonNull Namespace namespace,
      @NonNull MarquezConfig config)
      throws Exception {
    final int retentionDays = namespace.getInt(CMD_ARG_RETENTION_DAYS);

    final DataSourceFactory sourceFactory = config.getDataSourceFactory();
    final ManagedDataSource source =
        sourceFactory.build(bootstrap.getMetricRegistry(), DB_SOURCE_NAME);

    // Configure connection.
    final Jdbi jdbi = Jdbi.create(source);
    jdbi.installPlugin(new PostgresPlugin()); // Add postgres support.

    try {
      // Attempt to apply a database retention policy. An exception is thrown on failed retention
      // policy attempts requiring we handle the throwable and log the error.
      DbRetention.retentionOnDbOrError(jdbi, retentionDays);
    } catch (DbRetentionException errorOnDbRetention) {
      log.error(
          "Failed to apply retention policy of '{}' days to database!",
          retentionDays,
          errorOnDbRetention);
    }
  }
}
