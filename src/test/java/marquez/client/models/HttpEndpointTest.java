package marquez.client.models;

import static marquez.client.models.ModelGenerator.newHttpEndpoint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class HttpEndpointTest {
  private static final Dataset DATASET = newHttpEndpoint();
  private static final String JSON = JsonGenerator.newJsonFor(DATASET);

  @Test
  public void testFromJson() {
    final Dataset actual = HttpEndpoint.fromJson(JSON);
    assertThat(actual).isEqualTo(DATASET);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> HttpEndpoint.fromJson(null));
  }
}
