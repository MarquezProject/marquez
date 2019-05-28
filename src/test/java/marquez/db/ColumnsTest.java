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

package marquez.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import marquez.UnitTests;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class ColumnsTest {
  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private ResultSet results;
  @Mock private Array array;

  @Test
  public void testUuidOrNull_uuid() throws SQLException {
    final String column = "with_uuid";
    final UUID expected = UUID.randomUUID();
    when(results.getObject(column)).thenReturn(expected);
    when(results.getObject(column, UUID.class)).thenReturn(expected);

    final UUID actual = Columns.uuidOrNull(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testUuidOrNull_null() throws SQLException {
    final String column = "with_null_uuid";
    final UUID expected = null;
    when(results.getObject(column)).thenReturn(expected);

    final UUID actual = Columns.uuidOrNull(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testUuidOrThrow_uuid() throws SQLException {
    final String column = "with_uuid";
    final UUID expected = UUID.randomUUID();
    when(results.getObject(column)).thenReturn(expected);
    when(results.getObject(column, UUID.class)).thenReturn(expected);

    final UUID actual = Columns.uuidOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUuidOrThrow_throw() throws SQLException {
    final String column = "with_null_uuid";
    final UUID nullUuid = null;
    when(results.getObject(column)).thenReturn(nullUuid);

    Columns.uuidOrThrow(results, column);
  }

  @Test
  public void testTimestampOrNull_timestamp() throws SQLException {
    final String column = "with_timestamp";
    final Instant expected = Instant.now();
    when(results.getObject(column)).thenReturn(Timestamp.from(expected));
    when(results.getTimestamp(column)).thenReturn(Timestamp.from(expected));

    final Instant actual = Columns.timestampOrNull(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testTimestampOrNull_null() throws SQLException {
    final String column = "with_null_timestamp";
    final Instant expected = null;
    when(results.getObject(column)).thenReturn(expected);

    final Instant actual = Columns.timestampOrNull(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTimestampOrThrow_timestamp() throws SQLException {
    final String column = "with_timestamp";
    final Instant expected = Instant.now();
    when(results.getObject(Columns.CREATED_AT)).thenReturn(expected);

    final Instant actual = Columns.timestampOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTimestampOrThrow_throw() throws SQLException {
    final String column = "with_null_timestamp";
    final Timestamp nullTimestamp = null;
    when(results.getObject(Columns.CREATED_AT)).thenReturn(nullTimestamp);

    Columns.timestampOrThrow(results, column);
  }

  @Test
  public void testStringOrNull_string() throws SQLException {
    final String column = "with_string";
    final String expected = "string";
    when(results.getObject(column)).thenReturn(expected);
    when(results.getString(column)).thenReturn(expected);

    final String actual = Columns.stringOrNull(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testStringOrNull_null() throws SQLException {
    final String column = "with_null_string";
    final String expected = null;
    when(results.getObject(column)).thenReturn(expected);

    final String actual = Columns.stringOrNull(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  public void testStringOrThrow_string() throws SQLException {
    final String column = "with_string";
    final String expected = "string";
    when(results.getObject(column)).thenReturn(expected);
    when(results.getString(column)).thenReturn(expected);

    final String actual = Columns.stringOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStringOrThrow_throw() throws SQLException {
    final String column = "with_null_string";
    final String nullString = null;
    when(results.getObject(column)).thenReturn(nullString);

    Columns.stringOrThrow(results, column);
  }

  @Test
  public void testArrayOrThrow_array() throws SQLException {
    final String column = "with_array";
    final String[] values = new String[] {"test_value0", "test_value1", "test_value2"};
    when(array.getArray()).thenReturn(values);
    when(results.getObject(column)).thenReturn(array);
    when(results.getArray(column)).thenReturn(array);

    final List<String> expected = Arrays.asList(values);
    final List<String> actual = Columns.arrayOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testArrayOrThrow_throw() throws SQLException {
    final String column = "with_null_array";
    final Array nullArray = null;
    when(results.getObject(column)).thenReturn(nullArray);

    Columns.arrayOrThrow(results, column);
  }
}
