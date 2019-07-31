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

import static marquez.api.models.ApiModelGenerator.newIsoTimestamp;
import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static org.assertj.core.api.Assertions.assertThat;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceResponseTest {
  private static final String NAME_VALUE = newDatasourceName().getValue();
  private static final String CREATED_AT = newIsoTimestamp();
  private static final String URN_VALUE = newDatasourceUrn().getValue();
  private static final String URL_VALUE = newConnectionUrl().getRawValue();

  @Test
  public void testNewResponse() {
    final DatasourceResponse actual = new DatasourceResponse(NAME_VALUE, CREATED_AT, URN_VALUE, URL_VALUE);
    final DatasourceResponse expected = new DatasourceResponse(NAME_VALUE, CREATED_AT, URN_VALUE, URL_VALUE);
    assertThat(actual).isEqualTo(expected);
  }
}
