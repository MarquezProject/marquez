/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class ColumnLineageOutputField {
  @NonNull String namespace;
  @NonNull String dataset;
  @NonNull String field;
  @Nullable String transformationDescription;
  @Nullable String transformationType;
}
