/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/** Version ID for {@code Job}. */
@Value
@Builder
@AllArgsConstructor
public class JobVersionId {
  @NonNull String namespace;
  @NonNull String name;
  @NonNull UUID version;
}
