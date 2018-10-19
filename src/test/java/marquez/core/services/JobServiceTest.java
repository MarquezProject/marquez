package marquez.core.services;

import static org.mockito.Mockito.mock;
import org.junit.Test;
import org.junit.Assert;

import marquez.api.Job;
import marquez.dao.JobDAO;
import marquez.dao.JobVersionDAO;

public class JobServiceTest {
    final String TEST_NS = "test_namespace";

    @Test
    public void testGetAll() throws Exception {
        JobDAO jobDAOMock = mock(JobDAO.class);
        JobVersionDAO jobVersionDAOMock = mock(JobVersionDAO.class);

        JobService js = new JobService(jobDAOMock, jobVersionDAOMock);
        Job[] jobs = js.getAll(TEST_NS);
        Assert.assertTrue(jobs.length > 0);
    }
}