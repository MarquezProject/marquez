package marquez.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import marquez.core.models.Job;
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
  }

  @After
  public void tearDown() {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM jobs;");
              handle.execute("DELETE FROM job_runs;");
              handle.execute("DELETE FROM job_versions;");
              handle.execute("DELETE FROM owners;");
              handle.execute("DELETE FROM namespaces;");
            });
  }

  // this is a simple insert outside of JobDAO we can use to test findByID
  private void naiveInsertJob(Job job) {
    APP.getJDBI()
        .useHandle(
            handle -> {
              handle.execute(
                  "INSERT INTO jobs(guid, name, namespace_guid)" + "VALUES (?, ?, ?);",
                  job.getGuid(),
                  job.getName(),
                  nsID);
            });
  }

  private Job randomJob() {
    UUID id = UUID.randomUUID();
    String name = "job" + String.valueOf(new Random().nextInt());
    String owner = "owner" + String.valueOf(new Random().nextInt());
    Timestamp nominalTime = new Timestamp(new Date(0).getTime());
    String loc = "http://foo.bar/" + name;
    return new Job(id, name, "", loc, nsID);
  }

  private void assertJobFieldsMatch(Job job1, Job job2) {
    assertEquals(job1.getNamespaceGuid(), job2.getNamespaceGuid());
    assertEquals(job1.getName(), job2.getName());
  }

  @Test
  public void testFindByID() {
    Job job = randomJob();
    naiveInsertJob(job);
    Job jobFound = jobDAO.findByID(job.getGuid());
    assertJobFieldsMatch(job, jobFound);
    assertNull(null, jobDAO.findByID(UUID.randomUUID()));
  }

  public void testFindByName() {
    Job job = randomJob();
    naiveInsertJob(job);
    Job jobFound = jobDAO.findByName(job.getName());
    assertJobFieldsMatch(job, jobFound);
    assertNull(null, jobDAO.findByName("nonexistent job"));
  }

  @Test
  public void testInsert() {
    Job job = randomJob();
    jobDAO.insert(job);
    Job jobFound = jobDAO.findByID(job.getGuid());
    assertJobFieldsMatch(job, jobFound);
  }

  @Test
  public void testFindAllInNamespace() {
    List<Job> jobs = Arrays.asList(randomJob(), randomJob(), randomJob());
    jobs.forEach(
        job -> {
          jobDAO.insert(job);
        });
    List<Job> jobsFound = jobDAO.findAllInNamespace(nsName);
    assertEquals(jobs.size(), jobsFound.size());
    assertEquals(0, jobDAO.findAllInNamespace("nonexistent").size());
  }
}
