/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package marquez.api.models;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static marquez.api.models.ApiModelGenerator.newIsoTimestamp;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newOwnerName;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceResponseTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final String NAME_VALUE = newDatasetName().getValue();
  private static final String CREATED_AT = newIsoTimestamp();
  private static final String OWNER_NAME_VALUE = newOwnerName().getValue();
  private static final String DESCRIPTION_VALUE = newDescription().getValue();
  private static final String NO_DESCRIPTION_VALUE = NO_DESCRIPTION.getValue();

  private static final NamespaceResponse RESPONSE =
      new NamespaceResponse(
          "wedata",
          "2019-06-08T19:11:59.430162Z",
          "analytics",
          "Contains datasets such as room bookings for each office.");
  private static final NamespaceResponse RESPONSE_NO_DESCRIPTION =
      new NamespaceResponse("wedata", "2019-06-08T19:11:59.430162Z", "analytics", null);

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

  @Test
  public void testResponse_toJson() throws Exception {
    final NamespaceResponse actual =
        MAPPER.readValue(fixture("fixtures/namespace/response.json"), NamespaceResponse.class);
    assertThat(actual).isEqualTo(RESPONSE);
  }

  @Test
  public void testResponse_toJson_noDescription() throws Exception {
    final NamespaceResponse actual =
        MAPPER.readValue(
            fixture("fixtures/namespace/response_no_description.json"), NamespaceResponse.class);
    assertThat(actual).isEqualTo(RESPONSE_NO_DESCRIPTION);
  }
}
