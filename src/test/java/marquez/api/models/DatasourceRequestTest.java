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

import static marquez.api.models.ApiModelGenerator.newDatasourceRequest;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceRequestTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final DatasourceRequest REQUEST = newDatasourceRequest();

  @Test
  public void testNewRequest_fromJson() throws Exception {
    final String requestAsJson = JsonGenerator.newJsonFor(REQUEST);
    final DatasourceRequest actual = MAPPER.readValue(requestAsJson, DatasourceRequest.class);
    assertThat(actual).isEqualTo(REQUEST);
  }
}
