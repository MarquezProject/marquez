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
  public void testMap_jobRun() {
    final JobRun jobRun = newJobRun();
    final JobRunResponse response = JobRunResponseMapper.map(jobRun);
    assertThat(response).isNotNull();
    assertThat(response.getRunId()).isEqualTo(jobRun.getGuid());
    assertThat(response.getRunState())
        .isEqualTo(JobRunState.State.fromInt(jobRun.getCurrentState()).name());
    assertThat(response.getRunArgs()).isEqualTo(jobRun.getRunArgs());
    assertThat(response.getNominalStartTime()).isEqualTo(jobRun.getNominalStartTime());
    assertThat(response.getNominalEndTime()).isEqualTo(jobRun.getNominalEndTime());
  }

  @Test
  public void testMap_throwsException_onNullJobRun() {
    final JobRun nullJobRun = null;
    assertThatNullPointerException().isThrownBy(() -> JobRunResponseMapper.map(nullJobRun));
  }

  @Test
  public void testMap_jobRuns() {
    final List<JobRun> jobRuns = newJobRuns(4);
    final List<JobRunResponse> responses = JobRunResponseMapper.map(jobRuns);
    assertThat(responses).isNotNull();
    assertThat(responses).hasSize(4);
  }

  @Test
  public void testMap_emptyJobRuns() {
    final List<JobRun> emptyJobRuns = Collections.emptyList();
    final List<JobRunResponse> emptyResponses = JobRunResponseMapper.map(emptyJobRuns);
    assertThat(emptyResponses).isNotNull();
    assertThat(emptyResponses).isEmpty();
  }

  @Test
  public void testMap_throwsException_onNullJobRuns() {
    final List<JobRun> nullJobRuns = null;
    assertThatNullPointerException().isThrownBy(() -> JobRunResponseMapper.map(nullJobRuns));
  }

  @Test
  public void testToJobRunsResponse() {
    final List<JobRun> jobRuns = newJobRuns(4);
    final JobRunsResponse response = JobRunResponseMapper.toJobRunsResponse(jobRuns);
    assertThat(response).isNotNull();
    assertThat(response.getRuns()).hasSize(4);
  }

  @Test
  public void testToJobRunsResponse_emptyJobRuns() {
    final List<JobRun> emptyJobRuns = Collections.emptyList();
    final JobRunsResponse response = JobRunResponseMapper.toJobRunsResponse(emptyJobRuns);
    assertThat(response).isNotNull();
    assertThat(response.getRuns()).isEmpty();
  }

  @Test
  public void testToJobRunsResponse_throwsException_onNullJobs() {
    final List<JobRun> nullJobRuns = null;
    assertThatNullPointerException()
        .isThrownBy(() -> JobRunResponseMapper.toJobRunsResponse(nullJobRuns));
  }
}
