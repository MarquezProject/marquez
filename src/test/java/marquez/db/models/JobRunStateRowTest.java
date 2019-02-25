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
import java.util.UUID;
import org.junit.Test;

public class JobRunStateRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant TRANSITIONED_AT = Instant.now();
  private static final UUID JOB_RUN_UUID = UUID.randomUUID();
  private static final String RUN_STATE = "NEW";

  @Test
  public void testNewJobRunStateRow() {
    final JobRunStateRow jobRunStateRow =
        JobRunStateRow.builder()
            .uuid(ROW_UUID)
            .transitionedAt(TRANSITIONED_AT)
            .jobRunUuid(JOB_RUN_UUID)
            .runState(RUN_STATE)
            .build();
    assertEquals(ROW_UUID, jobRunStateRow.getUuid());
    assertEquals(TRANSITIONED_AT, jobRunStateRow.getTransitionedAt());
    assertEquals(JOB_RUN_UUID, jobRunStateRow.getJobRunUuid());
    assertEquals(RUN_STATE, jobRunStateRow.getRunState());
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullUuid() {
    final UUID nullUuid = null;
    JobRunStateRow.builder()
        .uuid(nullUuid)
        .transitionedAt(TRANSITIONED_AT)
        .jobRunUuid(JOB_RUN_UUID)
        .runState(RUN_STATE)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullTransitionedAt() {
    final Instant nullTransitionedAt = null;
    JobRunStateRow.builder()
        .uuid(ROW_UUID)
        .transitionedAt(nullTransitionedAt)
        .jobRunUuid(JOB_RUN_UUID)
        .runState(RUN_STATE)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullJobRunUuid() {
    final UUID nullJobRunUuid = null;
    JobRunStateRow.builder()
        .uuid(ROW_UUID)
        .transitionedAt(TRANSITIONED_AT)
        .jobRunUuid(nullJobRunUuid)
        .runState(RUN_STATE)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobRow_nullRunState() {
    final String nullRunState = null;
    JobRunStateRow.builder()
        .uuid(ROW_UUID)
        .transitionedAt(TRANSITIONED_AT)
        .jobRunUuid(JOB_RUN_UUID)
        .runState(nullRunState)
        .build();
  }
}
