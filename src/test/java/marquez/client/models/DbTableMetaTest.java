package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDbTableMeta;
import static org.assertj.core.api.Assertions.assertThat;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbTableMetaTest {
  private static final DatasetMeta META = newDbTableMeta();
  private static final String JSON = JsonGenerator.newJsonFor(META);

  @Test
  public void testToJson() {
    final String actual = META.toJson();
    assertThat(actual).isEqualTo(JSON);
  }
}
