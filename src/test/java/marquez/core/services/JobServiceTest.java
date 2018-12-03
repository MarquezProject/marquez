package marquez.core.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class JobServiceTest {
  final String TEST_NS = "test_namespace";
  private static final JobDAO jobDAO = mock(JobDAO.class);
  private static final JobVersionDAO jobVersionDAO = mock(JobVersionDAO.class);
  private static final JobRunDAO jobRunDAO = mock(JobRunDAO.class);
  private static final RunArgsDAO runArgsDAO = mock(RunArgsDAO.class);
  private static final UUID namespaceID = UUID.randomUUID();

  JobService jobService;

  @Before
  public void setUp() {
    jobService = new JobService(jobDAO, jobVersionDAO, jobRunDAO, runArgsDAO);
  }

  @After
  public void tearDown() {
    reset(jobDAO);
    reset(jobVersionDAO);
    reset(jobRunDAO);
    reset(runArgsDAO);
  }

  @Test
  public void testGetAll_OK() throws Exception {
    List<Job> jobs = new ArrayList<Job>();
    jobs.add(Generator.genJob(namespaceID));
    jobs.add(Generator.genJob(namespaceID));
    when(jobDAO.findAllInNamespace(TEST_NS)).thenReturn(jobs);
    Assert.assertEquals(jobs, jobService.getAllJobsInNamespace(TEST_NS));
  }

  @Test
  public void testGetAll_NoJobs_OK() throws Exception {
    List<Job> jobs = new ArrayList<Job>();
    when(jobDAO.findAllInNamespace(TEST_NS)).thenReturn(jobs);
    Assert.assertEquals(jobs, jobService.getAllJobsInNamespace(TEST_NS));
  }

  @Test
  public void testGetAllVersions_OK() throws Exception {
    String jobName = "a job";
    UUID jobGuid = UUID.randomUUID();
    List<JobVersion> jobVersions = new ArrayList<JobVersion>();
    jobVersions.add(Generator.genJobVersion(jobGuid));
    jobVersions.add(Generator.genJobVersion(jobGuid));
    when(jobVersionDAO.find(TEST_NS, jobName)).thenReturn(jobVersions);
    Assert.assertEquals(jobVersions, jobService.getAllVersionsOfJob(TEST_NS, jobName));
  }

  @Test
  public void testGetAllVersions_NoVersions_OK() throws Exception {
    String jobName = "a job";
    List<JobVersion> jobVersions = new ArrayList<JobVersion>();
    when(jobVersionDAO.find(TEST_NS, jobName)).thenReturn(jobVersions);
    Assert.assertEquals(jobVersions, jobService.getAllVersionsOfJob(TEST_NS, jobName));
  }

  @Test(expected = UnexpectedException.class)
  public void testGetAllVersions_Exception() throws Exception {
    String jobName = "job";
    when(jobVersionDAO.find(TEST_NS, jobName)).thenThrow(UnableToExecuteStatementException.class);
    jobService.getAllVersionsOfJob(TEST_NS, jobName);
  }

  @Test
  public void testCreate_NewJob_OK() throws Exception {
    ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
    ArgumentCaptor<JobVersion> jobVersionCaptor = ArgumentCaptor.forClass(JobVersion.class);
    Job job = Generator.genJob(namespaceID);
    when(jobDAO.findByName(TEST_NS, job.getName())).thenReturn(null);
    jobService.createJob(TEST_NS, job);
    verify(jobDAO).insertJobAndVersion(jobCaptor.capture(), jobVersionCaptor.capture());
    assertEquals(job.getName(), jobCaptor.getValue().getName());
    assertEquals(job.getLocation(), jobVersionCaptor.getValue().getUri());
  }

  @Test
  public void testCreate_JobFound_OK() throws Exception {
    Job existingJob = Generator.genJob(namespaceID);
    Job newJob = Generator.cloneJob(existingJob);
    when(jobDAO.findByName(eq(TEST_NS), any(String.class))).thenReturn(existingJob);
    Job jobCreated = jobService.createJob(TEST_NS, newJob);
    verify(jobDAO, never()).insert(newJob);
    assertEquals(existingJob, jobCreated);
  }

  @Test
  public void testCreate_NewVersion_OK() throws Exception {
    ArgumentCaptor<JobVersion> jobVersionCaptor = ArgumentCaptor.forClass(JobVersion.class);
    Job existingJob = Generator.genJob(namespaceID);
    Job newJob = Generator.genJob(namespaceID);
    when(jobDAO.findByName(eq(TEST_NS), any(String.class))).thenReturn(existingJob);
    when(jobVersionDAO.findByVersion(any(UUID.class))).thenReturn(null);
    Job jobCreated = jobService.createJob(TEST_NS, newJob);
    verify(jobDAO, never()).insert(newJob);
    verify(jobVersionDAO).insert(jobVersionCaptor.capture());
    assertEquals(jobCreated.getGuid(), jobVersionCaptor.getValue().getJobGuid());
    assertEquals(newJob.getLocation(), jobVersionCaptor.getValue().getUri());
  }

  @Test
  public void testCreate_JobAndVersionFound_NoInsert_OK() throws Exception {
    Job existingJob = Generator.genJob(namespaceID);
    Job newJob = Generator.cloneJob(existingJob);
    UUID existingJobVersionID = JobService.computeVersion(existingJob);
    JobVersion existingJobVersion =
        new JobVersion(
            UUID.randomUUID(),
            existingJob.getGuid(),
            existingJob.getLocation(),
            existingJobVersionID,
            null,
            null,
            null);
    when(jobDAO.findByName(TEST_NS, existingJob.getName())).thenReturn(existingJob);
    when(jobVersionDAO.findByVersion(existingJobVersionID)).thenReturn(existingJobVersion);
    assertEquals(existingJob, jobService.createJob(TEST_NS, newJob));
    verify(jobDAO, never()).insert(newJob);
    verify(jobVersionDAO, never()).insert(any(JobVersion.class));
  }

  @Test(expected = UnexpectedException.class)
  public void testGet_JobDAOException() throws Exception {
    when(jobDAO.findByName(eq(TEST_NS), any(String.class)))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.getJob(TEST_NS, "a job");
  }

  @Test(expected = UnexpectedException.class)
  public void testCreate_JobDAOException() throws Exception {
    Job job = Generator.genJob(namespaceID);
    when(jobDAO.findByName(eq(TEST_NS), any(String.class)))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.createJob(TEST_NS, job);
  }

  @Test(expected = UnexpectedException.class)
  public void testCreateJobRun() throws Exception {
    String runArgsJson = "{'foo': 1}";
    String jobName = "a job";
    JobService jobService = spy(this.jobService);
    when(jobService.computeRunArgsDigest(runArgsJson)).thenThrow(NoSuchAlgorithmException.class);
    jobService.createJobRun(TEST_NS, jobName, runArgsJson, null, null);
  }

  @Test(expected = UnexpectedException.class)
  public void testCreate_JobVersionDAOException() throws Exception {
    Job job = Generator.genJob(namespaceID);
    UUID jobVersionID = JobService.computeVersion(job);
    when(jobDAO.findByName(TEST_NS, job.getName())).thenReturn(job);
    when(jobVersionDAO.findByVersion(jobVersionID))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.createJob(TEST_NS, job);
  }

  @Test(expected = UnexpectedException.class)
  public void testCreate_JobVersionInsertException() throws Exception {
    Job job = Generator.genJob(namespaceID);
    when(jobDAO.findByName(TEST_NS, job.getName())).thenReturn(job);
    when(jobVersionDAO.findByVersion(any(UUID.class))).thenReturn(null);
    doThrow(UnableToExecuteStatementException.class)
        .when(jobVersionDAO)
        .insert(any(JobVersion.class));
    jobService.createJob(TEST_NS, job);
  }

  @Test(expected = UnexpectedException.class)
  public void testGetAll_Exception() throws Exception {
    when(jobDAO.findAllInNamespace(TEST_NS)).thenThrow(UnableToExecuteStatementException.class);
    jobService.getAllJobsInNamespace(TEST_NS);
  }

  @Test
  public void testGetJobRun() throws Exception {
    JobRun jobRun = Generator.genJobRun();
    when(jobRunDAO.findJobRunById(jobRun.getGuid())).thenReturn(jobRun);
    assertEquals(Optional.ofNullable(jobRun), jobService.getJobRun(jobRun.getGuid()));
  }

  @Test(expected = UnexpectedException.class)
  public void testGetJobRun_SQLException() throws Exception {
    UUID jobRunID = UUID.randomUUID();
    when(jobRunDAO.findJobRunById(jobRunID)).thenThrow(UnableToExecuteStatementException.class);
    jobService.getJobRun(jobRunID);
  }

  @Test(expected = UnexpectedException.class)
  public void testGetVersionLatest_Exception() throws Exception {
    String jobName = "a job";
    when(jobVersionDAO.findLatest(TEST_NS, jobName))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.getLatestVersionOfJob(TEST_NS, jobName);
  }

  @Test(expected = UnexpectedException.class)
  public void testUpdateJobRunState_Exception() throws Exception {
    UUID jobRunID = UUID.randomUUID();
    JobRunState.State state = JobRunState.State.NEW;
    doThrow(UnableToExecuteStatementException.class)
        .when(jobRunDAO)
        .updateState(jobRunID, JobRunState.State.toInt(state));
    jobService.updateJobRunState(jobRunID, state);
  }
}
