package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConnectionUrlTest {
  private static final DataSource DATA_SOURCE = DataSource.of("postgresql");
  private static final Db DB = Db.of("marquez");
  private static final String CONNECTION_URL =
      String.format("jdbc:%s://localhost:5432/%s", DATA_SOURCE.getValue(), DB.getValue());

  @Test
  public void testNewConnectionUrl() {
    final ConnectionUrl connectionUrl = ConnectionUrl.of(CONNECTION_URL);
    assertEquals(DATA_SOURCE, connectionUrl.getDataSource());
    assertEquals(DB, connectionUrl.getDb());
    assertEquals(CONNECTION_URL, connectionUrl.getRawValue());
  }
}
