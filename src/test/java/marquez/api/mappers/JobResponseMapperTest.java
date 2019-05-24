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

import static java.time.format.DateTimeFormatter.ISO_INSTANT;
import static marquez.service.models.ServiceModelGenerator.newJob;
import static marquez.service.models.ServiceModelGenerator.newJobs;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Collections;
import java.util.List;
import marquez.UnitTests;
import marquez.api.models.JobResponse;
import marquez.api.models.JobsResponse;
import marquez.service.models.Job;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobResponseMapperTest {
  @Test
  public void testMap_job() {
    final Job job = newJob();
    final JobResponse response = JobResponseMapper.map(job);
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo(job.getName());
    assertThat(response.getCreatedAt()).isEqualTo(ISO_INSTANT.format(job.getCreatedAt()));
    assertThat(response.getUpdatedAt()).isEqualTo(ISO_INSTANT.format(job.getUpdatedAt()));
    assertThat(response.getInputDatasetUrns()).containsAll(job.getInputDatasetUrns());
    assertThat(response.getOutputDatasetUrns()).containsAll(job.getOutputDatasetUrns());
    assertThat(response.getLocation()).isEqualTo(job.getLocation());
    assertThat(response.getDescription()).isEqualTo(job.getDescription());
  }

  @Test
  public void testMap_job_noDescription() {
    final Job job = newJob(false);
    final JobResponse response = JobResponseMapper.map(job);
    assertThat(response).isNotNull();
    assertThat(response.getName()).isEqualTo(job.getName());
    assertThat(response.getCreatedAt()).isEqualTo(ISO_INSTANT.format(job.getCreatedAt()));
    assertThat(response.getUpdatedAt()).isEqualTo(ISO_INSTANT.format(job.getUpdatedAt()));
    assertThat(response.getInputDatasetUrns()).containsAll(job.getInputDatasetUrns());
    assertThat(response.getOutputDatasetUrns()).containsAll(job.getOutputDatasetUrns());
    assertThat(response.getLocation()).isEqualTo(job.getLocation());
    assertThat(response.getDescription()).isNull();
  }

  @Test
  public void testMap_throwsException_onNullJob() {
    final Job nullJob = null;
    assertThatNullPointerException().isThrownBy(() -> JobResponseMapper.map(nullJob));
  }

  @Test
  public void testMap_jobs() {
    final List<Job> jobs = newJobs(4);
    final List<JobResponse> responses = JobResponseMapper.map(jobs);
    assertThat(responses).isNotNull();
    assertThat(responses).hasSize(4);
  }

  @Test
  public void testMap_emptyJobs() {
    final List<Job> emptyJobs = Collections.emptyList();
    final List<JobResponse> emptyResponses = JobResponseMapper.map(emptyJobs);
    assertThat(emptyResponses).isNotNull();
    assertThat(emptyResponses).isEmpty();
  }

  @Test
  public void testMap_throwsException_onNullJobs() {
    final List<Job> nullJobs = null;
    assertThatNullPointerException().isThrownBy(() -> JobResponseMapper.map(nullJobs));
  }

  @Test
  public void testToJobsResponse() {
    final List<Job> jobs = newJobs(4);
    final JobsResponse response = JobResponseMapper.toJobsResponse(jobs);
    assertThat(response).isNotNull();
    assertThat(response.getJobs()).hasSize(4);
  }

  @Test
  public void testToJobsResponse_emptyJobs() {
    final List<Job> emptyJobs = Collections.emptyList();
    final JobsResponse response = JobResponseMapper.toJobsResponse(emptyJobs);
    assertThat(response).isNotNull();
    assertThat(response.getJobs()).isEmpty();
  }

  @Test
  public void testToJobsResponse_throwsException_onNullJobs() {
    final List<Job> nullJobs = null;
    assertThatNullPointerException().isThrownBy(() -> JobResponseMapper.toJobsResponse(nullJobs));
  }
}
