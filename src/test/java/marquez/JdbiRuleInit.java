package marquez;

import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;

public class JdbiRuleInit {

  private static final PostgresContainer POSTGRES = PostgresContainer.create();

  static {
    POSTGRES.start();
  }

  public static JdbiRule init() {
    return JdbiRule.externalPostgres(
            POSTGRES.getHost(),
            POSTGRES.getPort(),
            POSTGRES.getUsername(),
            POSTGRES.getPassword(),
            POSTGRES.getDatabaseName())
        .withPlugin(new SqlObjectPlugin())
        .withPlugin(new PostgresPlugin())
        .withMigration(Migration.before().withPath("marquez/db/migration"));
  }
}
