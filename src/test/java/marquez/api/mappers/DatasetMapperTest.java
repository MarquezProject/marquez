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

package marquez.api.mappers;

import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDatasourceUrn;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.UnitTests;
import marquez.api.models.DatasetRequest;
import marquez.common.models.DatasetName;
import marquez.common.models.DatasourceUrn;
import marquez.common.models.Description;
import marquez.service.models.Dataset;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetMapperTest {
  private static final DatasourceUrn DATASOURCE_URN = newDatasourceUrn();
  private static final DatasetName DATASET_NAME = newDatasetName();
  private static final Description DESCRIPTION = newDescription();
  private static final DatasetRequest REQUEST =
      new DatasetRequest(DATASET_NAME, DATASOURCE_URN, DESCRIPTION);
  private static final DatasetRequest REQUEST_NO_DESCRIPTION =
      new DatasetRequest(DATASET_NAME, DATASOURCE_URN, null);

  @Test
  public void testMap_request() {
    final Dataset dataset = DatasetMapper.map(REQUEST);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getName()).isEqualTo(DATASET_NAME);
    assertThat(dataset.getDescription()).isEqualTo(DESCRIPTION);
  }

  @Test
  public void testMap_request_noDescription() {
    final Dataset dataset = DatasetMapper.map(REQUEST_NO_DESCRIPTION);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getName()).isEqualTo(DATASET_NAME);
    assertThat(dataset.getDescription()).isEqualTo(null);
  }

  @Test
  public void testMap_throwsException_onNullRequest() {
    final DatasetRequest nullRequest = null;
    assertThatNullPointerException().isThrownBy(() -> DatasetMapper.map(nullRequest));
  }
}
