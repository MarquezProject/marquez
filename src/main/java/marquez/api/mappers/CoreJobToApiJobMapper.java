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

import marquez.api.models.JobResponse;

public class CoreJobToApiJobMapper extends Mapper<marquez.service.models.Job, JobResponse> {
  @Override
  public JobResponse map(marquez.service.models.Job value) {
    return new JobResponse(
        value.getName(),
        ISO_INSTANT.format(value.getCreatedAt()),
        value.getInputDatasetUrns(),
        value.getOutputDatasetUrns(),
        value.getLocation(),
        value.getDescription());
  }
}
