package marquez.api;

import marquez.db.fixtures.AppWithPostgresRule;
import org.junit.ClassRule;

public class JobRunBaseTest {
  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();
}
