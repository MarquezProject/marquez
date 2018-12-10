package marquez.common;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;
import org.junit.Test;

public class NamespaceTest {
  @Test
  public void testNewNamespace() {
    final String namespace = "macondo";
    final Namespace expected = new Namespace(namespace);
    final Namespace actual = new Namespace(namespace);
    assertEquals(expected, actual);
  }

  @Test(expected = NullPointerException.class)
  public void testNamespaceNull() {
    final String nullNamespace = null;
    new Namespace(nullNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceEmpty() {
    final String emptyNamespace = "";
    new Namespace(emptyNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceBlank() {
    final String blankNamespace = " ";
    new Namespace(blankNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceNonAlphanumeric() {
    final String nonAlphanumericNamespace = "~$^+=<>";
    new Namespace(nonAlphanumericNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceGreaterThan1024() {
    final String greaterThan1024Namespace = newGreaterThan1024Namespace();
    new Namespace(greaterThan1024Namespace);
  }

  private String newGreaterThan1024Namespace() {
    return Stream.generate(() -> "a").limit(1024 + 1).collect(joining());
  }
}
