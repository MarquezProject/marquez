package marquez.common.models;

import static org.junit.Assert.assertNotNull;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class UrnPatternTest {
  private static final String NAMESPACE = "test";
  private static final int NUM_OF_PARTS = 2;

  @Test
  public void testNewUrnPattern() {
    final UrnPattern pattern = UrnPattern.from(NAMESPACE, NUM_OF_PARTS);
    assertNotNull(pattern);
  }

  @Test(expected = NullPointerException.class)
  public void testNewUrnPattern_throwsException_onNullNamespace() {
    final String nullNamespace = null;
    UrnPattern.from(nullNamespace, NUM_OF_PARTS);
  }

  @Test(expected = NullPointerException.class)
  public void testNewUrnPattern_throwsException_onNullNumOfParts() {
    final Integer nullNumOfParts = null;
    UrnPattern.from(NAMESPACE, nullNumOfParts);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewUrnPattern_throwsException_onEmptyNamespace() {
    final String emptyNamespace = "";
    UrnPattern.from(emptyNamespace, NUM_OF_PARTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewUrnPattern_throwsException_onBlankNamespace() {
    final String blankNamespace = " ";
    UrnPattern.from(blankNamespace, NUM_OF_PARTS);
  }

  @Test
  public void testThrowIfNoMatch() {
    final String value = "urn:test:a:b";
    final UrnPattern pattern = UrnPattern.from(NAMESPACE, NUM_OF_PARTS);
    pattern.throwIfNoMatch(value);
  }

  @Test(expected = NullPointerException.class)
  public void testThrowIfNoMatch_throwsException_onNullValue() {
    final String nullValue = null;
    final UrnPattern pattern = UrnPattern.from(NAMESPACE, NUM_OF_PARTS);
    pattern.throwIfNoMatch(nullValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewUrnPattern_throwsException_onEmptyValue() {
    final String emptyValue = "";
    UrnPattern.from(emptyValue, NUM_OF_PARTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewUrnPattern_throwsException_onBlankValue() {
    final String blankValue = " ";
    UrnPattern.from(blankValue, NUM_OF_PARTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowIfNoMatch_throwsException_onNoMatchValue() {
    final String noMatchValue = "urn:a:b";
    final UrnPattern pattern = UrnPattern.from(NAMESPACE, NUM_OF_PARTS);
    pattern.throwIfNoMatch(noMatchValue);
  }
}
