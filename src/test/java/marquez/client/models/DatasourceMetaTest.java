package marquez.client.models;

import static marquez.client.models.ModelGenerator.newConnectionUrl;
import static marquez.client.models.ModelGenerator.newDatasourceName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceMetaTest {
  private static final String DATASOURCE_NAME = newDatasourceName();
  private static final String CONNECTION_URL = newConnectionUrl();

  @Test
  public void testDatasourceMeta() {
    final DatasourceMeta datasourceMeta =
        DatasourceMeta.builder().name(DATASOURCE_NAME).connectionUrl(CONNECTION_URL).build();

    assertThat(datasourceMeta.getName()).isEqualTo(DATASOURCE_NAME);
    assertThat(datasourceMeta.getConnectionUrl()).isEqualTo(CONNECTION_URL);
  }

  @Test
  public void testDatasourceMeta_noDatasourceName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              DatasourceMeta.builder().connectionUrl(CONNECTION_URL).build();
            });
  }

  @Test
  public void testDatasourceMeta_blankDatasourceName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              DatasourceMeta.builder().name(" ").connectionUrl(CONNECTION_URL).build();
            });
  }

  @Test
  public void testDatasourceMeta_noConnectionUrl() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              DatasourceMeta.builder().name(DATASOURCE_NAME).build();
            });
  }

  @Test
  public void testDatasourceMeta_blankConnectionUrl() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              DatasourceMeta.builder().name(DATASOURCE_NAME).connectionUrl(" ").build();
            });
  }
}
