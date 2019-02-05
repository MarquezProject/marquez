package marquez.service.models;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class Generator {
  private static Random rand = new Random();

  private static int randNum() {
    return rand.nextInt(10000);
  }

  private static String randUrn() {
    return String.format("urn:rand:%d.%d.%d", randNum(), randNum(), randNum());
  }

  public static Job genJob() {
    return genJob(UUID.randomUUID());
  }

  public static Job genJob(UUID namespaceID) {
    int jobNum = randNum();
    return new Job(
        UUID.randomUUID(),
        "job" + jobNum,
        "http://foo.bar/" + jobNum,
        namespaceID,
        null,
        Arrays.asList(randUrn(), randUrn()),
        Arrays.asList(randUrn(), randUrn()));
  }

  public static Job cloneJob(Job job) {
    return new Job(
        job.getGuid(),
        job.getName(),
        job.getLocation(),
        job.getNamespaceGuid(),
        job.getDescription(),
        job.getInputDatasetUrns(),
        job.getOutputDatasetUrns(),
        job.getCreatedAt());
  }

  public static JobRun genJobRun() {
    return new JobRun(
        UUID.randomUUID(),
        JobRunState.State.toInt(JobRunState.State.NEW),
        UUID.randomUUID(),
        "abc123",
        "{'foo': 1}",
        null,
        null,
        null);
  }

  public static JobRun cloneJobRun(JobRun j) {
    return new JobRun(
        j.getGuid(),
        j.getCurrentState(),
        j.getJobVersionGuid(),
        j.getRunArgsHexDigest(),
        j.getRunArgs(),
        null,
        null,
        null);
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
        UUID.randomUUID(),
        UUID.randomUUID(),
        "http://foo.bar",
        UUID.randomUUID(),
        null,
        null,
        null);
  }

  public static JobVersion genJobVersion(UUID jobID) {
    return new JobVersion(
        UUID.randomUUID(), jobID, "http://foo.bar", UUID.randomUUID(), null, null, null);
  }

  public static JobVersion genJobVersion(Job job) {
    return new JobVersion(
        UUID.randomUUID(), job.getGuid(), job.getLocation(), UUID.randomUUID(), null, null, null);
  }

  public static JobVersion cloneJobVersion(JobVersion jv) {
    return new JobVersion(
        jv.getGuid(),
        jv.getJobGuid(),
        jv.getUri(),
        jv.getVersion(),
        jv.getLatestJobRunGuid(),
        jv.getCreatedAt(),
        jv.getUpdatedAt());
  }

  public static Namespace genNamespace() {
    int nsNum = randNum();
    return new Namespace(UUID.randomUUID(), "ns" + nsNum, "ns owner" + nsNum, "ns desc" + nsNum);
  }

  public static Namespace cloneNamespace(Namespace n) {
    return new Namespace(n.getGuid(), n.getName(), n.getOwnerName(), n.getDescription());
  }

  public static RunArgs genRunArgs() {
    return new RunArgs("abc123", "{'foo': 1}", null);
  }

  public static RunArgs cloneRunArgs(RunArgs ra) {
    return new RunArgs(ra.getHexDigest(), ra.getJson(), ra.getCreatedAt());
  }
}
