package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DbNameTest {
  @Test
  public void testNewDbName() {
    final String value = "test";
    assertEquals(value, DbName.of(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbName_throwsException_onNullValue() {
    final String nullValue = null;
    DbName.of(nullValue);
  }
}
