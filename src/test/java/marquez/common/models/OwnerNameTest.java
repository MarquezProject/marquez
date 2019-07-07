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

import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class OwnerNameTest {
  @Test
  public void testNewOwnerName() {
    final String value = "test";
    assertEquals(value, OwnerName.of(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewOwnerName_throwsException_onNullValue() {
    final String nullValue = null;
    OwnerName.of(nullValue);
  }
}
