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

import static marquez.api.models.ApiModelGenerator.newNamespaceResponses;
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
public class NamespacesResponseTest {
  private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

  private static final List<NamespaceResponse> NAMESPACES = newNamespaceResponses(1);
  private static final NamespacesResponse RESPONSE = new NamespacesResponse(NAMESPACES);

  @Test
  public void testNewResponse() {
    final NamespacesResponse expected = new NamespacesResponse(NAMESPACES);
    final NamespacesResponse actual = new NamespacesResponse(NAMESPACES);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testResponse_toJson() throws Exception {
    final ObjectNode obj =
        MAPPER
            .createObjectNode()
            .put("name", NAMESPACES.get(0).getName())
            .put("createdAt", NAMESPACES.get(0).getCreatedAt())
            .put("ownerName", NAMESPACES.get(0).getOwnerName())
            .put("description", NAMESPACES.get(0).getDescription().orElseThrow(Exception::new));
    final ArrayNode array = MAPPER.createArrayNode().addPOJO(obj);
    final String expected = MAPPER.createObjectNode().set("namespaces", array).toString();
    final String actual = MAPPER.writeValueAsString(RESPONSE);
    assertThat(actual).isEqualTo(expected);
  }
}
