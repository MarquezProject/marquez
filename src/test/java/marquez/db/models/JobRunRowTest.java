/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.db.models;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
    final Instant nominalStartTime = Instant.parse("2018-10-04T15:01:00.00Z");
    final Instant nominalEndTime = Instant.parse("2018-10-04T15:02:00.00Z");
    final Optional<Instant> expectedNominalStartTime = Optional.of(nominalStartTime);
    final Optional<Instant> expectedNominalEndTime = Optional.of(nominalEndTime);
    final JobRunRow jobRunRow =
        JobRunRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .updatedAt(UPDATED_AT)
            .jobVersionUuid(JOB_VERSION_UUID)
            .nominalStartTime(nominalStartTime)
            .nominalEndTime(nominalEndTime)
            .currentRunState(CURRENT_RUN_STATE)
            .inputDatasetVersionUuids(INPUT_DATASET_VERSION_UUIDS)
            .outputDatasetVersionUuids(OUTPUT_DATASET_VERSION_UUIDS)
            .build();
    assertEquals(ROW_UUID, jobRunRow.getUuid());
    assertEquals(CREATED_AT, jobRunRow.getCreatedAt());
    assertEquals(UPDATED_AT, jobRunRow.getUpdatedAt());
    assertEquals(JOB_VERSION_UUID, jobRunRow.getJobVersionUuid());
    assertEquals(expectedNominalStartTime, jobRunRow.getNominalStartTime());
    assertEquals(expectedNominalEndTime, jobRunRow.getNominalEndTime());
    assertEquals(CURRENT_RUN_STATE, jobRunRow.getCurrentRunState());
    assertEquals(INPUT_DATASET_VERSION_UUIDS, jobRunRow.getInputDatasetVersionUuids());
    assertEquals(OUTPUT_DATASET_VERSION_UUIDS, jobRunRow.getOutputDatasetVersionUuids());
  }

  @Test
  public void testNewJobRunRow_noNominalStartAndEndTime() {
    final Optional<Instant> noNominalStartTime = Optional.empty();
    final Optional<Instant> noNominalEndTime = Optional.empty();
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
    assertEquals(noNominalStartTime, jobRunRow.getNominalStartTime());
    assertEquals(noNominalEndTime, jobRunRow.getNominalEndTime());
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
  public void testNewJobRow_nullCurrentRunState() {
    final String nullCurrentRunState = null;
    JobRunRow.builder()
        .uuid(ROW_UUID)
        .createdAt(CREATED_AT)
        .updatedAt(UPDATED_AT)
        .jobVersionUuid(JOB_VERSION_UUID)
        .currentRunState(nullCurrentRunState)
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
