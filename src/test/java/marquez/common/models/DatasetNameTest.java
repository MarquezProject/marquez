package marquez.common.models;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DatasetNameTest {
  @Test
  public void testNewDatasetName() {
    final String value = "b.c";
    assertEquals(value, DatasetName.of(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDatasetName_throwsException_onNullValue() {
    final String nullValue = null;
    DatasetName.of(nullValue);
  }
}
