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

package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.api.models.JobRunResponse;
import marquez.service.models.JobRunState;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Test;

public class JobRunResponseTest {

  private static final UUID JOB_RUN_UUID = UUID.randomUUID();
  private static final Timestamp STARTED_AT_TIME = Timestamp.from(Instant.now());
  private static final Timestamp ENDED_AT_TIME = Timestamp.from(Instant.now());
  private static final JobRunState.State CURRENT_STATE = JobRunState.State.NEW;
  private static final String RUN_ARGS = "--no-such-argument";

  private static final JobRunResponse JOB_RUN =
      new JobRunResponse(
          JOB_RUN_UUID,
          STARTED_AT_TIME.toString(),
          ENDED_AT_TIME.toString(),
          RUN_ARGS,
          CURRENT_STATE.name());

  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  @Test
  public void testGuidSet() {
    assertThat(JOB_RUN.getRunId().equals(JOB_RUN_UUID));
  }

  @Test
  public void testJobRunEquality() {
    JobRunResponse jr2 =
        new JobRunResponse(
            JOB_RUN_UUID,
            STARTED_AT_TIME.toString(),
            ENDED_AT_TIME.toString(),
            RUN_ARGS,
            CURRENT_STATE.name());
    AssertionsForClassTypes.assertThat(JOB_RUN.equals(JOB_RUN));
    AssertionsForClassTypes.assertThat(JOB_RUN.equals(jr2));
    AssertionsForClassTypes.assertThat(jr2.equals(JOB_RUN));
  }

  @Test
  public void testHashCodeEquality() {
    JobRunResponse jr2 =
        new JobRunResponse(
            JOB_RUN_UUID,
            STARTED_AT_TIME.toString(),
            ENDED_AT_TIME.toString(),
            RUN_ARGS,
            CURRENT_STATE.name());
    assertEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }

  @Test
  public void testJobRunInequalityOnUUID() {
    JobRunResponse jr2 =
        new JobRunResponse(
            UUID.randomUUID(),
            STARTED_AT_TIME.toString(),
            ENDED_AT_TIME.toString(),
            RUN_ARGS,
            CURRENT_STATE.name());
    AssertionsForClassTypes.assertThat(!JOB_RUN.equals(jr2));
    AssertionsForClassTypes.assertThat(JOB_RUN.equals(JOB_RUN));
  }

  @Test
  public void testJobRunInequalityOnNonIDField() {
    JobRunResponse jr2 =
        new JobRunResponse(
            JOB_RUN_UUID,
            STARTED_AT_TIME.toString(),
            ENDED_AT_TIME.toString(),
            RUN_ARGS,
            CURRENT_STATE.name());
    AssertionsForClassTypes.assertThat(!JOB_RUN.equals(jr2));
  }

  @Test
  public void testJobRunHashcodeInequality() {
    JobRunResponse jr2 =
        new JobRunResponse(
            UUID.randomUUID(),
            STARTED_AT_TIME.toString(),
            ENDED_AT_TIME.toString(),
            RUN_ARGS,
            CURRENT_STATE.name());
    assertNotEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }

  @Test
  public void testJobRunHashcodeInequalityOnNonIdField() {
    JobRunResponse jr2 =
        new JobRunResponse(
            JOB_RUN_UUID,
            STARTED_AT_TIME.toString(),
            ENDED_AT_TIME.toString(),
            RUN_ARGS,
            "RUNNING");
    assertNotEquals(JOB_RUN.hashCode(), jr2.hashCode());
  }
}
