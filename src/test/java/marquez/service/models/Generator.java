package marquez.service.models;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import marquez.common.models.DatasetUrn;
import marquez.db.models.DataSourceRow;
import marquez.db.models.DatasetRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;

public class Generator {
  private static Random rand = new Random();

  private static int randNum() {
    return rand.nextInt(10000);
  }

  private static String randUrn() {
    return String.format("urn:rand:%d.%d.%d", randNum(), randNum(), randNum());
  }

  // Jobs
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

  // Job Runs
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

  // Job Run States
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

  // Job Versions
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

  // Namespaces
  public static Namespace genNamespace() {
    int nsNum = randNum();
    return new Namespace(UUID.randomUUID(), "ns" + nsNum, "ns owner" + nsNum, "ns desc" + nsNum);
  }

  public static Namespace cloneNamespace(Namespace n) {
    return new Namespace(n.getGuid(), n.getName(), n.getOwnerName(), n.getDescription());
  }

  // Run Args
  public static RunArgs genRunArgs() {
    return new RunArgs("abc123", "{'foo': 1}", null);
  }

  public static RunArgs cloneRunArgs(RunArgs ra) {
    return new RunArgs(ra.getHexDigest(), ra.getJson(), ra.getCreatedAt());
  }

  // Data Source Rows
  public static DataSourceRow genDataSourceRow() {
    int dataSourceNum = randNum();
    return DataSourceRow.builder()
        .uuid(UUID.randomUUID())
        .name("Data Source" + dataSourceNum)
        .connectionUrl("conn://" + dataSourceNum)
        .build();
  }

  // Dataset Rows
  public static DatasetRow genDatasetRow(UUID namespaceID, UUID dataSourceID) {
    return DatasetRow.builder()
        .uuid(UUID.randomUUID())
        .namespaceUuid(namespaceID)
        .dataSourceUuid(dataSourceID)
        .description("dataset " + randNum())
        .urn(DatasetUrn.fromString(randUrn()).toString())
        .build();
  }

  // DbTableInfo Rows
  public static DbTableInfoRow genDbTableInfowRow() {
    return DbTableInfoRow.builder()
        .uuid(UUID.randomUUID())
        .db("db" + randNum())
        .dbSchema("schema" + randNum())
        .build();
  }

  // DbTableVersion Rows
  public static DbTableVersionRow genDbTableVersionRow(UUID datasetUuid, UUID dbTableInfoUuid) {
    return DbTableVersionRow.builder()
        .uuid(UUID.randomUUID())
        .datasetUuid(datasetUuid)
        .dbTableInfoUuid(dbTableInfoUuid)
        .dbTable("table " + randNum())
        .build();
  }
}
