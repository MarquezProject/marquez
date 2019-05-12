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

import marquez.UnitTests;
import marquez.common.types.ConnectionUrl;
import marquez.common.types.DatasourceName;
import marquez.common.types.DatasourceType;
import marquez.common.types.DatasourceUrn;
import marquez.common.types.DbName;
import marquez.common.types.DbSchemaName;
import marquez.common.types.DbTableName;
import marquez.db.models.DatasourceRow;
import marquez.service.models.DbTableVersion;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceRowMapperTest {
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

  @Test
  public void testMap() {
    final DbTableVersion dbTableVersion =
        DbTableVersion.builder()
            .connectionUrl(CONNECTION_URL)
            .dbSchemaName(DB_SCHEMA_NAME)
            .dbTableName(DB_TABLE_NAME)
            .build();
    final DatasourceRow datasourceRow = DatasourceRowMapper.map(dbTableVersion);
    assertNotNull(datasourceRow);
    assertNotNull(datasourceRow.getUuid());
    assertEquals(DB_NAME.getValue(), datasourceRow.getName());
    assertEquals(CONNECTION_URL.getRawValue(), datasourceRow.getConnectionUrl());
  }

  @Test(expected = NullPointerException.class)
  public void testMap_throwsException_onNullDbTableVersion() {
    final DbTableVersion nullDbTableVersion = null;
    DatasourceRowMapper.map(nullDbTableVersion);
  }
}
