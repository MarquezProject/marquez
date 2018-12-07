package marquez.api;

import static java.lang.String.format;
import static marquez.api.JobRunState.State.toInt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.util.UUID;
import marquez.dao.deprecated.JobRunDAO;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class JobRunBaseTest {
  protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  static final String TEST_JOB_GUID = UUID.randomUUID().toString();
  static final String TEST_JOB_RUN_VERSION_GUID = UUID.randomUUID().toString();
  static final String TEST_JOB_RUN_DEFINITION_GUID = UUID.randomUUID().toString();

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final JobRunDAO jobRunDAO = APP.onDemand(JobRunDAO.class);

  protected static JobRun NEW_JOB_RUN =
      new JobRun(
          UUID.randomUUID(),
          null,
          null,
          UUID.fromString(TEST_JOB_RUN_DEFINITION_GUID),
          toInt(JobRunState.State.NEW));

  @BeforeClass
  public static void setUp() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "insert into jobs (guid, name, category, description) values "
                          + "('%s', 'my_job', 'testing', 'fake job for reference');",
                      TEST_JOB_GUID));
              handle.execute(
                  format(
                      "insert into job_versions (guid, input_dataset, output_dataset, job_guid) values "
                          + "('%s', 'input_set1', 'output_set1', '%s');",
                      TEST_JOB_RUN_VERSION_GUID, TEST_JOB_GUID));
              handle.execute(
                  format(
                      "insert into job_run_definitions (guid, job_version_guid, run_args_json, content_hash, nominal_start_time, nominal_end_time) values "
                          + "('%s', '%s', '--my-favorite-flag', '6706da44-61d9-454d-a6c3-b9fea5a92a43', 5000, 10000);",
                      TEST_JOB_RUN_DEFINITION_GUID, TEST_JOB_RUN_VERSION_GUID));
            });
  }

  @AfterClass
  public static void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "delete from job_run_definitions where guid = '%s'",
                      TEST_JOB_RUN_DEFINITION_GUID));
              handle.execute(
                  format("delete from job_versions where guid = '%s'", TEST_JOB_RUN_VERSION_GUID));
              handle.execute(format("delete from jobs where guid = '%s'", TEST_JOB_GUID));
            });
  }

  @Before
  public void setUpNewJobRun() {
    jobRunDAO.insert(NEW_JOB_RUN);
  }

  @After
  public void cleanup() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "delete from job_run_states where job_run_guid = '%s'",
                      NEW_JOB_RUN.getGuid()));
              handle.execute(
                  format("delete from job_runs where guid = '%s'", NEW_JOB_RUN.getGuid()));
            });
  }
}
