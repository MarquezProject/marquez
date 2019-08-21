package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDatasourceMeta;
import static org.assertj.core.api.Assertions.assertThat;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceMetaTest {
  private static final DatasourceMeta META = newDatasourceMeta();
  private static final String JSON = JsonGenerator.newJsonFor(META);

  @Test
  public void testToJson() {
    final String actual = META.toJson();
    assertThat(actual).isEqualTo(JSON);
  }
}
