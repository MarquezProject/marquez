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

package marquez.db.models;

import static marquez.common.models.CommonModelGenerator.newConnectionUrl;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import marquez.UnitTests;
import marquez.common.models.ConnectionUrl;
import marquez.common.models.DatasourceName;
import marquez.common.models.DatasourceUrn;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceRowTest {
  private static final UUID ROW_UUID = UUID.randomUUID();
  private static final Instant CREATED_AT = Instant.now();
  private static final DatasourceName NAME = newDatasourceName();
  private static final ConnectionUrl CONNECTION_URL = newConnectionUrl();
  private static final DatasourceUrn URN = DatasourceUrn.of(CONNECTION_URL, NAME);

  @Test
  public void testNewRow() {
    final DatasourceRow expected =
        DatasourceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .name(NAME.getValue())
            .urn(URN.getValue())
            .connectionUrl(CONNECTION_URL.getRawValue())
            .build();
    final DatasourceRow actual =
        DatasourceRow.builder()
            .uuid(ROW_UUID)
            .createdAt(CREATED_AT)
            .name(NAME.getValue())
            .urn(URN.getValue())
            .connectionUrl(CONNECTION_URL.getRawValue())
            .build();
    assertThat(expected).isEqualTo(actual);
  }
}
