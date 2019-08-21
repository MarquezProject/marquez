package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDatasource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceTest {
  private static final Datasource DATASOURCE = newDatasource();
  private static final String JSON = JsonGenerator.newJsonFor(DATASOURCE);

  @Test
  public void testFromJson() {
    final Datasource actual = Datasource.fromJson(JSON);
    assertThat(actual).isEqualTo(DATASOURCE);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Datasource.fromJson(null));
  }
}
