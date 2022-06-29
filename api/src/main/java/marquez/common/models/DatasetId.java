/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.Utils;

/**
 * ID for {@code Dataset}. The class implements {@link Comparable} to ensure job versions generated
 * with {@link Utils#newJobVersionFor(NamespaceName, JobName, ImmutableSet, ImmutableSet,
 * ImmutableMap, String)} are consistent as jobs may contain inputs and outputs out of order.
 */
@EqualsAndHashCode
@ToString
public final class DatasetId implements Comparable<DatasetId> {
  private final NamespaceName namespaceName;
  private final DatasetName datasetName;

  public DatasetId(
      @JsonProperty("namespace") @NonNull NamespaceName namespaceName,
      @JsonProperty("name") @NonNull DatasetName datasetName) {
    this.namespaceName = namespaceName;
    this.datasetName = datasetName;
  }

  public NamespaceName getNamespace() {
    return namespaceName;
  }

  public DatasetName getName() {
    return datasetName;
  }

  @Override
  public int compareTo(DatasetId o) {
    return ComparisonChain.start()
        .compare(this.namespaceName.getValue(), o.getNamespace().getValue())
        .compare(this.getName().getValue(), o.getName().getValue())
        .result();
  }
}
