package marquez.api;

import static marquez.Generator.newTimestamp;
import static marquez.api.NamespaceResource.Namespaces;
import static marquez.common.models.ModelGenerator.newNamespaceName;
import static marquez.service.models.ModelGenerator.newNamespace;
import static marquez.service.models.ModelGenerator.newNamespaceMeta;
import static marquez.service.models.ModelGenerator.newNamespaceWith;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.Optional;
import javax.ws.rs.core.Response;
import marquez.UnitTests;
import marquez.api.exceptions.NamespaceNotFoundException;
import marquez.common.models.NamespaceName;
import marquez.service.NamespaceService;
import marquez.service.exceptions.MarquezServiceException;
import marquez.service.models.Namespace;
import marquez.service.models.NamespaceMeta;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@Category(UnitTests.class)
public class NamespaceResourceTest {
  private static final NamespaceName NAMESPACE_NAME = newNamespaceName();

  private static final Namespace NAMESPACE_0 = newNamespace();
  private static final Namespace NAMESPACE_1 = newNamespace();
  private static final Namespace NAMESPACE_2 = newNamespace();
  private static final ImmutableList<Namespace> NAMESPACES =
      ImmutableList.of(NAMESPACE_0, NAMESPACE_1, NAMESPACE_2);

  @Rule public MockitoRule rule = MockitoJUnit.rule();

  @Mock private NamespaceService service;
  private NamespaceResource resource;

  @Before
  public void setUp() {
    resource = new NamespaceResource(service);
  }

  @Test
  public void testCreateOrUpdate() throws MarquezServiceException {
    final NamespaceMeta meta = newNamespaceMeta();
    final Namespace namespace = toNamespace(NAMESPACE_NAME, meta);
    when(service.createOrUpdate(NAMESPACE_NAME, meta)).thenReturn(namespace);

    final Response response = resource.createOrUpdate(NAMESPACE_NAME, meta);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Namespace) response.getEntity()).isEqualTo(namespace);
  }

  @Test
  public void testGet() throws MarquezServiceException {
    final Namespace namespace = newNamespaceWith(NAMESPACE_NAME);
    when(service.get(NAMESPACE_NAME)).thenReturn(Optional.of(namespace));

    final Response response = resource.get(NAMESPACE_NAME);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat((Namespace) response.getEntity()).isEqualTo(namespace);
  }

  @Test
  public void testGet_notFound() throws MarquezServiceException {
    when(service.get(NAMESPACE_NAME)).thenReturn(Optional.empty());

    assertThatExceptionOfType(NamespaceNotFoundException.class)
        .isThrownBy(() -> resource.get(NAMESPACE_NAME))
        .withMessageContaining(String.format("'%s' not found", NAMESPACE_NAME.getValue()));
  }

  @Test
  public void testList() throws MarquezServiceException {
    when(service.getAll(4, 0)).thenReturn(NAMESPACES);

    final Response response = resource.list(4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Namespaces) response.getEntity()).getValue())
        .containsOnly(NAMESPACE_0, NAMESPACE_1, NAMESPACE_2);
  }

  @Test
  public void testList_empty() throws MarquezServiceException {
    when(service.getAll(4, 0)).thenReturn(ImmutableList.of());

    final Response response = resource.list(4, 0);
    assertThat(response.getStatus()).isEqualTo(200);
    assertThat(((Namespaces) response.getEntity()).getValue()).isEmpty();
  }

  static Namespace toNamespace(final NamespaceName namespaceName, final NamespaceMeta meta) {
    final Instant now = newTimestamp();
    return new Namespace(
        namespaceName, now, now, meta.getOwnerName(), meta.getDescription().orElse(null));
  }
}
