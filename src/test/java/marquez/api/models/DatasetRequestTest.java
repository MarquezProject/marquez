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

import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;

import marquez.UnitTests;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetRequestTest {
  private static final DatasetName NAME = newDatasetName();
  private static final DatasourceUrn URN = newDatasourceUrn();
  private static final Description DESCRIPTION = newDescription();

  @Test
  public void testNewRequest() {
    final DatasetRequest expected =
        new DatasetRequest(NAME.getValue(), URN.getValue(), DESCRIPTION.getValue());
    final DatasetRequest actual =
        new DatasetRequest(NAME.getValue(), URN.getValue(), DESCRIPTION.getValue());
    assertThat(expected).isEqualTo(actual);
  }

  @Test
  public void testNewRequest_noDescription() {
    final DatasetRequest expected =
        new DatasetRequest(NAME.getValue(), URN.getValue(), NO_DESCRIPTION.getValue());
    final DatasetRequest actual =
        new DatasetRequest(NAME.getValue(), URN.getValue(), NO_DESCRIPTION.getValue());
    assertThat(expected).isEqualTo(actual);
  }
}
