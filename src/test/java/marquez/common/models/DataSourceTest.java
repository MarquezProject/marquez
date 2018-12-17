package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DataSourceTest {
  @Test
  public void testNewDataSource() {
    final String dataSource = "postgresql";
    assertEquals(dataSource, DataSource.of(dataSource).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testDataSourceNull() {
    final String nullDataSource = null;
    DataSource.of(nullDataSource);
  }
}
