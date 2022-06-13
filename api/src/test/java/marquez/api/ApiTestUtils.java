/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api;

import static org.mockito.Mockito.mock;

import java.util.Map;
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
}
