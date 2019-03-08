package marquez.common.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import marquez.db.models.DatasourceRow;
import marquez.service.models.Generator;
import org.junit.Test;

public class DatasourceUrnTest {

  private static final String URN = "urn:datasource:postgresql:myteamdb";

  private static final DatasourceRow DATASOURCE_ROW_1 = Generator.genDatasourceRow();
  private static final DatasourceRow DATASOURCE_ROW_2 = Generator.genDatasourceRow();

  private static final DatasourceUrn DATASOURCE_URN_1 =
      DatasourceUrn.from(DATASOURCE_ROW_1.getConnectionUrl(), DATASOURCE_ROW_1.getName());
  private static final DatasourceUrn DATASOURCE_URN_2 =
      DatasourceUrn.from(DATASOURCE_ROW_2.getConnectionUrl(), DATASOURCE_ROW_2.getName());

  @Test
  public void testFromString() {
    assertEquals(URN, DatasourceUrn.fromString(URN).toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOfTooFewComponents() {
    DatasourceUrn.fromString("urn:datasource:a");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testOfTooManyComponents() {
    DatasourceUrn.fromString("urn:datasource:a:b:c");
  }

  @Test
  public void testGetDatasourceType() {
    final DatasourceUrn datasourceUrn = DatasourceUrn.fromString(URN);
    final DatasourceType datasourceType = datasourceUrn.getDatasourceType();
    assertThat(datasourceType.toString()).isEqualTo("postgresql");
  }

  @Test(expected = NullPointerException.class)
  public void testDatasourceUrn_throwsException_onNullInput() {
    final String nullUrn = null;
    DatasourceUrn.fromString(nullUrn);
  }

  @Test
  public void testGetDatasourceName() {
    assertThat(DatasourceUrn.fromString(URN).getDatasourceName().getValue()).isEqualTo("myteamdb");
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
