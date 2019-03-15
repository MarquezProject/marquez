package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DatasourceUrnTest {
  @Test
  public void testFromString() {
    String urn = "urn:datasource:a:b";
    assertEquals(urn, DatasourceUrn.fromString(urn).getValue());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOfTooFewComponents() {
    DatasourceUrn.fromString("urn:datasource:a");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOfTooManyComponents() {
    DatasourceUrn.fromString("urn:datasource:a:b:c");
  }
}
