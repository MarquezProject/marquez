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

package marquez.common.models;

import static java.util.UUID.fromString;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class RunIdTest {
  private static final UUID ACTUAL = fromString("225adbdd-2a5d-4b5f-89b3-06a7cd47cc87");
  private static final UUID EXPECTED = fromString("225adbdd-2a5d-4b5f-89b3-06a7cd47cc87");

  @Test(expected = NullPointerException.class)
  public void testNull() {
    RunId.of(null);
  }

  @Test
  public void testForValue() {
    assertThat(RunId.of(ACTUAL).getValue()).isEqualByComparingTo(EXPECTED);
  }

  @Test
  public void testForEquals() {
    assertThat(RunId.of(ACTUAL)).isEqualTo(RunId.of(EXPECTED));
  }
}
