package marquez.api.models;

import static marquez.api.models.ApiModelGenerator.newNamespaceResponses;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespacesResponseTest {
  private static final List<NamespaceResponse> RESPONSES = newNamespaceResponses(4);

  @Test
  public void testNewResponse() {
    final NamespacesResponse expected = new NamespacesResponse(RESPONSES);
    final NamespacesResponse actual = new NamespacesResponse(RESPONSES);
    assertThat(actual).isEqualTo(expected);
  }
}
