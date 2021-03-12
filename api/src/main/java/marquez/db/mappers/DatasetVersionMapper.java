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
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.Columns.uuidOrThrow;
import static marquez.db.mappers.DatasetMapper.toFields;
import static marquez.db.mappers.DatasetMapper.toTags;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.Version;
import marquez.db.Columns;
import marquez.service.models.DatasetVersion;
import marquez.service.models.DbTableVersion;
import marquez.service.models.StreamVersion;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

@Slf4j
public final class DatasetVersionMapper implements RowMapper<DatasetVersion> {
  @Override
  public DatasetVersion map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    DatasetType type = DatasetType.valueOf(stringOrThrow(results, Columns.TYPE));
    DatasetVersion datasetVersion;
    if (type == DatasetType.DB_TABLE) {
      datasetVersion =
          new DbTableVersion(
              new DatasetId(
                  NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
                  DatasetName.of(stringOrThrow(results, Columns.NAME))),
              DatasetName.of(stringOrThrow(results, Columns.NAME)),
              DatasetName.of(stringOrThrow(results, Columns.PHYSICAL_NAME)),
              timestampOrThrow(results, Columns.CREATED_AT),
              Version.of(uuidOrThrow(results, Columns.VERSION)),
              SourceName.of(stringOrThrow(results, Columns.SOURCE_NAME)),
              toFields(results, "fields"),
              toTags(results, "tags"),
              stringOrNull(results, Columns.DESCRIPTION),
              null);
      datasetVersion.setCreatedByRunUuid(uuidOrNull(results, "createdByRunUuid"));
      return datasetVersion;
    } else {
      datasetVersion =
          new StreamVersion(
              new DatasetId(
                  NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
                  DatasetName.of(stringOrThrow(results, Columns.NAME))),
              DatasetName.of(stringOrThrow(results, Columns.NAME)),
              DatasetName.of(stringOrThrow(results, Columns.PHYSICAL_NAME)),
              timestampOrThrow(results, Columns.CREATED_AT),
              Version.of(uuidOrThrow(results, Columns.VERSION)),
              SourceName.of(stringOrThrow(results, Columns.SOURCE_NAME)),
              toURL(stringOrThrow(results, Columns.SCHEMA_LOCATION)),
              toFields(results, "fields"),
              toTags(results, "tags"),
              stringOrNull(results, Columns.DESCRIPTION),
              null);
    }
    // The createdByRun can be brought in via join, similar to the JobMapper
    datasetVersion.setCreatedByRunUuid(uuidOrNull(results, "createdByRunUuid"));
    return datasetVersion;
  }

  private URL toURL(String value) {
    try {
      return new URL(value);
    } catch (MalformedURLException e) {
      log.error("Could not decode url {}", value);
      return null;
    }
  }
}
