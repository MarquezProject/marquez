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

import marquez.api.models.JobRunResponse;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;

public class CoreJobRunToApiJobRunResponseMapper extends Mapper<JobRun, JobRunResponse> {
  @Override
  public JobRunResponse map(JobRun value) {
    return new JobRunResponse(
        value.getGuid(),
        value.getNominalStartTime() == null ? null : value.getNominalStartTime().toString(),
        value.getNominalEndTime() == null ? null : value.getNominalEndTime().toString(),
        value.getRunArgs(),
        JobRunState.State.fromInt(value.getCurrentState()).name());
  }
}
