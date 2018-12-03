package marquez.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import marquez.core.models.Generator;
import marquez.core.models.Job;
import marquez.core.models.JobVersion;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobDAOTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final JobDAO jobDAO = APP.onDemand(JobDAO.class);
  final UUID nsID = UUID.randomUUID();
  final String nsName = "my_ns";
  Job job;
  JobVersion jobVersion;

  @Before
  public void setUp() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  "INSERT INTO namespaces(guid, name, current_ownership)" + "VALUES (?, ?, ?);",
                  nsID,
                  nsName,
                  "Amaranta");
            });
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

  // this is a simple insert outside of JobDAO we can use to test findByID
  private void naiveInsertJob(Job job, JobVersion jobVersion) {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  "INSERT INTO jobs(guid, name, namespace_guid, current_version_guid)"
                      + "VALUES (?, ?, ?, ?);",
                  job.getGuid(),
                  job.getName(),
                  nsID,
                  jobVersion.getGuid());
              handle.execute(
                  "INSERT INTO job_versions(guid, job_guid, uri, version) VALUES(?, ?, ?, ?);",
                  jobVersion.getGuid(),
                  jobVersion.getJobGuid(),
                  jobVersion.getUri(),
                  jobVersion.getVersion());
            });
  }

  private void assertJobFieldsMatch(Job job1, Job job2) {
    assertEquals(job1.getNamespaceGuid(), job2.getNamespaceGuid());
    assertEquals(job1.getGuid(), job2.getGuid());
    assertEquals(job1.getName(), job2.getName());
  }

  @Test
  public void testFindByID() {
    naiveInsertJob(job, jobVersion);
    Job jobFound = jobDAO.findByID(job.getGuid());
    assertNotNull(jobFound);
    assertJobFieldsMatch(job, jobFound);
    assertNull(null, jobDAO.findByID(UUID.randomUUID()));
  }

  public void testFindByName() {
    naiveInsertJob(job, jobVersion);
    Job jobFound = jobDAO.findByName(nsName, job.getName());
    assertNotNull(jobFound);
    assertJobFieldsMatch(job, jobFound);
    assertNull(null, jobDAO.findByName(nsName, "nonexistent job"));
  }

  @Test
  public void testInsert() {
    JobVersion jobVersion = Generator.genJobVersion(job.getGuid());
    jobDAO.insertJobAndVersion(job, jobVersion);
    Job jobFound = jobDAO.findByID(job.getGuid());
    assertNotNull(jobFound);
    assertJobFieldsMatch(job, jobFound);
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
          jobDAO.insertJobAndVersion(job, Generator.genJobVersion(job.getGuid()));
        });
    List<Job> jobsFound = jobDAO.findAllInNamespace(nsName);
    assertEquals(jobs.size(), jobsFound.size());
    assertEquals(0, jobDAO.findAllInNamespace("nonexistent").size());
  }
}
