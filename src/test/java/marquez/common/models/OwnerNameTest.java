package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class OwnerNameTest {
  @Test
  public void testNewOwnerName() {
    final String value = "test";
    assertEquals(value, OwnerName.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewOwnerName_throwsException_onNullValue() {
    final String nullValue = null;
    OwnerName.fromString(nullValue);
  }
}
