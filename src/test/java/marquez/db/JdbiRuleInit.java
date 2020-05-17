package marquez.db;

import marquez.MarquezDb;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jdbi.v3.testing.JdbiRule;
import org.jdbi.v3.testing.Migration;

public class JdbiRuleInit {

  private static final MarquezDb DB = MarquezDb.create();

  static {
    DB.start();
  }

  public static JdbiRule init() {
    return JdbiRule.externalPostgres(
            DB.getHost(), DB.getPort(), DB.getUsername(), DB.getPassword(), DB.getDatabaseName())
        .withPlugin(new SqlObjectPlugin())
        .withPlugin(new PostgresPlugin())
        .withMigration(Migration.before().withPath("marquez/db/migration"));
  }
}
