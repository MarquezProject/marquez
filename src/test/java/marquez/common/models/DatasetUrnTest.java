package marquez.common.models;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.StringJoiner;
import java.util.stream.Stream;
import org.junit.Test;

public class DatasetUrnTest {
  @Test
  public void testNewDatasetUrn() {
    final String datasetUrn = "urn:a:b.c";
    assertEquals(datasetUrn, DatasetUrn.of(datasetUrn).getValue());
  }

  @Test
  public void testNewDatasetUrnFromNamespaceAndDataset() {
    final DatasetUrn expected = DatasetUrn.of("urn:a:b.c");
    final DatasetUrn actual = DatasetUrn.of(Namespace.of("a"), Dataset.of("b.c"));
    assertEquals(expected, actual);
  }

  @Test(expected = NullPointerException.class)
  public void testDatasetUrnNull() {
    final String nullDatasetUrn = null;
    DatasetUrn.of(nullDatasetUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDatasetUrnEmpty() {
    final String emptyDatasetUrn = "";
    DatasetUrn.of(emptyDatasetUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDatasetUrnBlank() {
    final String blankDatasetUrn = " ";
    DatasetUrn.of(blankDatasetUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDatasetUrnNoPrefix() {
    final String noPrefixDatasetUrn = "a:b";
    DatasetUrn.of(noPrefixDatasetUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDatasetUrnMissingPart() {
    final String missingPartDatasetUrn = "urn:a";
    DatasetUrn.of(missingPartDatasetUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDatasetUrnExtraPart() {
    final String extraPartDatasetUrn = "urn:a:b:c";
    DatasetUrn.of(extraPartDatasetUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDatasetUrnNonAlphanumericPart() {
    final String nonAlphanumericPartDatasetUrn = "urn:a:~$^";
    DatasetUrn.of(nonAlphanumericPartDatasetUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDatasetUrnWithPartGreaterThan64() {
    final String greaterThan1024DatasetUrn = newDatasetUrnWithPartGreaterThan64();
    DatasetUrn.of(greaterThan1024DatasetUrn);
  }

  private String newDatasetUrnWithPartGreaterThan64() {
    final String urnPart0 = Stream.generate(() -> "a").limit(64).collect(joining());
    final String urnPart1GreaterThan64 =
        Stream.generate(() -> "c").limit(64 + 1).collect(joining());
    return new StringJoiner(":").add("urn").add(urnPart0).add(urnPart1GreaterThan64).toString();
  }
}
