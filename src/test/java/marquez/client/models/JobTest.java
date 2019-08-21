package marquez.client.models;

import static marquez.client.models.ModelGenerator.newJob;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobTest {
  private static final Job JOB = newJob();
  private static final String JSON = JsonGenerator.newJsonFor(JOB);

  @Test
  public void testFromJson() {
    final Job actual = Job.fromJson(JSON);
    assertThat(actual).isEqualTo(JOB);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Job.fromJson(null));
  }
}
