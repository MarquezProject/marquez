package marquez.api.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;
import marquez.api.models.DatasetResponse;
import marquez.api.models.ListDatasetsResponse;
import marquez.common.Namespace;
import marquez.common.Urn;
import marquez.service.DatasetService;
import marquez.service.models.Dataset;
import org.junit.Test;

public class DatasetResourceTest {
  private static final Namespace NAMESPACE = new Namespace("test");

  private final DatasetService mockDatasetService = mock(DatasetService.class);
  private final DatasetResource datasetResource = new DatasetResource(mockDatasetService);

  @Test
  public void testListDatasets() {
    final Urn urn = new Urn("urn:a:b:c");
    final Instant createdAt = Instant.now();
    final String description = "test description";
    final Dataset expectedDataset = new Dataset(urn, createdAt, description);

    final List<Dataset> datasets = Arrays.asList(expectedDataset);
    when(mockDatasetService.getAll(NAMESPACE, 0, 100)).thenReturn(datasets);

    final Response response = datasetResource.list(NAMESPACE, 0, 100);
    assertEquals(200, response.getStatus());

    final ListDatasetsResponse listDatasetsResponse = (ListDatasetsResponse) response.getEntity();
    final List<DatasetResponse> datasetsResponses = listDatasetsResponse.getDatasetResponses();
    assertEquals(1, datasetsResponses.size());
    assertEquals(expectedDataset.getUrn(), datasetsResponses.get(0).getUrn());
    assertEquals(expectedDataset.getCreatedAt(), datasetsResponses.get(0).getCreatedAt());
    assertEquals(expectedDataset.getDescription(), datasetsResponses.get(0).getDescription());

    verify(mockDatasetService, times(1)).getAll(NAMESPACE, 0, 100);
  }
}
