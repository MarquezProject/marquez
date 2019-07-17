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

import java.util.List;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespacesResponseTest {
  private static final List<NamespaceResponse> RESPONSES = newNamespaceResponses(4);

  @Test
  public void testNewResponse() {
    final NamespacesResponse expected = new NamespacesResponse(RESPONSES);
    final NamespacesResponse actual = new NamespacesResponse(RESPONSES);
    assertThat(actual).isEqualTo(expected);
  }
}
