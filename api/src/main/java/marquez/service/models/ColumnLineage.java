/*
 * Copyright 2018-2023 contributors to the Marquez project
 * SPDX-License-Identifier: Apache-2.0
 */

package marquez.service.models;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode
@ToString
@Builder
@Getter
public class ColumnLineage {
  @NonNull private String name;
  @NonNull private List<ColumnLineageInputField> inputFields;
  @NonNull private List<ColumnLineageOutputField> outputFields;

  @Nullable private String transformationDescription;
  @Nullable private String transformationType;

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
