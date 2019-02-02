package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbSchemaNameTest {
  @Test
  public void testNewDbSchemaName() {
    final String value = "test";
    assertEquals(value, DbSchemaName.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbSchemaName_throwsException_onNullvalue() {
    final String nullValue = null;
    DbSchemaName.fromString(nullValue);
  }
}
