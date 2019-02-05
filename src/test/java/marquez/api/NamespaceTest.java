package marquez.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import marquez.service.models.Namespace;
import org.junit.Test;

public class NamespaceTest {

  private static final UUID NAMESPACE_UUID = UUID.randomUUID();
  private static final String NAMESPACE_NAME = "myNamespace";
  private static final String OWNER_NAME = "myOwner";
  private static final Timestamp CREATED_AT = Timestamp.from(Instant.now());
  private static final String DESCRIPTION = "the first namespace";

  private static final Namespace NAMESPACE =
      new Namespace(NAMESPACE_UUID, CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);

  @Test
  public void testGuidSet() {
    assertThat(NAMESPACE.getGuid()).isEqualTo(NAMESPACE_UUID);
  }

  @Test
  public void testNamespaceEquality() {
    Namespace namespace =
        new Namespace(NAMESPACE_UUID, CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE).isEqualTo(NAMESPACE);
    assertThat(NAMESPACE).isEqualTo(namespace);
    assertThat(namespace).isEqualTo(NAMESPACE);
  }

  @Test
  public void testHashCodeEquality() {
    Namespace namespace =
        new Namespace(NAMESPACE_UUID, CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE.hashCode()).isEqualTo(namespace.hashCode());
  }

  @Test
  public void testNamespaceInequalityOnUUID() {
    Namespace namespace =
        new Namespace(UUID.randomUUID(), CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE).isNotEqualTo(namespace);
  }

  @Test
  public void testNamespaceInequalityOnNonIDField() {
    Namespace namespace =
        new Namespace(
            NAMESPACE_UUID, CREATED_AT, "some other namespace name", OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE).isNotEqualTo(namespace);
  }

  @Test
  public void testNamespaceHashcodeInequality() {
    Namespace namespace =
        new Namespace(UUID.randomUUID(), CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE.hashCode()).isNotEqualTo(namespace.hashCode());
  }

  @Test
  public void testNamespaceHashcodeInequalityOnNonIdField() {
    Namespace namespace =
        new Namespace(
            NAMESPACE_UUID, CREATED_AT, "some other namespace name", OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE.hashCode()).isNotEqualTo(namespace.hashCode());
  }

  @Test
  public void testNamespaceToStringInequality() {
    Namespace namespace =
        new Namespace(UUID.randomUUID(), CREATED_AT, NAMESPACE_NAME, OWNER_NAME, DESCRIPTION);
    assertThat(NAMESPACE.toString()).isNotEqualTo(namespace.toString());
  }
}
