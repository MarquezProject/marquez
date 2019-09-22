package marquez.client.models;

import static marquez.client.models.ModelGenerator.newRun;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class RunTest {
  private static final Run RUN = newRun();
  private static final String JSON = JsonGenerator.newJsonFor(RUN);

  @Test
  public void testFromJson() {
    final Run actual = Run.fromJson(JSON);
    assertThat(actual).isEqualTo(RUN);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Run.fromJson(null));
  }
}
