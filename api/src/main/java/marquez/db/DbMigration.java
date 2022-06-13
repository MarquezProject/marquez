/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import javax.sql.DataSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.MigrateResult;

@Slf4j
public final class DbMigration {
  private DbMigration() {}

  public static void migrateDbOrError(
      @NonNull final FlywayFactory flywayFactory,
      @NonNull final DataSource source,
      final boolean migrateOnStartup) {
    final Flyway flyway = flywayFactory.build(source);
    // Only attempt a database migration if there are pending changes to be applied,
    // or on the initialization of a new database. Otherwise, error on pending changes
    // when the flag 'migrateOnStartup' is set to 'false'.
    if (!hasPendingDbMigrations(flyway)) {
      log.info("No pending migrations found, skipping...");
      return;
    } else if (!migrateOnStartup && hasDbMigrationsApplied(flyway)) {
      errorOnPendingDbMigrations(flyway);
    }
    // Attempt to perform a database migration. An exception is thrown on failed migration attempts
    // requiring we handle the throwable and apply a repair on the database to fix any
    // issues before app termination.
    try {
      log.info("Migrating database...");
      final MigrateResult migrateResult = flyway.migrate();
      log.info(
          "Successfully applied '{}' migrations to database.", migrateResult.migrationsExecuted);
    } catch (FlywayException errorOnDbMigrate) {
      log.error("Failed to apply migration to database.", errorOnDbMigrate);
      try {
        log.info("Repairing failed database migration...");
        flyway.repair();
        log.info("Successfully repaired database.");
      } catch (FlywayException errorOnDbRepair) {
        log.error("Failed to apply repair to database.", errorOnDbRepair);
      }

      // Propagate throwable up the stack.
      throw errorOnDbMigrate;
    }
  }

  private static boolean hasDbMigrationsApplied(@NonNull final Flyway flyway) {
    return flyway.info().applied().length > 0;
  }

  private static void errorOnPendingDbMigrations(@NonNull final Flyway flyway) {
    if (hasPendingDbMigrations(flyway)) {
      log.error(
          "Failed to apply migration! You must apply the migration manually using the flyway "
              + "command 'flyway migrate', or set 'MIGRATE_ON_STARTUP=true' to automatically apply "
              + "migrations to your database. We recommend you view database changes before "
              + "applying a new migration with 'flyway info'. You can download the flyway CLI "
              + "at 'https://flywaydb.org/download'");
      throw new FlywayException("Database has pending migrations!");
    }
  }

  private static boolean hasPendingDbMigrations(@NonNull final Flyway flyway) {
    return flyway.info().pending().length > 0;
  }
}
