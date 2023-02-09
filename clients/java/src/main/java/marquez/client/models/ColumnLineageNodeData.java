/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.client.models;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import marquez.client.Utils;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class ColumnLineageNodeData implements NodeData {
  @NonNull String namespace;
  @NonNull String dataset;
  @NonNull String field;
  @NonNull String fieldType;
  @NonNull List<ColumnLineageInputField> inputFields;

  public static ColumnLineageNodeData fromJson(@NonNull final String json) {
    return Utils.fromJson(json, new TypeReference<ColumnLineageNodeData>() {});
  }
}
