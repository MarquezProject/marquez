/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

/**
 * Class used to store dataset version and `outputFacets` which are assigned to datasets within
 * OpenLineage spec, but are exposed within Marquez api as a part of {@link
 * marquez.service.models.Run}
 */
@EqualsAndHashCode
@ToString
@Getter
public class OutputDatasetVersion {

  private final DatasetVersionId datasetVersionId;
  private final ImmutableMap<String, Object> facets;

  public OutputDatasetVersion(
      @JsonProperty("datasetVersionId") @NonNull DatasetVersionId datasetVersionId,
      @JsonProperty("facets") @NonNull ImmutableMap<String, Object> facets) {
    this.datasetVersionId = datasetVersionId;
    this.facets = facets;
  }
}
