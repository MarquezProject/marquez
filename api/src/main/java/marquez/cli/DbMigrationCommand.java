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
import marquez.db.migrations.V57_1__BackfillFacets;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

/**
 * A command to manually run database migrations when needed. This migration requires a heavy DB
 * operation which can be done asynchronously (with limited API downtime) due to separate migration
 * command.
 */
@Slf4j
public class DbMigrationCommand<MarquezConfig> extends EnvironmentCommand<marquez.MarquezConfig> {

  private static final String DB_MIGRATE = "db-migrate";
  private static final String MIGRATION_V57_DESCRIPTION =
      """
        A command to manually run V57 database migration.
        Please refer to https://github.com/MarquezProject/marquez/blob/main/api/src/main/resources/marquez/db/migration/V57__readme.md for more details.
        """;

  private static final String COMMAND_DESCRIPTION =
      """
        A command to manually run database migrations.
        Extra parameters are required to specify the migration to run.
        """;

  /**
   * Creates a new environment command.
   *
   * @param application the application providing this command
   */
  public DbMigrationCommand(Application<marquez.MarquezConfig> application) {
    super(application, DB_MIGRATE, COMMAND_DESCRIPTION);
  }

  @Override
  public void configure(Subparser subparser) {
    subparser
        .addArgument("--chunkSize")
        .dest("chunkSize")
        .type(Integer.class)
        .required(false)
        .setDefault(V57_1__BackfillFacets.DEFAULT_CHUNK_SIZE)
        .help("amount of lineage_events rows processed in a single SQL query and transaction.");

    subparser
        .addArgument("--version")
        .dest("version")
        .type(String.class)
        .required(true)
        .help("migration version to apply like 'v57'");

    addFileArgument(subparser);
  }

  @Override
  protected void run(
      Environment environment, Namespace namespace, marquez.MarquezConfig configuration)
      throws Exception {

    final DataSourceFactory sourceFactory = configuration.getDataSourceFactory();
    final DataSource source = sourceFactory.build(environment.metrics(), "MarquezApp-source");
    final JdbiFactory factory = new JdbiFactory();

    Jdbi jdbi =
        factory
            .build(
                environment,
                configuration.getDataSourceFactory(),
                (ManagedDataSource) source,
                "postgresql-command")
            .installPlugin(new SqlObjectPlugin())
            .installPlugin(new PostgresPlugin())
            .installPlugin(new Jackson2Plugin());

    MarquezMigrations.valueOf(namespace.getString("version")).run(jdbi, namespace);
  }

  enum MarquezMigrations {
    v57 {
      public void run(Jdbi jdbi, Namespace namespace) throws Exception {
        log.info("Running V57_1__BackfillFacets migration");
        V57_1__BackfillFacets migration = new V57_1__BackfillFacets();
        migration.setManual(true);
        migration.setJdbi(jdbi);
        migration.setChunkSize(namespace.getInt("chunkSize"));
        migration.migrate(null);
      }
    };

    public void run(Jdbi jdbi, Namespace namespace) throws Exception {
      throw new UnsupportedOperationException();
    }
  }
}
