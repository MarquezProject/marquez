package marquez.dao.deprecated;

import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;

public class JobDAOTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  @Before
  public void setUp() {}

  @After
  public void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM jobs;");
              handle.execute("DELETE FROM job_versions;");
              handle.execute("DELETE FROM job_runs;");
              handle.execute("DELETE FROM owners;");
            });
  }
}
