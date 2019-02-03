package marquez.common.models;

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobNameTest {
  @Test
  public void testNewJobName() {
    final String value = "test";
    assertEquals(value, JobName.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewJobName_throwsException_onNullValue() {
    final String nullValue = null;
    JobName.fromString(nullValue);
  }
}
