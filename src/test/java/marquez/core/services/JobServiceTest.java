package marquez.core.services;

import static org.mockito.Mockito.mock;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import marquez.core.models.Job;
import marquez.core.models.JobVersion;
import marquez.dao.JobDAO;
import marquez.dao.JobVersionDAO;
import marquez.dao.JobRunDAO;
import marquez.dao.RunArgsDAO;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;
import marquez.core.exceptions.JobServiceException;
import org.junit.Before;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.After;


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
        jobs.add(new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), "category", "a job", null, namespaceID));
        jobs.add(new Job(UUID.randomUUID(), "job2", "owner2", new Timestamp(new Date(0).getTime()), "category", "a job2", null, namespaceID));
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
        jobVersions.add(new JobVersion(UUID.randomUUID(), jobGuid, "git://foo.com/v1.git", UUID.randomUUID(), null, createdAt, updatedAt));
        createdAt = updatedAt = new Timestamp(new Date(1).getTime());
        jobVersions.add(new JobVersion(UUID.randomUUID(), jobGuid, "git://foo.com/v2.git", UUID.randomUUID(), null, createdAt, updatedAt));

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

    @Test(expected=JobServiceException.class)
    public void testGetAllVersions_Exception() throws Exception {
        String jobName = "job";
        when(jobVersionDAO.find(TEST_NS, jobName)).thenThrow(UnableToExecuteStatementException.class);
        jobService.getAllVersions(TEST_NS, jobName);
    }

    @Test
    public void testCreate_NewJob_OK() throws Exception {
        Job job = new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        when(jobDAO.findByName("job")).thenReturn(null);
        jobService.create(TEST_NS, job);
        verify(jobDAO).insert(job);
    }

    @Test
    public void testCreate_JobFound_OK() throws Exception {
        Job existingJob = new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        Job newJob = new Job(null, "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        when(jobDAO.findByName("job")).thenReturn(existingJob);
        Job jobCreated = jobService.create(TEST_NS, newJob);
        verify(jobDAO, never()).insert(newJob);
        verify(jobVersionDAO).findByVersion(JobService.computeVersion(existingJob));
        assertEquals(existingJob, jobCreated);
    }

    @Test
    public void testCreate_NewVersion_OK() throws Exception {
        Job existingJob = new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        Job newJob = new Job(null, "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        UUID existingJobVersion = JobService.computeVersion(existingJob);
        when(jobDAO.findByName("job")).thenReturn(existingJob);
        when(jobVersionDAO.findByVersion(existingJobVersion)).thenReturn(null);
        jobService.create(TEST_NS, newJob);
        verify(jobDAO, never()).insert(newJob);
        verify(jobVersionDAO).insert(any(UUID.class), eq(existingJobVersion), eq(existingJob.getGuid()), eq(existingJob.getLocation()));
    }

    @Test
    public void testCreate_VersionFound_OK() throws Exception {
        Job existingJob = new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        Job newJob = new Job(null, "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        UUID existingJobVersionID = JobService.computeVersion(existingJob);
        JobVersion existingJobVersion = new JobVersion(UUID.randomUUID(), existingJob.getGuid(), existingJob.getLocation(), existingJobVersionID, null, timeZero, timeZero);
        when(jobDAO.findByName("job")).thenReturn(existingJob);
        when(jobVersionDAO.findByVersion(existingJobVersionID)).thenReturn(existingJobVersion);
        assertEquals(existingJob, jobService.create(TEST_NS, newJob));
        verify(jobDAO, never()).insert(newJob);
        verify(jobVersionDAO, never()).insert(any(UUID.class), any(UUID.class), any(UUID.class), any(String.class));
    }
 
    @Test(expected=JobServiceException.class)
    public void testCreate_JobDAOException() throws Exception {
        Job job = new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        when(jobDAO.findByName("job")).thenThrow(UnableToExecuteStatementException.class);
        jobService.create(TEST_NS, job);
    } 

    @Test(expected=JobServiceException.class)
    public void testCreate_JobVersionDAOException() throws Exception {
        Job job = new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), null, null, "http://foo.com", namespaceID);
        UUID jobVersionID = JobService.computeVersion(job);
        when(jobDAO.findByName("job")).thenReturn(job);
        when(jobVersionDAO.findByVersion(jobVersionID)).thenThrow(UnableToExecuteStatementException.class);
        jobService.create(TEST_NS, job);
    } 

    @Test(expected=JobServiceException.class)
    public void testGetAll_Exception() throws Exception {
        when(jobDAO.findAllInNamespace(TEST_NS)).thenThrow(UnableToExecuteStatementException.class);
        jobService.getAll(TEST_NS);
    }

}