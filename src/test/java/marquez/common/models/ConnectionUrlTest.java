package marquez.common.models;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import org.junit.Test;

public class ConnectionUrlTest {
  private static final String CONNECTION_URL = "jdbc:postgresql://localhost:5432/marquez";
  @Test
  public void testNewConnectionUrl() {
    assertEquals(CONNECTION_URL, ConnectionUrl.of(CONNECTION_URL).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testConnectionUrlNull() {
    final String nullConnectionUrl = null;
    ConnectionUrl.of(nullConnectionUrl);
  }

  @Test
  public void testToUri() {
    final URI expected = URI.create(CONNECTION_URL);
    final URI actual = ConnectionUrl.of(CONNECTION_URL).toUri();
    assertEquals(expected, actual);
  }
}
