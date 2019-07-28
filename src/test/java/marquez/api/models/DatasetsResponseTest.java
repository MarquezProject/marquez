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

  private static final List<DatasetResponse> DATASETS = newDatasetResponses(1);
  private static final DatasetsResponse RESPONSE = new DatasetsResponse(DATASETS);

  @Test
  public void testNewResponse() {
    final DatasetsResponse expected = new DatasetsResponse(DATASETS);
    final DatasetsResponse actual = new DatasetsResponse(DATASETS);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson() throws Exception {
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("name", DATASETS.get(0).getName())
            .put("createdAt", DATASETS.get(0).getCreatedAt())
            .put("urn", DATASETS.get(0).getUrn())
            .put("datasourceUrn", DATASETS.get(0).getDatasourceUrn())
            .put("description", DATASETS.get(0).getDescription().orElseThrow(Exception::new));
    final ArrayNode array = MAPPER.createArrayNode().addPOJO(obj);
    final String expected = MAPPER.createObjectNode().set("datasets", array).toString();
    final String actual = MAPPER.writeValueAsString(RESPONSE);
    System.out.println(actual);
    assertThat(actual).isEqualTo(expected);
  }
}
