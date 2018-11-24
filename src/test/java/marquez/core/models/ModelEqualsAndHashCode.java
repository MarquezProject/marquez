package marquez.core.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ModelEqualsAndHashCode {
  @Test
  public void testJob_EqualsAndHashCode() throws Exception {
    Job job1 = Generator.genJob();
    Job job2 = Generator.cloneJob(job1);
    assertEquals(job1, job2);
    assertEquals(job1.toString(), job2.toString());
    assertEquals(job1.hashCode(), job2.hashCode());
  }

  @Test
  public void testJobRun_EqualsAndHashCode() throws Exception {
    JobRun jobRun1 = Generator.genJobRun();
    JobRun jobRun2 = Generator.cloneJobRun(jobRun1);
    assertEquals(jobRun1, jobRun2);
    assertEquals(jobRun1.toString(), jobRun2.toString());
    assertEquals(jobRun1.hashCode(), jobRun2.hashCode());
  }

  @Test
  public void testJobRunState_EqualsAndHashCode() throws Exception {
    JobRunState jobRunState1 = Generator.genJobRunState();
    JobRunState jobRunState2 = Generator.cloneJobRunState(jobRunState1);
    assertEquals(jobRunState1, jobRunState2);
    assertEquals(jobRunState1.toString(), jobRunState2.toString());
    assertEquals(jobRunState1.hashCode(), jobRunState2.hashCode());
  }

  @Test
  public void testJobVersion_EqualsAndHashCode() throws Exception {
    JobVersion jobVersion1 = Generator.genJobVersion();
    JobVersion jobVersion2 = Generator.cloneJobVersion(jobVersion1);
    assertEquals(jobVersion1, jobVersion2);
    assertEquals(jobVersion1.toString(), jobVersion2.toString());
    assertEquals(jobVersion1.hashCode(), jobVersion2.hashCode());
  }

  @Test
  public void testNamespace_EqualsAndHashCode() throws Exception {
    Namespace namespace1 = Generator.genNamespace();
    Namespace namespace2 = Generator.cloneNamespace(namespace1);
    assertEquals(namespace1, namespace2);
    assertEquals(namespace1.toString(), namespace2.toString());
    assertEquals(namespace1.hashCode(), namespace2.hashCode());
  }

  @Test
  public void testRunArgs_EqualsAndHashCode() throws Exception {
    RunArgs runArgs1 = Generator.genRunArgs();
    RunArgs runArgs2 = Generator.cloneRunArgs(runArgs1);
    assertEquals(runArgs1, runArgs2);
    assertEquals(runArgs1.toString(), runArgs2.toString());
    assertEquals(runArgs1.hashCode(), runArgs2.hashCode());
  }
}
