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
public class DatasourceUrnTest {

  private static final ConnectionUrl CONNECTION_URL =
      ConnectionUrl.fromString("jdbc:postgresql://localhost:5431/novelists_");

  private static final String NAMESPACE = "datasource";
  private static final String VALUE = String.format("urn:%s:postgresql:test", NAMESPACE);

  private static final DatasourceType DATASOURCE_TYPE = DatasourceType.POSTGRESQL;
  private static final DatasourceName DATASOURCE_NAME = DatasourceName.fromString("test");

  @Test
  public void testNewDatasourceUrn_from() {
    final DatasourceUrn urn = DatasourceUrn.from(DATASOURCE_TYPE, DATASOURCE_NAME);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test
  public void testNewDatasourceUrn_fromConnectionUrlAndName() {
    final DatasourceUrn urn = DatasourceUrn.from(CONNECTION_URL, DATASOURCE_NAME);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test
  public void testNewDatasourceUrn_fromString() {
    final DatasourceUrn urn = DatasourceUrn.fromString(VALUE);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test(expected = NullPointerException.class)
  public void testFrom_throwsException_onNullDatasourceType() {
    final DatasourceType nullDatasourceType = null;
    DatasourceUrn.from(nullDatasourceType, DATASOURCE_NAME);
  }

  @Test(expected = NullPointerException.class)
  public void tesFrom_throwsException_onNullDatasourceName() {
    final DatasourceName nullDatasourceName = null;
    DatasourceUrn.from(DATASOURCE_TYPE, nullDatasourceName);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromString_throwsException_onEmptyValue() {
    final String emptyValue = "";
    DatasourceUrn.fromString(emptyValue);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromString_throwsException_onBlankValue() {
    final String blankValue = " ";
    DatasourceUrn.fromString(blankValue);
  }

  @Test(expected = NullPointerException.class)
  public void testDatasourceUrn_throwsException_onNullInput() {
    final String nullUrn = null;
    DatasourceUrn.fromString(nullUrn);
  }
}
