package marquez.common.models;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;
import org.junit.Test;

public class NamespaceTest {
  @Test
  public void testNewNamespace() {
    String namespace = "macondo";
    Namespace expected = new Namespace(namespace);
    Namespace actual = new Namespace(namespace);

    assertEquals(expected, actual);
  }

  @Test(expected = NullPointerException.class)
  public void testNamespaceNull() {
    String nullNamespace = null;
    new Namespace(nullNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceEmpty() {
    String emptyNamespace = "";
    new Namespace(emptyNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceBlank() {
    String blankNamespace = " ";
    new Namespace(blankNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceNonAlphanumeric() {
    String nonAlphanumericNamespace = "~$^+=<>";
    new Namespace(nonAlphanumericNamespace);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNamespaceGreaterThan1024() {
    String greaterThan1024Namespace = newGreaterThan1024Namespace();
    new Namespace(greaterThan1024Namespace);
  }

  private String newGreaterThan1024Namespace() {
    return Stream.generate(() -> "a").limit(1024 + 1).collect(joining());
  }
}
