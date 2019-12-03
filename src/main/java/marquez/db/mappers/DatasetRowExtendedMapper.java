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

import static marquez.db.Columns.*;
import static marquez.db.Columns.uuidOrThrow;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.NonNull;
import marquez.db.models.DatasetRowExtended;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetRowExtendedMapper implements RowMapper<DatasetRowExtended> {
  @Override
  public DatasetRowExtended map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return DatasetRowExtended.builder()
        .dsUuid(uuidOrThrow(results, "dataset_uuid"))
        .dsName(stringOrThrow(results, "dataset_name"))
        .dsCreatedAt(timestampOrThrow(results, "dataset_created_at"))
        .dsUpdatedAt(timestampOrThrow(results, "dataset_updated_at"))
        .dsDescription(stringOrNull(results, "dataset_description"))
        .dfUuid(uuidOrNull(results, "dataset_field_uuid"))
        .dfName(stringOrNull(results, "dataset_field_name"))
        .dfType(stringOrNull(results, "dataset_field_type"))
        .dfDescription(stringOrNull(results, "dataset_field_description"))
        .dfCreatedAt(timestampOrNull(results, "dataset_field_created_at"))
        .dfUpdatedAt(timestampOrNull(results, "dataset_field_updated_at"))
        .taggedAt(timestampOrNull(results, "tagged_at"))
        .tagName(stringOrNull(results, "tag_name"))
        .build();
  }
}
