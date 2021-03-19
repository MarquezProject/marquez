package marquez.db;

import io.dropwizard.flyway.FlywayFactory;
import javax.sql.DataSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.MarquezConfig;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.output.MigrateResult;

@Slf4j
public final class DbMigration {
  private DbMigration() {}

  public static void migrateDbOrError(
      @NonNull final MarquezConfig config, @NonNull final DataSource source) {
    final FlywayFactory flywayFactory = config.getFlywayFactory();
    final Flyway flyway = flywayFactory.build(source);
    // Only attempt a database migration if there are pending changes to be applied,
    // or we're initialization of a new database. Otherwise, error on pending changes
    // when the flag 'migrateOnStartup' is set to false.
    if (config.isMigrateOnStartup() && !hasPendingMigrations(flyway)) {
      log.info("No pending migrations found, skipping...");
      return;
    } else if (!config.isMigrateOnStartup() && hasMigrationsApplied(flyway)) {
      errorOnPendingMigrations(flyway);
    }
    // Attempt to perform a database migration. An exception is thrown on failed migration attempts
    // requiring we handle the throwable and apply a repair on the database to fix any
    // issues before app termination.
    try {
      log.info("Migrating database...");
      final MigrateResult result = flyway.migrate();
      log.info("Successfully applied '{}' migrations to database.", result.migrationsExecuted);
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

  private static boolean hasMigrationsApplied(@NonNull final Flyway flyway) {
    return flyway.info().applied().length > 0;
  }

  private static void errorOnPendingMigrations(@NonNull final Flyway flyway) {
    if (hasPendingMigrations(flyway)) {
      log.error(
          "Failed to apply migration. You must apply the migration manually with "
              + "'path/to/marquez-api.jar db migrate marquez.yml', or set MIGRATE_ON_STARTUP=true "
              + "to automatically apply migrations to your database. We recommend you "
              + "view migrations before applying them with 'path/to/marquez-api.jar db info marquez.yml'.");
      throw new FlywayException("Database has pending migrations!");
    }
  }

  private static boolean hasPendingMigrations(@NonNull final Flyway flyway) {
    return flyway.info().pending().length > 0;
  }
}
