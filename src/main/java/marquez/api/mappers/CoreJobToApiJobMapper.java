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

import java.util.Optional;
import marquez.api.models.JobResponse;
import marquez.service.models.Job;

public class CoreJobToApiJobMapper extends Mapper<Job, JobResponse> {
  @Override
  public JobResponse map(Job job) {
    return new JobResponse(
        job.getName(),
        Optional.ofNullable(job.getCreatedAt()).map(time -> time.toString()).orElse(null),
        job.getInputDatasetUrns(),
        job.getOutputDatasetUrns(),
        job.getLocation(),
        job.getDescription());
  }
}
