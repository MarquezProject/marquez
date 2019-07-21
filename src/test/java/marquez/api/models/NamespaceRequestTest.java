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
public class NamespaceRequestTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final String OWNER_NAME_VALUE = newOwnerName().getValue();
  private static final String DESCRIPTION_VALUE = newDescription().getValue();
  private static final String NO_DESCRIPTION_VALUE = NO_DESCRIPTION.getValue();

  private static final NamespaceRequest REQUEST =
      new NamespaceRequest("analytics", "Contains datasets such as room bookings for each office.");
  private static final NamespaceRequest REQUEST_NO_DESCRIPTION =
      new NamespaceRequest("analytics", null);

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

  @Test
  public void testNewRequest_fromJson() throws Exception {
    final NamespaceRequest actual =
        MAPPER.readValue(fixture("fixtures/namespaces/request.json"), NamespaceRequest.class);
    assertThat(actual).isEqualTo(REQUEST);
  }

  @Test
  public void testNewRequest_fromJson_noDescription() throws Exception {
    final NamespaceRequest actual =
        MAPPER.readValue(
            fixture("fixtures/namespaces/request_no_description.json"), NamespaceRequest.class);
    assertThat(actual).isEqualTo(REQUEST_NO_DESCRIPTION);
  }
}
