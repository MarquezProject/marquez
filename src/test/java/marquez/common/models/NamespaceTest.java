package marquez.common.models;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;
import org.junit.Test;

public class NamespaceTest {
  @Test
  public void testNewNamespace() {
    final String namespace = "macondo";
    final Namespace expected = Namespace.of(namespace);
    final Namespace actual = Namespace.of(namespace);
    assertEquals(expected, actual);
  }

  @Test(expected = NullPointerException.class)
  public void testNamespaceNull() {
    final String nullNamespace = null;
    Namespace.of(nullNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceEmpty() {
    final String emptyNamespace = "";
    Namespace.of(emptyNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceBlank() {
    final String blankNamespace = " ";
    Namespace.of(blankNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceNonAlphanumeric() {
    final String nonAlphanumericNamespace = "~$^+=<>";
    Namespace.of(nonAlphanumericNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceGreaterThan1024() {
    final String greaterThan1024Namespace = newGreaterThan1024Namespace();
    Namespace.of(greaterThan1024Namespace);
  }

  private String newGreaterThan1024Namespace() {
    return Stream.generate(() -> "a").limit(1024 + 1).collect(joining());
  }
}
