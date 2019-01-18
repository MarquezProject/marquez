package marquez.db.models;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.Test;

public class JobVersionRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final UUID JOB_UUID = UUID.randomUUID();
  private static final List<String> INPUT_DATASET_URNS = Arrays.asList("urn:a:b.c");
  private static final List<String> OUTPUT_DATASET_URNS = Arrays.asList("urn:d:e.f");
  private static final UUID VERSION = UUID.randomUUID();
  private static final String LOCATION =
      "https://github.com/test/job/commit/1867de4c29e55d3667d6505426ec325767d998c9";

  @Test
  public void testNewJobVersionRow() {
    final Instant updatedAt = Instant.now();
    final UUID latestJobRunUuid = UUID.randomUUID();
    final Optional<Instant> expectedUpdatedAt = Optional.of(updatedAt);
    final Optional<UUID> expectedLatestJobRunUuid = Optional.of(latestJobRunUuid);
    final JobVersionRow jobVersionRow =
        JobVersionRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .updatedAt(updatedAt)
            .jobUuid(JOB_UUID)
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .version(VERSION)
            .location(LOCATION)
            .latestJobRunUuid(latestJobRunUuid)
            .build();
    assertEquals(ROW_UUID, jobVersionRow.getUuid());
    assertEquals(CREATED_AT, jobVersionRow.getCreatedAt());
    assertEquals(expectedUpdatedAt, jobVersionRow.getUpdatedAt());
    assertEquals(JOB_UUID, jobVersionRow.getJobUuid());
    assertEquals(INPUT_DATASET_URNS, jobVersionRow.getInputDatasetUrns());
    assertEquals(OUTPUT_DATASET_URNS, jobVersionRow.getOutputDatasetUrns());
    assertEquals(VERSION, jobVersionRow.getVersion());
    assertEquals(LOCATION, jobVersionRow.getLocation());
    assertEquals(expectedLatestJobRunUuid, jobVersionRow.getLatestJobRunUuid());
  }

  @Test
  public void testNewJobVersionRow_noLatestJobRunUuid() {
    final Optional<Instant> noUpdatedAt = Optional.empty();
    final Optional<UUID> noLatestJobRunUuid = Optional.empty();
    final JobVersionRow jobVersionRow =
        JobVersionRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .jobUuid(JOB_UUID)
            .inputDatasetUrns(INPUT_DATASET_URNS)
            .outputDatasetUrns(OUTPUT_DATASET_URNS)
            .version(VERSION)
            .location(LOCATION)
            .build();
    assertEquals(ROW_UUID, jobVersionRow.getUuid());
    assertEquals(CREATED_AT, jobVersionRow.getCreatedAt());
    assertEquals(noUpdatedAt, jobVersionRow.getUpdatedAt());
    assertEquals(JOB_UUID, jobVersionRow.getJobUuid());
    assertEquals(INPUT_DATASET_URNS, jobVersionRow.getInputDatasetUrns());
    assertEquals(OUTPUT_DATASET_URNS, jobVersionRow.getOutputDatasetUrns());
    assertEquals(VERSION, jobVersionRow.getVersion());
    assertEquals(LOCATION, jobVersionRow.getLocation());
    assertEquals(noLatestJobRunUuid, jobVersionRow.getLatestJobRunUuid());
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobVersionRow_nullUuid() {
    final UUID nullUuid = null;
    JobVersionRow.builder()
        .uuid(nullUuid)
        .createdAt(CREATED_AT)
        .jobUuid(JOB_UUID)
        .inputDatasetUrns(INPUT_DATASET_URNS)
        .outputDatasetUrns(OUTPUT_DATASET_URNS)
        .version(VERSION)
        .location(LOCATION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobVersionRow_nullCreatedAt() {
    final Instant nullCreatedAt = null;
    JobVersionRow.builder()
        .uuid(ROW_UUID)
        .createdAt(nullCreatedAt)
        .jobUuid(JOB_UUID)
        .inputDatasetUrns(INPUT_DATASET_URNS)
        .outputDatasetUrns(OUTPUT_DATASET_URNS)
        .version(VERSION)
        .location(LOCATION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobVersionRow_nullJobUuid() {
    final UUID nullJobUuid = null;
    JobVersionRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .jobUuid(nullJobUuid)
        .inputDatasetUrns(INPUT_DATASET_URNS)
        .outputDatasetUrns(OUTPUT_DATASET_URNS)
        .version(VERSION)
        .location(LOCATION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobVersionRow_nullInputDatasetUrns() {
    final List<String> nullInputDatasetUrns = null;
    JobVersionRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .jobUuid(JOB_UUID)
        .inputDatasetUrns(nullInputDatasetUrns)
        .outputDatasetUrns(OUTPUT_DATASET_URNS)
        .version(VERSION)
        .location(LOCATION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobVersionRow_nullOutDatasetUrns() {
    final List<String> nullOutDatasetUrns = null;
    JobVersionRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .jobUuid(JOB_UUID)
        .inputDatasetUrns(INPUT_DATASET_URNS)
        .outputDatasetUrns(nullOutDatasetUrns)
        .version(VERSION)
        .location(LOCATION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobVersionRow_nullVersion() {
    final UUID nullVersion = null;
    JobVersionRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .jobUuid(JOB_UUID)
        .inputDatasetUrns(INPUT_DATASET_URNS)
        .outputDatasetUrns(OUTPUT_DATASET_URNS)
        .version(nullVersion)
        .location(LOCATION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobVersionRow_nullLocation() {
    final String nullLocation = null;
    JobVersionRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .jobUuid(JOB_UUID)
        .inputDatasetUrns(INPUT_DATASET_URNS)
        .outputDatasetUrns(OUTPUT_DATASET_URNS)
        .version(VERSION)
        .location(nullLocation)
        .build();
  }
}
