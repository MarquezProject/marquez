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

import static marquez.api.models.ApiModelGenerator.newDatasetRequest;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
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
public class DatasetRequestTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final String NAME_VALUE = newDatasetName().getValue();
  private static final String DATASOURCE_URN_VALUE = newDatasourceUrn().getValue();
  private static final String DESCRIPTION_VALUE = newDescription().getValue();
  private static final String NO_DESCRIPTION_VALUE = NO_DESCRIPTION.getValue();

  private static final DatasetRequest REQUEST = newDatasetRequest();
  private static final DatasetRequest REQUEST_NO_DESCRIPTION = newDatasetRequest(false);

  @Test
  public void testNewRequest() {
    final DatasetRequest expected =
        new DatasetRequest(NAME_VALUE, DATASOURCE_URN_VALUE, DESCRIPTION_VALUE);
    final DatasetRequest actual =
        new DatasetRequest(NAME_VALUE, DATASOURCE_URN_VALUE, DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNewRequest_noDescription() {
    final DatasetRequest expected =
        new DatasetRequest(NAME_VALUE, DATASOURCE_URN_VALUE, NO_DESCRIPTION_VALUE);
    final DatasetRequest actual =
        new DatasetRequest(NAME_VALUE, DATASOURCE_URN_VALUE, NO_DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNewRequest_fromJson() throws Exception {
    final String requestAsJson =
        MAPPER
            .createObjectNode()
            .put("name", REQUEST.getName())
            .put("datasourceUrn", REQUEST.getDatasourceUrn())
            .put("description", REQUEST.getDescription().get())
            .toString();
    final DatasetRequest actual = MAPPER.readValue(requestAsJson, DatasetRequest.class);
    assertThat(actual).isEqualTo(REQUEST);
  }

  @Test
  public void testNewRequest_fromJson_noDescription() throws Exception {
    final String requestAsJson =
        MAPPER
            .createObjectNode()
            .put("name", REQUEST_NO_DESCRIPTION.getName())
            .put("datasourceUrn", REQUEST_NO_DESCRIPTION.getDatasourceUrn())
            .put("description", REQUEST_NO_DESCRIPTION.getDescription().orElse(null))
            .toString();
    final DatasetRequest actual = MAPPER.readValue(requestAsJson, DatasetRequest.class);
    assertThat(actual).isEqualTo(REQUEST_NO_DESCRIPTION);
  }
}
