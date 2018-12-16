package marquez.common.models;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import org.junit.Test;

public class ConnectionUrlTest {
  @Test
  public void testNewConnectionUrl() {
    final String connectionUrl = "jdbc:postgresql://localhost:5432/marquez";
    assertEquals(connectionUrl, ConnectionUrl.of(connectionUrl).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testConnectionUrlNull() {
    final String nullConnectionUrl = null;
    ConnectionUrl.of(nullConnectionUrl);
  }

  @Test
  public void testToUri() {
    final String connectionUrl = "jdbc:postgresql://localhost:5432/marquez";
    final URI expected = URI.create(connectionUrl);
    final URI actual = ConnectionUrl.of(connectionUrl).toUri();
    assertEquals(expected, actual);
  }
}
