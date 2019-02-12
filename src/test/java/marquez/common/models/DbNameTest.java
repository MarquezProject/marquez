package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbNameTest {
  @Test
  public void testNewDbName() {
    final String value = "test";
    assertEquals(value, DbName.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbName_throwsException_onNullValue() {
    final String nullValue = null;
    DbName.fromString(nullValue);
  }
}
