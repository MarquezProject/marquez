package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDbTable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DbTableTest {
  private static final Dataset DATASET = newDbTable();
  private static final String JSON = JsonGenerator.newJsonFor(DATASET);

  @Test
  public void testFromJson() {
    final Dataset actual = DbTable.fromJson(JSON);
    assertThat(actual).isEqualTo(DATASET);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> DbTable.fromJson(null));
  }
}
