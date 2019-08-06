package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newOwnerName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.Optional;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceMetaTest {
  private static final String OWNER_NAME = newOwnerName();
  private static final String DESCRIPTION = newDescription();

  @Test
  public void testNamespaceMeta() {
    final NamespaceMeta namespaceMeta =
        NamespaceMeta.builder().ownerName(OWNER_NAME).description(DESCRIPTION).build();

    assertThat(namespaceMeta.getOwnerName()).isEqualTo(OWNER_NAME);
    assertThat(namespaceMeta.getDescription()).isEqualTo(Optional.ofNullable(DESCRIPTION));
  }

  @Test
  public void testNamespaceMeta_noOwnerName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              NamespaceMeta.builder().description(DESCRIPTION).build();
            });
  }

  @Test
  public void testNamespaceMeta_blankOwnerName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              NamespaceMeta.builder().ownerName(" ").description(DESCRIPTION).build();
            });
  }

  @Test
  public void testNamespaceMeta_noDescription() {
    final NamespaceMeta namespaceMeta = NamespaceMeta.builder().ownerName(OWNER_NAME).build();

    assertThat(namespaceMeta.getOwnerName()).isEqualTo(OWNER_NAME);
    assertThat(namespaceMeta.getDescription()).isEqualTo(Optional.ofNullable(null));
  }
}
