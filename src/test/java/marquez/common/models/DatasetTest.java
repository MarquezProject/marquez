package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DatasetTest {
  @Test
  public void testNewDataset() {
    final String dataset = "b.c";
    assertEquals(dataset, Dataset.of(dataset).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testDatasetNull() {
    final String nullDataset = null;
    Dataset.of(nullDataset);
  }
}
