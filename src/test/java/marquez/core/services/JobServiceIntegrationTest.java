package marquez.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.UUID;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.models.Job;
import marquez.core.models.JobRun;
import marquez.core.models.JobRunState;
import marquez.core.models.JobVersion;
import marquez.dao.JobDAO;
import marquez.dao.JobRunDAO;
import marquez.dao.JobVersionDAO;
import marquez.dao.RunArgsDAO;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobServiceIntegrationTest {
  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();
  final JobDAO jobDAO = APP.onDemand(JobDAO.class);
  final JobVersionDAO jobVersionDAO = APP.onDemand(JobVersionDAO.class);
  final JobRunDAO jobRunDAO = APP.onDemand(JobRunDAO.class);
  final RunArgsDAO runArgsDAO = APP.onDemand(RunArgsDAO.class);
  final UUID namespaceID = UUID.randomUUID();
  final String namespaceName = "job_service_test_ns";
  final String jobOwner = "Amaranta";
  JobService jobService = new JobService(jobDAO, jobVersionDAO, jobRunDAO, runArgsDAO);

  @Before
  public void setup() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  "INSERT INTO namespaces(guid, name, current_ownership) VALUES(?, ?, ?);",
                  namespaceID,
                  namespaceName,
                  jobOwner);
            });
  }

  @After
  public void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM job_run_states;");
              handle.execute("DELETE FROM job_runs;");
              handle.execute("DELETE FROM job_run_args;");
              handle.execute("DELETE FROM job_versions;");
              handle.execute("DELETE FROM jobs;");
              handle.execute("DELETE FROM namespaces;");
            });
  }

  @Test
  public void testCreate() {
    Job job = new Job(UUID.randomUUID(), "a job", "http://foo.bar", namespaceID);
    try {
      jobService.create(namespaceName, job);
    } catch (UnexpectedException e) {
      fail("caught an unexpected exception");
    }

    Job jobFound = jobDAO.findByID(job.getGuid());
    assertEquals(job.getGuid(), jobFound.getGuid());
    List<JobVersion> versions = jobVersionDAO.find(namespaceName, job.getName());
    assertEquals(1, versions.size());
    assertEquals(job.getGuid(), versions.get(0).getJobGuid());
  }

  @Test
  public void createAndUpdateJobRun() {
    Job job = new Job(UUID.randomUUID(), "a job", "http://foo.bar", namespaceID);
    try {
      String runArgsJson = "{'foo': 1}";
      jobService.create(namespaceName, job);
      JobRun jobRun = jobService.createJobRun(namespaceName, job.getName(), runArgsJson);
      JobRun jobRunFound = jobRunDAO.findJobRunById(jobRun.getGuid());
      assertEquals(jobRun.getGuid(), jobRunFound.getGuid());
      assertEquals(
          JobRunState.State.toInt(JobRunState.State.NEW), jobRunFound.getCurrentState().intValue());
      String argsHexDigest = jobRun.getRunArgsHexDigest();
      assertEquals(runArgsJson, runArgsDAO.findByDigest(argsHexDigest).getJson());
      jobService.updateJobRunState(jobRun.getGuid(), JobRunState.State.RUNNING);
    } catch (UnexpectedException e) {
      fail("caught an unexpected exception");
    }
  }
}
