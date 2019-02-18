package marquez.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class ColumnsTest {
  @Test
  public void testToInstantOrNull() {
    final Instant expected = Instant.now();
    final Timestamp timestamp = Timestamp.from(expected);
    final Instant actual = Columns.toInstantOrNull(timestamp);
    assertEquals(expected, actual);
  }

  @Test
  public void testToInstantOrNull_nullTimestamp() {
    final Timestamp nullTimestamp = null;
    final Instant instant = Columns.toInstantOrNull(nullTimestamp);
    assertNull(instant);
  }

  @Test
  public void testToUuidOrNull() {
    final String uuidString = "4f4c72c1-281a-4b82-a4e0-9c909a97832f";
    final UUID expected = UUID.fromString(uuidString);
    final UUID actual = Columns.toUuidOrNull(uuidString);
    assertEquals(expected, actual);
  }

  @Test
  public void testToUuidOrNull_nullUuidString() {
    final String nullUuidString = null;
    final UUID uuid = Columns.toUuidOrNull(nullUuidString);
    assertNull(uuid);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToUuidOrNull_throwsException_onEmptyUuidString() {
    final String emptyUuidString = "";
    Columns.toUuidOrNull(emptyUuidString);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToUuidOrNull_throwsException_onBlankUuidString() {
    final String blankUuidString = " ";
    Columns.toUuidOrNull(blankUuidString);
  }

  @Test
  public void testToList() throws SQLException {
    final String[] values = new String[] {"value0", "value1", "value2"};
    final Array array = mock(Array.class);
    when(array.getArray()).thenReturn(values);

    final List<String> expected = Arrays.asList(values);
    final List<String> actual = Columns.toList(array);
    assertEquals(expected, actual);
  }

  @Test
  public void testToList_emptyArray() throws SQLException {
    final String[] values = new String[] {};
    final Array emptyArray = mock(Array.class);
    when(emptyArray.getArray()).thenReturn(values);

    final List<String> expected = Arrays.asList(values);
    final List<String> actual = Columns.toList(emptyArray);
    assertEquals(expected, actual);
  }

  @Test
  public void testToList_nullArray() throws SQLException {
    final Array nullArray = null;
    final List<String> arrayAsList = Columns.toList(nullArray);
    assertEquals(Collections.EMPTY_LIST, arrayAsList);
  }
}
