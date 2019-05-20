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
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import lombok.NonNull;
import marquez.api.models.JobRunResponse;
import marquez.api.models.JobRunsResponse;
import marquez.service.models.JobRun;
import marquez.service.models.JobRunState;

public final class JobRunResponseMapper {
  private JobRunResponseMapper() {}

  public static JobRunResponse map(@NonNull JobRun run) {
    return new JobRunResponse(
        run.getGuid(),
        run.getNominalStartTime() == null ? null : ISO_INSTANT.format(run.getNominalStartTime()),
        run.getNominalEndTime() == null ? null : ISO_INSTANT.format(run.getNominalEndTime()),
        run.getRunArgs(),
        JobRunState.State.fromInt(run.getCurrentState()).name());
  }

  public static List<JobRunResponse> map(@NonNull List<JobRun> runs) {
    return unmodifiableList(runs.stream().map(run -> map(run)).collect(toList()));
  }

  public static JobRunsResponse toJobRunsResponse(@NonNull List<JobRun> runs) {
    return new JobRunsResponse(map(runs));
  }
}
