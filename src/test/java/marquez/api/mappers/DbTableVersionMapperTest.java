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

package marquez.api.mappers;

import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import marquez.api.models.DatasetType;
import marquez.api.models.DbTableVersionRequest;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.service.models.DbTableVersion;
import org.junit.Test;

public class DbTableVersionMapperTest {
  private static final DatasetType DB_TYPE = DatasetType.DB;
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.fromString("jdbc:postgresql://localhost:5431/novelists");
  private static final DbSchemaName DB_SCHEMA_NAME = DbSchemaName.fromString("marquez");
  private static final DbTableName DB_TABLE_NAME = DbTableName.fromString("quotes");
  private static final Description DESCRIPTION =
      Description.fromString("It's enough for me to be sure that you and I exist as this moment.");

  @Test
  public void testMapDbTableVersionRequest() {
    final Optional<Description> nonEmptyDescription = Optional.of(DESCRIPTION);
    final DbTableVersionRequest dbTableVersionRequest =
        new DbTableVersionRequest(
            DB_TYPE,
            CONNECTION_URL.getRawValue(),
            DB_SCHEMA_NAME.getValue(),
            DB_TABLE_NAME.getValue(),
            DESCRIPTION.getValue());
    final DbTableVersion dbTableVersion = DbTableVersionMapper.map(dbTableVersionRequest);
    assertNotNull(dbTableVersion);
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA_NAME, dbTableVersion.getDbSchemaName());
    assertEquals(DB_TABLE_NAME, dbTableVersion.getDbTableName());
    assertEquals(nonEmptyDescription, dbTableVersion.getDescription());
  }

  @Test
  public void testMapDbTableVersionRequestNoDescription() {
    final Optional<Description> noDescription = Optional.of(NO_DESCRIPTION);
    final DbTableVersionRequest dbTableVersionRequest =
        new DbTableVersionRequest(
            DB_TYPE,
            CONNECTION_URL.getRawValue(),
            DB_SCHEMA_NAME.getValue(),
            DB_TABLE_NAME.getValue(),
            NO_DESCRIPTION.getValue());
    final DbTableVersion dbTableVersion = DbTableVersionMapper.map(dbTableVersionRequest);
    assertNotNull(dbTableVersion);
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA_NAME, dbTableVersion.getDbSchemaName());
    assertEquals(DB_TABLE_NAME, dbTableVersion.getDbTableName());
    assertEquals(noDescription, dbTableVersion.getDescription());
  }

  @Test(expected = NullPointerException.class)
  public void testMapNullDbTableVersionRequest() {
    final DbTableVersionRequest nullDbTableVersionRequest = null;
    DbTableVersionMapper.map(nullDbTableVersionRequest);
  }
}
