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

import static marquez.common.models.ModelGenerator.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.common.models.DatasetName;
import marquez.common.models.FieldName;
import marquez.common.models.TagName;
import marquez.db.Columns;
import marquez.db.mappers.DatasetRowExtendedMapper;
import marquez.db.mappers.DatasetRowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Test;

public class DatasetRowExtendedMapperTest {
  private static final UUID DATASET_UUID = UUID.randomUUID();
  private static final UUID DATASET_FIELD_UUID = UUID.randomUUID();

  private static final DatasetName DATASET_NAME = newDatasetName();
  private static final FieldName DATASET_FIELD_NAME = newFieldName();
  private static final TagName TAG_NAME = newTagName();

  private static final Instant DATASET_CREATED_AT = Instant.now();
  private static final Instant DATASET_FIELD_CREATED_AT = Instant.now();
  private static final Instant DATASET_UPDATED_AT = DATASET_CREATED_AT;
  private static final Instant DATASET_FIELD_UPDATED_AT = DATASET_FIELD_CREATED_AT;
  private static final Instant TAGGED_AT = Instant.now().plusSeconds(15);

  private static final String DATASET_DESCRIPTION = newDescription();
  private static final String DATASET_FIELD_DESCRIPTION = newDescription();

  @Test
  public void testMapper() throws SQLException {
    final StatementContext context = mock(StatementContext.class);
    final ResultSet results = mock(ResultSet.class);
    final Object exists = mock(Object.class);

    when(results.getObject(Columns.DATASET_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_NAME)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_NAME)).thenReturn(exists);
    when(results.getObject(Columns.TAG_NAME)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_UPDATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_UPDATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.TAGGED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_DESCRIPTION)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_DESCRIPTION)).thenReturn(exists);

    when(results.getObject(Columns.DATASET_UUID, UUID.class)).thenReturn(DATASET_UUID);
    when(results.getObject(Columns.DATASET_FIELD_UUID, UUID.class)).thenReturn(DATASET_FIELD_UUID);
    when(results.getString(Columns.DATASET_NAME)).thenReturn(DATASET_NAME.getValue());
    when(results.getString(Columns.DATASET_FIELD_NAME)).thenReturn(DATASET_FIELD_NAME.getValue());
    when(results.getString(Columns.TAG_NAME)).thenReturn(TAG_NAME.getValue());
    when(results.getTimestamp(Columns.DATASET_CREATED_AT))
        .thenReturn(Timestamp.from(DATASET_CREATED_AT));
    when(results.getTimestamp(Columns.DATASET_FIELD_CREATED_AT))
        .thenReturn(Timestamp.from(DATASET_FIELD_CREATED_AT));
    when(results.getTimestamp(Columns.DATASET_UPDATED_AT))
        .thenReturn(Timestamp.from(DATASET_UPDATED_AT));
    when(results.getTimestamp(Columns.DATASET_FIELD_UPDATED_AT))
        .thenReturn(Timestamp.from(DATASET_FIELD_UPDATED_AT));
    when(results.getTimestamp(Columns.TAGGED_AT)).thenReturn(Timestamp.from(TAGGED_AT));
    when(results.getString(Columns.DATASET_DESCRIPTION)).thenReturn(DATASET_DESCRIPTION);
    when(results.getString(Columns.DATASET_FIELD_DESCRIPTION))
        .thenReturn(DATASET_FIELD_DESCRIPTION);

    final DatasetRowExtended datasetRowExtended =
        new DatasetRowExtendedMapper().map(results, context);

    assertThat(datasetRowExtended.getDsUuid()).isEqualTo(DATASET_UUID);
    assertThat(datasetRowExtended.getDsCreatedAt()).isEqualTo(DATASET_CREATED_AT);
    assertThat(datasetRowExtended.getDsUpdatedAt()).isEqualTo(DATASET_UPDATED_AT);
    assertThat(datasetRowExtended.getDsName()).isEqualTo(DATASET_NAME.getValue());
    assertThat(datasetRowExtended.getDsDescription()).isEqualTo(DATASET_DESCRIPTION);

    assertThat(datasetRowExtended.getDfUuid()).isEqualTo(DATASET_FIELD_UUID);
    assertThat(datasetRowExtended.getDfCreatedAt()).isEqualTo(DATASET_FIELD_CREATED_AT);
    assertThat(datasetRowExtended.getDfUpdatedAt()).isEqualTo(DATASET_FIELD_UPDATED_AT);
    assertThat(datasetRowExtended.getDfName()).isEqualTo(DATASET_FIELD_NAME.getValue());
    assertThat(datasetRowExtended.getDfDescription()).isEqualTo(DATASET_FIELD_DESCRIPTION);

    assertThat(datasetRowExtended.getTagName()).isEqualTo(TAG_NAME.getValue());
    assertThat(datasetRowExtended.getTaggedAt()).isEqualTo(TAGGED_AT);
  }

  @Test
  public void testMapper_noDatasetFieldDescription() throws SQLException {
    final StatementContext context = mock(StatementContext.class);
    final ResultSet results = mock(ResultSet.class);
    final Object exists = mock(Object.class);

    when(results.getObject(Columns.DATASET_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_NAME)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_NAME)).thenReturn(exists);
    when(results.getObject(Columns.TAG_NAME)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_UPDATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_UPDATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.TAGGED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_DESCRIPTION)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_DESCRIPTION)).thenReturn(null);

    when(results.getObject(Columns.DATASET_UUID, UUID.class)).thenReturn(DATASET_UUID);
    when(results.getObject(Columns.DATASET_FIELD_UUID, UUID.class)).thenReturn(DATASET_FIELD_UUID);
    when(results.getString(Columns.DATASET_NAME)).thenReturn(DATASET_NAME.getValue());
    when(results.getString(Columns.DATASET_FIELD_NAME)).thenReturn(DATASET_FIELD_NAME.getValue());
    when(results.getString(Columns.TAG_NAME)).thenReturn(TAG_NAME.getValue());
    when(results.getTimestamp(Columns.DATASET_CREATED_AT))
        .thenReturn(Timestamp.from(DATASET_CREATED_AT));
    when(results.getTimestamp(Columns.DATASET_FIELD_CREATED_AT))
        .thenReturn(Timestamp.from(DATASET_FIELD_CREATED_AT));
    when(results.getTimestamp(Columns.DATASET_UPDATED_AT))
        .thenReturn(Timestamp.from(DATASET_UPDATED_AT));
    when(results.getTimestamp(Columns.DATASET_FIELD_UPDATED_AT))
        .thenReturn(Timestamp.from(DATASET_FIELD_UPDATED_AT));
    when(results.getTimestamp(Columns.TAGGED_AT)).thenReturn(Timestamp.from(TAGGED_AT));
    when(results.getString(Columns.DATASET_DESCRIPTION)).thenReturn(DATASET_DESCRIPTION);
    when(results.getString(Columns.DATASET_FIELD_DESCRIPTION))
        .thenReturn(DATASET_FIELD_DESCRIPTION);

    final DatasetRowExtended datasetRowExtended =
        new DatasetRowExtendedMapper().map(results, context);

    assertThat(datasetRowExtended.getDsUuid()).isEqualTo(DATASET_UUID);
    assertThat(datasetRowExtended.getDsCreatedAt()).isEqualTo(DATASET_CREATED_AT);
    assertThat(datasetRowExtended.getDsUpdatedAt()).isEqualTo(DATASET_UPDATED_AT);
    assertThat(datasetRowExtended.getDsName()).isEqualTo(DATASET_NAME.getValue());
    assertThat(datasetRowExtended.getDsDescription()).isEqualTo(DATASET_DESCRIPTION);

    assertThat(datasetRowExtended.getDfUuid()).isEqualTo(DATASET_FIELD_UUID);
    assertThat(datasetRowExtended.getDfCreatedAt()).isEqualTo(DATASET_FIELD_CREATED_AT);
    assertThat(datasetRowExtended.getDfUpdatedAt()).isEqualTo(DATASET_FIELD_UPDATED_AT);
    assertThat(datasetRowExtended.getDfName()).isEqualTo(DATASET_FIELD_NAME.getValue());
    assertThat(datasetRowExtended.getDfDescription()).isNullOrEmpty();

    assertThat(datasetRowExtended.getTagName()).isEqualTo(TAG_NAME.getValue());
    assertThat(datasetRowExtended.getTaggedAt()).isEqualTo(TAGGED_AT);
  }

  @Test
  public void testMapper_datasetWithoutFields() throws SQLException {
    final StatementContext context = mock(StatementContext.class);
    final ResultSet results = mock(ResultSet.class);
    final Object exists = mock(Object.class);

    when(results.getObject(Columns.DATASET_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_UUID)).thenReturn(null);
    when(results.getObject(Columns.DATASET_NAME)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_NAME)).thenReturn(null);
    when(results.getObject(Columns.TAG_NAME)).thenReturn(null);
    when(results.getObject(Columns.DATASET_CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_CREATED_AT)).thenReturn(null);
    when(results.getObject(Columns.DATASET_UPDATED_AT)).thenReturn(DATASET_UPDATED_AT);
    when(results.getObject(Columns.DATASET_FIELD_UPDATED_AT)).thenReturn(null);
    when(results.getObject(Columns.TAGGED_AT)).thenReturn(null);
    when(results.getObject(Columns.DATASET_DESCRIPTION)).thenReturn(DATASET_DESCRIPTION);
    when(results.getObject(Columns.DATASET_FIELD_DESCRIPTION)).thenReturn(null);

    when(results.getObject(Columns.DATASET_UUID, UUID.class)).thenReturn(DATASET_UUID);
    when(results.getObject(Columns.DATASET_FIELD_UUID, UUID.class)).thenReturn(DATASET_FIELD_UUID);
    when(results.getString(Columns.DATASET_NAME)).thenReturn(DATASET_NAME.getValue());
    when(results.getString(Columns.DATASET_FIELD_NAME)).thenReturn(DATASET_FIELD_NAME.getValue());
    when(results.getString(Columns.TAG_NAME)).thenReturn(TAG_NAME.getValue());
    when(results.getTimestamp(Columns.DATASET_CREATED_AT))
        .thenReturn(Timestamp.from(DATASET_CREATED_AT));
    when(results.getTimestamp(Columns.DATASET_FIELD_CREATED_AT))
        .thenReturn(Timestamp.from(DATASET_FIELD_CREATED_AT));
    when(results.getTimestamp(Columns.DATASET_UPDATED_AT))
        .thenReturn(Timestamp.from(DATASET_UPDATED_AT));
    when(results.getTimestamp(Columns.DATASET_FIELD_UPDATED_AT))
        .thenReturn(Timestamp.from(DATASET_FIELD_UPDATED_AT));
    when(results.getTimestamp(Columns.TAGGED_AT)).thenReturn(Timestamp.from(TAGGED_AT));
    when(results.getString(Columns.DATASET_DESCRIPTION)).thenReturn(DATASET_DESCRIPTION);
    when(results.getString(Columns.DATASET_FIELD_DESCRIPTION))
        .thenReturn(DATASET_FIELD_DESCRIPTION);

    final DatasetRowExtended datasetRowExtended =
        new DatasetRowExtendedMapper().map(results, context);

    assertThat(datasetRowExtended.getDsUuid()).isEqualTo(DATASET_UUID);
    assertThat(datasetRowExtended.getDsCreatedAt()).isEqualTo(DATASET_CREATED_AT);
    assertThat(datasetRowExtended.getDsUpdatedAt()).isEqualTo(DATASET_UPDATED_AT);
    assertThat(datasetRowExtended.getDsName()).isEqualTo(DATASET_NAME.getValue());
    assertThat(datasetRowExtended.getDsDescription()).isEqualTo(DATASET_DESCRIPTION);

    assertThat(datasetRowExtended.getDfUuid()).isNull();
    assertThat(datasetRowExtended.getDfCreatedAt()).isNull();
    assertThat(datasetRowExtended.getDfUpdatedAt()).isNull();
    assertThat(datasetRowExtended.getDfName()).isNullOrEmpty();
    assertThat(datasetRowExtended.getDfDescription()).isNullOrEmpty();

    assertThat(datasetRowExtended.getTagName()).isNullOrEmpty();
    assertThat(datasetRowExtended.getTaggedAt()).isNull();
  }

  @Test
  public void testMapper_throwsException_onMissingDatasetInfo() throws SQLException {
    final StatementContext context = mock(StatementContext.class);
    final ResultSet results = mock(ResultSet.class);
    final Object exists = mock(Object.class);

    when(results.getObject(Columns.DATASET_UUID)).thenReturn(null);

    when(results.getObject(Columns.DATASET_FIELD_UUID)).thenReturn(null);
    when(results.getObject(Columns.DATASET_NAME)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_NAME)).thenReturn(null);
    when(results.getObject(Columns.TAG_NAME)).thenReturn(null);
    when(results.getObject(Columns.DATASET_CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.DATASET_FIELD_CREATED_AT)).thenReturn(null);
    when(results.getObject(Columns.DATASET_UPDATED_AT)).thenReturn(DATASET_UPDATED_AT);
    when(results.getObject(Columns.DATASET_FIELD_UPDATED_AT)).thenReturn(null);
    when(results.getObject(Columns.TAGGED_AT)).thenReturn(null);
    when(results.getObject(Columns.DATASET_DESCRIPTION)).thenReturn(DATASET_DESCRIPTION);
    when(results.getObject(Columns.DATASET_FIELD_DESCRIPTION)).thenReturn(null);

    when(results.getObject(Columns.DATASET_UUID, UUID.class)).thenReturn(DATASET_UUID);
    when(results.getObject(Columns.DATASET_FIELD_UUID, UUID.class)).thenReturn(DATASET_FIELD_UUID);
    when(results.getString(Columns.DATASET_NAME)).thenReturn(DATASET_NAME.getValue());
    when(results.getString(Columns.DATASET_FIELD_NAME)).thenReturn(DATASET_FIELD_NAME.getValue());
    when(results.getString(Columns.TAG_NAME)).thenReturn(TAG_NAME.getValue());
    when(results.getTimestamp(Columns.DATASET_CREATED_AT))
        .thenReturn(Timestamp.from(DATASET_CREATED_AT));
    when(results.getTimestamp(Columns.DATASET_FIELD_CREATED_AT))
        .thenReturn(Timestamp.from(DATASET_FIELD_CREATED_AT));
    when(results.getTimestamp(Columns.DATASET_UPDATED_AT))
        .thenReturn(Timestamp.from(DATASET_UPDATED_AT));
    when(results.getTimestamp(Columns.DATASET_FIELD_UPDATED_AT))
        .thenReturn(Timestamp.from(DATASET_FIELD_UPDATED_AT));
    when(results.getTimestamp(Columns.TAGGED_AT)).thenReturn(Timestamp.from(TAGGED_AT));
    when(results.getString(Columns.DATASET_DESCRIPTION)).thenReturn(DATASET_DESCRIPTION);
    when(results.getString(Columns.DATASET_FIELD_DESCRIPTION))
        .thenReturn(DATASET_FIELD_DESCRIPTION);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new DatasetRowExtendedMapper().map(results, context));
  }

  @Test
  public void testMapper_throwsException_onNullResults() {
    final ResultSet results = null;
    final StatementContext context = mock(StatementContext.class);
    final DatasetRowMapper rowMapper = new DatasetRowMapper();

    assertThatNullPointerException().isThrownBy(() -> rowMapper.map(results, context));
  }

  @Test
  public void testMapper_throwsException_onNullContext() {
    final ResultSet results = mock(ResultSet.class);
    final StatementContext context = null;
    final DatasetRowMapper rowMapper = new DatasetRowMapper();

    assertThatNullPointerException().isThrownBy(() -> rowMapper.map(results, context));
  }
}
