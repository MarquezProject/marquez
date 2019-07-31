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

import static marquez.api.models.ApiModelGenerator.*;

import java.util.List;
import marquez.UnitTests;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourcesResponseTest {
  private static final List<DatasourceResponse> RESPONSES = newDatasourceResponses(4);

  @Test
  public void testResponse() {
    final DatasourcesResponse actual = new DatasourcesResponse(RESPONSES);
    final DatasourcesResponse expected = new DatasourcesResponse(RESPONSES);
    Assertions.assertThat(actual).isEqualTo(expected);
  }

}
