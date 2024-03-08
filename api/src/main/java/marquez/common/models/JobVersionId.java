/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Version ID for {@code Job}. */
@Value
@Builder
@AllArgsConstructor
public class JobVersionId {
  @NonNull NamespaceName namespace;
  @NonNull JobName name;
  @NonNull Version version;

  public static JobVersionId of(
      final NamespaceName namespaceName, final JobName jobName, final Version version) {
    return new JobVersionId(namespaceName, jobName, version);
  }
}
