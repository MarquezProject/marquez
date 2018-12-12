package marquez.api.resources;

import static javax.ws.rs.core.Response.Status.OK;
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
  private static final Urn URN = new Urn("urn:a:b.c");
  private static final Dataset DATASET = new Dataset(URN, Instant.now(), "test description");
  private static final Integer LIMIT = 100;
  private static final Integer OFFSET = 0;

  private final DatasetService mockDatasetService = mock(DatasetService.class);
  private final DatasetResource datasetResource = new DatasetResource(mockDatasetService);

  @Test
  public void testListDatasets() {
    final List<Dataset> datasets = Arrays.asList(DATASET);
    when(mockDatasetService.getAll(NAMESPACE, LIMIT, OFFSET)).thenReturn(datasets);

    final Response response = datasetResource.list(NAMESPACE, LIMIT, OFFSET);
    assertEquals(OK, response.getStatusInfo());

    final ListDatasetsResponse listDatasetsResponse = (ListDatasetsResponse) response.getEntity();
    final List<DatasetResponse> datasetsResponses = listDatasetsResponse.getDatasetResponses();
    assertEquals(1, datasetsResponses.size());
    assertEquals(DATASET.getUrn(), datasetsResponses.get(0).getUrn());
    assertEquals(DATASET.getCreatedAt(), datasetsResponses.get(0).getCreatedAt());
    assertEquals(DATASET.getDescription(), datasetsResponses.get(0).getDescription());

    verify(mockDatasetService, times(1)).getAll(NAMESPACE, LIMIT, OFFSET);
  }
}
