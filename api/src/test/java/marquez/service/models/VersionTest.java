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

package marquez.service.models;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import marquez.common.models.Version;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@org.junit.jupiter.api.Tag("UnitTests")
public class VersionTest {
  private static final UUID ACTUAL = fromString("225adbdd-2a5d-4b5f-89b3-06a7cd47cc87");
  private static final UUID EXPECTED = fromString("225adbdd-2a5d-4b5f-89b3-06a7cd47cc87");

  @Test
  public void testNull() {
    Assertions.assertThrows(NullPointerException.class, () -> Version.of(null));
  }

  @Test
  public void testForValue() {
    assertThat(Version.of(ACTUAL).getValue()).isEqualByComparingTo(EXPECTED);
  }

  @Test
  public void testForEquals() {
    assertThat(Version.of(ACTUAL)).isEqualTo(Version.of(EXPECTED));
  }
}
