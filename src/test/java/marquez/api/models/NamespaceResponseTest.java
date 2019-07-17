package marquez.api.models;

import static marquez.api.models.ApiModelGenerator.newIsoTimestamp;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceResponseTest {
  private static final String NAME_VALUE = newDatasetName().getValue();
  private static final String CREATED_AT = newIsoTimestamp();
  private static final String OWNER_NAME_VALUE = newOwnerName().getValue();
  private static final String DESCRIPTION_VALUE = newDescription().getValue();
  private static final String NO_DESCRIPTION_VALUE = NO_DESCRIPTION.getValue();

  @Test
  public void testNewResponse() {
    NamespaceResponse expected =
        new NamespaceResponse(NAME_VALUE, CREATED_AT, OWNER_NAME_VALUE, DESCRIPTION_VALUE);
    NamespaceResponse actual =
        new NamespaceResponse(NAME_VALUE, CREATED_AT, OWNER_NAME_VALUE, DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNewResponse_noDescription() {
    NamespaceResponse expected =
        new NamespaceResponse(NAME_VALUE, CREATED_AT, OWNER_NAME_VALUE, NO_DESCRIPTION_VALUE);
    NamespaceResponse actual =
        new NamespaceResponse(NAME_VALUE, CREATED_AT, OWNER_NAME_VALUE, NO_DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }
}
