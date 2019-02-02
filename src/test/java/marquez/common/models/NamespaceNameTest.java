package marquez.common.models;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;
import org.junit.Test;

public class NamespaceNameTest {
  private static final int ALLOWED_NAMESPACE_SIZE = 1024;
  private static final int NAMESPACE_SIZE_GREATER_THAN_ALLOWED = ALLOWED_NAMESPACE_SIZE + 1;

  @Test
  public void testNewNamespace() {
    final String value = "test";
    assertEquals(value, NamespaceName.valueOf(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNamespace_throwsException_onNullValue() {
    final String nullValue = null;
    NamespaceName.valueOf(nullValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespace_throwsException_onEmptyValue() {
    final String emptyValue = "";
    NamespaceName.valueOf(emptyValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespace_throwsException_onBlankValue() {
    final String blankValue = " ";
    NamespaceName.valueOf(blankValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespace_throwsException_onNonAlphanumericValue() {
    final String nonAlphanumericValue = "t@?t>";
    NamespaceName.valueOf(nonAlphanumericValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespace__throwsException_onGreaterThan1024Value() {
    final String greaterThan1024Value = newGreaterThan1024Value();
    NamespaceName.valueOf(greaterThan1024Value);
  }

  private String newGreaterThan1024Value() {
    return Stream.generate(() -> "a").limit(NAMESPACE_SIZE_GREATER_THAN_ALLOWED).collect(joining());
  }
}
