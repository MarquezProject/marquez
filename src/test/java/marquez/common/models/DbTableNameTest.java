package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbTableNameTest {
  @Test
  public void testNewDbTableName() {
    final String value = "test";
    assertEquals(value, DbTableName.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbTableName_throwsException_onNullValue() {
    final String nullValue = null;
    DbTableName.fromString(nullValue);
  }
}
