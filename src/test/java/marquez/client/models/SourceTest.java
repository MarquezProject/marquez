package marquez.client.models;

import static marquez.client.models.ModelGenerator.newSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class SourceTest {
  private static final Source SOURCE = newSource();
  private static final String JSON = JsonGenerator.newJsonFor(SOURCE);

  @Test
  public void testFromJson() {
    final Source actual = Source.fromJson(JSON);
    assertThat(actual).isEqualTo(SOURCE);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Source.fromJson(null));
  }
}
