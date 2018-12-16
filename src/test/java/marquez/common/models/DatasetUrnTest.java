package marquez.common.models;

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.StringJoiner;
import java.util.stream.Stream;
import org.junit.Test;

public class DatasetUrnTest {
  private static final String DATASET_URN_DELIM = ":";
  private static final String DATASET_URN_PREFIX = "urn";
  private static final Integer ALLOWED_DATASET_URN_SIZE = 64;
  private static final Integer DATASET_URN_SIZE_NOT_ALLOWED = ALLOWED_DATASET_URN_SIZE + 1;

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
    final String nonAlphanumericPartDatasetUrn = "urn:a:b$c^";
    DatasetUrn.of(nonAlphanumericPartDatasetUrn);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testDatasetUrnWithPartGreaterThan64() {
    final String greaterThan1024DatasetUrn = newDatasetUrnWithPartGreaterThan64();
    DatasetUrn.of(greaterThan1024DatasetUrn);
  }

  private String newDatasetUrnWithPartGreaterThan64() {
    final String part0 = newDatasetUrnPart("a", ALLOWED_DATASET_URN_SIZE);
    final String part1GreaterThan64 = newDatasetUrnPart("b", DATASET_URN_SIZE_NOT_ALLOWED);

    return new StringJoiner(DATASET_URN_DELIM)
        .add(DATASET_URN_PREFIX)
        .add(part0)
        .add(part1GreaterThan64)
        .toString();
  }

  private String newDatasetUrnPart(String s, Integer limit) {
    return Stream.generate(() -> s).limit(limit).collect(joining());
  }
}
