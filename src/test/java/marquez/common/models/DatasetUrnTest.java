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

import java.util.StringJoiner;
import java.util.stream.Stream;
import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetUrnTest {
  private static final int ALLOWED_DATASET_URN_SIZE = 64;
  private static final int DATASET_URN_SIZE_GREATER_THAN_ALLOWED = ALLOWED_DATASET_URN_SIZE + 1;
  private static final String DATASET_URN_DELIM = ":";
  private static final String DATASET_URN_PREFIX = "urn";

  @Test
  public void testNewDatasetUrn() {
    final String value = "urn:a:b.c";
    assertEquals(value, DatasetUrn.fromString(value).getValue());
  }

  @Test
  public void testNewDatasetUrn_fromNamespaceAndDataset() {
    final DatasetUrn expected = DatasetUrn.fromString("urn:a:b.c");
    final DatasetUrn actual =
        DatasetUrn.from(NamespaceName.fromString("a"), DatasetName.fromString("b.c"));
    assertEquals(expected, actual);
  }

  @Test(expected = NullPointerException.class)
  public void testNewDatasetUrn_throwsException_onNullValue() {
    final String nullValue = null;
    DatasetUrn.fromString(nullValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewDatasetUrn_throwsException_onEmptyValue() {
    final String emptyValue = "";
    DatasetUrn.fromString(emptyValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewDatasetUrn_throwsException_onBlankValue() {
    final String blankValue = " ";
    DatasetUrn.fromString(blankValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewDatasetUrn_throwsException_onNoPrefixValue() {
    final String noPrefixValue = "a:b";
    DatasetUrn.fromString(noPrefixValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewDatasetUrn_throwsException_onMissingPartValue() {
    final String missingPartValue = "urn:a";
    DatasetUrn.fromString(missingPartValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewDatasetUrn_throwsException_onExtraPartValue() {
    final String extraPartValue = "urn:a:b:c";
    DatasetUrn.fromString(extraPartValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewDatasetUrn_throwsException_onNonAlphanumericPartValue() {
    final String nonAlphanumericPartValue = "urn:a:b$c^";
    DatasetUrn.fromString(nonAlphanumericPartValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNewDatasetUrn_throwsException_onPartGreaterThan64Value() {
    final String partGreaterThan64Value = newDatasetUrnWithPartGreaterThan64();
    DatasetUrn.fromString(partGreaterThan64Value);
  }

  private String newDatasetUrnWithPartGreaterThan64() {
    final String part0 = newDatasetUrnPart("a", ALLOWED_DATASET_URN_SIZE);
    final String part1GreaterThan64 = newDatasetUrnPart("b", DATASET_URN_SIZE_GREATER_THAN_ALLOWED);

    return new StringJoiner(DATASET_URN_DELIM)
        .add(DATASET_URN_PREFIX)
        .add(part0)
        .add(part1GreaterThan64)
        .toString();
  }

  private String newDatasetUrnPart(String s, Integer limit) {
    return Stream.generate(() -> s).limit(limit).collect(joining());
  }
}
