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

package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDbTableVersion;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class DbTableVersionTest {
  private static final DatasetVersion DB_TABLE_VERSION = newDbTableVersion();
  private static final String JSON = JsonGenerator.newJsonFor(DB_TABLE_VERSION);

  @Test
  public void testFromJson() {
    final DatasetVersion actual = DbTableVersion.fromJson(JSON);
    assertThat(actual).isEqualTo(DB_TABLE_VERSION);
  }
}
