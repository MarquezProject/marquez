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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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

  @Test
  public void testUuidOrThrow_throw() throws SQLException {
    final String column = "with_null_uuid";
    when(results.getObject(column)).thenReturn(null);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Columns.uuidOrThrow(results, column));
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

  @Test
  public void testTimestampOrThrow_timestamp() throws SQLException {
    final String column = "with_timestamp";
    final Instant expected = Instant.now();
    when(results.getObject(column)).thenReturn(expected);
    when(results.getTimestamp(column)).thenReturn(Timestamp.from(expected));

    final Instant actual = Columns.timestampOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testTimestampOrThrow_throw() throws SQLException {
    final String column = "with_null_timestamp";
    when(results.getObject(Columns.CREATED_AT)).thenReturn(null);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Columns.timestampOrThrow(results, column));
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

  @Test
  public void testStringOrThrow_string() throws SQLException {
    final String column = "with_string";
    final String expected = "string";
    when(results.getObject(column)).thenReturn(expected);
    when(results.getString(column)).thenReturn(expected);

    final String actual = Columns.stringOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testIntOrThrow_int() throws SQLException {
    final String column = "count";
    final int expected = 1;
    when(results.getObject(column)).thenReturn(expected);
    when(results.getInt(column)).thenReturn(expected);

    final int actual = Columns.intOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testStringOrThrow_throw() throws SQLException {
    final String column = "with_null_string";
    when(results.getObject(column)).thenReturn(null);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Columns.stringOrThrow(results, column));
  }

  @Test
  public void testUuidArrayOrThrow_array() throws SQLException {
    final String column = "with_uuid_array";
    final UUID[] values = new UUID[] {UUID.randomUUID()};
    when(array.getArray()).thenReturn(values);
    when(results.getObject(column)).thenReturn(array);
    when(results.getArray(column)).thenReturn(array);

    final List<UUID> expected = Arrays.asList(values);
    final List<UUID> actual = Columns.uuidArrayOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testUuidArrayOrThrow_throw() throws SQLException {
    final String column = "with_null_uuid_array";
    when(results.getObject(column)).thenReturn(null);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Columns.uuidArrayOrThrow(results, column));
  }

  @Test
  public void testStringArrayOrThrow_array() throws SQLException {
    final String column = "with_string_array";
    final String[] values = new String[] {"test_value0", "test_value1", "test_value2"};
    when(array.getArray()).thenReturn(values);
    when(results.getObject(column)).thenReturn(array);
    when(results.getArray(column)).thenReturn(array);

    final List<String> expected = Arrays.asList(values);
    final List<String> actual = Columns.stringArrayOrThrow(results, column);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testStringArrayOrThrow_throw() throws SQLException {
    final String column = "with_null_string_array";
    when(results.getObject(column)).thenReturn(null);

    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> Columns.stringArrayOrThrow(results, column));
  }
}
