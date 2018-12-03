package marquez.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
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
  final Job job = new Job(UUID.randomUUID(), "a job", "description", "http://foo.com", nsID);

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
    return new JobVersion(
        UUID.randomUUID(), job.getGuid(), job.getLocation(), UUID.randomUUID(), null, null, null);
  }

  @Test
  public void testFindByVersion() {
    JobVersion jobVersion = randomJobVersion();
    jobVersionDAO.insert(jobVersion);
    JobVersion jobVersionFound = jobVersionDAO.findByVersion(jobVersion.getVersion());
    assertEquals(jobVersion, jobVersionFound);
    assertNull(jobVersionDAO.findByVersion(UUID.randomUUID()));
  }

  @Test
  public void testFindByVersion_Multi() {
    JobVersion jobVersion1 = randomJobVersion();
    JobVersion jobVersion2 = randomJobVersion();
    jobVersionDAO.insert(jobVersion1);
    jobVersionDAO.insert(jobVersion2);
    assertEquals(jobVersion1, jobVersionDAO.findByVersion(jobVersion1.getVersion()));
    assertEquals(jobVersion2, jobVersionDAO.findByVersion(jobVersion2.getVersion()));
  }

  @Test
  public void testFind() {
    List<JobVersion> versionsWeWant = Arrays.asList(randomJobVersion(), randomJobVersion());
    versionsWeWant.forEach(jv -> jobVersionDAO.insert(jv));
    assertEquals(versionsWeWant.size(), jobVersionDAO.find(nsName, job.getName()).size());
  }

  @Test
  public void testFind_Multi() {
    // add some unrelated jobs and job versions
    Job unrelatedJob =
        new Job(UUID.randomUUID(), "unrelated job", "description", "http://unrelated.job", nsID);
    jobDAO.insert(unrelatedJob);
    JobVersion dontWant1 =
        new JobVersion(
            UUID.randomUUID(),
            unrelatedJob.getGuid(),
            "http://random.version",
            UUID.randomUUID(),
            null,
            null,
            null);
    JobVersion dontWant2 =
        new JobVersion(
            UUID.randomUUID(),
            unrelatedJob.getGuid(),
            "http://random.version",
            UUID.randomUUID(),
            null,
            null,
            null);
    List<JobVersion> versionsWeDontWant = Arrays.asList(dontWant1, dontWant2);
    versionsWeDontWant.forEach(jv -> jobVersionDAO.insert(jv));

    // insert the job versions we want to fetch
    List<JobVersion> versionsWeWant = Arrays.asList(randomJobVersion(), randomJobVersion());
    versionsWeWant.forEach(jv -> jobVersionDAO.insert(jv));

    assertEquals(versionsWeWant.size(), jobVersionDAO.find(nsName, job.getName()).size());
  }

  @Test
  public void testFindLatest() {
    JobVersion jobVersion1 = randomJobVersion();
    JobVersion jobVersion2 = randomJobVersion();
    jobVersionDAO.insert(jobVersion1);
    jobVersionDAO.insert(jobVersion2);
    assertEquals(jobVersion2, jobVersionDAO.findLatest(nsName, job.getName()));
  }
}
