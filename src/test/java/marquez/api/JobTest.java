package marquez.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.Test;

public class JobTest {

  private static final String JOB_NAME = "myJob";
  private static final String DESCRIPTION = "the first job";
  private static final Timestamp CREATED_AT = Timestamp.from(Instant.now());
  private static final List<String> INPUT_DATA_SETS = Collections.EMPTY_LIST;
  private static final List<String> OUTPUT_DATA_SETS = Collections.EMPTY_LIST;
  private static final String LOCATION = "git://some/path/123";

  private static final Job JOB =
      new Job(JOB_NAME, CREATED_AT, INPUT_DATA_SETS, OUTPUT_DATA_SETS, LOCATION, DESCRIPTION);

  @Test
  public void testJobEquality() {
    Job j2 =
        new Job(JOB_NAME, CREATED_AT, INPUT_DATA_SETS, OUTPUT_DATA_SETS, LOCATION, DESCRIPTION);
    AssertionsForClassTypes.assertThat(JOB.equals(JOB));
    AssertionsForClassTypes.assertThat(JOB.equals(j2));
    AssertionsForClassTypes.assertThat(j2.equals(JOB));
  }

  @Test
  public void testHashCodeEquality() {
    Job j2 =
        new Job(JOB_NAME, CREATED_AT, INPUT_DATA_SETS, OUTPUT_DATA_SETS, LOCATION, DESCRIPTION);
    assertEquals(JOB.hashCode(), j2.hashCode());
  }

  @Test
  public void testJobInequalityOnNonIDField() {
    Job j2 =
        new Job(
            JOB_NAME,
            CREATED_AT,
            INPUT_DATA_SETS,
            OUTPUT_DATA_SETS,
            "someOtherLocation",
            DESCRIPTION);
    AssertionsForClassTypes.assertThat(!JOB.equals(j2));
    AssertionsForClassTypes.assertThat(JOB.equals(JOB));
  }

  @Test
  public void testJobHashcodeInequalityOnNonIdField() {
    Job j2 =
        new Job(
            JOB_NAME,
            CREATED_AT,
            INPUT_DATA_SETS,
            OUTPUT_DATA_SETS,
            "someOtherLocation",
            DESCRIPTION);
    assertNotEquals(JOB.hashCode(), j2.hashCode());
  }
}
