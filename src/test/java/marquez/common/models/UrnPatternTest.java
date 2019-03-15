package marquez.common.models;

import static org.junit.Assert.assertNotNull;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class UrnPatternTest {
  @Test
  public void testNewUrnPattern() {
    final String namespace = "test";
    final Integer numOfParts = 1;
    final UrnPattern pattern = UrnPattern.from(namespace, numOfParts);
    assertNotNull(pattern);
  }
}
