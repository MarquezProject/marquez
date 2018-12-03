package marquez.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.core.exceptions.UnexpectedException;
import marquez.core.models.Generator;
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
    Job job = Generator.genJob(namespaceID);
    try {
      jobService.createJob(namespaceName, job);
      Optional<Job> jobFound = jobService.getJob(namespaceName, job.getName());
      assertTrue(jobFound.isPresent());
      assertEquals(job.getName(), jobFound.get().getName());
      List<JobVersion> versions = jobService.getAllVersionsOfJob(namespaceName, job.getName());
      assertEquals(1, versions.size());
      assertEquals(jobFound.get().getGuid(), versions.get(0).getJobGuid());
    } catch (UnexpectedException e) {
      fail("caught an unexpected exception");
    }
  }

  @Test
  public void testGetJob_JobFound() {
    Job job = Generator.genJob(namespaceID);
    try {
      jobService.createJob(namespaceName, job);
      Optional<Job> jobFound = jobService.getJob(namespaceName, job.getName());
      assertTrue(jobFound.isPresent());
      assertEquals(job.getName(), jobFound.get().getName());
    } catch (UnexpectedException e) {
      fail("caught unexpected exception");
    }
  }

  @Test
  public void testGetJob_JobNotFound() {
    Job job = Generator.genJob(namespaceID);
    Job job2 = Generator.genJob(namespaceID);
    try {
      jobService.createJob(namespaceName, job);
      Optional<Job> jobFound = jobService.getJob(namespaceName, job2.getName());
      assertFalse(jobFound.isPresent());
    } catch (UnexpectedException e) {
      fail("caught unexpected exception");
    }
  }

  @Test
  public void createAndUpdateJobRun() {
    Job job = Generator.genJob(namespaceID);
    try {
      String runArgsJson = "{'foo': 1}";
      jobService.createJob(namespaceName, job);
      JobRun jobRun =
          jobService.createJobRun(namespaceName, job.getName(), runArgsJson, null, null);
      Optional<JobRun> jobRunFound = jobService.getJobRun(jobRun.getGuid());
      assertTrue(jobRunFound.isPresent());
      assertEquals(jobRun.getGuid(), jobRunFound.get().getGuid());
      assertEquals(
          JobRunState.State.toInt(JobRunState.State.NEW),
          jobRunFound.get().getCurrentState().intValue());
      String argsHexDigest = jobRun.getRunArgsHexDigest();
      assertEquals(runArgsJson, runArgsDAO.findByDigest(argsHexDigest).getJson());
      jobService.updateJobRunState(jobRun.getGuid(), JobRunState.State.RUNNING);
    } catch (UnexpectedException e) {
      fail("caught an unexpected exception");
    }
  }

  @Test
  public void testGetJobRun_NotFound() {
    try {
      Optional<JobRun> jobRunFound = jobService.getJobRun(UUID.randomUUID());
      assertFalse(jobRunFound.isPresent());
    } catch (UnexpectedException e) {
      fail("caught an unexpected exception");
    }
  }
}
