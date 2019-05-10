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

import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static marquez.db.models.DbModelGenerator.newRowUuid;
import static marquez.db.models.DbModelGenerator.newTimestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasetUrn;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;
import marquez.db.Columns;
import marquez.db.models.DatasetRowExtended;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetRowExtendedMapperTest {
  private static final UUID ROW_UUID = newRowUuid();
  private static final Instant CREATED_AT = newTimestamp();
  private static final Instant UPDATED_AT = CREATED_AT;
  private static final UUID NAMESPACE_ROW_UUID = newRowUuid();
  private static final UUID DATASOURCE_ROW_UUID = newRowUuid();
  private static final DatasetName NAME = newDatasetName();
  private static final DatasetUrn URN = newDatasetUrn();
  private static final DatasourceUrn DATASOURCE_URN = newDatasourceUrn();
  private static final Description DESCRIPTION = newDescription();
  private static final UUID CURRENT_VERSION_UUID = newRowUuid();

  private Object exists;
  private ResultSet results;
  private StatementContext context;

  @Before
  public void setUp() {
    exists = mock(Object.class);
    results = mock(ResultSet.class);
    context = mock(StatementContext.class);
  }

  @Test
  public void testMap_row() throws SQLException {
    when(results.getObject(Columns.ROW_UUID)).thenReturn(exists);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.UPDATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.NAMESPACE_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DATASOURCE_UUID)).thenReturn(exists);
    when(results.getObject(Columns.NAME)).thenReturn(exists);
    when(results.getObject(Columns.URN)).thenReturn(exists);
    when(results.getObject(Columns.DATASOURCE_URN)).thenReturn(exists);
    when(results.getObject(Columns.DESCRIPTION)).thenReturn(exists);
    when(results.getObject(Columns.CURRENT_VERSION_UUID)).thenReturn(exists);

    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getTimestamp(Columns.UPDATED_AT)).thenReturn(Timestamp.from(UPDATED_AT));
    when(results.getObject(Columns.NAMESPACE_UUID, UUID.class)).thenReturn(NAMESPACE_ROW_UUID);
    when(results.getObject(Columns.DATASOURCE_UUID, UUID.class)).thenReturn(DATASOURCE_ROW_UUID);
    when(results.getString(Columns.NAME)).thenReturn(NAME.getValue());
    when(results.getString(Columns.URN)).thenReturn(URN.getValue());
    when(results.getString(Columns.DATASOURCE_URN)).thenReturn(DATASOURCE_URN.getValue());
    when(results.getString(Columns.DESCRIPTION)).thenReturn(DESCRIPTION.getValue());
    when(results.getObject(Columns.CURRENT_VERSION_UUID, UUID.class))
        .thenReturn(CURRENT_VERSION_UUID);

    final DatasetRowExtendedMapper rowExtendedMapper = new DatasetRowExtendedMapper();
    final DatasetRowExtended rowExtended = rowExtendedMapper.map(results, context);
    assertThat(rowExtended.getUuid()).isEqualTo(ROW_UUID);
    assertThat(rowExtended.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(rowExtended.getUpdatedAt()).isEqualTo(UPDATED_AT);
    assertThat(rowExtended.getNamespaceUuid()).isEqualTo(NAMESPACE_ROW_UUID);
    assertThat(rowExtended.getDatasourceUuid()).isEqualTo(DATASOURCE_ROW_UUID);
    assertThat(rowExtended.getName()).isEqualTo(NAME.getValue());
    assertThat(rowExtended.getUrn()).isEqualTo(URN.getValue());
    assertThat(rowExtended.getDatasourceUrn()).isEqualTo(DATASOURCE_URN.getValue());
    assertThat(rowExtended.getDescription()).isEqualTo(DESCRIPTION.getValue());
    assertThat(rowExtended.getCurrentVersionUuid()).isEqualTo(CURRENT_VERSION_UUID);
  }

  @Test
  public void testMap_row_noDescription() throws SQLException {
    final Object exists = mock(Object.class);
    final ResultSet results = mock(ResultSet.class);
    when(results.getObject(Columns.ROW_UUID)).thenReturn(exists);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.UPDATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.NAMESPACE_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DATASOURCE_UUID)).thenReturn(exists);
    when(results.getObject(Columns.NAME)).thenReturn(exists);
    when(results.getObject(Columns.URN)).thenReturn(exists);
    when(results.getObject(Columns.DATASOURCE_URN)).thenReturn(exists);
    when(results.getObject(Columns.DESCRIPTION)).thenReturn(NO_DESCRIPTION.getValue());
    when(results.getObject(Columns.CURRENT_VERSION_UUID)).thenReturn(exists);

    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getTimestamp(Columns.UPDATED_AT)).thenReturn(Timestamp.from(UPDATED_AT));
    when(results.getObject(Columns.NAMESPACE_UUID, UUID.class)).thenReturn(NAMESPACE_ROW_UUID);
    when(results.getObject(Columns.DATASOURCE_UUID, UUID.class)).thenReturn(DATASOURCE_ROW_UUID);
    when(results.getString(Columns.NAME)).thenReturn(NAME.getValue());
    when(results.getString(Columns.URN)).thenReturn(URN.getValue());
    when(results.getString(Columns.DATASOURCE_URN)).thenReturn(DATASOURCE_URN.getValue());
    when(results.getObject(Columns.CURRENT_VERSION_UUID, UUID.class))
        .thenReturn(CURRENT_VERSION_UUID);

    final DatasetRowExtendedMapper rowExtendedMapper = new DatasetRowExtendedMapper();
    final DatasetRowExtended rowExtended = rowExtendedMapper.map(results, context);
    assertThat(rowExtended.getUuid()).isEqualTo(ROW_UUID);
    assertThat(rowExtended.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(rowExtended.getUpdatedAt()).isEqualTo(UPDATED_AT);
    assertThat(rowExtended.getNamespaceUuid()).isEqualTo(NAMESPACE_ROW_UUID);
    assertThat(rowExtended.getDatasourceUuid()).isEqualTo(DATASOURCE_ROW_UUID);
    assertThat(rowExtended.getName()).isEqualTo(NAME.getValue());
    assertThat(rowExtended.getUrn()).isEqualTo(URN.getValue());
    assertThat(rowExtended.getDatasourceUrn()).isEqualTo(DATASOURCE_URN.getValue());
    assertThat(rowExtended.getDescription()).isEqualTo(NO_DESCRIPTION.getValue());
    assertThat(rowExtended.getCurrentVersionUuid()).isEqualTo(CURRENT_VERSION_UUID);

    verify(results, never()).getString(Columns.DESCRIPTION);
  }

  @Test
  public void testMap_throwsException_onNullResults() throws SQLException {
    final ResultSet nullResults = null;
    final StatementContext context = mock(StatementContext.class);
    final DatasetRowExtendedMapper rowExtendedMapper = new DatasetRowExtendedMapper();
    assertThatNullPointerException().isThrownBy(() -> rowExtendedMapper.map(nullResults, context));
  }

  @Test
  public void testMap_throwsException_onNullContext() throws SQLException {
    final ResultSet results = mock(ResultSet.class);
    final StatementContext nullContext = null;
    final DatasetRowExtendedMapper rowExtendedMapper = new DatasetRowExtendedMapper();
    assertThatNullPointerException().isThrownBy(() -> rowExtendedMapper.map(results, nullContext));
  }
}
