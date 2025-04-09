/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.api.models;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.NonNull;
import lombok.Value;

@Value
public class ColumnLineage {
  @NonNull String name;
  @NonNull List<ColumnLineageInputField> inputFields;
  @NonNull List<ColumnLineageOutputField> outputFields;
}

@Value
class ColumnLineageInputField {
  @NonNull String namespace;
  @NonNull String dataset;
  @NonNull String field;
  @Nullable UUID datasetVersion;
}

@Value
class ColumnLineageOutputField {
  @NonNull String namespace;
  @NonNull String dataset;
  @NonNull String field;
  @Nullable UUID datasetVersion;
}
