package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConnectionUrlTest {
  private static final DataSource DATA_SOURCE = DataSource.fromString("postgresql");
  private static final DbName DB_NAME = DbName.fromString("test");

  @Test
  public void testNewConnectionUrl() {
    final String rawValue =
        String.format("jdbc:%s://localhost:5432/%s", DATA_SOURCE.getValue(), DB_NAME.getValue());
    final ConnectionUrl connectionUrl = ConnectionUrl.fromString(rawValue);
    assertEquals(DATA_SOURCE, connectionUrl.getDataSource());
    assertEquals(DB_NAME, connectionUrl.getDbName());
    assertEquals(rawValue, connectionUrl.getRawValue());
  }

  @Test(expected = NullPointerException.class)
  public void testConnectionUrlNull() {
    final String nullConnectionUrl = null;
    ConnectionUrl.fromString(nullConnectionUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionUrlEmpty() {
    final String emptyConnectionUrl = "";
    ConnectionUrl.fromString(emptyConnectionUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionUrlBlank() {
    final String blankConnectionUrl = " ";
    ConnectionUrl.fromString(blankConnectionUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionUrlUnknownProtocol() {
    final String unknownProtocolConnectionUrl =
        String.format("foo:postgresql://localhost:5432/%s", DB_NAME.getValue());
    ConnectionUrl.fromString(unknownProtocolConnectionUrl);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConnectionUrlMissingParts() {
    final String missingPartsConnectionUrl =
        String.format("jdbc:postgresql://localhost/%s", DB_NAME.getValue());
    ConnectionUrl.fromString(missingPartsConnectionUrl);
  }
}
