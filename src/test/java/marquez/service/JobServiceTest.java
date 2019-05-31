package marquez.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import marquez.common.models.NamespaceName;
import marquez.db.JobDao;
import marquez.db.JobRunArgsDao;
import marquez.db.JobRunDao;
import marquez.db.JobVersionDao;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Generator;
import marquez.service.models.Job;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;
import marquez.service.models.JobVersion;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

public class JobServiceTest {
  final String TEST_NS = "test_namespace";
  private static final int TEST_LIMIT = 20;
  private static final int TEST_OFFSET = 0;

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private JobDao jobDao;
  @Mock private JobVersionDao jobVersionDao;
  @Mock private JobRunDao jobRunDao;
  @Mock private JobRunArgsDao jobRunArgsDao;
  private static final UUID namespaceID = UUID.randomUUID();

  JobService jobService;

  @Before
  public void setUp() {
    jobService = new JobService(jobDao, jobVersionDao, jobRunDao, jobRunArgsDao);
  }

  private void assertJobFieldsMatch(Job job1, Job job2) {
    assertEquals(job1.getNamespaceGuid(), job2.getNamespaceGuid());
    assertEquals(job1.getGuid(), job2.getGuid());
    assertEquals(job1.getName(), job2.getName());
    assertEquals(job1.getLocation(), job2.getLocation());
    assertEquals(job1.getNamespaceGuid(), job2.getNamespaceGuid());
    assertEquals(job1.getInputDatasetUrns(), job2.getInputDatasetUrns());
    assertEquals(job1.getOutputDatasetUrns(), job2.getOutputDatasetUrns());
  }

  @Test
  public void testGetAll_OK() throws MarquezServiceException {
    List<Job> jobs = new ArrayList<Job>();
    jobs.add(Generator.genJob(namespaceID));
    jobs.add(Generator.genJob(namespaceID));
    when(jobDao.findAllInNamespace(TEST_NS)).thenReturn(jobs);
    Assert.assertEquals(jobs, jobService.getAllJobsInNamespace(TEST_NS));
  }

  @Test
  public void testGetAll_NoJobs_OK() throws MarquezServiceException {
    List<Job> jobs = new ArrayList<Job>();
    when(jobDao.findAllInNamespace(TEST_NS)).thenReturn(jobs);
    Assert.assertEquals(jobs, jobService.getAllJobsInNamespace(TEST_NS));
  }

  @Test
  public void testGetAllVersions_OK() throws MarquezServiceException {
    String jobName = "a job";
    UUID jobGuid = UUID.randomUUID();
    List<JobVersion> jobVersions = new ArrayList<JobVersion>();
    jobVersions.add(Generator.genJobVersion(jobGuid));
    jobVersions.add(Generator.genJobVersion(jobGuid));
    when(jobVersionDao.find(TEST_NS, jobName)).thenReturn(jobVersions);
    Assert.assertEquals(jobVersions, jobService.getAllVersionsOfJob(TEST_NS, jobName));
  }

  @Test
  public void testGetAllVersions_NoVersions_OK() throws MarquezServiceException {
    String jobName = "a job";
    List<JobVersion> jobVersions = new ArrayList<JobVersion>();
    when(jobVersionDao.find(TEST_NS, jobName)).thenReturn(jobVersions);
    Assert.assertEquals(jobVersions, jobService.getAllVersionsOfJob(TEST_NS, jobName));
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetAllVersions_Exception() throws MarquezServiceException {
    String jobName = "job";
    when(jobVersionDao.find(TEST_NS, jobName)).thenThrow(UnableToExecuteStatementException.class);
    jobService.getAllVersionsOfJob(TEST_NS, jobName);
  }

  @Test
  public void testCreate_NewJob_OK() throws MarquezServiceException {
    ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
    ArgumentCaptor<JobVersion> jobVersionCaptor = ArgumentCaptor.forClass(JobVersion.class);
    Job job = Generator.genJob(namespaceID);
    when(jobDao.findByName(TEST_NS, job.getName())).thenReturn(null);
    when(jobDao.findByID(any(UUID.class))).thenReturn(job);
    Job jobReturned = jobService.createJob(TEST_NS, job);
    verify(jobDao).insertJobAndVersion(jobCaptor.capture(), jobVersionCaptor.capture());
    assertEquals(job.getNamespaceGuid(), jobReturned.getNamespaceGuid());
    assertEquals(job.getName(), jobReturned.getName());
    assertEquals(job.getLocation(), jobReturned.getLocation());
    assertEquals(job.getNamespaceGuid(), jobReturned.getNamespaceGuid());
    assertEquals(job.getInputDatasetUrns(), jobReturned.getInputDatasetUrns());
    assertEquals(job.getOutputDatasetUrns(), jobReturned.getOutputDatasetUrns());
  }

  @Test
  public void testCreate_JobFound_OK() throws MarquezServiceException {
    Job existingJob = Generator.genJob(namespaceID);
    JobVersion existingJobVersion = Generator.genJobVersion(existingJob);
    Job newJob = Generator.cloneJob(existingJob);
    when(jobDao.findByName(eq(TEST_NS), any(String.class))).thenReturn(existingJob);
    when(jobVersionDao.findByVersion(any(UUID.class))).thenReturn(existingJobVersion);
    Job jobCreated = jobService.createJob(TEST_NS, newJob);
    verify(jobDao, never()).insert(newJob);
    assertNotNull(jobCreated);
    assertJobFieldsMatch(existingJob, jobCreated);
  }

  @Test
  public void testCreate_NewVersion_OK() throws MarquezServiceException {
    ArgumentCaptor<JobVersion> jobVersionCaptor = ArgumentCaptor.forClass(JobVersion.class);
    Job existingJob = Generator.genJob(namespaceID);
    Job newJob = Generator.genJob(namespaceID);
    when(jobDao.findByName(eq(TEST_NS), any(String.class))).thenReturn(existingJob);
    when(jobVersionDao.findByVersion(any(UUID.class))).thenReturn(null);
    when(jobDao.findByID(existingJob.getGuid())).thenReturn(existingJob);
    Job jobCreated = jobService.createJob(TEST_NS, newJob);
    verify(jobDao, never()).insert(newJob);
    verify(jobVersionDao).insert(jobVersionCaptor.capture());
    assertEquals(jobCreated.getGuid(), jobVersionCaptor.getValue().getJobGuid());
    assertEquals(newJob.getLocation(), jobVersionCaptor.getValue().getUri());
  }

  @Test
  public void testCreate_JobAndVersionFound_NoInsert_OK() throws MarquezServiceException {
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
    when(jobDao.findByName(TEST_NS, existingJob.getName())).thenReturn(existingJob);
    when(jobVersionDao.findByVersion(existingJobVersionID)).thenReturn(existingJobVersion);
    assertJobFieldsMatch(existingJob, jobService.createJob(TEST_NS, newJob));
    verify(jobDao, never()).insert(newJob);
    verify(jobVersionDao, never()).insert(any(JobVersion.class));
  }

  @Test(expected = MarquezServiceException.class)
  public void testGet_JobDaoException() throws MarquezServiceException {
    when(jobDao.findByName(eq(TEST_NS), any(String.class)))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.getJob(TEST_NS, "a job");
  }

  @Test(expected = MarquezServiceException.class)
  public void testCreate_JobDaoException() throws MarquezServiceException {
    Job job = Generator.genJob(namespaceID);
    when(jobDao.findByName(eq(TEST_NS), any(String.class)))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.createJob(TEST_NS, job);
  }

  @Test(expected = MarquezServiceException.class)
  public void testCreateJobRun() throws MarquezServiceException, NoSuchAlgorithmException {
    String runArgsJson = "{'foo': 1}";
    String jobName = "a job";
    JobService jobService = spy(this.jobService);
    when(jobService.computeRunArgsDigest(runArgsJson)).thenThrow(NoSuchAlgorithmException.class);
    jobService.createJobRun(TEST_NS, jobName, runArgsJson, null, null);
  }

  @Test(expected = MarquezServiceException.class)
  public void testCreate_JobVersionDaoException() throws MarquezServiceException {
    Job job = Generator.genJob(namespaceID);
    UUID jobVersionID = JobService.computeVersion(job);
    when(jobDao.findByName(TEST_NS, job.getName())).thenReturn(job);
    when(jobVersionDao.findByVersion(jobVersionID))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.createJob(TEST_NS, job);
  }

  @Test(expected = MarquezServiceException.class)
  public void testCreate_JobVersionInsertException() throws MarquezServiceException {
    Job job = Generator.genJob(namespaceID);
    when(jobDao.findByName(TEST_NS, job.getName())).thenReturn(job);
    when(jobVersionDao.findByVersion(any(UUID.class))).thenReturn(null);
    doThrow(UnableToExecuteStatementException.class)
        .when(jobVersionDao)
        .insert(any(JobVersion.class));
    jobService.createJob(TEST_NS, job);
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetAll_Exception() throws MarquezServiceException {
    when(jobDao.findAllInNamespace(TEST_NS)).thenThrow(UnableToExecuteStatementException.class);
    jobService.getAllJobsInNamespace(TEST_NS);
  }

  @Test
  public void testGetJobRun() throws MarquezServiceException {
    JobRun jobRun = Generator.genJobRun();
    when(jobRunDao.findJobRunById(jobRun.getGuid())).thenReturn(jobRun);
    assertEquals(Optional.ofNullable(jobRun), jobService.getJobRun(jobRun.getGuid()));
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetJobRun_SQLException() throws MarquezServiceException {
    UUID jobRunID = UUID.randomUUID();
    when(jobRunDao.findJobRunById(jobRunID)).thenThrow(UnableToExecuteStatementException.class);
    jobService.getJobRun(jobRunID);
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetVersionLatest_Exception() throws MarquezServiceException {
    String jobName = "a job";
    when(jobVersionDao.findLatest(TEST_NS, jobName))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.getLatestVersionOfJob(TEST_NS, jobName);
  }

  @Test(expected = MarquezServiceException.class)
  public void testUpdateJobRunState_Exception() throws MarquezServiceException {
    UUID jobRunID = UUID.randomUUID();
    JobRunState.State state = JobRunState.State.NEW;
    doThrow(UnableToExecuteStatementException.class)
        .when(jobRunDao)
        .updateState(jobRunID, JobRunState.State.toInt(state));
    jobService.updateJobRunState(jobRunID, state);
  }

  @Test
  public void testGetAllRunsOfJob_jobAndRunsFound() throws MarquezServiceException {
    Job job = Generator.genJob();
    NamespaceName jobNamespace = NamespaceName.fromString(TEST_NS);
    List<JobRun> jobRuns = new ArrayList<JobRun>();
    jobRuns.add(Generator.genJobRun());
    jobRuns.add(Generator.genJobRun());
    when(jobDao.findByName(jobNamespace.getValue(), job.getName())).thenReturn(job);
    when(jobRunDao.findAllByJobUuid(job.getGuid(), TEST_LIMIT, TEST_OFFSET)).thenReturn(jobRuns);
    List<JobRun> jobRunsFound =
        jobService.getAllRunsOfJob(jobNamespace, job.getName(), TEST_LIMIT, TEST_OFFSET);
    assertEquals(2, jobRunsFound.size());
  }

  @Test
  public void testGetAllRunsOfJob_jobNotFound() throws MarquezServiceException {
    Job job = Generator.genJob();
    NamespaceName jobNamespace = NamespaceName.fromString(TEST_NS);
    when(jobDao.findByName(jobNamespace.getValue(), job.getName())).thenReturn(null);
    assertEquals(
        0, jobService.getAllRunsOfJob(jobNamespace, job.getName(), TEST_LIMIT, TEST_OFFSET).size());
  }

  @Test
  public void testGetAllRunsOfJob_noRunsFound() throws MarquezServiceException {
    Job job = Generator.genJob();
    NamespaceName jobNamespace = NamespaceName.fromString(TEST_NS);
    List<JobRun> jobRuns = new ArrayList<JobRun>();
    when(jobDao.findByName(jobNamespace.getValue(), job.getName())).thenReturn(job);
    when(jobRunDao.findAllByJobUuid(job.getGuid(), TEST_LIMIT, TEST_OFFSET)).thenReturn(jobRuns);
    List<JobRun> jobRunsFound =
        jobService.getAllRunsOfJob(jobNamespace, job.getName(), TEST_LIMIT, TEST_OFFSET);
    assertEquals(0, jobRunsFound.size());
  }

  @Test(expected = MarquezServiceException.class)
  public void testGetAllRunsOfJob_exception() throws MarquezServiceException {
    Job job = Generator.genJob();
    NamespaceName jobNamespace = NamespaceName.fromString(TEST_NS);
    when(jobDao.findByName(jobNamespace.getValue(), job.getName())).thenReturn(job);
    when(jobRunDao.findAllByJobUuid(job.getGuid(), TEST_LIMIT, TEST_OFFSET))
        .thenThrow(UnableToExecuteStatementException.class);
    jobService.getAllRunsOfJob(jobNamespace, job.getName(), TEST_LIMIT, TEST_OFFSET);
  }
}
