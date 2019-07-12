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
public class RunIdTest {
  @Test
  public void testNewRunId() {
    final String value = "f662a43d-ab87-4b4b-a9ba-828b53c3368b";
    assertEquals(value, RunId.of(value).getValue());
  }

  @Test(expected = NullPointerException.class)
  public void testNewRunId_throwsException_onNullValue() {
    final String nullValue = null;
    RunId.of(nullValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRunId_throwsException_onEmptyValue() {
    final String emptyValue = "";
    RunId.of(emptyValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRunId_throwsException_onBlankValue() {
    final String blankValue = " ";
    RunId.of(blankValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRunId_throwsException_onGreaterThan36Value() {
    final String greaterThan36Value = "5f217783-ff48-4650-aab4-ek1306afb3960";
    RunId.of(greaterThan36Value);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewRunId_throwsException_onLessThan36Value() {
    final String lessThan36Value = "5f217783-ff48-4650-ek1306afb3960";
    RunId.of(lessThan36Value);
  }
}
