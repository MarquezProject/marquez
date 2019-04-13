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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceType;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.DbName;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.db.Columns;
import marquez.db.mappers.DatasetRowMapper;
import marquez.db.mappers.DbTableInfoRowMapper;
import marquez.db.models.DatasetRow;
import marquez.db.models.DbTableInfoRow;
import marquez.db.models.DbTableVersionRow;
import marquez.service.models.DbTableVersion;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbTableVersionRowMapperTest {
  private static final DatasourceType DATASOURCE_TYPE = DatasourceType.POSTGRESQL;
  private static final DatasourceName DATASOURCE_NAME = DatasourceName.fromString("mydatabase123");
  private static final DbName DB_NAME = DbName.fromString("test_db");
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.fromString(
          String.format(
              "jdbc:%s://localhost:5432/%s",
              DATASOURCE_TYPE.toString().toLowerCase(), DB_NAME.getValue()));
  private static final DatasourceUrn DATASOURCE_URN =
      DatasourceUrn.from(CONNECTION_URL, DATASOURCE_NAME);
  private static final DbSchemaName DB_SCHEMA_NAME = DbSchemaName.fromString("test_schema");
  private static final DbTableName DB_TABLE_NAME = DbTableName.fromString("test_table");
  private static final Instant CREATED_UPDATED_AT = Instant.now();
  final UUID G_UUID = UUID.randomUUID();
  private final String URN = "test_urn";
  private final String DESCRIPTION = "test_description";
  private final String NAME = "test_name";

  @Test
  public void testMap() throws SQLException {
    final Object exists = mock(Object.class);
    final ResultSet resultss = mock(ResultSet.class);
    when(resultss.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(G_UUID);
    when(resultss.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_UPDATED_AT));
    when(resultss.getString(Columns.DB_NAME)).thenReturn(DB_NAME.toString());
    when(resultss.getString(Columns.DB_SCHEMA_NAME)).thenReturn(DB_SCHEMA_NAME.toString());
    final StatementContext context = mock(StatementContext.class);
    final DbTableInfoRowMapper dbTableInfoRowMapper = new DbTableInfoRowMapper();
    final DbTableInfoRow dbTableInfoRow = dbTableInfoRowMapper.map(resultss, context);

    final ResultSet results = mock(ResultSet.class);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(G_UUID);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_UPDATED_AT));
    when(results.getTimestamp(Columns.UPDATED_AT)).thenReturn(Timestamp.from(CREATED_UPDATED_AT));
    when(results.getObject(Columns.NAMESPACE_UUID, UUID.class)).thenReturn(G_UUID);
    when(results.getObject(Columns.DATASOURCE_UUID, UUID.class)).thenReturn(G_UUID);
    when(results.getString(Columns.URN)).thenReturn(URN);
    when(results.getString(Columns.NAME)).thenReturn(NAME);
    when(results.getString(Columns.DESCRIPTION)).thenReturn(DESCRIPTION);
    when(results.getObject(Columns.CURRENT_VERSION_UUID, UUID.class)).thenReturn(G_UUID);
    final DatasetRowMapper datasetRowMapper = new DatasetRowMapper();
    final DatasetRow datasetRow = datasetRowMapper.map(results, context);

    final DbTableVersion dbTableVersion =
        DbTableVersion.builder()
            .connectionUrl(CONNECTION_URL)
            .dbSchemaName(DB_SCHEMA_NAME)
            .dbTableName(DB_TABLE_NAME)
            .build();

    final DbTableVersionRow dbTableVersionRow =
        DbTableVersionRowMapper.map(datasetRow, dbTableInfoRow, dbTableVersion);
    assertNotNull(dbTableVersionRow);
    assertNotNull(dbTableVersionRow.getUuid());
    assertEquals(G_UUID, dbTableVersionRow.getDatasetUuid());
    assertEquals(G_UUID, dbTableVersionRow.getDbTableInfoUuid());
    assertEquals(DB_TABLE_NAME.getValue(), dbTableVersionRow.getDbTable());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullDbTableVersion() {
    final DbTableVersion nullDbTableVersion = null;
    final DatasetRow nullDatasetRow = null;
    final DbTableInfoRow nullDbTableInfoRow = null;
    DbTableVersionRowMapper.map(nullDatasetRow, nullDbTableInfoRow, nullDbTableVersion);
  }
}
