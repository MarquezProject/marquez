package marquez.common.models;

import org.junit.Test;

public class DatasourceUrnTest {
  @Test
  public void testOf() {
    DatasourceUrn.of("urn:a:b");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOfTooFewComponents() {
    DatasourceUrn.of("urn:a");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOfTooManyComponents() {
    DatasourceUrn.of("urn:a:b:c");
  }
}
