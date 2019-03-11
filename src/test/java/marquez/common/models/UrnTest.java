package marquez.common.models;

import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;
import org.junit.Test;

public class UrnTest {
  private String urnType = "foo";
  private int numComponents = 3;
  private Pattern pattern = Urn.buildPattern(urnType, numComponents);

  @Test(expected = Test.None.class /* no exception expected */)
  public void testCreate() {
    String urn = "urn:foo:1st:2nd:3rd";
    new Urn(urn, pattern);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEnforceUrnType() {
    new Urn("urn:bar:1st:2nd:3rd", pattern);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEnforceTooManyComponents() {
    new Urn("urn:foo:1st:2nd:3rd:4th", pattern);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEnforceTooFewComponents() {
    new Urn("urn:foo:1st:2nd", pattern);
  }

  @Test
  public void testFromComponentsPreserveValidChars() {
    String urn = "urn:foo:1:2:abcdefghijklmnopqrstuvwxyz34567890.";
    assertEquals(urn, Urn.fromComponents(urnType, "1", "2", "abcdefghijklmnopqrstuvwxyz34567890."));
  }

  @Test
  public void testFromComponentsStripInvalidChars() {
    String urn = "urn:foo:1:2:3'!@#$%^&*()";
    assertEquals("urn:foo:1:2:3", Urn.fromComponents(urnType, "1", "2", "3'!@#$%^&*()"));
  }
}
