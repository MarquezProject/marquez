/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import org.postgresql.util.PGInterval;
import org.postgresql.util.PGobject;

@Slf4j
public final class Columns {

  private Columns() {}

  private static final ObjectMapper MAPPER = Utils.getMapper();

  /* COMMON ROW COLUMNS */
  public static final String ROW_UUID = "uuid";
  public static final String PARENT_RUN_UUID = "parent_run_uuid";
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
  public static final String DATASET_SCHEMA_VERSION_UUID = "dataset_schema_version_uuid";
  public static final String JOB_VERSION_UUID = "job_version_uuid";
  public static final String CURRENT_VERSION_UUID = "current_version_uuid";
  public static final String CHECKSUM = "checksum";
  public static final String NAMESPACE_NAME = "namespace_name";
  public static final String DATASET_NAME = "dataset_name";
  public static final String FACETS = "facets";
  public static final String DATASET_FACETS = "dataset_facets";
  public static final String TAGS = "tags";
  public static final String IS_HIDDEN = "is_hidden";

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
  public static final String IS_DELETED = "is_deleted";

  /* DATASET VERSION ROW COLUMNS */
  public static final String FIELD_UUIDS = "field_uuids";
  public static final String LIFECYCLE_STATE = "lifecycle_state";

  /* DATASET SYMLINK ROW COLUMNS */
  public static final String IS_PRIMARY = "is_primary";

  /* STREAM VERSION ROW COLUMNS */
  public static final String SCHEMA_LOCATION = "schema_location";

  /* JOB ROW COLUMNS */
  public static final String PARENT_JOB_NAME = "parent_job_name";
  public static final String PARENT_JOB_UUID = "parent_job_uuid";
  public static final String SIMPLE_NAME = "simple_name";
  public static final String SYMLINK_TARGET_UUID = "symlink_target_uuid";
  public static final String CURRENT_RUN_UUID = "current_run_uuid";

  /* JOB VERSION I/O ROW COLUMNS */
  public static final String INPUT_UUIDS = "input_uuids";
  public static final String OUTPUT_UUIDS = "output_uuids";
  public static final String IO_TYPE = "io_type";

  public static final String INPUT_DATASETS = "input_datasets";
  public static final String OUTPUT_DATASETS = "output_datasets";

  /* JOB VERSION ROW COLUMNS */
  public static final String JOB_UUID = "job_uuid";
  public static final String LOCATION = "location";
  public static final String LATEST_RUN_UUID = "latest_run_uuid";

  /* RUN ROW COLUMNS */
  public static final String EXTERNAL_ID = "external_id";
  public static final String RUN_ARGS_UUID = "run_args_uuid";
  public static final String INPUT_VERSION_UUIDS = "input_version_uuids";
  public static final String NOMINAL_START_TIME = "nominal_start_time";
  public static final String NOMINAL_END_TIME = "nominal_end_time";
  public static final String CURRENT_RUN_STATE = "current_run_state";
  public static final String START_RUN_STATE_UUID = "start_run_state_uuid";
  public static final String END_RUN_STATE_UUID = "end_run_state_uuid";

  public static final String JOB_VERSION = "job_version";
  public static final String INPUT_VERSIONS = "input_versions";
  public static final String OUTPUT_VERSIONS = "output_versions";

  /* RUN ARGS ROW COLUMNS */
  public static final String ARGS = "args";

  /* RUN STATE ROW COLUMNS */
  public static final String TRANSITIONED_AT = "transitioned_at";
  public static final String RUN_UUID = "run_uuid";
  public static final String STATE = "state";

  /* COLUMN LEVEL LINEAGE ROW COLUMNS */
  public static final String FIELD_NAME = "field_name";
  public static final String OUTPUT_DATASET_VERSION_UUID = "output_dataset_version_uuid";
  public static final String OUTPUT_DATASET_FIELD_UUID = "output_dataset_field_uuid";
  public static final String INPUT_DATASET_FIELD_UUID = "input_dataset_field_uuid";
  public static final String INPUT_DATASET_VERSION_UUID = "input_dataset_version_uuid";
  public static final String TRANSFORMATION_DESCRIPTION = "transformation_description";
  public static final String TRANSFORMATION_TYPE = "transformation_type";

  /* LINEAGE EVENT ROW COLUMNS */
  public static final String EVENT = "event";
  public static final String EVENT_TIME = "event_time";

  /* METRICS ROW COLUMNS */
  public static final String START_INTERVAL = "start_interval";
  public static final String END_INTERVAL = "end_interval";
  public static final String START = "start";
  public static final String COMPLETE = "complete";
  public static final String FAIL = "fail";
  public static final String ABORT = "abort";
  public static final String COUNT = "count";

  /* ALERT ROW COLUMNS */
  public static final String CONFIG = "config";
  public static final String ENTITY_TYPE = "entity_type";
  public static final String ENTITY_UUID = "entity_uuid";
  public static final String ARCHIVED_AT = "archived_at";

  public static UUID uuidOrNull(final ResultSet results, final String column) throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    return results.getObject(column, UUID.class);
  }

  public static UUID uuidOrThrow(final ResultSet results, final String column) throws SQLException {
    checkNotNull(results, column);
    return results.getObject(column, UUID.class);
  }

  public static Instant timestampOrNull(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    return results.getTimestamp(column).toInstant();
  }

  public static JsonNode jsonOrNull(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    String jsonString = results.getString(column);
    try {
      return MAPPER.readTree(jsonString);
    } catch (Exception e) {
      throw new SQLException("Failed to parse JSONB column: " + column, e);
    }
  }

  public static Instant timestampOrThrow(final ResultSet results, final String column)
      throws SQLException {
    checkNotNull(results, column);
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
    checkNotNull(results, column);
    return results.getString(column);
  }

  public static boolean booleanOrDefault(
      final ResultSet results, final String column, final boolean defaultValue)
      throws SQLException {
    if (results.getObject(column) == null) {
      return defaultValue;
    }
    return results.getBoolean(column);
  }

  public static boolean booleanOrThrow(final ResultSet results, final String column)
      throws SQLException {
    checkNotNull(results, column);
    return results.getBoolean(column);
  }

  public static int intOrThrow(final ResultSet results, final String column) throws SQLException {
    checkNotNull(results, column);
    return results.getInt(column);
  }

  public static PGInterval pgIntervalOrThrow(final ResultSet results, final String column)
      throws SQLException {
    checkNotNull(results, column);
    return new PGInterval(results.getString(column));
  }

  public static BigDecimal bigDecimalOrThrow(final ResultSet results, final String column)
      throws SQLException {
    checkNotNull(results, column);
    return results.getBigDecimal(column);
  }

  public static List<UUID> uuidArrayOrThrow(final ResultSet results, final String column)
      throws SQLException {
    checkNotNull(results, column);
    return Arrays.asList((UUID[]) results.getArray(column).getArray());
  }

  public static List<UUID> uuidArrayOrEmpty(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return Collections.emptyList();
    }
    return Arrays.asList((UUID[]) results.getArray(column).getArray());
  }

  public static List<String> stringArrayOrThrow(final ResultSet results, final String column)
      throws SQLException {
    checkNotNull(results, column);
    return Arrays.asList((String[]) results.getArray(column).getArray());
  }

  public static URI uriOrNull(ResultSet results, String column) {
    try {
      final String result = stringOrNull(results, column);
      if (result == null || result.isBlank()) {
        return null;
      }
      return new URI(result);
    } catch (URISyntaxException | SQLException e) {
      log.warn("Could not read source URI for column '{}' null would be returned", column, e);
      return null;
    }
  }

  public static URL urlOrNull(ResultSet results, String column) {
    try {
      final String result = stringOrNull(results, column);
      if (result == null || result.isBlank()) {
        return null;
      }
      return new URL(result);
    } catch (SQLException | MalformedURLException e) {
      log.warn("Could not read source URI for column '{}' null would be returned", column, e);
      return null;
    }
  }

  public static boolean exists(final ResultSet results, @NonNull final String column)
      throws SQLException {
    final ResultSetMetaData resultSetMetaData = results.getMetaData();
    final int columnCount = resultSetMetaData.getColumnCount();
    for (int i = 1; i <= columnCount; i++) {
      if (resultSetMetaData.getColumnName(i).equals(column)) {
        return true;
      }
    }
    return false;
  }

  public static ImmutableMap<String, String> mapOrNull(final ResultSet results, final String column)
      throws SQLException {
    if (results.getString(column) == null) {
      return null;
    }
    final String mapAsString = results.getString(column);
    return Utils.fromJson(mapAsString, new TypeReference<>() {});
  }

  public static PGobject toPgObject(@NonNull final Object object) {
    final PGobject jsonObject = new PGobject();
    jsonObject.setType("jsonb");
    final String json = Utils.toJson(object);
    try {
      jsonObject.setValue(json);
    } catch (SQLException e) {
      log.error("Error when ...", e);
      return null;
    }
    return jsonObject;
  }

  private static void checkNotNull(final ResultSet results, final String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      throw new IllegalArgumentException(column + " not found in result");
    }
  }
}
