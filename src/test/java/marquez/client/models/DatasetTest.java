package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDataset;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetTest {
  private static final Dataset DATASET = newDataset();
  private static final String JSON = JsonGenerator.newJsonFor(DATASET);

  @Test
  public void testFromJson() {
    final Dataset actual = Dataset.fromJson(JSON);
    assertThat(actual).isEqualTo(DATASET);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Dataset.fromJson(null));
  }
}
