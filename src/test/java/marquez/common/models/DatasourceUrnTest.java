package marquez.common.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DatasourceUrnTest {

  private static final String URN = "urn:datasource:postgresql:myteamdb";

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
  public void testGetType() {
    final DatasourceUrn datasourceUrn = DatasourceUrn.fromString(URN);
    final DatasourceType datasourceType = datasourceUrn.getDatasourceType();
    assertThat(datasourceType.toString()).isEqualTo("postgresql");
  }

  @Test
  public void testGetDatasourceName() {
    assertThat(DatasourceUrn.fromString(URN).getDatasourceName().getValue()).isEqualTo("myteamdb");
  }
}
