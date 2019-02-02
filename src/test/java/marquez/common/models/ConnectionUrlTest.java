package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
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
  public void testNewConnectionUrl_throwsException_onUnknownProtocol() {
    final String rawValueWithUnknownProtocol =
        String.format("foo:postgresql://localhost:5432/%s", DB_NAME.getValue());
    ConnectionUrl.fromString(rawValueWithUnknownProtocol);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewConnectionUrl_throwsException_onMissingParts() {
    final String rawValueWithMissingParts =
        String.format("jdbc:postgresql://localhost/%s", DB_NAME.getValue());
    ConnectionUrl.fromString(rawValueWithMissingParts);
  }
}
