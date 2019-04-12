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

package marquez.service.mappers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceType;
import marquez.common.models.DbName;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.service.models.DbTableVersion;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbTableVersionRowMapperTest {
  private static final DbSchemaName DATASOURCE_TYPE = DatasourceType.POSTGRESQL;
  private static final DbName DB_NAME = DbName.fromString("test_db");
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.fromString(
          String.format(
              "jdbc:%s://localhost:5432/%s",
              DATASOURCE_TYPE.toString().toLowerCase(), DB_NAME.getValue()));
  private static final DbSchemaName DB_SCHEMA_NAME = DbSchemaName.fromString("test_schema");
  private static final DbTableName DB_TABLE_NAME = DbTableName.fromString("test_table");
  private static final Description DESCRIPTION = Description.fromString("test description");

  @Test
  public void testMap() {
    final ResultSet results = mock(ResultSet.class);
    wwhen(results.getString(Columns.CONNECTION_URL)).thenReturn(CONNECTION_URL);
    when(results.getString(Columns.DB_SCHEMA_NAME)).thenReturn(DBSCHEMA);
    when(results.getString(Columns.DB_TABLE_NAME)).thenReturn(DB_TABLE_NAME);
    when(results.getString(Columns.DESCRIPTION)).thenReturn(DESCRIPTION);
    final StatementContext context = mock(StatementContext.class);

    final DbTableVersionRowMapper dbTableVersionRowMapper = new DbTableVersionRowMapper();
    final DbTableVersion dbTableVersion = dbTableVersionRowMapper.map(results, context);
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA_NAME, dbTableVersion.getDbSchema());
    assertEquals(DB_TABLE_NAME, dbTableVersion.getDbTable());
    assertEquals(DESCRIPTION, dbTableVersion.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullDbTableVersion() {
    final DbTableVersion nullDbTableVersion = null;
    DbTableVersionRowMapper.map(nullDbTableVersion);
  }
}
