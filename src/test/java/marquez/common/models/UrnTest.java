package marquez.common.models;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.StringJoiner;
import java.util.stream.Stream;
import org.junit.Test;

public class UrnTest {
  @Test
  public void testNewUrn() {
    final String urnString = "urn:a:b.c";
    final Urn expected = Urn.of(urnString);
    final Urn actual = Urn.of(urnString);
    assertEquals(expected, actual);
  }

  @Test
  public void testNewUrnFromNamespaceAndDataset() {
    final Urn expected = Urn.of("urn:a:b.c");
    final Namespace namespace = Namespace.of("a");
    final Dataset dataset = Dataset.of("b.c");
    final Urn actual = Urn.of(namespace, dataset);
    assertEquals(expected, actual);
  }

  @Test(expected = NullPointerException.class)
  public void testUrnNull() {
    final String nullUrn = null;
    Urn.of(nullUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnEmpty() {
    final String emptyUrn = "";
    Urn.of(emptyUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnBlank() {
    final String blankUrn = " ";
    Urn.of(blankUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnNoPrefix() {
    final String noPrefixUrn = "a:b";
    Urn.of(noPrefixUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnMissingPart() {
    final String missingPartUrn = "urn:a";
    Urn.of(missingPartUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnExtraPart() {
    final String extraPartUrn = "urn:a:b:c";
    Urn.of(extraPartUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnNonAlphanumericPart() {
    final String nonAlphanumericPartUrn = "urn:a:~$^";
    Urn.of(nonAlphanumericPartUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnWithPartGreaterThan64() {
    final String greaterThan1024Urn = newUrnWithPartGreaterThan64();
    Urn.of(greaterThan1024Urn);
  }

  private String newUrnWithPartGreaterThan64() {
    final String urnPart0 = Stream.generate(() -> "a").limit(64).collect(joining());
    final String urnPart1GreaterThan64 =
        Stream.generate(() -> "c").limit(64 + 1).collect(joining());
    return new StringJoiner(":").add("urn").add(urnPart0).add(urnPart1GreaterThan64).toString();
  }
}
