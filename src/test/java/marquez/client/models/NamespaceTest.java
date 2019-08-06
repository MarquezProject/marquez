package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDescription;
import static marquez.client.models.ModelGenerator.newNamespaceName;
import static marquez.client.models.ModelGenerator.newOwnerName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import java.util.Optional;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceTest {
  private static final String NAMESPACE_NAME = newNamespaceName();
  private static final Instant CREATED_AT = Instant.now();
  private static final String OWNER_NAME = newOwnerName();
  private static final String DESCRIPTION = newDescription();

  @Test
  public void testNamespace() {
    final Namespace namespace = new Namespace(NAMESPACE_NAME, CREATED_AT, OWNER_NAME, DESCRIPTION);

    assertThat(namespace.getName()).isEqualTo(NAMESPACE_NAME);
    assertThat(namespace.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(namespace.getOwnerName()).isEqualTo(OWNER_NAME);
    assertThat(namespace.getDescription()).isEqualTo(Optional.ofNullable(DESCRIPTION));
  }

  @Test
  public void testNamespace_noNamespaceName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Namespace(null, CREATED_AT, OWNER_NAME, DESCRIPTION);
            });
  }

  @Test
  public void testNamespace_blankNamespaceName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Namespace(" ", CREATED_AT, OWNER_NAME, DESCRIPTION);
            });
  }

  @Test
  public void testNamespace_noCreatedAt() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Namespace(NAMESPACE_NAME, null, OWNER_NAME, DESCRIPTION);
            });
  }

  @Test
  public void testNamespace_noOwnerName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Namespace(NAMESPACE_NAME, CREATED_AT, null, DESCRIPTION);
            });
  }

  @Test
  public void testNamespace_blankOwnerName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Namespace(NAMESPACE_NAME, CREATED_AT, " ", DESCRIPTION);
            });
  }

  @Test
  public void testNamespace_noDescription() {
    final Namespace namespace = new Namespace(NAMESPACE_NAME, CREATED_AT, OWNER_NAME, null);

    assertThat(namespace.getName()).isEqualTo(NAMESPACE_NAME);
    assertThat(namespace.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(namespace.getOwnerName()).isEqualTo(OWNER_NAME);
    assertThat(namespace.getDescription()).isEqualTo(Optional.ofNullable(null));
  }
}
