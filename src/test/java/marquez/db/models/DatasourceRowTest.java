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

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final String TYPE = "postgresql";
  private static final String NAME = "mydatabase123";
  private static final String CONNECTION_URL =
      String.format("jdbc:%s://localhost:5432/test_db", TYPE);
  private static final String DATASOURCE_URN =
      DatasourceUrn.from(ConnectionUrl.fromString(CONNECTION_URL), DatasourceName.fromString(NAME))
          .toString();

  @Test
  public void testNewDatasourceRow() {
    final Optional<Instant> expectedCreatedAt = Optional.of(CREATED_AT);
    final DatasourceRow datasourceRow =
        DatasourceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .urn(DATASOURCE_URN)
            .name(NAME)
            .connectionUrl(CONNECTION_URL)
            .build();
    assertEquals(ROW_UUID, datasourceRow.getUuid());
    assertEquals(expectedCreatedAt, datasourceRow.getCreatedAt());
    assertEquals(NAME, datasourceRow.getName());
    assertEquals(CONNECTION_URL, datasourceRow.getConnectionUrl());
  }

  @Test
  public void testNewDatasourceRow_noCreatedAt() {
    final Optional<Instant> noCreatedAt = Optional.empty();
    final DatasourceRow datasourceRow =
        DatasourceRow.builder()
            .uuid(ROW_UUID)
            .urn(DATASOURCE_URN)
            .name(NAME)
            .connectionUrl(CONNECTION_URL)
            .build();
    assertEquals(ROW_UUID, datasourceRow.getUuid());
    assertEquals(noCreatedAt, datasourceRow.getCreatedAt());
    assertEquals(NAME, datasourceRow.getName());
    assertEquals(CONNECTION_URL, datasourceRow.getConnectionUrl());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDatasourceRow_throwsException_onNullUuid() {
    final UUID nullUuid = null;
    DatasourceRow.builder().uuid(nullUuid).name(NAME).connectionUrl(CONNECTION_URL).build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewDatasourceRow_throwsException_onNullName() {
    final String nullName = null;
    DatasourceRow.builder().uuid(ROW_UUID).name(nullName).connectionUrl(CONNECTION_URL).build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewDatasourceRow_throwsException_onNullConnectionUrl() {
    final String nullConnectionUrl = null;
    DatasourceRow.builder().uuid(ROW_UUID).name(NAME).connectionUrl(nullConnectionUrl).build();
  }
}
