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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
  private static final Timestamp timeZero = new Timestamp(new Date(0).getTime());
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

    Assert.assertEquals(jobs, jobService.getAll(TEST_NS));
  }

  @Test
  public void testGetAll_NoJobs_OK() throws Exception {
    List<Job> jobs = new ArrayList<Job>();
    when(jobDAO.findAllInNamespace(TEST_NS)).thenReturn(jobs);
    Assert.assertEquals(jobs, jobService.getAll(TEST_NS));
  }

  @Test
  public void testGetAllVersions_OK() throws Exception {
    String jobName = "a job";
    UUID jobGuid = UUID.randomUUID();
    Timestamp createdAt, updatedAt;

    List<JobVersion> jobVersions = new ArrayList<JobVersion>();
    createdAt = updatedAt = new Timestamp(new Date(0).getTime());
    jobVersions.add(
        new JobVersion(
            UUID.randomUUID(),
            jobGuid,
            "git://foo.com/v1.git",
            UUID.randomUUID(),
            null,
            createdAt,
            updatedAt));
    createdAt = updatedAt = new Timestamp(new Date(1).getTime());
    jobVersions.add(
        new JobVersion(
            UUID.randomUUID(),
            jobGuid,
            "git://foo.com/v2.git",
            UUID.randomUUID(),
            null,
            createdAt,
            updatedAt));

    when(jobVersionDAO.find(TEST_NS, jobName)).thenReturn(jobVersions);
    Assert.assertEquals(jobVersions, jobService.getAllVersions(TEST_NS, jobName));
  }

  @Test
  public void testGetAllVersions_NoVersions_OK() throws Exception {
    String jobName = "a job";
    List<JobVersion> jobVersions = new ArrayList<JobVersion>();
    when(jobVersionDAO.find(TEST_NS, jobName)).thenReturn(jobVersions);
    Assert.assertEquals(jobVersions, jobService.getAllVersions(TEST_NS, jobName));
  }

  @Test(expected = UnexpectedException.class)
  public void testGetAllVersions_Exception() throws Exception {
    String jobName = "job";
    when(jobVersionDAO.find(TEST_NS, jobName)).thenThrow(UnableToExecuteStatementException.class);
    jobService.getAllVersions(TEST_NS, jobName);
  }

  @Test
  public void testCreate_NewJob_OK() throws Exception {
    Job job = Generator.genJob(namespaceID);
    when(jobDAO.findByName(TEST_NS, "job")).thenReturn(null);
    jobService.create(TEST_NS, job);
    verify(jobDAO).insertJobAndVersion(eq(job), any(JobVersion.class));
  }

  @Test
  public void testCreate_JobFound_OK() throws Exception {
    Job existingJob = new Job(UUID.randomUUID(), "job", null, "http://foo.com", namespaceID);
    Job newJob = new Job(null, "job", null, "http://foo.com", namespaceID);
    when(jobDAO.findByName(TEST_NS, "job")).thenReturn(existingJob);
    Job jobCreated = jobService.create(TEST_NS, newJob);
    verify(jobDAO, never()).insert(newJob);
    verify(jobVersionDAO).findByVersion(JobService.computeVersion(existingJob));
    assertEquals(existingJob, jobCreated);
  }

  @Test
  public void testCreate_NewVersion_OK() throws Exception {
    ArgumentCaptor<JobVersion> jobVersionCaptor = ArgumentCaptor.forClass(JobVersion.class);
    Job existingJob = new Job(UUID.randomUUID(), "job", null, "http://foo.com/1", namespaceID);
    Job newJob = new Job(null, "job", null, "http://foo.com/2", namespaceID);
    when(jobDAO.findByName(TEST_NS, "job")).thenReturn(existingJob);
    when(jobVersionDAO.findByVersion(any(UUID.class))).thenReturn(null);
    jobService.create(TEST_NS, newJob);
    verify(jobDAO, never()).insert(newJob);
    verify(jobVersionDAO).insert(jobVersionCaptor.capture());
    assertEquals(newJob.getGuid(), jobVersionCaptor.getValue().getJobGuid());
    assertEquals(newJob.getLocation(), jobVersionCaptor.getValue().getUri());
  }

  @Test
  public void testCreate_JobAndVersionFound_NoInsert_OK() throws Exception {
    Job existingJob = new Job(UUID.randomUUID(), "job", null, "http://foo.com", namespaceID);
    Job newJob = new Job(null, "job", null, "http://foo.com", namespaceID);
    UUID existingJobVersionID = JobService.computeVersion(existingJob);
    JobVersion existingJobVersion =
        new JobVersion(
            UUID.randomUUID(),
            existingJob.getGuid(),
            existingJob.getLocation(),
            existingJobVersionID);
    when(jobDAO.findByName(TEST_NS, "job")).thenReturn(existingJob);
    when(jobVersionDAO.findByVersion(existingJobVersionID)).thenReturn(existingJobVersion);
    assertEquals(existingJob, jobService.create(TEST_NS, newJob));
    verify(jobDAO, never()).insert(newJob);
    verify(jobVersionDAO, never()).insert(any(JobVersion.class));
  }

  @Test(expected = UnexpectedException.class)
  public void testCreate_JobDAOException() throws Exception {
    Job job = new Job(UUID.randomUUID(), "job", null, "http://foo.com", namespaceID);
    when(jobDAO.findByName(TEST_NS, "job")).thenThrow(UnableToExecuteStatementException.class);
    jobService.create(TEST_NS, job);
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
    Job job = new Job(UUID.randomUUID(), "job", null, "http://foo.com", namespaceID);
    UUID jobVersionID = JobService.computeVersion(job);
    when(jobDAO.findByName(TEST_NS, "job")).thenReturn(job);
    when(jobVersionDAO.findByVersion(jobVersionID))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.create(TEST_NS, job);
  }

  @Test(expected = UnexpectedException.class)
  public void testCreate_JobVersionInsertException() throws Exception {
    Job job = new Job(UUID.randomUUID(), "job", null, "http://foo.com", namespaceID);
    when(jobDAO.findByName(TEST_NS, job.getName())).thenReturn(job);
    when(jobVersionDAO.findByVersion(any(UUID.class))).thenReturn(null);
    doThrow(UnableToExecuteStatementException.class)
        .when(jobVersionDAO)
        .insert(any(JobVersion.class));
    jobService.create(TEST_NS, job);
  }

  @Test(expected = UnexpectedException.class)
  public void testGetAll_Exception() throws Exception {
    when(jobDAO.findAllInNamespace(TEST_NS)).thenThrow(UnableToExecuteStatementException.class);
    jobService.getAll(TEST_NS);
  }

  @Test
  public void testGetJobRun() throws Exception {
    JobRun jobRun =
        new JobRun(
            UUID.randomUUID(),
            JobRunState.State.toInt(JobRunState.State.NEW),
            UUID.randomUUID(),
            "abc123",
            "{'foo': 1}",
            null,
            null,
            null,
            null);
    when(jobRunDAO.findJobRunById(jobRun.getGuid())).thenReturn(jobRun);
    assertEquals(jobRun, jobService.getJobRun(jobRun.getGuid()));
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
    jobService.getVersionLatest(TEST_NS, jobName);
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
