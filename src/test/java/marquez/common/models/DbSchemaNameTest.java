package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DbSchemaNameTest {
  @Test
  public void testNewDbSchemaName() {
    final String value = "test";
    assertEquals(value, DbSchemaName.of(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbSchemaName_throwsException_onNullvalue() {
    final String nullValue = null;
    DbSchemaName.of(nullValue);
  }
}
