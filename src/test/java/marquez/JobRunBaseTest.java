package marquez;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.core.models.JobRun;
import marquez.core.models.JobRunState;
import marquez.dao.JobRunDAO;
import marquez.dao.RunArgsDAO;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class JobRunBaseTest {
  protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  static final String TEST_NAMESPACE_GUID_STRING = UUID.randomUUID().toString();
  static final String TEST_JOB_GUID_STRING = UUID.randomUUID().toString();
  static final UUID TEST_JOB_RUN_VERSION_GUID = UUID.randomUUID();
  static final UUID TEST_JOB_RUN_VERSION_VERSION_ID = UUID.randomUUID();

  static final String TEST_JOB_RUN_ARGS = "--my-flag -Dkey=value";
  static final String TEST_JOB_RUN_ARGS_HEX_DIGEST = UUID.randomUUID().toString();

  public static final String NAMESPACE_NAME = "nsname";
  public static final String NAMESPACE_OWNER = "nsowner";
  public static final String NAMESPACE_DESC = "nsdesc";
  public static final String TEST_JOB_NAME = "testjob";

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  protected final RunArgsDAO runArgsDAO = APP.onDemand(RunArgsDAO.class);
  protected final JobRunDAO jobRunDAO = APP.onDemand(JobRunDAO.class);

  protected marquez.core.models.JobRun newJobRun;

  @BeforeClass
  public static void setUp() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "insert into namespaces (guid, name, description, current_ownership) values "
                          + "('%s', '%s', '%s', '%s');",
                      TEST_NAMESPACE_GUID_STRING, NAMESPACE_NAME, NAMESPACE_DESC, NAMESPACE_OWNER));
              handle.execute(
                  format(
                      "insert into jobs (guid, name, description, namespace_guid) values "
                          + "('%s', '%s', 'fake job for reference', '%s');",
                      TEST_JOB_GUID_STRING, TEST_JOB_NAME, TEST_NAMESPACE_GUID_STRING));
              handle.execute(
                  format(
                      "insert into job_versions (guid, input_dataset, output_dataset, job_guid, uri, version) values "
                          + "('%s', 'input_set1', 'output_set1', '%s', 'http://wework.github.com', '%s' );",
                      TEST_JOB_RUN_VERSION_GUID,
                      TEST_JOB_GUID_STRING,
                      TEST_JOB_RUN_VERSION_VERSION_ID));
              handle.execute(
                  format(
                      "update jobs set current_version_guid = '%s' where guid = '%s';",
                      TEST_JOB_RUN_VERSION_GUID, TEST_JOB_GUID_STRING));
            });
  }

  @AfterClass
  public static void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "DELETE from job_versions where guid in (select job_versions.guid as guid from jobs inner join job_versions on job_versions.job_guid=jobs.guid and jobs.namespace_guid='%s')",
                      TEST_NAMESPACE_GUID_STRING));
              handle.execute(
                  format(
                      "delete from jobs where namespace_guid = '%s'", TEST_NAMESPACE_GUID_STRING));
              handle.execute(
                  format("delete from namespaces where guid = '%s'", TEST_NAMESPACE_GUID_STRING));
            });
  }

  @Before
  public void setUpNewJobRun() {
    newJobRun =
        new marquez.core.models.JobRun(
            UUID.randomUUID(),
            JobRunState.State.toInt(JobRunState.State.NEW),
            TEST_JOB_RUN_VERSION_GUID,
            TEST_JOB_RUN_ARGS_HEX_DIGEST,
            TEST_JOB_RUN_ARGS,
            null,
            null,
            Timestamp.from(Instant.now()));

    marquez.core.models.RunArgs sampleRunArgs =
        new marquez.core.models.RunArgs(TEST_JOB_RUN_ARGS_HEX_DIGEST, "{'a':'1', 'b':'2'}", null);
    if (!runArgsDAO.digestExists(TEST_JOB_RUN_ARGS_HEX_DIGEST)) {
      runArgsDAO.insert(sampleRunArgs);
    }
    jobRunDAO.insert(newJobRun);
    JobRun result = jobRunDAO.findJobRunById(newJobRun.getGuid());
    result.getGuid();
  }

  @After
  public void cleanup() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "delete from job_run_states where job_run_guid = '%s'", newJobRun.getGuid()));
              handle.execute(format("delete from job_runs where guid = '%s'", newJobRun.getGuid()));
            });
  }
}
