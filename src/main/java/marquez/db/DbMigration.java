package marquez.db;

import javax.sql.DataSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;

@Slf4j
public class DbMigration {

  public void migrateDbOrError(@NonNull FlywayFactory flywayFactory, @NonNull DataSource source) {
    final Flyway flyway = flywayFactory.build(source);

    // Attempt to perform a database migration. An exception is thrown on failed migration attempts
    // requiring we handle the throwable and apply a repair on the database to fix any
    // issues before app termination.
    try {
      log.info("Migrating database...");
      final int migrations = flyway.migrate();
      log.info("Successfully applied '{}' migrations to database.", migrations);
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
      throw errorOnDbMigrate; // Signal app termination.
    }
  }
}
