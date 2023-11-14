/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import lombok.NonNull;
import lombok.Value;

@Value
public class DatasetFieldId {
  @NonNull String namespace;
  @NonNull String dataset;
  @NonNull String field;
}
