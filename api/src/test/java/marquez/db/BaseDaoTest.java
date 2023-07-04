package marquez.db;

import javax.sql.DataSource;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObject;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.junit5.JdbiExtension;
import org.jdbi.v3.testing.junit5.tc.JdbiTestcontainersExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * The base class for {@code DAO} test suites that implement {@link SqlObject}. A {@code postgres}
 * container is managed automatically and started only once for a given test suite. Note, the
 * container will be shared between test methods.
 *
 * <p>After the underlying {@code postgres} container starts, but before a given test suite is
 * executed, the latest {@code flyway} migrations for Marquez will be applied to the database using
 * {@link DbMigration#migrateDbOrError(DataSource)}
 */
@Tag("DataAccessTests")
@Testcontainers
public class BaseDaoTest {
  static DockerImageName POSTGRES_12_1 = DockerImageName.parse("postgres:12.1");

  @Container static PostgreSQLContainer<?> DB_CONTAINER = new PostgreSQLContainer<>(POSTGRES_12_1);

  @RegisterExtension
  static JdbiExtension jdbiExtension =
      JdbiTestcontainersExtension.instance(DB_CONTAINER)
          .withPlugin(new SqlObjectPlugin())
          .withPlugin(new PostgresPlugin())
          .withPlugin(new Jackson2Plugin())
          .withInitializer(
              (source, handle) -> {
                // Apply migrations.
                DbMigration.migrateDbOrError(source);
              });
}
