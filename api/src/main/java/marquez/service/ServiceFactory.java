/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Builder
@Value
public class ServiceFactory {
  @NonNull DatasetService datasetService;
  @NonNull JobService jobService;
  @NonNull NamespaceService namespaceService;
  @NonNull OpenLineageService openLineageService;
  @NonNull RunService runService;
  @NonNull SourceService sourceService;
  @NonNull TagService tagService;
  @NonNull DatasetVersionService datasetVersionService;
  @NonNull DatasetFieldService datasetFieldService;
  @NonNull LineageService lineageService;
}
