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

package marquez.api.mappers;

import static marquez.service.models.ServiceModelGenerator.newJobRun;
import static marquez.service.models.ServiceModelGenerator.newJobRuns;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import marquez.UnitTests;
import marquez.api.models.JobRunResponse;
import marquez.api.models.JobRunsResponse;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobRunResponseMapperTest {
  @Test
  public void testMap_run() {
    final JobRun run = newJobRun();
    final JobRunResponse response = JobRunResponseMapper.map(run);
    assertThat(response).isNotNull();
    assertThat(UUID.fromString(response.getRunId())).isEqualTo(run.getUuid());
    assertThat(response.getRunState())
        .isEqualTo(JobRunState.State.fromInt(run.getCurrentState()).name());
    assertThat(response.getRunArgs().orElse(null)).isEqualTo(run.getRunArgs());
    assertThat(response.getNominalStartTime().orElse(null)).isEqualTo(run.getNominalStartTime());
    assertThat(response.getNominalEndTime().orElse(null)).isEqualTo(run.getNominalEndTime());
  }

  @Test
  public void testMap_throwsException_onNullRun() {
    final JobRun nullRun = null;
    assertThatNullPointerException().isThrownBy(() -> JobRunResponseMapper.map(nullRun));
  }

  @Test
  public void testMap_runs() {
    final List<JobRun> runs = newJobRuns(4);
    final List<JobRunResponse> responses = JobRunResponseMapper.map(runs);
    assertThat(responses).isNotNull();
    assertThat(responses).hasSize(4);
  }

  @Test
  public void testMap_emptyRuns() {
    final List<JobRun> emptyRuns = Collections.emptyList();
    final List<JobRunResponse> emptyResponses = JobRunResponseMapper.map(emptyRuns);
    assertThat(emptyResponses).isNotNull();
    assertThat(emptyResponses).isEmpty();
  }

  @Test
  public void testMap_throwsException_onNullRuns() {
    final List<JobRun> nullRuns = null;
    assertThatNullPointerException().isThrownBy(() -> JobRunResponseMapper.map(nullRuns));
  }

  @Test
  public void testToJobRunsResponse() {
    final List<JobRun> runs = newJobRuns(4);
    final JobRunsResponse response = JobRunResponseMapper.toJobRunsResponse(runs);
    assertThat(response).isNotNull();
    assertThat(response.getRuns()).hasSize(4);
  }

  @Test
  public void testToJobRunsResponse_emptyRuns() {
    final List<JobRun> emptyRuns = Collections.emptyList();
    final JobRunsResponse response = JobRunResponseMapper.toJobRunsResponse(emptyRuns);
    assertThat(response).isNotNull();
    assertThat(response.getRuns()).isEmpty();
  }

  @Test
  public void testToJobRunsResponse_throwsException_onNullRuns() {
    final List<JobRun> nullRuns = null;
    assertThatNullPointerException()
        .isThrownBy(() -> JobRunResponseMapper.toJobRunsResponse(nullRuns));
  }
}
