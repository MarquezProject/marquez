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

package marquez.service.models;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DbSchemaName;
import marquez.common.models.DbTableName;
import marquez.common.models.Description;
import marquez.common.models.NamespaceName;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbTableVersionTest {
  private static final NamespaceName NAMESPACE_NAME = NamespaceName.fromString("test_namepace");
  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.fromString("jdbc:postgresql://localhost:5432/test_db");
  private static final DbSchemaName DB_SCHEMA_NAME = DbSchemaName.fromString("test_schema");
  private static final DbTableName DB_TABLE_NAME = DbTableName.fromString("test_table");
  private static final Description DESCRIPTION = Description.fromString("test description");
  private static final String QUALIFIED_NAME =
      DB_SCHEMA_NAME.getValue() + '.' + DB_TABLE_NAME.getValue();
  private static final DatasetUrn DATASET_URN =
      DatasetUrn.from(NAMESPACE_NAME, DatasetName.fromString(QUALIFIED_NAME));

  @Test
  public void testNewDbTableVersion() {
    final Optional<Description> expectedDescription = Optional.of(DESCRIPTION);
    final DbTableVersion dbTableVersion =
        DbTableVersion.builder()
            .connectionUrl(CONNECTION_URL)
            .dbSchemaName(DB_SCHEMA_NAME)
            .dbTableName(DB_TABLE_NAME)
            .description(DESCRIPTION)
            .build();
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA_NAME, dbTableVersion.getDbSchemaName());
    assertEquals(DB_TABLE_NAME, dbTableVersion.getDbTableName());
    assertEquals(QUALIFIED_NAME, dbTableVersion.getQualifiedName());
    assertEquals(expectedDescription, dbTableVersion.getDescription());
    assertEquals(DATASET_URN, dbTableVersion.toDatasetUrn(NAMESPACE_NAME));
  }

  @Test
  public void testNewDbTableVersion_noDescription() {
    final Optional<Description> expectedDescription = Optional.empty();
    final DbTableVersion dbTableVersion =
        DbTableVersion.builder()
            .connectionUrl(CONNECTION_URL)
            .dbSchemaName(DB_SCHEMA_NAME)
            .dbTableName(DB_TABLE_NAME)
            .build();
    assertEquals(CONNECTION_URL, dbTableVersion.getConnectionUrl());
    assertEquals(DB_SCHEMA_NAME, dbTableVersion.getDbSchemaName());
    assertEquals(DB_TABLE_NAME, dbTableVersion.getDbTableName());
    assertEquals(QUALIFIED_NAME, dbTableVersion.getQualifiedName());
    assertEquals(expectedDescription, dbTableVersion.getDescription());
    assertEquals(DATASET_URN, dbTableVersion.toDatasetUrn(NAMESPACE_NAME));
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbTableVersion_throwsException_onNullConnectionUrl() {
    final ConnectionUrl nullConnectionUrl = null;
    DbTableVersion.builder()
        .connectionUrl(nullConnectionUrl)
        .dbSchemaName(DB_SCHEMA_NAME)
        .dbTableName(DB_TABLE_NAME)
        .description(DESCRIPTION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbTableVersion_throwsException_onNullDbSchemaName() {
    final DbSchemaName nullDbSchemaName = null;
    DbTableVersion.builder()
        .connectionUrl(CONNECTION_URL)
        .dbSchemaName(nullDbSchemaName)
        .dbTableName(DB_TABLE_NAME)
        .description(DESCRIPTION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbTableVersion_throwsException_onNullDbTableName() {
    final DbTableName nullDbTableName = null;
    DbTableVersion.builder()
        .connectionUrl(CONNECTION_URL)
        .dbSchemaName(DB_SCHEMA_NAME)
        .dbTableName(nullDbTableName)
        .description(DESCRIPTION)
        .build();
  }

  @Test(expected = NullPointerException.class)
  public void testToDatasetUrn_throwsException_onNullNamespaceName() {
    final NamespaceName nullNamespaceName = null;
    final DbTableVersion dbTableVersion =
        DbTableVersion.builder()
            .connectionUrl(CONNECTION_URL)
            .dbSchemaName(DB_SCHEMA_NAME)
            .dbTableName(DB_TABLE_NAME)
            .description(DESCRIPTION)
            .build();
    dbTableVersion.toDatasetUrn(nullNamespaceName);
  }
}
