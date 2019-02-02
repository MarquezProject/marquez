package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DataSourceTest {
  @Test
  public void testNewDataSource() {
    final String value = "postgresql";
    assertEquals(value, DataSource.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDataSource_throwsException_onNullValue() {
    final String nullValue = null;
    DataSource.fromString(nullValue);
  }
}
