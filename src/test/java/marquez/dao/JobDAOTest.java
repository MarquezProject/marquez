package marquez.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import marquez.core.models.Generator;
import marquez.core.models.Job;
import marquez.core.models.JobVersion;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobDAOTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final JobDAO jobDAO = APP.onDemand(JobDAO.class);
  final UUID nsID = UUID.randomUUID();
  final String nsName = "my_ns";
  Job job = Generator.genJob(nsID);
  JobVersion jobVersion = Generator.genJobVersion(job);

  @Before
  public void setUp() {
    insertNamespace(nsID, nsName, "Amaranta");
    job = Generator.genJob(nsID);
    jobVersion = Generator.genJobVersion(job);
  }

  @After
  public void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM job_runs;");
              handle.execute("DELETE FROM job_versions;");
              handle.execute("DELETE FROM jobs;");
              handle.execute("DELETE FROM owners;");
              handle.execute("DELETE FROM namespaces;");
            });
  }

  private void insertNamespace(UUID namespaceId, String name, String ownerName) {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  "INSERT INTO namespaces(guid, name, current_ownership)" + "VALUES (?, ?, ?);",
                  namespaceId,
                  name,
                  ownerName);
            });
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
  public void testFindByID() {
    jobDAO.insertJobAndVersion(job, jobVersion);
    Job jobFound = jobDAO.findByID(job.getGuid());
    assertNotNull(jobFound);
    assertJobFieldsMatch(job, jobFound);
    assertNull(null, jobDAO.findByID(UUID.randomUUID()));
  }

  public void testFindByName() {
    jobDAO.insertJobAndVersion(job, jobVersion);
    Job jobFound = jobDAO.findByName(nsName, job.getName());
    assertNotNull(jobFound);
    assertJobFieldsMatch(job, jobFound);
    assertNull(null, jobDAO.findByName(nsName, "nonexistent job"));
  }

  @Test
  public void testInsert() {
    JobVersion jobVersion = Generator.genJobVersion(job);
    jobDAO.insertJobAndVersion(job, jobVersion);
    Job jobFound = jobDAO.findByID(job.getGuid());
    assertNotNull(jobFound);
    assertJobFieldsMatch(job, jobFound);
  }

  @Test
  public void testInsert_DiffNsSameName() {
    UUID newNamespaceId = UUID.randomUUID();
    insertNamespace(newNamespaceId, "newNsForDupTest", "Amaranta");
    jobDAO.insert(job);
    Job jobWithDiffNsSameName =
        new Job(
            UUID.randomUUID(),
            job.getName(),
            "location",
            newNamespaceId,
            "desc",
            Collections.<String>emptyList(),
            Collections.<String>emptyList());
    jobDAO.insert(jobWithDiffNsSameName);
  }

  @Test(expected = UnableToExecuteStatementException.class)
  public void testInsert_SameNsSameName() {
    jobDAO.insert(job);
    Job jobWithSameNsSameName =
        new Job(
            UUID.randomUUID(),
            job.getName(),
            "location",
            job.getNamespaceGuid(),
            "desc",
            Collections.<String>emptyList(),
            Collections.<String>emptyList());
    jobDAO.insert(jobWithSameNsSameName);
  }

  @Test
  public void testInsertJobAndVersion() {
    jobDAO.insertJobAndVersion(job, jobVersion);
    Job jobFound = jobDAO.findByID(job.getGuid());
    assertNotNull(jobFound);
    assertJobFieldsMatch(job, jobFound);
    assertEquals(job.getLocation(), jobFound.getLocation());
  }

  @Test
  public void testFindAllInNamespace() {
    List<Job> jobs =
        Arrays.asList(Generator.genJob(nsID), Generator.genJob(nsID), Generator.genJob(nsID));
    jobs.forEach(
        job -> {
          jobDAO.insertJobAndVersion(job, Generator.genJobVersion(job));
        });
    List<Job> jobsFound = jobDAO.findAllInNamespace(nsName);
    assertEquals(jobs.size(), jobsFound.size());
    assertEquals(0, jobDAO.findAllInNamespace("nonexistent").size());
  }

  @Test
  public void testFetchJob_EmptyUrns() {
    UUID jobId = UUID.randomUUID();
    Job jobWithEmptyInputsOutputs =
        new Job(
            jobId,
            "job",
            "location",
            nsID,
            "description",
            Collections.<String>emptyList(),
            Collections.<String>emptyList(),
            null);
    JobVersion jobVersion = Generator.genJobVersion(jobWithEmptyInputsOutputs);
    jobDAO.insertJobAndVersion(jobWithEmptyInputsOutputs, jobVersion);
    Job jobFound = jobDAO.findByID(jobId);
    assertEquals(0, jobFound.getInputDatasetUrns().size());
    assertEquals(0, jobFound.getOutputDatasetUrns().size());
  }
}
