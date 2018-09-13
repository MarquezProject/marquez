package marquez.db.dao;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import marquez.api.Job;
import marquez.api.JobRunDefinition;
import marquez.db.dao.fixtures.DAOSetup;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

public class JobRunDefinitionDAOTest {

  @ClassRule public static final DAOSetup daoSetup = new DAOSetup();

  final Timestamp defaultTimestamp = new Timestamp(new Date(0).getTime());

  final JobDAO jobDAO = daoSetup.onDemand(JobDAO.class);
  final JobVersionDAO jobVersionDAO = daoSetup.onDemand(JobVersionDAO.class);
  final JobRunDefinitionDAO jobRunDefDAO = daoSetup.onDemand(JobRunDefinitionDAO.class);

  final UUID jobGuid = UUID.randomUUID();
  final UUID jobVersionGuid = UUID.randomUUID();
  final UUID jobVersionVersion = UUID.randomUUID();
  final UUID jobRunDefinitionGuid = UUID.randomUUID();
  final UUID jobRunDefinitionHash = UUID.randomUUID();

  @Before
  public void setUp() {
    Job job = new Job(jobGuid, "my name", "my owner", defaultTimestamp, "", "");
    jobDAO.insert(job);
    jobVersionDAO.insert(jobVersionGuid, jobVersionVersion, jobGuid, "http://foo.bar");
  }

  @After
  public void tearDown() {
    daoSetup
        .getJDBI()
        .useHandle(
            handle -> {
              handle.execute("DELETE FROM job_run_definitions;");
              handle.execute("DELETE FROM job_versions;");
              handle.execute("DELETE FROM jobs;");
              handle.execute("DELETE FROM owners;");
            });
  }

  private JobRunDefinition genRandomFixture() {
    String runArgs = String.format("{'foo': %d}", new Random().nextInt(100));
    return new JobRunDefinition(UUID.randomUUID(), jobVersionGuid, runArgs, 0, 0);
  }

  private static void insertJobRunDefinition(final JobRunDefinition jrd) {
    daoSetup
        .getJDBI()
        .useHandle(
            handle -> {
              handle
                  .createUpdate(
                      "INSERT INTO job_run_definitions(guid, job_version_guid, run_args_json, content_hash, nominal_time) VALUES (:guid, :job_version_guid, :run_args_json, :content_hash, :nominal_time)")
                  .bind("guid", jrd.getGuid())
                  .bind("job_version_guid", jrd.getJobVersionGuid())
                  .bind("run_args_json", jrd.getRunArgsJson())
                  .bind("content_hash", jrd.computeDefinitionHash())
                  .bind(
                      "nominal_time", new Timestamp(new Date(jrd.getNominalTimeStart()).getTime()))
                  .execute();
            });
  }

  @Test
  public void testFindByHash() {
    JobRunDefinition jrd = genRandomFixture();
    insertJobRunDefinition(jrd);
    assertEquals(jrd, jobRunDefDAO.findByHash(jrd.computeDefinitionHash()));
  }

  @Test
  public void testFindByHash_Multi() {
    JobRunDefinition jrd1 = genRandomFixture();
    insertJobRunDefinition(jrd1);
    JobRunDefinition jrd2 = genRandomFixture();
    insertJobRunDefinition(jrd2);

    assertEquals(jrd1, jobRunDefDAO.findByHash(jrd1.computeDefinitionHash()));
    assertEquals(jrd2, jobRunDefDAO.findByHash(jrd2.computeDefinitionHash()));
  }

  @Test
  public void testFindByGuid() {
    JobRunDefinition jrd = genRandomFixture();
    insertJobRunDefinition(jrd);
    assertEquals(jrd, jobRunDefDAO.findByGuid(jrd.getGuid()));
  }

  @Test
  public void testFindByGuid_Multi() {
    JobRunDefinition jrd1 = genRandomFixture();
    insertJobRunDefinition(jrd1);
    JobRunDefinition jrd2 = genRandomFixture();
    insertJobRunDefinition(jrd2);

    assertEquals(jrd1, jobRunDefDAO.findByGuid(jrd1.getGuid()));
    assertEquals(jrd2, jobRunDefDAO.findByGuid(jrd2.getGuid()));
  }

  @Test
  public void testInsert() {
    JobRunDefinition jrd = genRandomFixture();
    jobRunDefDAO.insert(
        jrd.getGuid(), jrd.computeDefinitionHash(), jrd.getJobVersionGuid(), jrd.getRunArgsJson());
    assertEquals(jrd, jobRunDefDAO.findByHash(jrd.computeDefinitionHash()));
  }
}
