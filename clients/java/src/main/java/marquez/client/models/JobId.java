/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import lombok.NonNull;
import lombok.Value;

/** ID for {@link Job}. */
@Value
public class JobId {
  @NonNull String namespace;
  @NonNull String name;
}
