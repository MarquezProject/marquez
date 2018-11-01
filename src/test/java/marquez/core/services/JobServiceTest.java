package marquez.core.services;

import static org.mockito.Mockito.mock;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import marquez.api.Job;
import marquez.api.JobVersion;
import marquez.dao.JobDAO;
import marquez.dao.JobVersionDAO;
import marquez.dao.JobRunDAO;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;
import marquez.core.exceptions.JobServiceException;
import org.junit.Before;
import org.junit.After;


public class JobServiceTest {
    final String TEST_NS = "test_namespace";
    private static final JobDAO jobDAO = mock(JobDAO.class);
    private static final JobVersionDAO jobVersionDAO = mock(JobVersionDAO.class);
    private static final JobRunDAO jobRunDAO = mock(JobRunDAO.class);


    JobService jobService;
    

    @Before
    public void setUp() {
        jobService = new JobService(jobDAO, jobVersionDAO, jobRunDAO);
    }

    @After
    public void tearDown() {
        reset(jobDAO);
        reset(jobVersionDAO);
    }

    @Test
    public void testGetAll_OK() throws Exception {
        List<Job> jobs = new ArrayList<Job>();
        jobs.add(new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), "category", "a job", null));
        jobs.add(new Job(UUID.randomUUID(), "job2", "owner2", new Timestamp(new Date(0).getTime()), "category", "a job2", null));
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
}