package marquez.api;

import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.ClassRule;

public class JobRunBaseTest {
  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();
}
