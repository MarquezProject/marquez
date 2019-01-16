package marquez.db.models;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Test;

public class JobRunRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = Instant.now();
  private static final UUID JOB_VERSION_UUID = UUID.randomUUID();
  private static final String CURRENT_RUN_STATE = "NEW";
  private static final List<UUID> INPUT_DATASET_VERSION_UUIDS = Arrays.asList(UUID.randomUUID());
  private static final List<UUID> OUTPUT_DATASET_VERSION_UUIDS = Arrays.asList(UUID.randomUUID());

  @Test
  public void testNewJobRunRow() {
    final JobRunRow jobRunRow =
        JobRunRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .jobVersionUuid(JOB_VERSION_UUID)
            .currentRunState(CURRENT_RUN_STATE)
            .inputDatasetVersionUuids(INPUT_DATASET_VERSION_UUIDS)
            .outputDatasetVersionUuids(OUTPUT_DATASET_VERSION_UUIDS)
            .build();
    assertEquals(ROW_UUID, jobRunRow.getUuid());
    assertEquals(CREATED_AT, jobRunRow.getCreatedAt());
    assertEquals(UPDATED_AT, jobRunRow.getUpdatedAt());
    assertEquals(JOB_VERSION_UUID, jobRunRow.getJobVersionUuid());
    assertEquals(CURRENT_RUN_STATE, jobRunRow.getCurrentRunState());
    assertEquals(INPUT_DATASET_VERSION_UUIDS, jobRunRow.getInputDatasetVersionUuids());
    assertEquals(OUTPUT_DATASET_VERSION_UUIDS, jobRunRow.getOutputDatasetVersionUuids());
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullUuid() {
    final UUID nullUuid = null;
    JobRunRow.builder()
        .uuid(nullUuid)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .jobVersionUuid(JOB_VERSION_UUID)
        .currentRunState(CURRENT_RUN_STATE)
        .inputDatasetVersionUuids(INPUT_DATASET_VERSION_UUIDS)
        .outputDatasetVersionUuids(OUTPUT_DATASET_VERSION_UUIDS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullCreatedAt() {
    final Instant nullCreatedAt = null;
    JobRunRow.builder()
        .uuid(ROW_UUID)
        .createdAt(nullCreatedAt)
        .updatedAt(UPDATED_AT)
        .jobVersionUuid(JOB_VERSION_UUID)
        .currentRunState(CURRENT_RUN_STATE)
        .inputDatasetVersionUuids(INPUT_DATASET_VERSION_UUIDS)
        .outputDatasetVersionUuids(OUTPUT_DATASET_VERSION_UUIDS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullUpdatedAt() {
    final Instant nullUpdatedAt = null;
    JobRunRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(nullUpdatedAt)
        .jobVersionUuid(JOB_VERSION_UUID)
        .currentRunState(CURRENT_RUN_STATE)
        .inputDatasetVersionUuids(INPUT_DATASET_VERSION_UUIDS)
        .outputDatasetVersionUuids(OUTPUT_DATASET_VERSION_UUIDS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullJobVersionUuid() {
    final UUID nullJobVersionUuid = null;
    JobRunRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .jobVersionUuid(nullJobVersionUuid)
        .currentRunState(CURRENT_RUN_STATE)
        .inputDatasetVersionUuids(INPUT_DATASET_VERSION_UUIDS)
        .outputDatasetVersionUuids(OUTPUT_DATASET_VERSION_UUIDS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullInputDatasetVersionUuids() {
    final List<UUID> nullInputDatasetVersionUuids = null;
    JobRunRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .jobVersionUuid(JOB_VERSION_UUID)
        .currentRunState(CURRENT_RUN_STATE)
        .inputDatasetVersionUuids(nullInputDatasetVersionUuids)
        .outputDatasetVersionUuids(OUTPUT_DATASET_VERSION_UUIDS)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullOutputDatasetVersionUuids() {
    final List<UUID> nullOutputDatasetVersionUuids = null;
    JobRunRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .jobVersionUuid(JOB_VERSION_UUID)
        .currentRunState(CURRENT_RUN_STATE)
        .inputDatasetVersionUuids(INPUT_DATASET_VERSION_UUIDS)
        .outputDatasetVersionUuids(nullOutputDatasetVersionUuids)
        .build();
  }
}
