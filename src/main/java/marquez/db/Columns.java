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

package marquez.db;

import static marquez.common.Preconditions.checkNotBlank;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class Columns {
  private Columns() {}

  // Common column names

  public static final String ROW_UUID = "guid";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
  public static final String NAME = "name";
  public static final String DESCRIPTION = "description";

  // Namespace ownership column names

  public static final String NAMESPACE_UUID = "namespace_guid";
  public static final String STARTED_AT = "started_at";
  public static final String ENDED_AT = "ended_at";
  public static final String OWNER_UUID = "owner_uuid";
  public static final String CURRENT_OWNER_NAME = "current_ownership";

  // Job column names

  public static final String JOB_UUID = "job_guid";
  public static final String JOB_VERSION_UUID = "job_version_guid";
  public static final String INPUT_DATASET_URNS = "input_dataset_urns";
  public static final String OUTPUT_DATASET_URNS = "output_dataset_urns";
  public static final String LOCATION = "uri";
  public static final String VERSION = "version";
  public static final String CURRENT_VERSION_UUID = "current_version_uuid";
  public static final String JOB_RUN_UUID = "job_run_guid";
  public static final String NOMINAL_START_TIME = "nominal_start_time";
  public static final String NOMINAL_END_TIME = "nominal_end_time";
  public static final String LATEST_JOB_RUN_UUID = "latest_run_guid";
  public static final String CURRENT_RUN_STATE = "current_state";
  public static final String CHECKSUM = "hex_digest";
  public static final String RUN_ARGS_CHECKSUM = "job_run_args_hex_digest"; // TODO: revisit usage
  public static final String RUN_ARGS = "args_json";
  public static final String RUN_STATE = "state";
  public static final String TRANSITIONED_AT = "transitioned_at";

  // Dataset column names

  public static final String DATASET_UUID = "dataset_uuid";
  public static final String URN = "urn";
  public static final String DATA_SOURCE_UUID = "datasource_uuid";
  public static final String CONNECTION_URL = "connection_url";
  public static final String DB_TABLE_INFO_UUID = "db_table_info_uuid";
  public static final String DB_NAME = "db";
  public static final String DB_SCHEMA_NAME = "db_schema";
  public static final String DB_TABLE_NAME = "db_table_name";

  public static Instant toInstantOrNull(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toInstant();
  }

  public static UUID toUuidOrNull(String uuidString) {
    return uuidString == null ? null : UUID.fromString(checkNotBlank(uuidString));
  }

  public static List<String> toList(Array array) throws SQLException {
    return array == null ? Collections.emptyList() : Arrays.asList((String[]) array.getArray());
  }
}
