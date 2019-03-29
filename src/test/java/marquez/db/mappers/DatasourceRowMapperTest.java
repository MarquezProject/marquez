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
import java.util.Optional;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.Columns;
import marquez.db.models.DatasourceRow;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceRowMapperTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final String NAME = "mypostgresqldb";
  private static final String DB_TYPE = "postgresql";
  private static final String CONNECTION_URL =
      String.format("jdbc:%s://localhost:5432/test_db", DB_TYPE);
  private static final String URN =
      DatasourceUrn.from(ConnectionUrl.fromString(CONNECTION_URL), DatasourceName.fromString(NAME))
          .getValue();

  @Test
  public void testMap() throws SQLException {
    final Optional<Instant> expectedCreatedAt = Optional.of(CREATED_AT);
    final ResultSet results = mock(ResultSet.class);
    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getString(Columns.NAME)).thenReturn(NAME);
    when(results.getString(Columns.CONNECTION_URL)).thenReturn(CONNECTION_URL);
    when(results.getString(Columns.URN)).thenReturn(URN);
    final StatementContext context = mock(StatementContext.class);

    final DatasourceRowMapper datasourceRowMapper = new DatasourceRowMapper();
    final DatasourceRow datasourceRow = datasourceRowMapper.map(results, context);
    assertEquals(ROW_UUID, datasourceRow.getUuid());
    assertEquals(expectedCreatedAt, datasourceRow.getCreatedAt());
    assertEquals(URN, datasourceRow.getUrn());
    assertEquals(NAME, datasourceRow.getName());
    assertEquals(CONNECTION_URL, datasourceRow.getConnectionUrl());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullResults() throws SQLException {
    final ResultSet nullResults = null;
    final StatementContext context = mock(StatementContext.class);
    final DatasourceRowMapper datasourceRowMapper = new DatasourceRowMapper();
    datasourceRowMapper.map(nullResults, context);
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullContext() throws SQLException {
    final ResultSet results = mock(ResultSet.class);
    final StatementContext nullContext = null;
    final DatasourceRowMapper datasourceRowMapper = new DatasourceRowMapper();
    datasourceRowMapper.map(results, nullContext);
  }
}
