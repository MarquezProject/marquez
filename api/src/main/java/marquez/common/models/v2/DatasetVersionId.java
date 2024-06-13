/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import marquez.common.models.DatasetName;
import marquez.common.models.NamespaceName;
import marquez.common.models.Version;

/** Version ID for {@code Dataset}. */
@EqualsAndHashCode
@ToString
public final class DatasetVersionId {
  @Getter private final NamespaceName namespace;
  @Getter private final DatasetName name;
  @Getter private final Version version;

  @JsonCreator
  public DatasetVersionId(
      @NonNull final NamespaceName namespaceName,
      @NonNull final DatasetName datasetName,
      @NonNull final Version datasetVersion) {
    this.namespace = namespaceName;
    this.name = datasetName;
    this.version = datasetVersion;
  }

  public static DatasetVersionId of(
      @NonNull final NamespaceName namespaceName,
      @NonNull final DatasetName datasetName,
      @NonNull final Version version) {
    return new DatasetVersionId(namespaceName, datasetName, version);
  }
}
