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

package marquez.db.mappers;

import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidArrayOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import marquez.common.Utils;
import marquez.db.Columns;
import marquez.db.MapperUtils;
import marquez.db.RunDao.CurrentRunStateData;
import marquez.db.RunDao.DatasetVersionIdData;
import marquez.db.RunDao.JobVersionIdData;
import marquez.db.RunDao.RunArgsData;
import marquez.db.RunDao.RunData;
import marquez.db.RunDao.RunStateData;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class RunMapper implements RowMapper<RunData> {
  @Override
  public RunData map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    Set<String> columnNames = MapperUtils.getColumnNames(results.getMetaData());
    return new RunData(
        uuidOrThrow(results, Columns.ROW_UUID),
        timestampOrThrow(results, Columns.CREATED_AT),
        timestampOrThrow(results, Columns.UPDATED_AT),
        toJobVersionIdData(uuidOrThrow(results, Columns.JOB_VERSION_UUID)),
        toInputDatasetVersionIdData(uuidArrayOrThrow(results, Columns.INPUT_VERSION_UUIDS)),
        Optional.ofNullable(timestampOrNull(results, Columns.NOMINAL_START_TIME)),
        Optional.ofNullable(timestampOrNull(results, Columns.NOMINAL_END_TIME)),
        toCurrentRunState(stringOrNull(results, Columns.CURRENT_RUN_STATE)),
        toRunState(
            columnNames.contains(Columns.STARTED_AT)
                ? timestampOrNull(results, Columns.STARTED_AT)
                : null,
            uuidOrNull(results, Columns.START_RUN_STATE_UUID)),
        toRunState(
            columnNames.contains(Columns.ENDED_AT)
                ? timestampOrNull(results, Columns.ENDED_AT)
                : null,
            uuidOrNull(results, Columns.END_RUN_STATE_UUID)),
        toArgs(
            uuidOrThrow(results, Columns.RUN_ARGS_UUID),
            columnNames.contains(Columns.ARGS) ? stringOrThrow(results, Columns.ARGS) : ""));
  }

  private RunArgsData toArgs(UUID uuid, String args) {
    try {
      return new RunArgsData(
          uuid, Utils.getMapper().readValue(args, new TypeReference<Map<String, String>>() {}));
    } catch (JsonProcessingException e) {
      return new RunArgsData(uuid, new HashMap<>());
    }
  }

  private Optional<RunStateData> toRunState(Instant transitionTime, UUID uuid) {
    if (uuid == null) {
      return Optional.empty();
    }
    return Optional.of(new RunStateData(uuid, transitionTime));
  }

  private Optional<CurrentRunStateData> toCurrentRunState(String runState) {
    if (runState == null) {
      return Optional.empty();
    }
    return Optional.of(new CurrentRunStateData(marquez.common.models.RunState.valueOf(runState)));
  }

  private JobVersionIdData toJobVersionIdData(UUID uuid) {
    return new JobVersionIdData(uuid);
  }

  private List<DatasetVersionIdData> toInputDatasetVersionIdData(List<UUID> input)
      throws SQLException {
    return input.stream().map(DatasetVersionIdData::new).collect(Collectors.toList());
  }
}
