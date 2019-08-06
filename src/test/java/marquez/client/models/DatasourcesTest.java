package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDatasources;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasourcesTest {
  @Test
  public void testNamespaces() {
    final List<Datasource> datasourceList = newDatasources(3);
    final Datasources datasources = new Datasources(datasourceList);

    assertThat(datasources.getDatasources().size()).isEqualTo(3);
    assertThat(datasources.getDatasources()).isEqualTo(datasourceList);
  }
}
