/* SPDX-License-Identifier: Apache-2.0 */

package marquez.api;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static marquez.common.models.CommonModelGenerator.newDatasetName;
import static marquez.common.models.CommonModelGenerator.newDescription;
import static marquez.common.models.CommonModelGenerator.newFieldName;
import static marquez.common.models.CommonModelGenerator.newFieldType;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import marquez.client.models.DatasetId;
import marquez.client.models.Field;
import marquez.service.DatasetFieldService;
import marquez.service.DatasetService;
import marquez.service.DatasetVersionService;
import marquez.service.JobService;
import marquez.service.LineageService;
import marquez.service.NamespaceService;
import marquez.service.OpenLineageService;
import marquez.service.RunService;
import marquez.service.ServiceFactory;
import marquez.service.SourceService;
import marquez.service.TagService;

public class ApiTestUtils {

  /**
   * Create an {@link ServiceFactory} for test usage
   *
   * @param mocks map of mocks to be replaced
   * @return ServiceFactory with mocked services
   */
  @SuppressWarnings("rawtypes")
  public static ServiceFactory mockServiceFactory(Map<Class, Object> mocks) {
    return ServiceFactory.builder()
        .lineageService(
            (LineageService) mocks.getOrDefault(LineageService.class, (mock(LineageService.class))))
        .openLineageService(
            (OpenLineageService)
                mocks.getOrDefault(OpenLineageService.class, (mock(OpenLineageService.class))))
        .datasetFieldService(
            (DatasetFieldService)
                mocks.getOrDefault(DatasetFieldService.class, (mock(DatasetFieldService.class))))
        .jobService((JobService) mocks.getOrDefault(JobService.class, (mock(JobService.class))))
        .runService((RunService) mocks.getOrDefault(RunService.class, (mock(RunService.class))))
        .tagService((TagService) mocks.getOrDefault(TagService.class, (mock(TagService.class))))
        .datasetVersionService(
            (DatasetVersionService)
                mocks.getOrDefault(
                    DatasetVersionService.class, (mock(DatasetVersionService.class))))
        .namespaceService(
            (NamespaceService)
                mocks.getOrDefault(NamespaceService.class, (mock(NamespaceService.class))))
        .sourceService(
            (SourceService) mocks.getOrDefault(SourceService.class, (mock(SourceService.class))))
        .datasetService(
            (DatasetService) mocks.getOrDefault(DatasetService.class, (mock(DatasetService.class))))
        .build();
  }

  public static class ClientModelGenerator {
    public static ImmutableSet<DatasetId> newDatasetIdsWith(
        final String namespaceName, final int limit) {
      return java.util.stream.Stream.generate(() -> newDatasetIdWith(namespaceName))
          .limit(limit)
          .collect(toImmutableSet());
    }

    public static DatasetId newDatasetIdWith(final String namespaceName) {
      return new DatasetId(namespaceName, newDatasetName().getValue());
    }

    public static Field newField() {
      return newFieldWith(ImmutableSet.of());
    }

    public static Field newFieldWith(final String tag) {
      return newFieldWith(ImmutableSet.of(tag));
    }

    public static Field newFieldWith(final ImmutableSet<String> tags) {
      return new Field(newFieldName().getValue(), newFieldType(), tags, newDescription());
    }
  }
}
