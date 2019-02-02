package marquez.common.models;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import org.junit.Test;

public class RunIdTest {
  @Test
  public void testNewRunId() {
    final String value = "f662a43d-ab87-4b4b-a9ba-828b53c3368b";
    final UUID expected = UUID.fromString(value);
    assertEquals(expected, RunId.fromString(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewRunId_throwsException_onNullValue() {
    final String nullValue = null;
    RunId.fromString(nullValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRunId_throwsException_onEmptyValue() {
    final String emptyValue = "";
    RunId.fromString(emptyValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRunId_throwsException_onBlankValue() {
    final String blankValue = " ";
    RunId.fromString(blankValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRunId_throwsException_onGreaterThan36Value() {
    final String greaterThan36Value = "5f217783-ff48-4650-aab4-ek1306afb3960";
    RunId.fromString(greaterThan36Value);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRunId_throwsException_onLessThan36Value() {
    final String lessThan36Value = "5f217783-ff48-4650-ek1306afb3960";
    RunId.fromString(lessThan36Value);
  }
}
