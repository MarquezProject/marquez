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

package marquez.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class UtilsTest {
  @Test
  public void testChecksumFor_equal() {
    final Map<String, String> kvMap = ImmutableMap.of("key0", "value0", "key1", "value1");

    final String checksum0 = Utils.checksumFor(kvMap);
    final String checksum1 = Utils.checksumFor(kvMap);
    assertThat(checksum0).isEqualTo(checksum1);
  }

  @Test
  public void testChecksumFor_notEqual() {
    final Map<String, String> kvMap0 = ImmutableMap.of("key0", "value0", "key1", "value1");
    final Map<String, String> kvMap1 = ImmutableMap.of("key2", "value2", "key3", "value3");

    final String checksum0 = Utils.checksumFor(kvMap0);
    final String checksum1 = Utils.checksumFor(kvMap1);
    assertThat(checksum0).isNotEqualTo(checksum1);
  }
}
