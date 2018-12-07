package marquez.dao;

import static org.junit.Assert.assertEquals;
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

public class JobVersionDAOTest {

  @ClassRule public static final AppWithPostgresRule APP = new AppWithPostgresRule();

  final JobDAO jobDAO = APP.onDemand(JobDAO.class);
  final JobVersionDAO jobVersionDAO = APP.onDemand(JobVersionDAO.class);
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

  @Test
  public void testFindByVersion() {
    JobVersion jobVersion = Generator.genJobVersion(job);
    jobVersionDAO.insert(jobVersion);
    JobVersion jobVersionFound = jobVersionDAO.findByVersion(jobVersion.getVersion());
    assertEquals(jobVersion.getGuid(), jobVersionFound.getGuid());
    assertNull(jobVersionDAO.findByVersion(UUID.randomUUID()));
  }

  @Test
  public void testFindByVersion_Multi() {
    JobVersion jobVersion1 = Generator.genJobVersion(job);
    JobVersion jobVersion2 = Generator.genJobVersion(job);
    jobVersionDAO.insert(jobVersion1);
    jobVersionDAO.insert(jobVersion2);
    assertEquals(
        jobVersion1.getGuid(), jobVersionDAO.findByVersion(jobVersion1.getVersion()).getGuid());
    assertEquals(
        jobVersion2.getGuid(), jobVersionDAO.findByVersion(jobVersion2.getVersion()).getGuid());
  }

  @Test
  public void testFind() {
    List<JobVersion> versionsWeWant =
        Arrays.asList(Generator.genJobVersion(job), Generator.genJobVersion(job));
    versionsWeWant.forEach(jv -> jobVersionDAO.insert(jv));
    assertEquals(versionsWeWant.size(), jobVersionDAO.find(nsName, job.getName()).size());
  }

  @Test
  public void testFind_Multi() {
    // add some unrelated jobs and job versions
    Job unrelatedJob = Generator.genJob(nsID);
    jobDAO.insert(unrelatedJob);
    JobVersion dontWant1 = Generator.genJobVersion(unrelatedJob);
    JobVersion dontWant2 = Generator.genJobVersion(unrelatedJob);
    List<JobVersion> versionsWeDontWant = Arrays.asList(dontWant1, dontWant2);
    versionsWeDontWant.forEach(jv -> jobVersionDAO.insert(jv));
    List<JobVersion> versionsWeWant =
        Arrays.asList(Generator.genJobVersion(job), Generator.genJobVersion(job));
    versionsWeWant.forEach(jv -> jobVersionDAO.insert(jv));
    assertEquals(versionsWeWant.size(), jobVersionDAO.find(nsName, job.getName()).size());
  }

  @Test
  public void testFindLatest() {
    JobVersion jobVersion1 = Generator.genJobVersion(job);
    JobVersion jobVersion2 = Generator.genJobVersion(job);
    jobVersionDAO.insert(jobVersion1);
    jobVersionDAO.insert(jobVersion2);
    assertEquals(jobVersion2.getGuid(), jobVersionDAO.findLatest(nsName, job.getName()).getGuid());
  }
}
