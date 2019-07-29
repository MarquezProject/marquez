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

import static marquez.api.models.ApiModelGenerator.newJobResponses;
import static marquez.common.models.Description.NO_DESCRIPTION;
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
public class JobsResponseTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final List<JobResponse> JOBS = newJobResponses(4);
  private static final JobsResponse RESPONSE = new JobsResponse(JOBS);

  @Test
  public void testNewResponse() {
    final JobsResponse actual = new JobsResponse(JOBS);
    final JobsResponse expected = new JobsResponse(JOBS);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson() throws Exception {
    final String expected = buildJsonFor(RESPONSE);
    final String actual = MAPPER.writeValueAsString(RESPONSE);
    assertThat(actual).isEqualTo(expected);
  }

  private String buildJsonFor(JobsResponse response) {
    final ArrayNode array = MAPPER.createArrayNode();
    response
        .getJobs()
        .forEach(
            (job) -> {
              final ArrayNode array0 = MAPPER.valueToTree(job.getInputDatasetUrns());
              final ArrayNode array1 = MAPPER.valueToTree(job.getOutputDatasetUrns());
              final ObjectNode obj =
                  MAPPER
                      .createObjectNode()
                      .put("name", job.getName())
                      .put("createdAt", job.getCreatedAt())
                      .put("updatedAt", job.getUpdatedAt());
              obj.putArray("inputDatasetUrns").addAll(array0);
              obj.putArray("outputDatasetUrns").addAll(array1);
              obj.put("location", job.getLocation());
              obj.put("description", job.getDescription().orElse(NO_DESCRIPTION.getValue()));
              array.addPOJO(obj);
            });
    final JsonNode responseAsJson = MAPPER.createObjectNode().set("jobs", array);
    return responseAsJson.toString();
  }
}
