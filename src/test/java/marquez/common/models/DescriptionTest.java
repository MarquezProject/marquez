package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DescriptionTest {
  @Test
  public void testNewDescription() {
    final String value = "test";
    assertEquals(value, Description.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewDescription_throwsException_onNullValue() {
    final String nullValue = null;
    Description.fromString(nullValue);
  }
}
