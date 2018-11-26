package marquez.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import marquez.core.models.Job;
import marquez.core.models.JobVersion;
import marquez.dao.fixtures.AppWithPostgresRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobVersionDAOTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final JobDAO jobDAO = APP.onDemand(JobDAO.class);
  final JobVersionDAO jobVersionDAO = APP.onDemand(JobVersionDAO.class);
  final UUID nsID = UUID.randomUUID();
  final String nsName = "my_ns";
  final Job job =
      new Job(
          UUID.randomUUID(),
          "a job",
          new Timestamp(new Date(0).getTime()),
          "category",
          "description",
          "http://foo.com",
          nsID);

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
    jobDAO.insert(job);
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

  private JobVersion randomJobVersion() {
    return new JobVersion(UUID.randomUUID(), job.getGuid(), job.getLocation(), UUID.randomUUID());
  }

  @Test
  public void testFindByVersion() {
    JobVersion jobVersion = randomJobVersion();
    jobVersionDAO.insert(
        jobVersion.getGuid(),
        jobVersion.getVersion(),
        jobVersion.getJobGuid(),
        jobVersion.getUri());
    JobVersion jobVersionFound = jobVersionDAO.findByVersion(jobVersion.getVersion());
    assertEquals(jobVersion, jobVersionFound);
    assertNull(jobVersionDAO.findByVersion(UUID.randomUUID()));
  }

  @Test
  public void testFindByVersion_Multi() {
    JobVersion jobVersion1 = randomJobVersion();
    JobVersion jobVersion2 = randomJobVersion();
    jobVersionDAO.insert(
        jobVersion1.getGuid(),
        jobVersion1.getVersion(),
        jobVersion1.getJobGuid(),
        jobVersion1.getUri());
    jobVersionDAO.insert(
        jobVersion2.getGuid(),
        jobVersion2.getVersion(),
        jobVersion2.getJobGuid(),
        jobVersion2.getUri());
    assertEquals(jobVersion1, jobVersionDAO.findByVersion(jobVersion1.getVersion()));
    assertEquals(jobVersion2, jobVersionDAO.findByVersion(jobVersion2.getVersion()));
  }

  @Test
  public void testFind() {
    List<JobVersion> versionsWeWant = Arrays.asList(randomJobVersion(), randomJobVersion());
    versionsWeWant.forEach(
        jv -> jobVersionDAO.insert(jv.getGuid(), jv.getVersion(), jv.getJobGuid(), jv.getUri()));
    assertEquals(versionsWeWant.size(), jobVersionDAO.find(nsName, job.getName()).size());
  }

  @Test
  public void testFind_Multi() {
    // add some unrelated jobs and job versions
    Job unrelatedJob =
        new Job(
            UUID.randomUUID(),
            "unrelated job",
            new Timestamp(new Date(0).getTime()),
            "category",
            "description",
            "http://unrelated.job",
            nsID);
    jobDAO.insert(unrelatedJob);
    JobVersion dontWant1 =
        new JobVersion(
            UUID.randomUUID(), unrelatedJob.getGuid(), "http://random.version", UUID.randomUUID());
    JobVersion dontWant2 =
        new JobVersion(
            UUID.randomUUID(), unrelatedJob.getGuid(), "http://random.version", UUID.randomUUID());
    List<JobVersion> versionsWeDontWant = Arrays.asList(dontWant1, dontWant2);
    versionsWeDontWant.forEach(
        jv -> jobVersionDAO.insert(jv.getGuid(), jv.getVersion(), jv.getJobGuid(), jv.getUri()));

    // insert the job versions we want to fetch
    List<JobVersion> versionsWeWant = Arrays.asList(randomJobVersion(), randomJobVersion());
    versionsWeWant.forEach(
        jv -> jobVersionDAO.insert(jv.getGuid(), jv.getVersion(), jv.getJobGuid(), jv.getUri()));

    assertEquals(versionsWeWant.size(), jobVersionDAO.find(nsName, job.getName()).size());
  }

  @Test
  public void testFindLatest() {
    JobVersion jobVersion1 = randomJobVersion();
    JobVersion jobVersion2 = randomJobVersion();
    jobVersionDAO.insert(
        jobVersion1.getGuid(),
        jobVersion1.getVersion(),
        jobVersion1.getJobGuid(),
        jobVersion1.getUri());
    jobVersionDAO.insert(
        jobVersion2.getGuid(),
        jobVersion2.getVersion(),
        jobVersion2.getJobGuid(),
        jobVersion2.getUri());
    assertEquals(jobVersion2, jobVersionDAO.findLatest(nsName, job.getName()));
  }
}
