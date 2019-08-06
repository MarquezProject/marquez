package marquez.client.models;

import static marquez.client.models.ModelGenerator.newConnectionUrl;
import static marquez.client.models.ModelGenerator.newDatasetUrn;
import static marquez.client.models.ModelGenerator.newDatasourceName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourceTest {
  private static final String DATASOURCE_NAME = newDatasourceName();
  private static final Instant CREATED_AT = Instant.now();
  private static final String URN = newDatasetUrn();
  private static final String CONNECTION_URL = newConnectionUrl();

  @Test
  public void testDatasource() {
    final Datasource datasource = new Datasource(DATASOURCE_NAME, CREATED_AT, URN, CONNECTION_URL);

    assertThat(datasource.getName()).isEqualTo(DATASOURCE_NAME);
    assertThat(datasource.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(datasource.getUrn()).isEqualTo(URN);
    assertThat(datasource.getConnectionUrl()).isEqualTo(CONNECTION_URL);
  }

  @Test
  public void testDatasource_noDatasourceName() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Datasource(null, CREATED_AT, URN, CONNECTION_URL);
            });
  }

  @Test
  public void testDatasource_blankDatasourceName() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Datasource(" ", CREATED_AT, URN, CONNECTION_URL);
            });
  }

  @Test
  public void testDatasource_noCreatedAt() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Datasource(DATASOURCE_NAME, null, URN, CONNECTION_URL);
            });
  }

  @Test
  public void testDatasource_noUrn() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Datasource(DATASOURCE_NAME, CREATED_AT, null, CONNECTION_URL);
            });
  }

  @Test
  public void testDatasource_blankUrn() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Datasource(DATASOURCE_NAME, CREATED_AT, " ", CONNECTION_URL);
            });
  }

  @Test
  public void testDatasource_noConnectionUrl() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> {
              new Datasource(DATASOURCE_NAME, CREATED_AT, URN, null);
            });
  }

  @Test
  public void testDatasource_blankConnectionUrl() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> {
              new Datasource(DATASOURCE_NAME, CREATED_AT, URN, " ");
            });
  }
}
