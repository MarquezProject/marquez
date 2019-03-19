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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import marquez.UnitTests;
import marquez.db.models.DatasourceRow;
import marquez.service.models.Generator;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceUrnTest {
  private static final String URN = "urn:datasource:postgresql:myteamdb";

  private static final DatasourceRow DATASOURCE_ROW_1 = Generator.genDatasourceRow();
  private static final DatasourceRow DATASOURCE_ROW_2 = Generator.genDatasourceRow();

  private static final DatasourceUrn DATASOURCE_URN_1 =
      DatasourceUrn.from(DATASOURCE_ROW_1.getConnectionUrl(), DATASOURCE_ROW_1.getName());
  private static final DatasourceUrn DATASOURCE_URN_2 =
      DatasourceUrn.from(DATASOURCE_ROW_2.getConnectionUrl(), DATASOURCE_ROW_2.getName());

  private static final String NAMESPACE = "datasource";
  private static final String VALUE = String.format("urn:%s:postgresql:test", NAMESPACE);

  private static final DatasourceType DATASOURCE_TYPE = DatasourceType.POSTGRESQL;
  private static final DatasourceName DATASOURCE_NAME = DatasourceName.fromString("test");

  @Test
  public void testFromString() {
    assertEquals(URN, DatasourceUrn.fromString(URN).getValue());
  }

  @Test
  public void testNewDatasourceUrn_from() {
    final DatasourceUrn urn = DatasourceUrn.from(DATASOURCE_TYPE, DATASOURCE_NAME);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test
  public void testNewDatasourceUrn_fromConnectionUrlAndName() {
    final ConnectionUrl connectionUrl =
        ConnectionUrl.fromString(DATASOURCE_ROW_1.getConnectionUrl());
    final DatasourceUrn urn = DatasourceUrn.from(connectionUrl, DATASOURCE_NAME);
    assertEquals(VALUE, urn.getValue());
    assertEquals(NAMESPACE, urn.namespace());
  }

  @Test
  public void testNewDatasourceUrn_fromConnectionUrlAndNameStrings() {
    final DatasourceUrn urn =
        DatasourceUrn.from(DATASOURCE_ROW_1.getConnectionUrl(), DATASOURCE_NAME.getValue());
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

  @Test(expected = NullPointerException.class)
  public void testFromString_throwsException_onNullValue() {
    final String nullValue = null;
    DatasourceUrn.fromString(nullValue);
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

  @Test
  public void testInequality() {
    assertThat(DATASOURCE_URN_1).isNotEqualTo(DATASOURCE_URN_2);
  }

  @Test
  public void testEquality() {
    assertThat(DATASOURCE_URN_1).isEqualTo(DATASOURCE_URN_1);
    assertThat(DATASOURCE_URN_2).isEqualTo(DATASOURCE_URN_2);
  }
}
