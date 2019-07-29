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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static marquez.api.models.ApiModelGenerator.newIsoTimestamp;
import static marquez.api.models.ApiModelGenerator.newJobResponse;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasetUrns;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newLocation;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.dropwizard.jackson.Jackson;
import java.util.List;
import marquez.UnitTests;
import marquez.common.models.DatasetUrn;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class JobResponseTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final String NAME_VALUE = newDatasetName().getValue();
  private static final String CREATED_AT = newIsoTimestamp();
  private static final String UPDATED_AT = CREATED_AT;
  private static final List<String> INPUT_DATASET_URNS =
      newDatasetUrns(4).stream().map(DatasetUrn::getValue).collect(toImmutableList());
  private static final List<String> OUTPUT_DATASET_URNS =
      newDatasetUrns(2).stream().map(DatasetUrn::getValue).collect(toImmutableList());
  private static final String LOCATION_VALUE = newLocation().toString();
  private static final String DESCRIPTION_VALUE = newDescription().getValue();
  private static final String NO_DESCRIPTION_VALUE = NO_DESCRIPTION.getValue();

  private static final JobResponse RESPONSE = newJobResponse();
  private static final JobResponse RESPONSE_NO_DESCRIPTION = newJobResponse(false);

  @Test
  public void testNewResponse() {
    final JobResponse actual =
        new JobResponse(
            NAME_VALUE,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION_VALUE,
            DESCRIPTION_VALUE);
    final JobResponse expected =
        new JobResponse(
            NAME_VALUE,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION_VALUE,
            DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testNewResponse_noDescription() {
    final JobResponse actual =
        new JobResponse(
            NAME_VALUE,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION_VALUE,
            NO_DESCRIPTION_VALUE);
    final JobResponse expected =
        new JobResponse(
            NAME_VALUE,
            CREATED_AT,
            UPDATED_AT,
            INPUT_DATASET_URNS,
            OUTPUT_DATASET_URNS,
            LOCATION_VALUE,
            NO_DESCRIPTION_VALUE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson() throws Exception {
    final String expected = buildJsonFor(RESPONSE);
    final String actual = MAPPER.writeValueAsString(RESPONSE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson_noDescription() throws Exception {
    final String expected = buildJsonFor(RESPONSE_NO_DESCRIPTION);
    final String actual = MAPPER.writeValueAsString(RESPONSE_NO_DESCRIPTION);
    assertThat(actual).isEqualTo(expected);
  }

  private String buildJsonFor(JobResponse response) {
    final ArrayNode array0 = MAPPER.valueToTree(response.getInputDatasetUrns());
    final ArrayNode array1 = MAPPER.valueToTree(response.getOutputDatasetUrns());
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("name", response.getName())
            .put("createdAt", response.getCreatedAt())
            .put("updatedAt", response.getUpdatedAt());
    obj.putArray("inputDatasetUrns").addAll(array0);
    obj.putArray("outputDatasetUrns").addAll(array1);
    obj.put("location", response.getLocation());
    obj.put("description", response.getDescription().orElse(null));
    return obj.toString();
  }
}
