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

import com.google.common.collect.ImmutableSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import lombok.NonNull;
import marquez.common.models.Field;
import marquez.common.models.FieldName;
import marquez.common.models.TagName;
import marquez.db.Columns;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

public final class DatasetFieldMapper implements RowMapper<Field> {
  @Override
  public Field map(@NonNull ResultSet results, @NonNull StatementContext context)
      throws SQLException {
    return new Field(
        FieldName.of(stringOrThrow(results, Columns.NAME)),
        stringOrNull(results, Columns.TYPE),
        toTagNames(stringArrayOrThrow(results, Columns.TAGS)),
        stringOrNull(results, Columns.DESCRIPTION));
  }

  private ImmutableSet<TagName> toTagNames(List<String> tags) {
    if (tags == null) {
      return ImmutableSet.of();
    }
    return tags.stream().map(TagName::of).collect(ImmutableSet.toImmutableSet());
  }
}
