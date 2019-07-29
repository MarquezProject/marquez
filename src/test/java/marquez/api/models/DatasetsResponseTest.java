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

import static marquez.api.models.ApiModelGenerator.newDatasetResponses;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.jackson.Jackson;
import java.util.List;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetsResponseTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final List<DatasetResponse> DATASETS = newDatasetResponses(4);
  private static final DatasetsResponse RESPONSE = new DatasetsResponse(DATASETS);

  @Test
  public void testNewResponse() {
    final DatasetsResponse expected = new DatasetsResponse(DATASETS);
    final DatasetsResponse actual = new DatasetsResponse(DATASETS);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson() throws Exception {
    final String expected = buildJsonFor(RESPONSE);
    final String actual = MAPPER.writeValueAsString(RESPONSE);
    assertThat(actual).isEqualTo(expected);
  }

  private String buildJsonFor(DatasetsResponse response) {
    final ArrayNode array = MAPPER.createArrayNode();
    response
        .getDatasets()
        .forEach(
            (dataset) -> {
              final ObjectNode obj =
                  MAPPER
                      .createObjectNode()
                      .put("name", dataset.getName())
                      .put("createdAt", dataset.getCreatedAt())
                      .put("urn", dataset.getUrn())
                      .put("datasourceUrn", dataset.getDatasourceUrn())
                      .put("description", dataset.getDescription().orElse(null));
              array.addPOJO(obj);
            });
    final JsonNode responseAsJson = MAPPER.createObjectNode().set("datasets", array);
    return responseAsJson.toString();
  }
}
