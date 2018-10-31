package marquez.core.services;

import static org.mockito.Mockito.mock;
import org.junit.Test;
import org.junit.Assert;

import marquez.api.Job;
import marquez.dao.JobDAO;
import marquez.dao.JobVersionDAO;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Date;


import static org.mockito.Mockito.when;


public class JobServiceTest {
    final String TEST_NS = "test_namespace";

    @Test
    public void testGetAll() throws Exception {
        List<Job> jobs = new ArrayList<Job>();
        jobs.add(new Job(UUID.randomUUID(), "job", "owner", new Timestamp(new Date(0).getTime()), "category", "a job"));
        jobs.add(new Job(UUID.randomUUID(), "job2", "owner2", new Timestamp(new Date(0).getTime()), "category", "a job2"));

        JobDAO jobDAOMock = mock(JobDAO.class);
        JobVersionDAO jobVersionDAOMock = mock(JobVersionDAO.class);
        when(jobDAOMock.findAllInNamespace(TEST_NS)).thenReturn(jobs);

        JobService js = new JobService(jobDAOMock, jobVersionDAOMock);
        List<Job> jobsFound = js.getAll(TEST_NS);
        Assert.assertEquals(jobs, jobsFound);
    }
}