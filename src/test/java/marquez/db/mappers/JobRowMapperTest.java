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

import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newJobName;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static marquez.db.models.DbModelGenerator.newNamespaceRow;
import static marquez.db.models.DbModelGenerator.newRowUuid;
import static marquez.db.models.DbModelGenerator.newTimestamp;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.Description;
import marquez.common.models.JobName;
import marquez.db.Columns;
import marquez.service.models.Job;
import org.jdbi.v3.core.statement.StatementContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class JobRowMapperTest {
  private static final UUID ROW_UUID = newRowUuid();
  private static final JobName NAME = newJobName();
  private static final URI LOCATION = newLocation();
  private static final UUID NAMESPACE_UUID = newNamespaceRow().getUuid();
  private static final Description DESCRIPTION = newDescription();
  private static final String[] INPUT_DATASET_URNS = new String[] {newDatasetUrn().getValue()};
  private static final String[] OUTPUT_DATASET_URNS = new String[] {newDatasetUrn().getValue()};
  private static final Instant CREATED_AT = newTimestamp();
  private static final Instant UPDATED_AT = CREATED_AT;

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private Object exists;
  @Mock private ResultSet results;
  @Mock private StatementContext context;

  @Test
  public void testMap_row() throws SQLException {
    Array inputArray = mock(Array.class);
    Array outputArray = mock(Array.class);

    when(results.getObject(Columns.ROW_UUID)).thenReturn(exists);
    when(results.getObject(Columns.NAME)).thenReturn(exists);
    when(results.getObject(Columns.LOCATION)).thenReturn(exists);
    when(results.getObject(Columns.NAMESPACE_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DESCRIPTION)).thenReturn(exists);
    when(results.getObject(Columns.INPUT_DATASET_URNS)).thenReturn(exists);
    when(results.getObject(Columns.OUTPUT_DATASET_URNS)).thenReturn(exists);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.UPDATED_AT)).thenReturn(exists);

    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getString(Columns.NAME)).thenReturn(NAME.getValue());
    when(results.getString(Columns.LOCATION)).thenReturn(LOCATION.toASCIIString());
    when(results.getObject(Columns.NAMESPACE_UUID, UUID.class)).thenReturn(NAMESPACE_UUID);
    when(results.getString(Columns.DESCRIPTION)).thenReturn(DESCRIPTION.getValue());
    when(inputArray.getArray()).thenReturn(INPUT_DATASET_URNS);
    when(outputArray.getArray()).thenReturn(OUTPUT_DATASET_URNS);
    when(results.getArray(Columns.INPUT_DATASET_URNS)).thenReturn(inputArray);
    when(results.getArray(Columns.OUTPUT_DATASET_URNS)).thenReturn(outputArray);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getTimestamp(Columns.UPDATED_AT)).thenReturn(Timestamp.from(UPDATED_AT));

    final JobRowMapper jobRowMapper = new JobRowMapper();
    final Job job = jobRowMapper.map(results, context);
    assertThat(job.getUuid()).isEqualTo(ROW_UUID);
    assertThat(job.getName()).isEqualTo(NAME.getValue());
    assertThat(job.getLocation()).isEqualTo(LOCATION.toASCIIString());
    assertThat(job.getNamespaceUuid()).isEqualTo(NAMESPACE_UUID);
    assertThat(job.getDescription()).isEqualTo(DESCRIPTION.getValue());
    assertThat(job.getInputDatasetUrns()).isEqualTo(Arrays.asList(INPUT_DATASET_URNS));
    assertThat(job.getOutputDatasetUrns()).isEqualTo(Arrays.asList(OUTPUT_DATASET_URNS));
    assertThat(job.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(job.getUpdatedAt()).isEqualTo(UPDATED_AT);
  }

  @Test
  public void testMap_row_noDescription() throws SQLException {
    Array inputArray = mock(Array.class);
    Array outputArray = mock(Array.class);

    when(results.getObject(Columns.ROW_UUID)).thenReturn(exists);
    when(results.getObject(Columns.NAME)).thenReturn(exists);
    when(results.getObject(Columns.LOCATION)).thenReturn(exists);
    when(results.getObject(Columns.NAMESPACE_UUID)).thenReturn(exists);
    when(results.getObject(Columns.DESCRIPTION)).thenReturn(NO_DESCRIPTION.getValue());
    when(results.getObject(Columns.INPUT_DATASET_URNS)).thenReturn(exists);
    when(results.getObject(Columns.OUTPUT_DATASET_URNS)).thenReturn(exists);
    when(results.getObject(Columns.CREATED_AT)).thenReturn(exists);
    when(results.getObject(Columns.UPDATED_AT)).thenReturn(exists);

    when(results.getObject(Columns.ROW_UUID, UUID.class)).thenReturn(ROW_UUID);
    when(results.getString(Columns.NAME)).thenReturn(NAME.getValue());
    when(results.getString(Columns.LOCATION)).thenReturn(LOCATION.toASCIIString());
    when(results.getObject(Columns.NAMESPACE_UUID, UUID.class)).thenReturn(NAMESPACE_UUID);
    when(inputArray.getArray()).thenReturn(INPUT_DATASET_URNS);
    when(outputArray.getArray()).thenReturn(OUTPUT_DATASET_URNS);
    when(results.getArray(Columns.INPUT_DATASET_URNS)).thenReturn(inputArray);
    when(results.getArray(Columns.OUTPUT_DATASET_URNS)).thenReturn(outputArray);
    when(results.getTimestamp(Columns.CREATED_AT)).thenReturn(Timestamp.from(CREATED_AT));
    when(results.getTimestamp(Columns.UPDATED_AT)).thenReturn(Timestamp.from(UPDATED_AT));

    final JobRowMapper jobRowMapper = new JobRowMapper();
    final Job job = jobRowMapper.map(results, context);
    assertThat(job.getUuid()).isEqualTo(ROW_UUID);
    assertThat(job.getName()).isEqualTo(NAME.getValue());
    assertThat(job.getLocation()).isEqualTo(LOCATION.toASCIIString());
    assertThat(job.getNamespaceUuid()).isEqualTo(NAMESPACE_UUID);
    assertThat(job.getDescription()).isEqualTo(NO_DESCRIPTION.getValue());
    assertThat(job.getInputDatasetUrns()).isEqualTo(Arrays.asList(INPUT_DATASET_URNS));
    assertThat(job.getOutputDatasetUrns()).isEqualTo(Arrays.asList(OUTPUT_DATASET_URNS));
    assertThat(job.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(job.getUpdatedAt()).isEqualTo(UPDATED_AT);

    verify(results, never()).getString(Columns.DESCRIPTION);
  }

  @Test
  public void testMap_throwsException_onNullResults() throws SQLException {
    final ResultSet nullResults = null;
    final JobRowMapper jobRowMapper = new JobRowMapper();
    assertThatNullPointerException().isThrownBy(() -> jobRowMapper.map(nullResults, context));
  }

  @Test
  public void testMap_throwsException_onNullContext() throws SQLException {
    final StatementContext nullContext = null;
    final JobRowMapper jobRowMapper = new JobRowMapper();
    assertThatNullPointerException().isThrownBy(() -> jobRowMapper.map(results, nullContext));
  }
}
