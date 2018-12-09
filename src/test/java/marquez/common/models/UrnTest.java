package marquez.common.models;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.StringJoiner;
import java.util.stream.Stream;
import org.junit.Test;

public class UrnTest {
  @Test
  public void testNewUrn() {
    String urn = "urn:a:b:c";
    Urn expected = new Urn(urn);
    Urn actual = new Urn(urn);

    assertEquals(expected, actual);
  }

  @Test(expected = NullPointerException.class)
  public void testUrnNull() {
    String nullUrn = null;
    new Urn(nullUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnEmpty() {
    String emptyUrn = "";
    new Urn(emptyUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnBlank() {
    String blankUrn = " ";
    new Urn(blankUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnNoPrefix() {
    String noPrefixUrn = "a:b:c";
    new Urn(noPrefixUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnMissingPart() {
    String missingPartUrn = "urn:a:b";
    new Urn(missingPartUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnExtraPart() {
    String extraPartUrn = "urn:a:b:c:d";
    new Urn(extraPartUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnNonAlphanumericPart() {
    String nonAlphanumericPartUrn = "urn:a:~$^:c";
    new Namespace(nonAlphanumericPartUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUrnWithPartGreaterThan64() {
    String greaterThan1024Urn = newUrnWithPartGreaterThan64();
    new Urn(greaterThan1024Urn);
  }

  private String newUrnWithPartGreaterThan64() {
    String urnPart0 = Stream.generate(() -> "a").limit(64).collect(joining());
    String urnPart1 = Stream.generate(() -> "b").limit(64).collect(joining());
    String urnPart2GreaterThan64 = Stream.generate(() -> "c").limit(64 + 1).collect(joining());

    return new StringJoiner(":")
        .add("urn")
        .add(urnPart0)
        .add(urnPart1)
        .add(urnPart2GreaterThan64)
        .toString();
  }
}
