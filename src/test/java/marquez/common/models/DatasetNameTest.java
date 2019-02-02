package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetNameTest {
  @Test
  public void testNewDatasetName() {
    final String value = "b.c";
    assertEquals(value, DatasetName.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDatasetName_throwsException_onNullValue() {
    final String nullValue = null;
    DatasetName.fromString(nullValue);
  }
}
