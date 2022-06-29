/*
 * Copyright 2018-2022 contributors to the Marquez project
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
}
