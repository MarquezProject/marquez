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

import java.util.Optional;
import marquez.UnitTests;
import marquez.api.models.DatasetRequest;
import marquez.service.models.DatasetMeta;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetMetaMapperTest {
  @Test
  public void testMap_request() {
    final DatasetRequest request = newDatasetRequest();
    final DatasetMeta meta = DatasetMetaMapper.map(request);
    assertThat(meta).isNotNull();
    assertThat(meta.getName().getValue()).isEqualTo(request.getName());
    assertThat(meta.getDatasourceUrn().getValue()).isEqualTo(request.getDatasourceUrn());
    assertThat(meta.getDescription()).isNotEmpty();
  }

  @Test
  public void testMap_request_noDescription() {
    final DatasetRequest request = newDatasetRequest(false);
    final DatasetMeta meta = DatasetMetaMapper.map(request);
    assertThat(meta).isNotNull();
    assertThat(meta.getName().getValue()).isEqualTo(request.getName());
    assertThat(meta.getDatasourceUrn().getValue()).isEqualTo(request.getDatasourceUrn());
    assertThat(meta.getDescription()).isEqualTo(Optional.ofNullable(NO_DESCRIPTION));
  }

  @Test
  public void testMap_throwsException_onNullRequest() {
    final DatasetRequest nullRequest = null;
    assertThatNullPointerException().isThrownBy(() -> DatasetMetaMapper.map(nullRequest));
  }
}
