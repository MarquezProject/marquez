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

import static marquez.api.models.ApiModelGenerator.newDatasetRequest;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.UnitTests;
import marquez.api.models.DatasetRequest;
import marquez.service.models.Dataset;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetMapperTest {
  @Test
  public void testMap_request() {
    final DatasetRequest request = newDatasetRequest();
    final Dataset dataset = DatasetMapper.map(request);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getName().getValue()).isEqualTo(request.getName());
    assertThat(dataset.getDatasourceUrn().getValue()).isEqualTo(request.getDatasourceUrn());
    assertThat(dataset.getDescription().getValue()).isEqualTo(request.getDescription().get());
  }

  @Test
  public void testMap_request_noDescription() {
    final DatasetRequest request = newDatasetRequest(false);
    final Dataset dataset = DatasetMapper.map(request);
    assertThat(dataset).isNotNull();
    assertThat(dataset.getName().getValue()).isEqualTo(request.getName());
    assertThat(dataset.getDatasourceUrn().getValue()).isEqualTo(request.getDatasourceUrn());
    assertThat(dataset.getDescription().getValue()).isEqualTo(NO_DESCRIPTION.getValue());
  }

  @Test
  public void testMap_throwsException_onNullRequest() {
    final DatasetRequest nullRequest = null;
    assertThatNullPointerException().isThrownBy(() -> DatasetMapper.map(nullRequest));
  }
}
