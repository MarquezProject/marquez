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

import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class NamespaceNameTest {
  private static final int ALLOWED_NAMESPACE_SIZE = 1024;
  private static final int NAMESPACE_SIZE_GREATER_THAN_ALLOWED = ALLOWED_NAMESPACE_SIZE + 1;

  @Test
  public void testNewNamespace() {
    final String value = "test";
    assertEquals(value, NamespaceName.of(value).getValue());
  }

  @Test
  public void testNewNamespace_withDashesAndUnderscore() {
    final String nameWithDashesAndUnderscore = "test_a-b_c";
    NamespaceName.of(nameWithDashesAndUnderscore);
  }

  @Test(expected = NullPointerException.class)
  public void testNewNamespace_throwsException_onNullValue() {
    final String nullValue = null;
    NamespaceName.of(nullValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewNamespace_throwsException_onEmptyValue() {
    final String emptyValue = "";
    NamespaceName.of(emptyValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewNamespace_throwsException_onBlankValue() {
    final String blankValue = " ";
    NamespaceName.of(blankValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewNamespace_throwsException_onNonAlphanumericValue() {
    final String nonAlphanumericValue = "t@?t>";
    NamespaceName.of(nonAlphanumericValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewNamespace_throwsException_onGreaterThan1024Value() {
    final String greaterThan1024Value = newGreaterThan1024Value();
    NamespaceName.of(greaterThan1024Value);
  }

  private String newGreaterThan1024Value() {
    return Stream.generate(() -> "a").limit(NAMESPACE_SIZE_GREATER_THAN_ALLOWED).collect(joining());
  }
}
