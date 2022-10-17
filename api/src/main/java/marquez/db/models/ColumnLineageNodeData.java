/*
 * Copyright 2018-2022 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class ColumnLineageNodeData implements NodeData {
  @NonNull String namespace;
  @NonNull String dataset;
  @NonNull String field;
  @NonNull String fieldType;
  String transformationDescription;
  String transformationType;
  @NonNull List<InputFieldNodeData> inputFields;
}
