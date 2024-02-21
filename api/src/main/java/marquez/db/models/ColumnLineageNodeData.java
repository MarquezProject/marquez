/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.db.models;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.NonNull;
import marquez.service.models.ColumnLineageInputField;
import marquez.service.models.NodeData;

@Getter
public class ColumnLineageNodeData implements NodeData {
  @NonNull String namespace;
  @NonNull String dataset;
  @Nullable UUID datasetVersion;
  @NonNull String field;
  @Nullable String fieldType;
  @Nullable String transformationDescription;
  @Nullable String transformationType;
  @NonNull List<InputFieldNodeData> inputFields;

  public ColumnLineageNodeData(
      String namespace,
      String dataset,
      UUID datasetVersion,
      String field,
      String fieldType,
      ImmutableList<InputFieldNodeData> inputFields) {
    this.namespace = namespace;
    this.dataset = dataset;
    this.datasetVersion = datasetVersion;
    this.field = field;
    this.fieldType = fieldType;
    this.inputFields = inputFields;
  }

  public ColumnLineageNodeData(InputFieldNodeData data) {
    this.namespace = data.namespace;
    this.dataset = data.dataset;
    this.datasetVersion = data.datasetVersion;
    this.field = data.field;
    this.fieldType = "";
    this.transformationDescription = data.transformationDescription;
    this.transformationType = data.transformationType;
    this.inputFields = ImmutableList.of();
  }

  /**
   * @deprecated Moved into {@link ColumnLineageInputField} to support multiple jobs writing to a
   *     single dataset. This method is scheduled to be removed in release {@code 0.30.0}.
   */
  public String getTransformationDescription() {
    return Optional.ofNullable(inputFields).map(List::stream).stream()
        .flatMap(Function.identity())
        .findAny()
        .map(d -> d.getTransformationDescription())
        .orElse(null);
  }

  /**
   * @deprecated Moved into {@link ColumnLineageInputField} to support multiple jobs writing to a
   *     single dataset. This method is scheduled to be removed in release {@code 0.30.0}.
   */
  public String getTransformationType() {
    return Optional.ofNullable(inputFields).map(List::stream).stream()
        .flatMap(Function.identity())
        .findAny()
        .map(d -> d.getTransformationType())
        .orElse(null);
  }
}
