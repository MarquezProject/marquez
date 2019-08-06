package marquez.client.models;

import static marquez.client.models.ModelGenerator.newNamespaces;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespacesTest {
  @Test
  public void testNamespaces() {
    final List<Namespace> namespaceList = newNamespaces(3);
    final Namespaces namespaces = new Namespaces(namespaceList);

    assertThat(namespaces.getNamespaces().size()).isEqualTo(3);
    assertThat(namespaces.getNamespaces()).isEqualTo(namespaceList);
  }
}
