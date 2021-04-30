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

import static marquez.db.Columns.stringArrayOrThrow;
import static marquez.db.Columns.stringOrNull;
import static marquez.db.Columns.stringOrThrow;
import static marquez.db.Columns.timestampOrNull;
import static marquez.db.Columns.timestampOrThrow;
import static marquez.db.Columns.uuidOrNull;
import static marquez.db.mappers.MapperUtils.toFacetsOrNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import marquez.common.Utils;
import marquez.common.models.DatasetId;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetType;
import marquez.common.models.Field;
import marquez.common.models.NamespaceName;
import marquez.common.models.SourceName;
import marquez.common.models.TagName;
import marquez.db.Columns;
import marquez.service.models.Dataset;
import marquez.service.models.DbTable;
import marquez.service.models.Stream;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

@Slf4j
public final class DatasetMapper implements RowMapper<Dataset> {
  private static final ObjectMapper MAPPER = Utils.getMapper();

  @SneakyThrows
  @Override
  public Dataset map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    DatasetType type = DatasetType.valueOf(stringOrThrow(results, Columns.TYPE));

    if (type == DatasetType.DB_TABLE) {
      return new DbTable(
          new DatasetId(
              NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
              DatasetName.of(stringOrThrow(results, Columns.NAME))),
          DatasetName.of(stringOrThrow(results, Columns.NAME)),
          DatasetName.of(stringOrThrow(results, Columns.PHYSICAL_NAME)),
          timestampOrThrow(results, Columns.CREATED_AT),
          timestampOrThrow(results, Columns.UPDATED_AT),
          SourceName.of(stringOrThrow(results, Columns.SOURCE_NAME)),
          toFields(results, "fields"),
          toTags(results, "tags"),
          timestampOrNull(results, Columns.LAST_MODIFIED_AT),
          stringOrNull(results, Columns.DESCRIPTION),
          Optional.ofNullable(uuidOrNull(results, Columns.CURRENT_VERSION_UUID)),
          toFacetsOrNull(results));
    } else {
      return new Stream(
          new DatasetId(
              NamespaceName.of(stringOrThrow(results, Columns.NAMESPACE_NAME)),
              DatasetName.of(stringOrThrow(results, Columns.NAME))),
          DatasetName.of(stringOrThrow(results, Columns.NAME)),
          DatasetName.of(stringOrThrow(results, Columns.PHYSICAL_NAME)),
          timestampOrThrow(results, Columns.CREATED_AT),
          timestampOrThrow(results, Columns.UPDATED_AT),
          SourceName.of(stringOrThrow(results, Columns.SOURCE_NAME)),
          getUrl(results, Columns.SCHEMA_LOCATION),
          toFields(results, "fields"),
          toTags(results, "tags"),
          timestampOrNull(results, Columns.LAST_MODIFIED_AT),
          stringOrNull(results, Columns.DESCRIPTION),
          Optional.ofNullable(uuidOrNull(results, Columns.CURRENT_VERSION_UUID)),
          toFacetsOrNull(results));
    }
  }

  private URL getUrl(ResultSet results, String column) throws SQLException, MalformedURLException {
    if (!Columns.exists(results, column)) {
      return null;
    }
    String url = stringOrNull(results, column);
    if (url == null) {
      return null;
    }
    return new URL(url);
  }

  public static ImmutableSet<TagName> toTags(@NonNull ResultSet results, String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return null;
    }
    List<String> arr = stringArrayOrThrow(results, column);
    return arr.stream().map(TagName::of).collect(ImmutableSet.toImmutableSet());
  }

  public static ImmutableList<Field> toFields(ResultSet results, String column)
      throws SQLException {
    if (results.getObject(column) == null) {
      return ImmutableList.of();
    }
    PGobject pgObject = (PGobject) results.getObject(column);
    try {
      return MAPPER.readValue(pgObject.getValue(), new TypeReference<ImmutableList<Field>>() {});
    } catch (JsonProcessingException e) {
      log.error(String.format("Could not read dataset from job row %s", column), e);
      return ImmutableList.of();
    }
  }
}
