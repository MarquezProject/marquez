package marquez.client.models;

import static marquez.client.models.ModelGenerator.newJobRun;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobRunTest {
  private static final JobRun RUN = newJobRun();
  private static final String JSON = JsonGenerator.newJsonFor(RUN);

  @Test
  public void testFromJson() {
    final JobRun actual = JobRun.fromJson(JSON);
    assertThat(actual).isEqualTo(RUN);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> JobRun.fromJson(null));
  }
}
