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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.UnitTests;
import marquez.db.Columns;
import marquez.db.models.DbTableVersionRow;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbTableVersionRowMapperTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final UUID DATASET_UUID = UUID.randomUUID();
  private static final UUID DB_TABLE_INFO_UUID = UUID.randomUUID();
  private static final String DB_TABLE_NAME = "Test_Table_Name";

  @Test
  public void testMap() throws SQLException {
    final Object exists = mock(Object.class);
    final ResultSet results = mock(ResultSet.class);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);

    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getObject(Columns.DATASET_UUID, UUID.class)).thenReturn(DATASET_UUID);
    when(results.getObject(Columns.DB_TABLE_INFO_UUID, UUID.class)).thenReturn(DB_TABLE_INFO_UUID);
    when(results.getString(Columns.DB_TABLE_NAME)).thenReturn(DB_TABLE_NAME);

    final StatementContext context = mock(StatementContext.class);

    final DbTableVersionRowMapper dbTableVersionRowMapper = new DbTableVersionRowMapper();
    final DbTableVersionRow dbTableVersionRow = dbTableVersionRowMapper.map(results, context);
    assertEquals(ROW_UUID, dbTableVersionRow.getUuid());
    assertEquals(CREATED_AT, dbTableVersionRow.getCreatedAt());
    assertEquals(DATASET_UUID, dbTableVersionRow.getDatasetUuid());
    assertEquals(DB_TABLE_INFO_UUID, dbTableVersionRow.getDbTableInfoUuid());
    assertEquals(DB_TABLE_NAME, dbTableVersionRow.getDbTable());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullResults() throws SQLException {
    final ResultSet nullResults = null;
    final StatementContext context = mock(StatementContext.class);
    final DbTableVersionRowMapper dbTableVersionRowMapper = new DbTableVersionRowMapper();
    dbTableVersionRowMapper.map(nullResults, context);
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullContext() throws SQLException {
    final ResultSet results = mock(ResultSet.class);
    final StatementContext nullContext = null;
    final DbTableVersionRowMapper dbTableVersionRowMapper = new DbTableVersionRowMapper();
    dbTableVersionRowMapper.map(results, nullContext);
  }
}
