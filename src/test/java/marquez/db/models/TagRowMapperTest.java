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

package marquez.db.models;

import static marquez.common.models.ModelGenerator.newDescription;
import static marquez.common.models.ModelGenerator.newTagName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.common.models.TagName;
import marquez.db.Columns;
import marquez.db.mappers.TagRowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Test;

public class TagRowMapperTest {

  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final TagName NAME = newTagName();
  private static final Instant CREATED_AT = Instant.now();
  private static final Instant UPDATED_AT = CREATED_AT;
  private static final String DESCRIPTION = newDescription();

  @Test
  public void testMapper() throws SQLException {
    final StatementContext context = mock(StatementContext.class);
    final ResultSet results = mock(ResultSet.class);

    final Object exists = mock(Object.class);

    when(results.getObject(Columns.ROW_UUID)).thenReturn(exists);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.UPDATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.NAME)).thenReturn(exists);
    when(results.getObject(Columns.DESCRIPTION)).thenReturn(exists);

    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getTimestamp(Columns.UPDATED_AT)).thenReturn(Timestamp.from(UPDATED_AT));
    when(results.getString(Columns.NAME)).thenReturn(NAME.getValue());
    when(results.getString(Columns.DESCRIPTION)).thenReturn(DESCRIPTION);

    final TagRow tagRow = new TagRowMapper().map(results, context);

    assertThat(tagRow.getUuid()).isEqualTo(ROW_UUID);
    assertThat(tagRow.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(tagRow.getUpdatedAt()).isEqualTo(UPDATED_AT);
    assertThat(tagRow.getName()).isEqualTo(NAME.getValue());
    assertThat(tagRow.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  public void testMapper_noDescription() throws SQLException {
    final StatementContext context = mock(StatementContext.class);
    final ResultSet results = mock(ResultSet.class);

    final Object exists = mock(Object.class);

    when(results.getObject(Columns.ROW_UUID)).thenReturn(exists);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.UPDATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.NAME)).thenReturn(exists);
    when(results.getObject(Columns.DESCRIPTION)).thenReturn(null);

    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getTimestamp(Columns.UPDATED_AT)).thenReturn(Timestamp.from(UPDATED_AT));
    when(results.getString(Columns.NAME)).thenReturn(NAME.getValue());
    when(results.getString(Columns.DESCRIPTION)).thenReturn(DESCRIPTION);

    final TagRow tagRow = new TagRowMapper().map(results, context);

    assertThat(tagRow.getUuid()).isEqualTo(ROW_UUID);
    assertThat(tagRow.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(tagRow.getUpdatedAt()).isEqualTo(UPDATED_AT);
    assertThat(tagRow.getName()).isEqualTo(NAME.getValue());
    assertThat(tagRow.getDescription()).isNullOrEmpty();
  }

  @Test
  public void testMapper_throwsException_onNullResults() {
    final ResultSet results = null;
    final StatementContext context = mock(StatementContext.class);
    final TagRowMapper rowMapper = new TagRowMapper();

    assertThatNullPointerException().isThrownBy(() -> rowMapper.map(results, context));
  }

  @Test
  public void testMapper_throwsException_onNulContext() {
    final ResultSet results = mock(ResultSet.class);
    final StatementContext context = null;
    final TagRowMapper rowMapper = new TagRowMapper();

    assertThatNullPointerException().isThrownBy(() -> rowMapper.map(results, context));
  }
}
