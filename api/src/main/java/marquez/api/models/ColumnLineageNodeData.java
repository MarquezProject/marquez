/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class ColumnLineageNodeData {
  @NonNull String namespace;
  @NonNull String dataset;
  @Nullable UUID datasetVersion;
  @NonNull String field;
}
