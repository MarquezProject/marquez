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

package marquez.service.mappers;

import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import marquez.db.models.DatasourceRow;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceRowMapperTest {
  private static final ConnectionUrl CONNECTION_URL = newConnectionUrl();
  private static final DatasourceName DATASOURCE_NAME = newDatasourceName();
  private static final DatasourceUrn DATASOURCE_URN =
      DatasourceUrn.of(CONNECTION_URL, DATASOURCE_NAME);

  @Test
  public void testMap_row() {
    final DatasourceRow row = DatasourceRowMapper.map(CONNECTION_URL, DATASOURCE_NAME);
    assertThat(row).isNotNull();
    assertThat(row.getUuid()).isNotNull();
    assertThat(row.getName()).isEqualTo(DATASOURCE_NAME.getValue());
    assertThat(row.getUrn()).isEqualTo(DATASOURCE_URN.getValue());
    assertThat(row.getConnectionUrl()).isEqualTo(CONNECTION_URL.getRawValue());
  }

  @Test
  public void testMap_throwsException_onNullConnectionUrl() {
    assertThatNullPointerException()
        .isThrownBy(() -> DatasourceRowMapper.map(null, DATASOURCE_NAME));
  }

  @Test
  public void testMap_throwsException_onNullDatasourceName() {
    assertThatNullPointerException()
        .isThrownBy(() -> DatasourceRowMapper.map(CONNECTION_URL, null));
  }
}
