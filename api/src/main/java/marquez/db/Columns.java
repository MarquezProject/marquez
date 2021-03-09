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

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.postgresql.util.PGInterval;

public final class Columns {
  private Columns() {}

  /* COMMON ROW COLUMNS */
  public static final String ROW_UUID = "uuid";
  public static final String TYPE = "type";
  public static final String CREATED_AT = "created_at";
  public static final String UPDATED_AT = "updated_at";
  public static final String STARTED_AT = "started_at";
  public static final String ENDED_AT = "ended_at";
  public static final String NAME = "name";
  public static final String JOB_NAME = "job_name";
  public static final String VERSION = "version";
  public static final String DESCRIPTION = "description";
  public static final String NAMESPACE_UUID = "namespace_uuid";
  public static final String DATASET_UUID = "dataset_uuid";
  public static final String DATASET_VERSION_UUID = "dataset_version_uuid";
  public static final String JOB_VERSION_UUID = "job_version_uuid";
  public static final String CURRENT_VERSION_UUID = "current_version_uuid";
  public static final String CHECKSUM = "checksum";

  /* COMMON RESULTSET COLUMNS */
  public static final String NAMESPACE_NAME = "namespace_name";
  public static final String DATASET_NAME = "dataset_name";

  /* NAMESPACE ROW COLUMNS */
  public static final String CURRENT_OWNER_NAME = "current_owner_name";

  /* NAMESPACE OWNERSHIP ROW COLUMNS */
  public static final String OWNER_UUID = "owner_uuid";

  /* SOURCE ROW COLUMNS */
  public static final String CONNECTION_URL = "connection_url";

  /* DATASET ROW COLUMNS */
  public static final String SOURCE_UUID = "source_uuid";
  public static final String SOURCE_NAME = "source_name";
  public static final String PHYSICAL_NAME = "physical_name";
  public static final String DATASET_FIELD_UUID = "dataset_field_uuid";
  public static final String TAG_UUID = "tag_uuid";
  public static final String TAG_UUIDS = "tag_uuids";
  public static final String TAGGED_AT = "tagged_at";
  public static final String LAST_MODIFIED_AT = "last_modified_at";

  /* DATASET VERSION ROW COLUMNS */
  public static final String FIELD_UUIDS = "field_uuids";

  /* STREAM VERSION ROW COLUMNS */
  public static final String SCHEMA_LOCATION = "schema_location";

  /* JOB VERSION I/O ROW COLUMNS */
  public static final String INPUT_UUIDS = "input_uuids";
  public static final String OUTPUT_UUIDS = "output_uuids";
  public static final String IO_TYPE = "io_type";

  /* JOB VERSION ROW COLUMNS */
  public static final String JOB_UUID = "job_uuid";
  public static final String JOB_CONTEXT_UUID = "job_context_uuid";
  public static final String LOCATION = "location";
  public static final String LATEST_RUN_UUID = "latest_run_uuid";

  /* JOB CONTEXT ROW COLUMNS */
  public static final String CONTEXT = "context";

  /* RUN ROW COLUMNS */
  public static final String RUN_ARGS_UUID = "run_args_uuid";
  public static final String INPUT_VERSION_UUIDS = "input_version_uuids";
  public static final String NOMINAL_START_TIME = "nominal_start_time";
  public static final String NOMINAL_END_TIME = "nominal_end_time";
  public static final String CURRENT_RUN_STATE = "current_run_state";
  public static final String START_RUN_STATE_UUID = "start_run_state_uuid";
  public static final String END_RUN_STATE_UUID = "end_run_state_uuid";

  /* RUN ARGS ROW COLUMNS */
  public static final String ARGS = "args";

  /* RUN STATE ROW COLUMNS */
  public static final String TRANSITIONED_AT = "transitioned_at";
  public static final String RUN_UUID = "run_uuid";
  public static final String STATE = "state";

  public static UUID uuidOrNull(final ResultSet results, final String column) throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    return results.getObject(column, UUID.class);
  }

  public static UUID uuidOrThrow(final ResultSet results, final String column) throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException();
    }
    return results.getObject(column, UUID.class);
  }

  public static Instant timestampOrNull(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    return results.getTimestamp(column).toInstant();
  }

  public static Instant timestampOrThrow(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException();
    }
    return results.getTimestamp(column).toInstant();
  }

  public static String stringOrNull(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    return results.getString(column);
  }

  public static String stringOrThrow(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException();
    }
    return results.getString(column);
  }

  public static int intOrThrow(final ResultSet results, final String column) throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException();
    }
    return results.getInt(column);
  }

  public static PGInterval pgIntervalOrThrow(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException();
    }
    return new PGInterval(results.getString(column));
  }

  public static BigDecimal bigDecimalOrThrow(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException();
    }
    return results.getBigDecimal(column);
  }

  public static List<UUID> uuidArrayOrThrow(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException();
    }
    return Arrays.asList((UUID[]) results.getArray(column).getArray());
  }

  public static List<String> stringArrayOrThrow(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException();
    }
    return Arrays.asList((String[]) results.getArray(column).getArray());
  }
}
