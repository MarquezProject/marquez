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
import static marquez.common.models.UrnPattern.DELIM;
import static marquez.common.models.UrnPattern.PREFIX;

import java.util.StringJoiner;
import java.util.stream.Stream;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class UrnPatternTest {
  private static final int ALLOWED_PART_SIZE = 64;
  private static final int PART_SIZE_GREATER_THAN_ALLOWED = ALLOWED_PART_SIZE + 1;
  private static final String NAMESPACE = "test";
  private static final int NUM_OF_PARTS = 2;
  private static final UrnPattern PATTERN = UrnPattern.from(NAMESPACE, NUM_OF_PARTS);

  @Test(expected = NullPointerException.class)
  public void testFrom_throwsException_onNullNamespace() {
    final String nullNamespace = null;
    UrnPattern.from(nullNamespace, NUM_OF_PARTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFrom_throwsException_onEmptyNamespace() {
    final String emptyNamespace = "";
    UrnPattern.from(emptyNamespace, NUM_OF_PARTS);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFrom_throwsException_onBlankNamespace() {
    final String blankNamespace = " ";
    UrnPattern.from(blankNamespace, NUM_OF_PARTS);
  }

  @Test(expected = NullPointerException.class)
  public void testFrom_throwsException_onNullNumOfParts() {
    final Integer nullNumOfParts = null;
    UrnPattern.from(NAMESPACE, nullNumOfParts);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFrom_throwsException_onNegativeNumOfParts() {
    final Integer negativeNumOfParts = -1;
    UrnPattern.from(NAMESPACE, negativeNumOfParts);
  }

  @Test
  public void testThrowIfNoMatch_noExceptionThrown() {
    final String value = "urn:test:a:b";
    PATTERN.throwIfNoMatch(value);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowIfNoMatch_throwsException_onNoMatchValue() {
    final String noMatchValue = "urn:other:a:b";
    PATTERN.throwIfNoMatch(noMatchValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowIfNoMatch_throwsException_onNoPrefixValue() {
    final String noPrefixValue = "other:a:b";
    PATTERN.throwIfNoMatch(noPrefixValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewDatasetUrn_throwsException_onMissingPartValue() {
    final String missingPartValue = "urn:test:a";
    PATTERN.throwIfNoMatch(missingPartValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowIfNoMatch_throwsException_onExtraPartValue() {
    final String extraPartValue = "urn:test:a:b:c";
    PATTERN.throwIfNoMatch(extraPartValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowIfNoMatch_throwsException_onNonAlphanumericPartValue() {
    final String nonAlphanumericPartValue = "urn:test:a:b$c^";
    PATTERN.throwIfNoMatch(nonAlphanumericPartValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowIfNoMatch_throwsException_onPartGreaterThan64Value() {
    final String partGreaterThan64Value = newUrnWithPartGreaterThan64();
    PATTERN.throwIfNoMatch(partGreaterThan64Value);
  }

  @Test(expected = NullPointerException.class)
  public void testThrowIfNoMatch_throwsException_onNullValue() {
    final String nullValue = null;
    PATTERN.throwIfNoMatch(nullValue);
  }

  private String newUrnWithPartGreaterThan64() {
    final String part0 = newUrnPart("a", ALLOWED_PART_SIZE);
    final String part1GreaterThan64 = newUrnPart("b", PART_SIZE_GREATER_THAN_ALLOWED);
    return new StringJoiner(DELIM).add(PREFIX).add(part0).add(part1GreaterThan64).toString();
  }

  private String newUrnPart(String part, Integer limit) {
    return Stream.generate(() -> part).limit(limit).collect(joining());
  }
}
