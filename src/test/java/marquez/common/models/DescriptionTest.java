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
public class DescriptionTest {
  private static final String VALUE = "test";

  @Test
  public void testNewDescription() {
    final Description description = Description.of(VALUE);
    assertEquals(VALUE, description.getValue());
  }

  @Test
  public void testNewDescription_nullValue() {
    final String nullValue = null;
    final Description description = Description.of(nullValue);
    assertEquals(nullValue, description.getValue());
  }
}
