package marquez.client.models;

import static marquez.client.models.ModelGenerator.newStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class StreamTest {
  private static final Dataset DATASET = newStream();
  private static final String JSON = JsonGenerator.newJsonFor(DATASET);

  @Test
  public void testFromJson() {
    final Dataset actual = Stream.fromJson(JSON);
    assertThat(actual).isEqualTo(DATASET);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Stream.fromJson(null));
  }
}
