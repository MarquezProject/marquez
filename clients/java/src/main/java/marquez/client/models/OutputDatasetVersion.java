/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import lombok.Value;

/** Class to contain outputFacets. */
@Value
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
