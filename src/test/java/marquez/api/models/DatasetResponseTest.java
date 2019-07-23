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
import static marquez.api.models.ApiModelGenerator.asJson;
import static marquez.api.models.ApiModelGenerator.newDatasetResponse;
import static marquez.api.models.ApiModelGenerator.newIsoTimestamp;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrn;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetResponseTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final String NAME_VALUE = newDatasetName().getValue();
  private static final String CREATED_AT = newIsoTimestamp();
  private static final String URN_VALUE = newDatasetUrn().getValue();
  private static final String DATASOURCE_URN_VALUE = newDatasourceUrn().getValue();
  private static final String DESCRIPTION_VALUE = newDescription().getValue();
  private static final String NO_DESCRIPTION_VALUE = NO_DESCRIPTION.getValue();

  private static final DatasetResponse RESPONSE = newDatasetResponse();
  private static final DatasetResponse RESPONSE_NO_DESCRIPTION = newDatasetResponse(false);

  @Test
  public void testNewResponse() {
    final DatasetResponse expected =
        new DatasetResponse(
            NAME_VALUE, CREATED_AT, URN_VALUE, DATASOURCE_URN_VALUE, DESCRIPTION_VALUE);
    final DatasetResponse actual =
        new DatasetResponse(
            NAME_VALUE, CREATED_AT, URN_VALUE, DATASOURCE_URN_VALUE, DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNewResponse_noDescription() {
    final DatasetResponse expected =
        new DatasetResponse(
            NAME_VALUE, CREATED_AT, URN_VALUE, DATASOURCE_URN_VALUE, NO_DESCRIPTION_VALUE);
    final DatasetResponse actual =
        new DatasetResponse(
            NAME_VALUE, CREATED_AT, URN_VALUE, DATASOURCE_URN_VALUE, NO_DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson() throws Exception {
    final String expected = MAPPER.writeValueAsString(RESPONSE);
    final String actual = MAPPER.writeValueAsString(MAPPER.readValue(asJson(RESPONSE), DatasetResponse.class));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson_noDescription() throws Exception {
    final String expected = MAPPER.writeValueAsString(RESPONSE_NO_DESCRIPTION);
    final String actual = MAPPER.writeValueAsString(MAPPER.readValue(asJson(RESPONSE_NO_DESCRIPTION), DatasetResponse.class));
    assertThat(actual).isEqualTo(expected);
  }
}
