/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.cli;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.MarquezConfig;
import marquez.db.DbMigration;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * A command to manually run database migrations. This command to be used to run migrations
 * decoupled from application deployment.
 */
@Slf4j
public class DbMigrateCommand extends ConfiguredCommand<MarquezConfig> {

  public DbMigrateCommand() {
    super("db-migrate", "A command to manually run database migrations.");
  }

  @Override
  protected void run(
      @NonNull Bootstrap<MarquezConfig> bootstrap,
      @NonNull Namespace namespace,
      @NonNull MarquezConfig configuration)
      throws Exception {

    final DataSourceFactory sourceFactory = configuration.getDataSourceFactory();
    final ManagedDataSource source =
        sourceFactory.build(bootstrap.getMetricRegistry(), "MarquezApp-source");

    DbMigration.migrateDbOrError(configuration.getFlywayFactory(), source, true);
  }
}
