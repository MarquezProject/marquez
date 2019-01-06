package marquez.common.models;

import static org.junit.Assert.assertEquals;

import java.util.UUID;
import org.junit.Test;

public class RunIdTest {
  private static final String ID_STRING = "f662a43d-ab87-4b4b-a9ba-828b53c3368b";
  private static final UUID RUN_ID = UUID.fromString(ID_STRING);

  @Test
  public void testNewRunId() {
    final RunId runId = new RunId(ID_STRING);
    assertEquals(RUN_ID, runId.getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testRunIdNull() {
    final String nullRunId = null;
    new RunId(nullRunId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRunIdEmpty() {
    final String emptyRunId = "";
    new RunId(emptyRunId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRunIdBlank() {
    final String blankRunId = " ";
    new RunId(blankRunId);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRunIdGreaterThan36() {
    final String runIdGreaterThan36 = "5f217783-ff48-4650-aab4-ek1306afb3960";
    new RunId(runIdGreaterThan36);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRunIdLessThan36() {
    final String runIdLessThan36 = "5f217783-ff48-4650-ek1306afb3960";
    new RunId(runIdLessThan36);
  }
}
