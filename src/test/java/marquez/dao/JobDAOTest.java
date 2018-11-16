package marquez.dao;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import marquez.api.Job;
import marquez.dao.JobDAO;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobDAOTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  @Before
  public void setUp() {
  }

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