package marquez.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import marquez.db.fixtures.AppWithPostgresRule;
import marquez.service.models.Generator;
import marquez.service.models.Job;
import marquez.service.models.JobVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobVersionDaoTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final JobDao jobDao = APP.onDemand(JobDao.class);
  final JobVersionDao jobVersionDao = APP.onDemand(JobVersionDao.class);
  final UUID nsID = UUID.randomUUID();
  final String nsName = "my_ns";
  final Job job = Generator.genJob(nsID);

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
    jobDao.insert(job);
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

  @Test
  public void testFindByVersion() {
    JobVersion jobVersion = Generator.genJobVersion(job);
    jobVersionDao.insert(jobVersion);
    JobVersion jobVersionFound = jobVersionDao.findByVersion(jobVersion.getVersion());
    assertEquals(jobVersion.getGuid(), jobVersionFound.getGuid());
    assertNull(jobVersionDao.findByVersion(UUID.randomUUID()));
  }

  @Test
  public void testFindByVersion_Multi() {
    JobVersion jobVersion1 = Generator.genJobVersion(job);
    JobVersion jobVersion2 = Generator.genJobVersion(job);
    jobVersionDao.insert(jobVersion1);
    jobVersionDao.insert(jobVersion2);
    assertEquals(
        jobVersion1.getGuid(), jobVersionDao.findByVersion(jobVersion1.getVersion()).getGuid());
    assertEquals(
        jobVersion2.getGuid(), jobVersionDao.findByVersion(jobVersion2.getVersion()).getGuid());
  }

  @Test
  public void testFind() {
    List<JobVersion> versionsWeWant =
        Arrays.asList(Generator.genJobVersion(job), Generator.genJobVersion(job));
    versionsWeWant.forEach(jv -> jobVersionDao.insert(jv));
    assertEquals(versionsWeWant.size(), jobVersionDao.find(nsName, job.getName()).size());
  }

  @Test
  public void testFind_Multi() {
    // add some unrelated jobs and job versions
    Job unrelatedJob = Generator.genJob(nsID);
    jobDao.insert(unrelatedJob);
    JobVersion dontWant1 = Generator.genJobVersion(unrelatedJob);
    JobVersion dontWant2 = Generator.genJobVersion(unrelatedJob);
    List<JobVersion> versionsWeDontWant = Arrays.asList(dontWant1, dontWant2);
    versionsWeDontWant.forEach(jv -> jobVersionDao.insert(jv));
    List<JobVersion> versionsWeWant =
        Arrays.asList(Generator.genJobVersion(job), Generator.genJobVersion(job));
    versionsWeWant.forEach(jv -> jobVersionDao.insert(jv));
    assertEquals(versionsWeWant.size(), jobVersionDao.find(nsName, job.getName()).size());
  }

  @Test
  public void testFindLatest() {
    JobVersion jobVersion1 = Generator.genJobVersion(job);
    JobVersion jobVersion2 = Generator.genJobVersion(job);
    jobVersionDao.insert(jobVersion1);
    jobVersionDao.insert(jobVersion2);
    assertEquals(jobVersion2.getGuid(), jobVersionDao.findLatest(nsName, job.getName()).getGuid());
  }
}
