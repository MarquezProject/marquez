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
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DataSourceRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final String NAME = "postgresql";
  private static final String CONNECTION_URL =
      String.format("jdbc:%s://localhost:5432/test_db", NAME);

  @Test
  public void testNewDataSourceRow() {
    final Optional<Instant> expectedCreatedAt = Optional.of(CREATED_AT);
    final DataSourceRow dataSourceRow =
        DataSourceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .name(NAME)
            .connectionUrl(CONNECTION_URL)
            .build();
    assertEquals(ROW_UUID, dataSourceRow.getUuid());
    assertEquals(expectedCreatedAt, dataSourceRow.getCreatedAt());
    assertEquals(NAME, dataSourceRow.getName());
    assertEquals(CONNECTION_URL, dataSourceRow.getConnectionUrl());
  }

  @Test
  public void testNewDataSourceRow_noCreatedAt() {
    final Optional<Instant> noCreatedAt = Optional.empty();
    final DataSourceRow dataSourceRow =
        DataSourceRow.builder().uuid(ROW_UUID).name(NAME).connectionUrl(CONNECTION_URL).build();
    assertEquals(ROW_UUID, dataSourceRow.getUuid());
    assertEquals(noCreatedAt, dataSourceRow.getCreatedAt());
    assertEquals(NAME, dataSourceRow.getName());
    assertEquals(CONNECTION_URL, dataSourceRow.getConnectionUrl());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDataSourceRow_throwsException_onNullUuid() {
    final UUID nullUuid = null;
    DataSourceRow.builder().uuid(nullUuid).name(NAME).connectionUrl(CONNECTION_URL).build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewDataSourceRow_throwsException_onNullName() {
    final String nullName = null;
    DataSourceRow.builder().uuid(ROW_UUID).name(nullName).connectionUrl(CONNECTION_URL).build();
  }

  @Test(expected = NullPointerException.class)
  public void testNewDataSourceRow_throwsException_onNullConnectionUrl() {
    final String nullConnectionUrl = null;
    DataSourceRow.builder().uuid(ROW_UUID).name(NAME).connectionUrl(nullConnectionUrl).build();
  }
}
