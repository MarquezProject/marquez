/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Version ID for {@code Dataset}. */
@Value
@Builder
@AllArgsConstructor
public class DatasetVersionId {
  @NonNull NamespaceName namespace;
  @NonNull DatasetName name;
  @NonNull UUID version;

  public static DatasetVersionId of(
      final NamespaceName namespaceName, final DatasetName jobName, final UUID version) {
    return new DatasetVersionId(namespaceName, jobName, version);
  }
}
