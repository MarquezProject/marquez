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

import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
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
  private static final DatasourceName NAME = newDatasourceName();
  private static final ConnectionUrl CONNECTION_URL = newConnectionUrl();
  private static final DatasourceUrn URN = DatasourceUrn.from(CONNECTION_URL, NAME);

  @Test
  public void testMap() throws SQLException {
    final Object exists = mock(Object.class);
    final ResultSet results = mock(ResultSet.class);
    when(results.getObject(Columns.ROW_UUID)).thenReturn(exists);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.NAME)).thenReturn(exists);
    when(results.getObject(Columns.URN)).thenReturn(exists);
    when(results.getObject(Columns.CONNECTION_URL)).thenReturn(exists);

    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getString(Columns.NAME)).thenReturn(NAME.getValue());
    when(results.getString(Columns.URN)).thenReturn(URN.getValue());
    when(results.getString(Columns.CONNECTION_URL)).thenReturn(CONNECTION_URL.getRawValue());

    final StatementContext context = mock(StatementContext.class);

    final DatasourceRowMapper rowMapper = new DatasourceRowMapper();
    final DatasourceRow row = rowMapper.map(results, context);
    assertThat(ROW_UUID).isEqualTo(row.getUuid());
    assertThat(CREATED_AT).isEqualTo(row.getCreatedAt());
    assertThat(NAME.getValue()).isEqualTo(row.getName());
    assertThat(URN.getValue()).isEqualTo(row.getUrn());
    assertThat(CONNECTION_URL.getRawValue()).isEqualTo(row.getConnectionUrl());
  }

  @Test
  public void testMap_throwsException_onNullResults() throws SQLException {
    final ResultSet nullResults = null;
    final StatementContext context = mock(StatementContext.class);
    final DatasourceRowMapper rowMapper = new DatasourceRowMapper();
    assertThatNullPointerException().isThrownBy(() -> rowMapper.map(nullResults, context));
  }

  @Test
  public void testMap_throwsException_onNullContext() throws SQLException {
    final ResultSet results = mock(ResultSet.class);
    final StatementContext nullContext = null;
    final DatasourceRowMapper rowMapper = new DatasourceRowMapper();
    assertThatNullPointerException().isThrownBy(() -> rowMapper.map(results, nullContext));
  }
}
