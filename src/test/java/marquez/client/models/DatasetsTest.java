package marquez.client.models;

import static marquez.client.models.ModelGenerator.newDatasets;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import marquez.client.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTests.class)
public class DatasetsTest {
  @Test
  public void testDatasets() {
    final List<Dataset> datasetList = newDatasets(3);
    final Datasets datasets = new Datasets(datasetList);

    assertThat(datasets.getDatasets().size()).isEqualTo(3);
    assertThat(datasets.getDatasets()).isEqualTo(datasetList);
  }
}
