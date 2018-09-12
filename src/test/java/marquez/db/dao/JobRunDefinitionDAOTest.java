package marquez.db.dao;

import static org.junit.Assert.assertEquals;

import java.sql.Timestamp;
import java.util.Date;
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

  @Test
  public void testFindByHash() {
    JobRunDefinition expectedJrd =
        new JobRunDefinition(jobRunDefinitionGuid, jobVersionGuid, "{}", "", 0, 0);
    daoSetup
        .getJDBI()
        .useHandle(
            handle -> {
              handle
                  .createUpdate(
                      "INSERT INTO job_run_definitions(guid, job_version_guid, run_args_json, content_hash, nominal_time) VALUES (:guid, :job_version_guid, :run_args_json, :content_hash, :nominal_time)")
                  .bind("guid", expectedJrd.getGuid())
                  .bind("job_version_guid", expectedJrd.getJobVersionGuid())
                  .bind("run_args_json", expectedJrd.getRunArgsJson())
                  .bind("content_hash", jobRunDefinitionHash)
                  .bind(
                      "nominal_time",
                      new Timestamp(new Date(expectedJrd.getNominalTimeStart()).getTime()))
                  .execute();
            });
    assertEquals(expectedJrd, jobRunDefDAO.findByHash(jobRunDefinitionHash));
  }

  @Test
  public void testInsert() {
    jobRunDefDAO.insert(jobRunDefinitionGuid, jobRunDefinitionHash, jobVersionGuid, "{}");
    JobRunDefinition expectedJrd =
        new JobRunDefinition(jobRunDefinitionGuid, jobVersionGuid, "{}", "", 0, 0);
    assertEquals(expectedJrd, jobRunDefDAO.findByHash(jobRunDefinitionHash));
  }
}
