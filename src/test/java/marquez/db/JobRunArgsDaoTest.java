package marquez.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import marquez.db.fixtures.AppWithPostgresRule;
import marquez.service.models.RunArgs;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

public class JobRunArgsDaoTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final JobRunArgsDao jobRunArgsDao = APP.onDemand(JobRunArgsDao.class);
  final String hexDigest = "07d4ee12aac795ec60a549dce809c8105c541f0c4f3e7715686953f1702940e0";
  final String argsJson = "{'foo': 1}";
  final RunArgs runArgs = new RunArgs(hexDigest, argsJson, null);

  @After
  public void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM job_run_args;");
            });
  }

  @Test
  public void testFindByDigest() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  "INSERT INTO job_run_args(hex_digest, args_json) VALUES(?, ?)",
                  hexDigest,
                  argsJson);
            });
    RunArgs runArgsFound = jobRunArgsDao.findByDigest(hexDigest);
    assertEquals(runArgs.getHexDigest(), runArgsFound.getHexDigest());
    assertEquals(runArgs.getJson(), runArgsFound.getJson());
  }

  @Test
  public void testInsert() {
    jobRunArgsDao.insert(runArgs);
    RunArgs runArgsFound = jobRunArgsDao.findByDigest(hexDigest);
    assertEquals(runArgs.getHexDigest(), runArgsFound.getHexDigest());
    assertEquals(runArgs.getJson(), runArgsFound.getJson());
  }

  @Test
  public void testDigestExists() {
    jobRunArgsDao.insert(runArgs);
    assertTrue(jobRunArgsDao.digestExists(hexDigest));
    assertFalse(jobRunArgsDao.digestExists("non-existent"));
  }
}
