/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import io.dropwizard.Application;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Environment;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import marquez.db.migrations.V55_5__BackfillFacets;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/**
 * A command to manually run V55 database migration. This migration requires a heavy DB operation
 * which can be done asynchronously (with limited API downtime) due to separate migration command.
 *
 * <p>Please refer to @link marquez/db/migration/V55__readme.md for more details.
 */
@Slf4j
public class V55MigrationCommand<MarquezConfig> extends EnvironmentCommand<marquez.MarquezConfig> {

  private static final String COMMAND_NAME = "v55_migrate";
  private static final String COMMAND_DESCRIPTION =
      """
        A command to manually run V55 database migration.
        Please refer to https://github.com/MarquezProject/marquez/blob/main/api/src/main/resources/marquez/db/migration/V55__readme.md for more details.
        """;

  /**
   * Creates a new environment command.
   *
   * @param application the application providing this command
   */
  public V55MigrationCommand(Application<marquez.MarquezConfig> application) {
    super(application, COMMAND_NAME, COMMAND_DESCRIPTION);
  }

  @Override
  public void configure(Subparser subparser) {
    subparser
        .addArgument("--chunkSize")
        .dest("chunkSize")
        .type(Integer.class)
        .required(false)
        .setDefault(V55_5__BackfillFacets.DEFAULT_CHUNK_SIZE)
        .help("amount of lineage_events rows processed in a single SQL query and transaction.");
    addFileArgument(subparser);
  }

  @Override
  protected void run(
      Environment environment, Namespace namespace, marquez.MarquezConfig configuration)
      throws Exception {
    log.info("Running v55 migration command");

    final DataSourceFactory sourceFactory = configuration.getDataSourceFactory();
    final DataSource source = sourceFactory.build(environment.metrics(), "MarquezApp-source");

    final JdbiFactory factory = new JdbiFactory();
    final Jdbi jdbi =
        factory
            .build(
                environment,
                configuration.getDataSourceFactory(),
                (ManagedDataSource) source,
                "postgresql-command")
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin())
            .installPlugin(new Jackson2Plugin());

    V55_5__BackfillFacets migration = new V55_5__BackfillFacets();
    migration.setTriggeredByCommand(true);
    migration.setJdbi(jdbi);
    migration.setChunkSize(namespace.getInt("chunkSize"));
    migration.migrate(null);

    log.info("Migration finished successfully");
  }
}
