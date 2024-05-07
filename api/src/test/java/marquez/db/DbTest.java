/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import javax.sql.DataSource;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.junit5.JdbiExtension;
import org.jdbi.v3.testing.junit5.tc.JdbiTestcontainersExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * The base class for interactions with test database. A {@code postgres} container is managed
 * automatically and started only once for a given test suite. The {@code postgres} container will
 * be shared between test methods.
 *
 * <p>After the underlying {@code postgres} container starts, but before a given test suite is
 * executed, the latest {@code flyway} migrations for Marquez will be applied to the database using
 * {@link DbMigration#migrateDbOrError(DataSource)}. When querying the test database, we recommend
 * using the {@code DB} wrapper, but you can also obtain a {@code jdbi} instance directly via {@link
 * JdbiExtension#getJdbi()}}.
 */
@Tag("DataAccessTests")
@Testcontainers
public abstract class DbTest {
  public static final DockerImageName POSTGRES_14 = DockerImageName.parse("postgres:14");

  @Container
  private static final PostgreSQLContainer<?> DB_CONTAINER = new PostgreSQLContainer<>(POSTGRES_14);

  // Defined statically to significantly improve overall test execution.
  @RegisterExtension
  static final JdbiExtension jdbiExtension =
      JdbiTestcontainersExtension.instance(DB_CONTAINER)
          .withPlugin(new SqlObjectPlugin())
          .withPlugin(new PostgresPlugin())
          .withPlugin(new Jackson2Plugin())
          .withInitializer(
              (source, handle) -> {
                // Apply migrations.
                DbMigration.migrateDbOrError(source);
              });

  // Wraps test database connection.
  static TestingDb DB;

  @BeforeAll
  public static void setUpOnce() {
    // Wrap jdbi configured for running container.
    DB = TestingDb.newInstance(jdbiExtension.getJdbi());
  }
}
