package marquez.api.models;

import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceRequestTest {

  private static final String OWNER_NAME_VALUE = newOwnerName().getValue();
  private static final String DESCRIPTION_VALUE = newDescription().getValue();
  private static final String NO_DESCRIPTION_VALUE = NO_DESCRIPTION.getValue();

  @Test
  public void testNewRequest() {
    final NamespaceRequest expected = new NamespaceRequest(OWNER_NAME_VALUE, DESCRIPTION_VALUE);
    final NamespaceRequest actual = new NamespaceRequest(OWNER_NAME_VALUE, DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNewRequest_noDescription() {
    final NamespaceRequest expected = new NamespaceRequest(OWNER_NAME_VALUE, NO_DESCRIPTION_VALUE);
    final NamespaceRequest actual = new NamespaceRequest(OWNER_NAME_VALUE, NO_DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }
}
