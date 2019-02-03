package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class ConnectionUrlTest {
  private static final DataSource DATA_SOURCE = DataSource.fromString("postgresql");
  private static final int DB_PORT = 5432;
  private static final DbName DB_NAME = DbName.fromString("test");

  @Test
  public void testNewConnectionUrl() {
    final String rawValue =
        String.format(
            "jdbc:%s://localhost:%d/%s", DATA_SOURCE.getValue(), DB_PORT, DB_NAME.getValue());
    final ConnectionUrl connectionUrl = ConnectionUrl.fromString(rawValue);
    assertEquals(DATA_SOURCE, connectionUrl.getDataSource());
    assertEquals(DB_NAME, connectionUrl.getDbName());
    assertEquals(rawValue, connectionUrl.getRawValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewConnectionUrl_throwsException_onNullRawValue() {
    final String nullRawValue = null;
    ConnectionUrl.fromString(nullRawValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onEmptyRawValue() {
    final String emptyRawValue = "";
    ConnectionUrl.fromString(emptyRawValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onBlankRawValue() {
    final String blankRawValue = " ";
    ConnectionUrl.fromString(blankRawValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onUnknownProtocolValue() {
    final String unknownProtocolValue =
        String.format("foo:postgresql://localhost:%d/%s", DB_PORT, DB_NAME.getValue());
    ConnectionUrl.fromString(unknownProtocolValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onMissingPartValue() {
    final String missingPartValue =
        String.format("jdbc:postgresql://localhost/%s", DB_NAME.getValue());
    ConnectionUrl.fromString(missingPartValue);
  }
}
