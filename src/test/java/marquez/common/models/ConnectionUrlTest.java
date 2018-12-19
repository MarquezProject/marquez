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

  @Test(expected = NullPointerException.class)
  public void testConnectionUrlNull() {
    final String nullConnectionUrl = null;
    ConnectionUrl.of(nullConnectionUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionUrlEmpty() {
    final String emptyConnectionUrl = "";
    ConnectionUrl.of(emptyConnectionUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionUrlBlank() {
    final String blankConnectionUrl = " ";
    ConnectionUrl.of(blankConnectionUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionUrlUnknownProtocol() {
    final String unknownProtocolConnectionUrl =
        String.format("melquiades:postgresql://localhost:5432/%s", DB.getValue());
    ConnectionUrl.of(unknownProtocolConnectionUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionUrlMissingParts() {
    final String missingPartsConnectionUrl =
        String.format("jdbc:postgresql://localhost/%s", DB.getValue());
    ConnectionUrl.of(missingPartsConnectionUrl);
  }
}
