package marquez.api.resources;

import static javax.ws.rs.core.Response.Status.OK;
import static marquez.common.models.Description.NO_DESCRIPTION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import marquez.api.models.DatasetResponse;
import marquez.api.models.DatasetsResponse;
import marquez.common.models.DatasetUrn;
import marquez.common.models.Namespace;
import marquez.service.DatasetService;
import marquez.service.NamespaceService;
import marquez.service.exceptions.UnexpectedException;
import marquez.service.models.Dataset;
import org.junit.Test;

public class DatasetResourceTest {
  private static final DatasetUrn DATASET_URN = DatasetUrn.of("urn:a:b.c");
  private static final Instant CREATED_AT = Instant.now();
  private static final Namespace NAMESPACE = Namespace.of("test");
  private static final Integer LIMIT = 100;
  private static final Integer OFFSET = 0;

  private final NamespaceService mockNamespaceService = mock(NamespaceService.class);
  private final DatasetService mockDatasetService = mock(DatasetService.class);
  private final DatasetResource datasetResource =
      new DatasetResource(mockNamespaceService, mockDatasetService);

  @Test(expected = NullPointerException.class)
  public void testNamespaceServiceNull() {
    final NamespaceService nullNamespaceService = null;
    new DatasetResource(nullNamespaceService, mockDatasetService);
  }

  @Test(expected = NullPointerException.class)
  public void testDatasetServiceNull() {
    final DatasetService nullDatasetService = null;
    new DatasetResource(mockNamespaceService, nullDatasetService);
  }

  @Test
  public void testListDatasets200() throws UnexpectedException {
    when(mockNamespaceService.exists(NAMESPACE.getValue())).thenReturn(true);

    final Dataset dataset = new Dataset(DATASET_URN, CREATED_AT, NO_DESCRIPTION);
    final List<Dataset> datasets = Arrays.asList(dataset);
    when(mockDatasetService.getAll(NAMESPACE, LIMIT, OFFSET)).thenReturn(datasets);

    final Response response = datasetResource.list(NAMESPACE.getValue(), LIMIT, OFFSET);
    assertEquals(OK, response.getStatusInfo());

    final DatasetsResponse datasetsResponse = (DatasetsResponse) response.getEntity();
    final List<DatasetResponse> datasetsResponses = datasetsResponse.getDatasetResponses();
    assertEquals(1, datasetsResponses.size());
    assertEquals(DATASET_URN.getValue(), datasetsResponses.get(0).getUrn());
    assertEquals(CREATED_AT.toString(), datasetsResponses.get(0).getCreatedAt());

    verify(mockDatasetService, times(1)).getAll(NAMESPACE, LIMIT, OFFSET);
  }

  @Test(expected = WebApplicationException.class)
  public void testListDatasetsNamespaceDoesNotExist() throws UnexpectedException {
    when(mockNamespaceService.exists(NAMESPACE.getValue())).thenReturn(false);

    datasetResource.list(NAMESPACE.getValue(), LIMIT, OFFSET);
  }
}
