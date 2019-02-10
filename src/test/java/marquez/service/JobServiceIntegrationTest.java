package marquez.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.db.JobDao;
import marquez.db.JobRunArgsDao;
import marquez.db.JobRunDao;
import marquez.db.JobVersionDao;
import marquez.db.fixtures.AppWithPostgresRule;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.Generator;
import marquez.service.models.Job;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;
import marquez.service.models.JobVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobServiceIntegrationTest {
  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();
  final JobDao jobDao = APP.onDemand(JobDao.class);
  final JobVersionDao jobVersionDao = APP.onDemand(JobVersionDao.class);
  final JobRunDao jobRunDao = APP.onDemand(JobRunDao.class);
  final JobRunArgsDao jobRunArgsDao = APP.onDemand(JobRunArgsDao.class);
  final UUID namespaceID = UUID.randomUUID();
  final String namespaceName = "job_service_test_ns";
  final String jobOwner = "Amaranta";
  JobService jobService = new JobService(jobDao, jobVersionDao, jobRunDao, jobRunArgsDao);

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
  public void testCreate() throws UnexpectedException {
    Job job = Generator.genJob(namespaceID);
    Job jobCreateRet = jobService.createJob(namespaceName, job);
    assertNotNull(jobCreateRet.getCreatedAt());
    Optional<Job> jobGetRet = jobService.getJob(namespaceName, job.getName());
    assertTrue(jobGetRet.isPresent());
    assertEquals(jobGetRet.get(), jobCreateRet);
    assertEquals(job.getName(), jobGetRet.get().getName());
    List<JobVersion> versions = jobService.getAllVersionsOfJob(namespaceName, job.getName());
    assertEquals(1, versions.size());
    assertEquals(jobGetRet.get().getGuid(), versions.get(0).getJobGuid());
  }

  @Test
  public void testCreateNewVersion() throws UnexpectedException {
    Job job = Generator.genJob(namespaceID);
    jobService.createJob(namespaceName, job);
    Job jobWithNewLoc =
        new Job(
            null,
            job.getName(),
            job.getLocation() + "/new",
            job.getNamespaceGuid(),
            job.getDescription(),
            job.getInputDatasetUrns(),
            job.getOutputDatasetUrns());
    Job jobCreateRet =
        jobService.createJob(namespaceName, jobWithNewLoc); // should create new version implicitly
    Optional<Job> jobGetRet = jobService.getJob(namespaceName, job.getName());
    assertTrue(jobGetRet.isPresent());
    assertEquals(jobGetRet.get(), jobCreateRet);
    assertEquals(job.getName(), jobGetRet.get().getName());
    assertEquals(jobCreateRet, jobGetRet.get());
    List<JobVersion> versions = jobService.getAllVersionsOfJob(namespaceName, job.getName());
    assertEquals(jobGetRet.get().getGuid(), versions.get(0).getJobGuid());
    assertEquals(jobGetRet.get().getGuid(), versions.get(1).getJobGuid());
  }

  @Test
  public void testGetJob_JobFound() throws UnexpectedException {
    Job job = Generator.genJob(namespaceID);
    Job jobCreateRet = jobService.createJob(namespaceName, job);
    Optional<Job> jobGetRet = jobService.getJob(namespaceName, job.getName());
    assertTrue(jobGetRet.isPresent());
    assertEquals(job.getName(), jobGetRet.get().getName());
    assertEquals(jobGetRet.get(), jobCreateRet);
  }

  @Test
  public void testGetJob_JobNotFound() throws UnexpectedException {
    Job job = Generator.genJob(namespaceID);
    Job job2 = Generator.genJob(namespaceID);
    jobService.createJob(namespaceName, job);
    Optional<Job> jobFound = jobService.getJob(namespaceName, job2.getName());
    assertFalse(jobFound.isPresent());
  }

  @Test
  public void createAndUpdateJobRun() throws UnexpectedException {
    Job job = Generator.genJob(namespaceID);
    String runArgsJson = "{'foo': 1}";
    jobService.createJob(namespaceName, job);
    JobRun jobRun = jobService.createJobRun(namespaceName, job.getName(), runArgsJson, null, null);
    Optional<JobRun> jobRunFound = jobService.getJobRun(jobRun.getGuid());
    assertTrue(jobRunFound.isPresent());
    assertEquals(jobRun.getGuid(), jobRunFound.get().getGuid());
    assertEquals(
        JobRunState.State.toInt(JobRunState.State.NEW),
        jobRunFound.get().getCurrentState().intValue());
    String argsHexDigest = jobRun.getRunArgsHexDigest();
    assertEquals(runArgsJson, jobRunArgsDao.findByDigest(argsHexDigest).getJson());
    jobService.updateJobRunState(jobRun.getGuid(), JobRunState.State.RUNNING);
  }

  @Test
  public void testCreateJobRun_NullArgs() throws UnexpectedException {
    Job job = Generator.genJob(namespaceID);
    String nullRunArgsJson = null;
    jobService.createJob(namespaceName, job);
    JobRun jobRun =
        jobService.createJobRun(namespaceName, job.getName(), nullRunArgsJson, null, null);
    Optional<JobRun> jobRunFound = jobService.getJobRun(jobRun.getGuid());
    assertTrue(jobRunFound.isPresent());
    assertNull(jobRun.getRunArgsHexDigest());
    assertNull(jobRun.getRunArgs());
  }

  @Test
  public void testCreateJobRun_NonNullArgs() throws UnexpectedException {
    Job job = Generator.genJob(namespaceID);
    String argsJson = "{'foo': 1}";
    jobService.createJob(namespaceName, job);
    JobRun jobRun = jobService.createJobRun(namespaceName, job.getName(), argsJson, null, null);
    Optional<JobRun> jobRunFound = jobService.getJobRun(jobRun.getGuid());
    assertTrue(jobRunFound.isPresent());
    assertNotNull(jobRun.getRunArgsHexDigest());
    assertEquals(argsJson, jobRun.getRunArgs());
  }

  @Test
  public void testGetJobRun_NotFound() throws UnexpectedException {
    Optional<JobRun> jobRunFound = jobService.getJobRun(UUID.randomUUID());
    assertFalse(jobRunFound.isPresent());
  }
}
