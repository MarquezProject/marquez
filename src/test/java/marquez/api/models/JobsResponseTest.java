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
public class JobsResponseTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final List<JobResponse> JOBS = newJobResponses(1);
  private static final JobsResponse RESPONSE = new JobsResponse(JOBS);

  @Test
  public void testNewResponse() {
    final JobsResponse actual = new JobsResponse(JOBS);
    final JobsResponse expected = new JobsResponse(JOBS);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson() throws Exception {
    final ArrayNode array0 = MAPPER.valueToTree(JOBS.get(0).getInputDatasetUrns());
    final ArrayNode array1 = MAPPER.valueToTree(JOBS.get(0).getOutputDatasetUrns());
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("name", JOBS.get(0).getName())
            .put("createdAt", JOBS.get(0).getCreatedAt())
            .put("updatedAt", JOBS.get(0).getUpdatedAt());
    obj.putArray("inputDatasetUrns").addAll(array0);
    obj.putArray("outputDatasetUrns").addAll(array1);
    obj.put("location", JOBS.get(0).getLocation());
    obj.put("description", JOBS.get(0).getDescription().orElseThrow(Exception::new));
    final ArrayNode array2 = MAPPER.createArrayNode().addPOJO(obj);
    final String expected = MAPPER.createObjectNode().set("jobs", array2).toString();
    final String actual = MAPPER.writeValueAsString(RESPONSE);
    assertThat(actual).isEqualTo(expected);
  }
}
