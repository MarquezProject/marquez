/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.db;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.UUID;
import marquez.api.resources.JobRunBaseTest;
import marquez.common.models.NamespaceName;
import marquez.service.JobService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Generator;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;
import marquez.service.models.Namespace;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.statement.Query;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class JobRunDaoTest extends JobRunBaseTest {
  protected static String NAMESPACE_NAME;
  protected static String CREATED_JOB_NAME;
  protected static UUID CREATED_JOB_RUN_UUID;

  protected static UUID CREATED_NAMESPACE_UUID;

  protected static final String JOB_RUN_ARGS = "{'key': 'value'}";

  protected static final NamespaceDao namespaceDao = APP.onDemand(NamespaceDao.class);
  protected static final JobDao jobDao = APP.onDemand(JobDao.class);
  protected static final JobVersionDao jobVersionDao = APP.onDemand(JobVersionDao.class);
  protected static final JobRunDao jobRunDao = APP.onDemand(JobRunDao.class);
  protected static final JobRunStateDao jobRunStateDao = APP.onDemand(JobRunStateDao.class);
  protected static final JobRunArgsDao jobRunArgsDao = APP.onDemand(JobRunArgsDao.class);

  protected static final int TEST_OFFSET = 0;
  protected static final int TEST_LIMIT = 10;

  protected static NamespaceService namespaceService;
  protected static final JobService jobService =
      new JobService(jobDao, jobVersionDao, jobRunDao, jobRunArgsDao);

  @BeforeClass
  public static void setUpOnce() throws MarquezServiceException {
    namespaceService = new NamespaceService(namespaceDao);
    APP.getJDBI()
        .registerRowMapper(
            JobRunState.class,
            (rs, ctx) ->
                new JobRunState(
                    UUID.fromString(rs.getString("guid")),
                    rs.getTimestamp("transitioned_at").toInstant(),
                    UUID.fromString(rs.getString("job_run_guid")),
                    JobRunState.State.fromInt(rs.getInt("state"))));

    Namespace generatedNamespace = namespaceService.createOrUpdate(Generator.genNamespace());
    NAMESPACE_NAME = generatedNamespace.getName();
    CREATED_NAMESPACE_UUID = generatedNamespace.getGuid();

    marquez.service.models.Job job = Generator.genJob(generatedNamespace.getGuid());
    marquez.service.models.Job createdJob = jobService.createJob(NAMESPACE_NAME, job);

    CREATED_JOB_NAME = createdJob.getName();
    CREATED_JOB_RUN_UUID = createdJob.getNamespaceGuid();
  }

  @Before
  public void createJobRun() throws MarquezServiceException {
    JobRun createdJobRun =
        jobService.createJobRun(NAMESPACE_NAME, CREATED_JOB_NAME, JOB_RUN_ARGS, null, null);
    CREATED_JOB_RUN_UUID = createdJobRun.getGuid();
  }

  @Test
  public void testJobRunCreationCreatesJobRunState() {
    JobRunState returnedJobRunState = getLatestJobRunStateForJobId(CREATED_JOB_RUN_UUID);
    assertEquals(JobRunState.State.NEW, returnedJobRunState.getState());
  }

  @Test
  public void testJobRunUpdateCreatesJobRunState() {
    jobRunDao.updateState(CREATED_JOB_RUN_UUID, JobRunState.State.toInt(JobRunState.State.RUNNING));

    JobRunState returnedJobRunState = getLatestJobRunStateForJobId(CREATED_JOB_RUN_UUID);
    assertEquals(JobRunState.State.RUNNING, returnedJobRunState.getState());
  }

  @Test
  public void testJobRunGetter() {
    JobRun returnedJobRun = jobRunDao.findJobRunById(CREATED_JOB_RUN_UUID);
    assertNull(returnedJobRun.getNominalStartTime());
    assertNull(returnedJobRun.getNominalEndTime());
    assertEquals(
        JobRunState.State.NEW, JobRunState.State.fromInt(returnedJobRun.getCurrentState()));
  }

  @Test
  public void testLatestGetJobRunStateForJobId() {
    assertThat(jobRunStateDao.findByLatestJobRun(CREATED_JOB_RUN_UUID))
        .isEqualTo(getLatestJobRunStateForJobId(CREATED_JOB_RUN_UUID));
  }

  @Test
  public void testFindAllByJobUuid() throws MarquezServiceException {
    List<JobRun> jobRuns =
        jobService.getAllRunsOfJob(
            NamespaceName.of(NAMESPACE_NAME), CREATED_JOB_NAME, TEST_LIMIT, TEST_OFFSET);
    assertEquals(jobRuns.size(), 1);
    jobService.createJobRun(NAMESPACE_NAME, CREATED_JOB_NAME, JOB_RUN_ARGS, null, null);
    jobRuns =
        jobService.getAllRunsOfJob(
            NamespaceName.of(NAMESPACE_NAME), CREATED_JOB_NAME, TEST_LIMIT, TEST_OFFSET);
    assertEquals(jobRuns.size(), 2);
  }

  @Test
  public void testFindAllByJobUuid_withLimit() throws MarquezServiceException {
    int singleJobLimit = 1;
    jobService.createJobRun(NAMESPACE_NAME, CREATED_JOB_NAME, JOB_RUN_ARGS, null, null);
    List<JobRun> jobRuns =
        jobService.getAllRunsOfJob(
            NamespaceName.of(NAMESPACE_NAME), CREATED_JOB_NAME, singleJobLimit, TEST_OFFSET);
    assertEquals(jobRuns.size(), 1);
  }

  @Test
  public void testFindAllByJobUuid_JobDoesntExist() throws MarquezServiceException {
    String nonexistentJobName = "this_job_doesnt_exist";
    List<JobRun> jobRuns =
        jobService.getAllRunsOfJob(
            NamespaceName.of(NAMESPACE_NAME), nonexistentJobName, TEST_LIMIT, TEST_OFFSET);
    assertEquals(jobRuns.size(), 0);
  }

  @Test
  public void testFindAllByJobUuid_NoRuns() throws MarquezServiceException {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  format(
                      "DELETE FROM job_run_states WHERE job_run_guid = '%s'",
                      CREATED_JOB_RUN_UUID));
              handle.execute(
                  format("DELETE FROM job_runs WHERE guid = '%s'", CREATED_JOB_RUN_UUID));
            });
    List<JobRun> jobRuns =
        jobService.getAllRunsOfJob(
            NamespaceName.of(NAMESPACE_NAME), CREATED_JOB_NAME, TEST_LIMIT, TEST_OFFSET);
    assertEquals(jobRuns.size(), 0);
  }

  private JobRunState getLatestJobRunStateForJobId(UUID jobRunId) {
    Handle handle = APP.getJDBI().open();
    Query qr =
        handle.select(
            format(
                "SELECT * FROM job_run_states WHERE job_run_guid = '%s' ORDER by transitioned_at DESC",
                jobRunId.toString()));
    return qr.mapTo(JobRunState.class).stream().findFirst().get();
  }

  @After
  public void cleanup() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(format("delete from job_run_states;", CREATED_JOB_RUN_UUID));
              handle.execute(format("delete from job_runs;", CREATED_JOB_RUN_UUID));
            });
  }

  @AfterClass
  public static void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("delete from job_versions");
              handle.execute("delete from jobs");
              handle.execute("delete from namespaces");
            });
  }
}
