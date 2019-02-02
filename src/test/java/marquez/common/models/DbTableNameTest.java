package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DbTableNameTest {
  @Test
  public void testNewDbTableName() {
    final String value = "test";
    assertEquals(value, DbTableName.of(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDbTableName_throwsException_onNullValue() {
    final String nullValue = null;
    DbTableName.of(nullValue);
  }
}
