package marquez.client.models;

import static marquez.client.models.ModelGenerator.newNamespace;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceTest {
  private static final Namespace NAMESPACE = newNamespace();
  private static final String JSON = JsonGenerator.newJsonFor(NAMESPACE);

  @Test
  public void testFromJson() {
    final Namespace actual = Namespace.fromJson(JSON);
    assertThat(actual).isEqualTo(NAMESPACE);
  }

  @Test
  public void testFromJson_throwsOnNull() {
    assertThatNullPointerException().isThrownBy(() -> Namespace.fromJson(null));
  }
}
