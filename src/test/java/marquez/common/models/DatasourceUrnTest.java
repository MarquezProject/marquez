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

import static marquez.common.models.CommonModelGenerator.newConnectionUrlWith;
import static marquez.common.models.CommonModelGenerator.newDatasourceName;
import static marquez.common.models.CommonModelGenerator.newDatasourceType;
import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceUrnTest {

  private static final DatasourceName DATASOURCE_NAME = newDatasourceName();
  private static final DatasourceType DATASOURCE_TYPE = newDatasourceType();

  private static final ConnectionUrl CONNECTION_URL = newConnectionUrlWith(DATASOURCE_TYPE);

  private static final String NAMESPACE = "datasource";
  private static final String VALUE =
      String.format(
          "urn:%s:%s:%s", NAMESPACE, DATASOURCE_TYPE.toString(), DATASOURCE_NAME.getValue());

  @Test
  public void testNewDatasourceUrn_from() {
    final DatasourceUrn urn = DatasourceUrn.of(DATASOURCE_TYPE, DATASOURCE_NAME);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test
  public void testNewDatasourceUrn_fromConnectionUrlAndName() {
    final DatasourceUrn urn = DatasourceUrn.of(CONNECTION_URL, DATASOURCE_NAME);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test
  public void testNewDatasourceUrn_fromString() {
    final DatasourceUrn urn = DatasourceUrn.of(VALUE);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test(expected = NullPointerException.class)
  public void testFrom_throwsException_onNullDatasourceType() {
    final DatasourceType nullDatasourceType = null;
    DatasourceUrn.of(nullDatasourceType, DATASOURCE_NAME);
  }

  @Test(expected = NullPointerException.class)
  public void tesFrom_throwsException_onNullDatasourceName() {
    final DatasourceName nullDatasourceName = null;
    DatasourceUrn.of(DATASOURCE_TYPE, nullDatasourceName);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromString_throwsException_onEmptyValue() {
    final String emptyValue = "";
    DatasourceUrn.of(emptyValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromString_throwsException_onBlankValue() {
    final String blankValue = " ";
    DatasourceUrn.of(blankValue);
  }

  @Test(expected = NullPointerException.class)
  public void testDatasourceUrn_throwsException_onNullInput() {
    final String nullUrn = null;
    DatasourceUrn.of(nullUrn);
  }
}
