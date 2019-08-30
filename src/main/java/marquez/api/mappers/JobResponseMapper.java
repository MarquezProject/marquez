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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.format.DateTimeFormatter.ISO_INSTANT;

import java.util.List;
import lombok.NonNull;
import marquez.api.models.JobResponse;
import marquez.api.models.JobsResponse;
import marquez.service.models.Job;

public final class JobResponseMapper {
  private JobResponseMapper() {}

  public static JobResponse map(@NonNull final Job job) {
    return new JobResponse(
        job.getType().toString(),
        job.getName(),
        ISO_INSTANT.format(job.getCreatedAt()),
        ISO_INSTANT.format(job.getUpdatedAt()),
        job.getInputDatasetUrns(),
        job.getOutputDatasetUrns(),
        job.getLocation(),
        job.getDescription());
  }

  public static List<JobResponse> map(@NonNull final List<Job> jobs) {
    return jobs.stream().map(job -> map(job)).collect(toImmutableList());
  }

  public static JobsResponse toJobsResponse(@NonNull final List<Job> jobs) {
    return new JobsResponse(map(jobs));
  }
}
