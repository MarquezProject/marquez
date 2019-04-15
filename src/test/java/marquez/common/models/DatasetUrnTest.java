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
public class DatasetUrnTest {
  private static final String NAMESPACE = "dataset";
  private static final String VALUE = String.format("urn:%s:postgresql:public.foo", NAMESPACE);

  private static final DatasourceName DATASOURCE_NAME = DatasourceName.fromString("postgresql");
  private static final DatasetName DATASET_NAME = DatasetName.fromString("public.foo");

  @Test
  public void testNewDatasetUrn_from() {
    final DatasetUrn urn = DatasetUrn.from(DATASOURCE_NAME, DATASET_NAME);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test
  public void testNewDatasetUrn_fromString() {
    final DatasetUrn urn = DatasetUrn.fromString(VALUE);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test(expected = NullPointerException.class)
  public void testFrom_throwsException_onNullDatasourceName() {
    final DatasourceName nullDatasourceName = null;
    DatasetUrn.from(nullDatasourceName, DATASET_NAME);
  }

  @Test(expected = NullPointerException.class)
  public void testFrom_throwsException_onNullDatasetName() {
    final DatasetName nullDatasetName = null;
    DatasetUrn.from(DATASOURCE_NAME, nullDatasetName);
  }

  @Test(expected = NullPointerException.class)
  public void testFromString_throwsException_onNullValue() {
    final String nullValue = null;
    DatasetUrn.fromString(nullValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromString_throwsException_onEmptyValue() {
    final String emptyValue = "";
    DatasetUrn.fromString(emptyValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromStringn_throwsException_onBlankValue() {
    final String blankValue = " ";
    DatasetUrn.fromString(blankValue);
  }
}
