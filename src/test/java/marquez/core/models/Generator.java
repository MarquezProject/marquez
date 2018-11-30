package marquez.core.models;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class Generator {
  public static Job genJob() {
    return new Job(UUID.randomUUID(), "a job", "http://foo.bar/", UUID.randomUUID());
  }

  public static Job genJob(UUID namespaceID) {
    Random r = new Random();
    int jobNum = r.nextInt();
    return new Job(UUID.randomUUID(), "job" + jobNum, "http://foo.bar/" + jobNum, namespaceID);
  }

  public static Job cloneJob(Job job) {
    return new Job(job.getGuid(), job.getName(), job.getLocation(), job.getNamespaceGuid());
  }

  public static JobRun genJobRun() {
    return new JobRun(
        UUID.randomUUID(),
        JobRunState.State.toInt(JobRunState.State.NEW),
        UUID.randomUUID(),
        "abc123",
        "{'foo': 1}");
  }

  public static JobRun cloneJobRun(JobRun j) {
    return new JobRun(
        j.getGuid(),
        j.getCurrentState(),
        j.getJobVersionGuid(),
        j.getRunArgsHexDigest(),
        j.getRunArgs());
  }

  public static JobRunState genJobRunState() {
    return new JobRunState(
        UUID.randomUUID(),
        new Timestamp(new Date(0).getTime()),
        UUID.randomUUID(),
        JobRunState.State.NEW);
  }

  public static JobRunState cloneJobRunState(JobRunState jrs) {
    return new JobRunState(
        jrs.getGuid(), jrs.getTransitionedAt(), jrs.getJobRunGuid(), jrs.getState());
  }

  public static JobVersion genJobVersion() {
    return new JobVersion(
        UUID.randomUUID(), UUID.randomUUID(), "http://foo.bar", UUID.randomUUID());
  }

  public static JobVersion genJobVersion(UUID jobID) {
    return new JobVersion(
        UUID.randomUUID(), jobID, "http://foo.bar", UUID.randomUUID());
  }

  public static JobVersion cloneJobVersion(JobVersion jv) {
    return new JobVersion(jv.getGuid(), jv.getJobGuid(), jv.getUri(), jv.getVersion());
  }

  public static Namespace genNamespace() {
    return new Namespace(UUID.randomUUID(), "ns name", "ns owner", "ns desc");
  }

  public static Namespace cloneNamespace(Namespace n) {
    return new Namespace(n.getGuid(), n.getName(), n.getOwnerName(), n.getDescription());
  }

  public static RunArgs genRunArgs() {
    return new RunArgs("abc123", "{'foo': 1}");
  }

  public static RunArgs cloneRunArgs(RunArgs ra) {
    return new RunArgs(ra.getHexDigest(), ra.getJson());
  }
}
